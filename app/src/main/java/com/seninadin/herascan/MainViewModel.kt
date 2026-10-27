package com.seninadin.herascan

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.seninadin.herascan.data.ScanRepository
import com.seninadin.herascan.db.ScanHistory
import com.seninadin.herascan.utils.Logger
import com.seninadin.herascan.utils.copyUriToPrivateStorage
import com.seninadin.herascan.utils.deletePrivateCopies
import com.seninadin.herascan.utils.resolveDocumentName
import com.seninadin.herascan.utils.toSafeFileName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class ScanUiState(
    val imageUris: List<Uri> = emptyList(),
    val pdfUri: Uri? = null,
    val extractedText: String = "",
    val isOcrLoading: Boolean = false,
    val ocrProgress: Int = 0,
    val ocrTotal: Int = 0,
    val showBottomSheet: Boolean = false,
    val showRenameDialog: Boolean = false,
    val showPdfViewer: Boolean = false,
    val documentName: String = ""
)

sealed class ScanUiEvent {
    data class ShowToast(val message: String) : ScanUiEvent()
    data class ShowToastRes(val resId: Int) : ScanUiEvent()
}

private const val TAG = "MainViewModel"

/** Çok sayfalı OCR metinlerini birleştiren ayraç (TXT çıktısına da yazılır). */
private const val PAGE_SEPARATOR = "\n\n---\n\n"

class MainViewModel(
    application: Application,
    private val repository: ScanRepository
) : AndroidViewModel(application) {

    private val textRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<ScanUiEvent>(extraBufferCapacity = 1)
    val uiEvents = _uiEvents.asSharedFlow()

    /** Çalışan OCR işi — yeni tarama/OCR eskisini iptal eder (stale-response engeli). */
    private var ocrJob: Job? = null

    /** Kaydet çift-dokunuş guard'ı (duplicate history engeli). */
    private var isSaving = false

    val pagedScans: Flow<PagingData<ScanHistory>> = repository.getScansPaged()
        .cachedIn(viewModelScope)

    fun updateScanResults(images: List<Uri>, pdf: Uri?, defaultName: String) {
        _uiState.update { 
            it.copy(
                imageUris = images, 
                pdfUri = pdf,
                documentName = defaultName,
                showRenameDialog = true
            )
        }
    }

    fun updateDocumentName(name: String) {
        _uiState.update { it.copy(documentName = name) }
    }

    fun dismissRenameDialog() {
        _uiState.update { it.copy(showRenameDialog = false) }
    }

    fun dismissBottomSheet() {
        _uiState.update { it.copy(showBottomSheet = false, extractedText = "") }
    }

    fun showPdfViewer() {
        _uiState.update { it.copy(showPdfViewer = true) }
    }

    fun openScanFromHistory(scan: ScanHistory) {
        val thumbnailUris = scan.thumbnailUri?.let { raw ->
            runCatching { Uri.parse(raw) }.getOrNull()?.let { listOf(it) }
        } ?: emptyList()
        val pdf = scan.pdfUri?.let { raw ->
            runCatching { Uri.parse(raw) }.getOrNull()
        }
        _uiState.update {
            it.copy(
                imageUris = thumbnailUris,
                pdfUri = pdf,
                documentName = scan.title,
                showPdfViewer = pdf != null
            )
        }
    }

    fun dismissPdfViewer() {
        _uiState.update { it.copy(showPdfViewer = false) }
    }

    /** Tek görseli ML Kit ile yazıya çevirir (iki OCR akışının ortak çekirdeği — DUP-01). */
    private suspend fun recognizeImage(imageUri: Uri): String {
        val context = getApplication<Application>()
        val image = withContext(Dispatchers.IO) {
            InputImage.fromFilePath(context, imageUri)
        }
        return suspendCancellableCoroutine { continuation ->
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    continuation.resume(visionText.text)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    private fun showOcrError() {
        _uiState.update {
            it.copy(
                extractedText = "",
                isOcrLoading = false,
                showBottomSheet = false
            )
        }
        _uiEvents.tryEmit(ScanUiEvent.ShowToastRes(R.string.ocr_failed))
    }

    fun extractTextFromImage(imageUri: Uri) {
        ocrJob?.cancel()
        ocrJob = viewModelScope.launch {
            _uiState.update { it.copy(isOcrLoading = true, extractedText = "", ocrProgress = 0, ocrTotal = 1) }
            val started = System.currentTimeMillis()
            Logger.d(TAG, "OCR başladı (tek sayfa)")

            try {
                val result = recognizeImage(imageUri)
                ensureActive()
                _uiState.update {
                    it.copy(
                        extractedText = result,
                        isOcrLoading = false,
                        showBottomSheet = result.isNotBlank()
                    )
                }
                if (result.isBlank()) {
                    _uiEvents.emit(ScanUiEvent.ShowToastRes(R.string.text_not_found))
                }
                Logger.d(TAG, "OCR bitti (tek sayfa, ${System.currentTimeMillis() - started} ms)")
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Logger.e(TAG, "OCR başarısız (tek sayfa)", e)
                showOcrError()
            }
        }
    }

    fun extractTextFromAllImages() {
        val imageUris = _uiState.value.imageUris
        if (imageUris.isEmpty()) return

        ocrJob?.cancel()
        ocrJob = viewModelScope.launch {
            _uiState.update { it.copy(isOcrLoading = true, extractedText = "", ocrProgress = 0, ocrTotal = imageUris.size) }
            val context = getApplication<Application>()
            val started = System.currentTimeMillis()
            Logger.d(TAG, "OCR başladı (${imageUris.size} sayfa)")

            try {
                val results = mutableListOf<String>()
                var failedPages = 0

                imageUris.forEachIndexed { index, uri ->
                    ensureActive()
                    try {
                        val result = recognizeImage(uri)
                        results.add(result)
                        _uiState.update { it.copy(ocrProgress = index + 1) }
                    } catch (e: Exception) {
                        if (e is kotlinx.coroutines.CancellationException) throw e
                        Logger.e(TAG, "OCR sayfa başarısız (index=$index)", e)
                        results.add("")
                        failedPages++
                    }
                }

                val combinedText = results.filter { it.isNotBlank() }.joinToString(PAGE_SEPARATOR)

                _uiState.update {
                    it.copy(
                        extractedText = combinedText,
                        isOcrLoading = false,
                        showBottomSheet = combinedText.isNotBlank()
                    )
                }

                when {
                    failedPages > 0 && combinedText.isBlank() ->
                        _uiEvents.emit(ScanUiEvent.ShowToast(context.getString(R.string.ocr_all_pages_failed)))
                    failedPages > 0 ->
                        _uiEvents.emit(ScanUiEvent.ShowToast(context.getString(R.string.ocr_pages_failed, failedPages)))
                    combinedText.isBlank() ->
                        _uiEvents.emit(ScanUiEvent.ShowToastRes(R.string.text_not_found))
                }
                Logger.d(TAG, "OCR bitti (${imageUris.size} sayfa, $failedPages hata, ${System.currentTimeMillis() - started} ms)")
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Logger.e(TAG, "OCR başarısız (çok sayfa)", e)
                showOcrError()
            }
        }
    }

    fun saveCurrentScanToHistory() {
        val currentState = _uiState.value
        if (currentState.imageUris.isEmpty() && currentState.pdfUri == null) return
        if (isSaving) return
        val context = getApplication<Application>()
        val fallbackName = context.getString(R.string.default_doc_name)
        val resolvedName = resolveDocumentName(currentState.documentName, fallbackName)
        if (currentState.documentName.isBlank()) {
            _uiEvents.tryEmit(ScanUiEvent.ShowToastRes(R.string.doc_name_required))
            return
        }

        isSaving = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Scanner URI'leri kalıcı değil — geçmiş restart sonrası da açılsın
                // diye PDF ve kapağı app-private alana kopyala, DB'ye kopyayı yaz.
                val safeBase = resolvedName.toSafeFileName("")
                    .removeSuffix(".")
                    .ifBlank { "HeraScan" }
                val stablePdfUri = currentState.pdfUri?.let { uri ->
                    copyUriToPrivateStorage(context, uri, "pdfs", "$safeBase.pdf")
                        ?.toString() ?: uri.toString()
                }
                val stableThumbUri = currentState.imageUris.firstOrNull()?.let { uri ->
                    copyUriToPrivateStorage(context, uri, "images", "${safeBase}_kapak.jpg")
                        ?.toString() ?: uri.toString()
                }
                val history = ScanHistory(
                    title = resolvedName,
                    dateMillis = System.currentTimeMillis(),
                    pdfUri = stablePdfUri,
                    thumbnailUri = stableThumbUri
                )
                repository.insert(history)

                withContext(Dispatchers.Main) {
                    dismissRenameDialog()
                    _uiEvents.emit(ScanUiEvent.ShowToastRes(R.string.saved_to_history))
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Geçmişe kayıt başarısız", e)
                withContext(Dispatchers.Main) {
                    _uiEvents.emit(ScanUiEvent.ShowToastRes(R.string.save_history_failed))
                }
            } finally {
                isSaving = false
            }
        }
    }

    fun deleteScan(scan: ScanHistory) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.delete(scan)
                // Kayda ait app-private kopyaları da sil; yoksa dosyalar sahipsiz kalıp
                // uygulama verisi şişiyor (hiçbir yerden erişilemeyen PDF/kapak).
                deletePrivateCopies(getApplication<Application>(), scan.pdfUri, scan.thumbnailUri)
            } catch (e: Exception) {
                Logger.e(TAG, "Kayıt silinemedi (id=${scan.id})", e)
                withContext(Dispatchers.Main) {
                    _uiEvents.emit(ScanUiEvent.ShowToastRes(R.string.operation_failed))
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        textRecognizer.close()
    }
}

class MainViewModelFactory(
    private val application: Application,
    private val repository: ScanRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Bilinmeyen ViewModel sınıfı")
    }
}

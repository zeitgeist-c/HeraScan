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
import kotlinx.coroutines.Dispatchers
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

class MainViewModel(
    application: Application,
    private val repository: ScanRepository
) : AndroidViewModel(application) {

    private val textRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<ScanUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

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
        _uiState.update {
            it.copy(
                imageUris = if (scan.thumbnailUri != null) listOf(Uri.parse(scan.thumbnailUri)) else emptyList(),
                pdfUri = scan.pdfUri?.let { Uri.parse(it) },
                documentName = scan.title,
                showPdfViewer = scan.pdfUri != null
            )
        }
    }

    fun dismissPdfViewer() {
        _uiState.update { it.copy(showPdfViewer = false) }
    }

    fun extractTextFromImage(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOcrLoading = true, extractedText = "", ocrProgress = 0, ocrTotal = 1) }
            val context = getApplication<Application>()
            
            try {
                val image = withContext(Dispatchers.IO) {
                    InputImage.fromFilePath(context, imageUri)
                }
                
                val result = suspendCancellableCoroutine<String> { continuation ->
                    textRecognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            continuation.resume(visionText.text)
                        }
                        .addOnFailureListener { e ->
                            continuation.resumeWithException(e)
                        }
                }
                
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
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        extractedText = context.getString(R.string.error_occurred, e.message.toString()),
                        isOcrLoading = false,
                        showBottomSheet = true
                    )
                }
            }
        }
    }

    fun extractTextFromAllImages() {
        val imageUris = _uiState.value.imageUris
        if (imageUris.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isOcrLoading = true, extractedText = "", ocrProgress = 0, ocrTotal = imageUris.size) }
            val context = getApplication<Application>()
            
            try {
                val results = mutableListOf<String>()
                
                imageUris.forEachIndexed { index, uri ->
                    try {
                        val image = withContext(Dispatchers.IO) {
                            InputImage.fromFilePath(context, uri)
                        }
                        
                        val result = suspendCancellableCoroutine<String> { continuation ->
                            textRecognizer.process(image)
                                .addOnSuccessListener { visionText ->
                                    continuation.resume(visionText.text)
                                }
                                .addOnFailureListener { e ->
                                    continuation.resumeWithException(e)
                                }
                        }
                        
                        results.add(result)
                        _uiState.update { it.copy(ocrProgress = index + 1) }
                        
                    } catch (e: Exception) {
                        results.add("")
                    }
                }
                
                val combinedText = results.filter { it.isNotBlank() }.joinToString("\n\n---\n\n")
                
                _uiState.update { 
                    it.copy(
                        extractedText = combinedText,
                        isOcrLoading = false,
                        showBottomSheet = combinedText.isNotBlank()
                    )
                }
                
                if (combinedText.isBlank()) {
                    _uiEvents.emit(ScanUiEvent.ShowToastRes(R.string.text_not_found))
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        extractedText = context.getString(R.string.error_occurred, e.message.toString()),
                        isOcrLoading = false,
                        showBottomSheet = true
                    )
                }
            }
        }
    }

    fun saveCurrentScanToHistory() {
        val currentState = _uiState.value
        if (currentState.imageUris.isEmpty() && currentState.pdfUri == null) return
        
        viewModelScope.launch(Dispatchers.IO) {
            val history = ScanHistory(
                title = currentState.documentName,
                dateMillis = System.currentTimeMillis(),
                pdfUri = currentState.pdfUri?.toString(),
                thumbnailUri = currentState.imageUris.firstOrNull()?.toString()
            )
            repository.insert(history)
            
            withContext(Dispatchers.Main) {
                dismissRenameDialog()
                _uiEvents.emit(ScanUiEvent.ShowToastRes(R.string.saved_to_history))
            }
        }
    }

    fun deleteScan(scan: ScanHistory) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(scan)
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

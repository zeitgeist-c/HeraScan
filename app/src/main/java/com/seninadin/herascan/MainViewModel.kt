package com.seninadin.herascan

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.seninadin.herascan.data.ScanRepository
import com.seninadin.herascan.db.ScanHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ScanUiState(
    val imageUris: List<Uri> = emptyList(),
    val pdfUri: Uri? = null,
    val extractedText: String = "",
    val isOcrLoading: Boolean = false,
    val showBottomSheet: Boolean = false,
    val showRenameDialog: Boolean = false,
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

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<ScanUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    val scanHistory = repository.allScans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun extractTextFromImage(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOcrLoading = true, extractedText = "") }
            val context = getApplication<Application>()
            
            try {
                val image = withContext(Dispatchers.IO) {
                    InputImage.fromFilePath(context, imageUri)
                }
                
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        _uiState.update { 
                            it.copy(
                                extractedText = visionText.text,
                                isOcrLoading = false,
                                showBottomSheet = visionText.text.isNotBlank()
                            )
                        }
                        if (visionText.text.isBlank()) {
                            viewModelScope.launch {
                                _uiEvents.emit(ScanUiEvent.ShowToastRes(R.string.text_not_found))
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        _uiState.update { 
                            it.copy(
                                extractedText = context.getString(R.string.error_occurred, e.message.toString()),
                                isOcrLoading = false,
                                showBottomSheet = true
                            )
                        }
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

package com.seninadin.herascan

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.seninadin.herascan.data.ScanRepository
import com.seninadin.herascan.db.ScanHistory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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

class MainViewModel(private val repository: ScanRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

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

    fun extractTextFromImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOcrLoading = true, extractedText = "") }
            
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
                            Toast.makeText(context, context.getString(R.string.text_not_found), Toast.LENGTH_SHORT).show()
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
            }
        }
    }

    fun deleteScan(scan: ScanHistory) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(scan)
        }
    }
}

class MainViewModelFactory(private val repository: ScanRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Bilinmeyen ViewModel sınıfı")
    }
}

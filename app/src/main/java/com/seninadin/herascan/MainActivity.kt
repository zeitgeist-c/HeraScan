package com.seninadin.herascan

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.seninadin.herascan.data.ScanRepository
import com.seninadin.herascan.db.HeraScanDatabase
import com.seninadin.herascan.ui.components.*
import com.seninadin.herascan.ui.theme.HeraScanTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HeraScanTheme {
                HeraScanApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeraScanApp() {
    val context = LocalContext.current
    
    // Repository ve ViewModel Kurulumu
    val dao = remember { HeraScanDatabase.getDatabase(context.applicationContext).scanHistoryDao() }
    val repository = remember { ScanRepository(dao) }
    val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(repository))
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val history by viewModel.scanHistory.collectAsStateWithLifecycle()
    
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val scannerOptions = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(20)
            .setResultFormats(
                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG, 
                GmsDocumentScannerOptions.RESULT_FORMAT_PDF
            )
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
    }
    val scanner = remember { GmsDocumentScanning.getClient(scannerOptions) }
    val formatter = remember { SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("tr", "TR")) }

    val cancelMsg = stringResource(R.string.scan_cancelled)
    val defaultFileNamePrefix = stringResource(R.string.default_doc_name)

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanningResult?.let { scanResult ->
                val images = scanResult.pages?.map { it.imageUri } ?: emptyList()
                val pdf = scanResult.pdf?.uri
                val defaultName = defaultFileNamePrefix + " " + formatter.format(Date())
                viewModel.updateScanResults(images, pdf, defaultName)
            }
        } else {
            Toast.makeText(context, cancelMsg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.app_name), 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            )
        },
        floatingActionButton = {
            val failMsg = stringResource(R.string.scanner_failed)
            ExtendedFloatingActionButton(
                onClick = {
                    scanner.getStartScanIntent(context as Activity)
                        .addOnSuccessListener { intentSender ->
                            scanLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, String.format(failMsg, e.message), Toast.LENGTH_LONG).show()
                        }
                },
                icon = { Icon(Icons.Default.AddPhotoAlternate, contentDescription = stringResource(R.string.new_scan_desc)) },
                text = { Text(stringResource(R.string.new_scan), fontWeight = FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.imageUris.isNotEmpty()) {
                ActiveScanSection(uiState, viewModel, context, scope)
            } else {
                EmptyStateSection()
            }

            if (history.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                HistorySection(history = history, onDelete = { viewModel.deleteScan(it) })
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (uiState.showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.dismissBottomSheet() },
                sheetState = sheetState
            ) {
                OcrResultContent(uiState.extractedText, context)
            }
        }
        
        if (uiState.showRenameDialog) {
            RenameDocumentDialog(uiState, viewModel, context)
        }
    }
}

// ------ YARDIMCI GÖREV FONKSİYONLARI (Kapsam dışına çıkmaması için burada kaldılar) ------

fun shareFile(context: Context, scope: kotlinx.coroutines.CoroutineScope, uri: Uri, mimeType: String) {
    scope.launch {
        try {
            val extension = if (mimeType == "application/pdf") ".pdf" else ".jpg"
            val folderName = if (mimeType == "application/pdf") "pdfs" else "images"
            
            val providerUri = withContext(Dispatchers.IO) {
                val tempFolder = File(context.cacheDir, folderName)
                if (!tempFolder.exists()) tempFolder.mkdirs()

                val tempFile = File(tempFolder, "share_temp_${System.currentTimeMillis()}$extension")
                
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                FileProvider.getUriForFile(
                    context,
                    "${context.applicationContext.packageName}.fileprovider",
                    tempFile
                )
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, providerUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_file_chooser)))
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, context.getString(R.string.share_error, e.message.toString()), Toast.LENGTH_SHORT).show()
        }
    }
}

fun savePdfToDevice(context: Context, scope: kotlinx.coroutines.CoroutineScope, sourceUri: Uri?) {
    if (sourceUri == null) {
        Toast.makeText(context, context.getString(R.string.save_pdf_not_found), Toast.LENGTH_SHORT).show()
        return
    }

    scope.launch {
        try {
            val fileName = "HeraScan_${System.currentTimeMillis()}.pdf"
            val resolver = context.contentResolver
            
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/HeraScan")
                }
            }

            val isSuccess = withContext(Dispatchers.IO) {
                val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        resolver.openInputStream(sourceUri)?.use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    true
                } else false
            }

            if (isSuccess) {
                Toast.makeText(context, context.getString(R.string.save_success, fileName), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, context.getString(R.string.save_failed), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, context.getString(R.string.save_error, e.message.toString()), Toast.LENGTH_SHORT).show()
        }
    }
}

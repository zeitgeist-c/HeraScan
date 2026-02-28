package com.seninadin.herascan

import android.app.Activity
import android.os.Bundle
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
import kotlinx.coroutines.tasks.await
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
    val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(context.applicationContext as android.app.Application, repository))
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val history by viewModel.scanHistory.collectAsStateWithLifecycle()
    
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel.uiEvents) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is ScanUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is ScanUiEvent.ShowToastRes -> {
                    Toast.makeText(context, context.getString(event.resId), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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
                    scope.launch {
                        try {
                            val intentSender = withContext(Dispatchers.IO) {
                                kotlinx.coroutines.tasks.await(scanner.getStartScanIntent(context as Activity))
                            }
                            scanLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                        } catch (e: Exception) {
                            Toast.makeText(context, String.format(failMsg, e.message), Toast.LENGTH_LONG).show()
                        }
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

@file:Suppress("SpellCheckingInspection")
package com.seninadin.herascan

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.seninadin.herascan.data.ScanRepository
import com.seninadin.herascan.db.HeraScanDatabase
import com.seninadin.herascan.ui.components.*
import com.seninadin.herascan.ui.theme.HeraScanTheme
import com.seninadin.herascan.utils.AppLanguage
import com.seninadin.herascan.utils.DateFormats
import com.seninadin.herascan.utils.cleanupTempFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.util.Date

class MainActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context?) {
        // API<=32: delegate locale'i activity context'i kurulmadan once
        // gormeli; onCreate'teki cagri bu surumlerde gec kalir.
        if (newBase != null) {
            AppLanguage.applyStoredOrDefault(newBase)
        }
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // No saved language means English: the system language must not override the default.
        // On API<=32 AppCompatDelegate applies the locale only through AppCompatActivity
        // set up early in attachBaseContext.
        AppLanguage.applyStoredOrDefault(this)
        super.onCreate(savedInstanceState)
        setContent {
            // dynamicColor = false → Marka renkleri korunur
            HeraScanTheme(dynamicColor = false) {
                HeraScanApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeraScanApp() {
    val context = LocalContext.current
    val activity = remember { context as? Activity }

    LaunchedEffect(Unit) {
        cleanupTempFiles(context)
    }

    // ViewModel kurulumu
    val application = context.applicationContext as android.app.Application
    val dao         = remember { HeraScanDatabase.getDatabase(application).scanHistoryDao() }
    val repository  = remember { ScanRepository(dao) }
    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(application, repository)
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagingItems = viewModel.pagedScans.collectAsLazyPagingItems()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope      = rememberCoroutineScope()

    // Language: persisted preference + first-launch step (English when unset).
    var langMenuOpen by remember { mutableStateOf(false) }
    var showLangOnboarding by remember { mutableStateOf(AppLanguage.savedTag(context) == null) }
    var pendingLang by remember { mutableStateOf(AppLanguage.DEFAULT) }
    val activeLang = AppLanguage.currentTag(context)

    // Toast events
    LaunchedEffect(viewModel.uiEvents) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is ScanUiEvent.ShowToast    -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is ScanUiEvent.ShowToastRes -> Toast.makeText(context, context.getString(event.resId), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Scanner settings
    val scannerOptions = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(50)
            .setResultFormats(
                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                GmsDocumentScannerOptions.RESULT_FORMAT_PDF
            )
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
    }
    val scanner   = remember { GmsDocumentScanning.getClient(scannerOptions) }

    val cancelMsg           = stringResource(R.string.scan_cancelled)
    val defaultFileNamePrefix = stringResource(R.string.default_doc_name)

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanningResult?.let { scanResult ->
                val images = scanResult.pages?.map { it.imageUri } ?: emptyList()
                val pdf    = scanResult.pdf?.uri
                val name   = defaultFileNamePrefix + " " + DateFormats.formatScanTimestamp(Date())
                viewModel.updateScanResults(images, pdf, name)
            }
        } else {
            Toast.makeText(context, cancelMsg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Logo mark
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = Icons.Default.DocumentScanner,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "v${BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                actions = {
                    IconButton(onClick = { langMenuOpen = true }) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = stringResource(R.string.language_title)
                        )
                    }
                    DropdownMenu(
                        expanded = langMenuOpen,
                        onDismissRequest = { langMenuOpen = false }
                    ) {
                        AppLanguage.LANGUAGES.forEach { (code, _, label) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (code == activeLang) "● $label" else label,
                                        fontWeight = if (code == activeLang) FontWeight.SemiBold else null
                                    )
                                },
                                onClick = {
                                    langMenuOpen = false
                                    if (code != activeLang) AppLanguage.apply(context, code)
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            val failMsg = stringResource(R.string.scanner_start_failed)
            var isLaunching by remember { mutableStateOf(false) }
            ExtendedFloatingActionButton(
                onClick = {
                    if (activity == null || isLaunching) return@ExtendedFloatingActionButton
                    isLaunching = true
                    scope.launch {
                        try {
                            val intentSender = withContext(Dispatchers.IO) {
                                scanner.getStartScanIntent(activity).await()
                            }
                            scanLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                        } catch (e: Exception) {
                            Toast.makeText(context, failMsg, Toast.LENGTH_LONG).show()
                        } finally {
                            isLaunching = false
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = stringResource(R.string.new_scan_desc)
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.new_scan),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary,
                shape          = MaterialTheme.shapes.large
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // Main content
            if (uiState.imageUris.isNotEmpty()) {
                ActiveScanSection(uiState, viewModel, context, scope)
            } else {
                EmptyStateSection()
            }

            // History
            if (pagingItems.itemCount > 0) {
                Spacer(Modifier.height(28.dp))
                Divider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))
                HistorySection(
                    pagingItems = pagingItems,
                    onScanClick = { viewModel.openScanFromHistory(it) },
                    onDelete = { viewModel.deleteScan(it) }
                )
                Spacer(Modifier.height(80.dp))
            } else {
                Spacer(Modifier.height(80.dp))
            }
        }

        // First-launch language step: ask with the English default when unset.
        if (showLangOnboarding) {
            AlertDialog(
                onDismissRequest = { /* cannot be dismissed without choosing */ },
                title = { Text(stringResource(R.string.onboarding_language_title)) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_language_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        AppLanguage.LANGUAGES.forEach { (code, _, label) ->
                            val selected = code == pendingLang
                            TextButton(
                                onClick = { pendingLang = code },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (selected) "● $label" else label,
                                    fontWeight = if (selected) FontWeight.SemiBold else null,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showLangOnboarding = false
                        AppLanguage.apply(context, pendingLang)
                    }) {
                        Text(stringResource(R.string.continue_button))
                    }
                }
            )
        }

        // OCR Bottom Sheet
        if (uiState.showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.dismissBottomSheet() },
                sheetState       = sheetState,
                shape            = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                containerColor   = MaterialTheme.colorScheme.surface,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp, bottom = 6.dp)
                            .size(width = 36.dp, height = 4.dp)
                    ) {
                        BottomSheetDefaults.DragHandle()
                    }
                }
            ) {
                OcrResultContent(uiState.extractedText, uiState.documentName, context)
            }
        }

        // Naming dialog
        if (uiState.showRenameDialog) {
            RenameDocumentDialog(uiState, viewModel, context)
        }

        // PDF viewer
        if (uiState.showPdfViewer) {
            PdfViewerScreen(
                pdfUri  = uiState.pdfUri,
                onClose = { viewModel.dismissPdfViewer() }
            )
        }
    }
}

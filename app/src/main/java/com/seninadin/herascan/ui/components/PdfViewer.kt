@file:Suppress("SpellCheckingInspection")
package com.seninadin.herascan.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seninadin.herascan.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MAX_PDF_DIMENSION = 2048
private const val TARGET_WIDTH_DP = 400

/**
 * Açılmış PDF oturumu (fd + renderer). Main thread'de açılmaz:
 * `openFileDescriptor` disk I/O, `PdfRenderer` kurulumu dosya ayrıştırma yapar;
 * büyük PDF'lerde kare düşmesi/ANR üretiyordu. Sahibi PdfViewerScreen'dir.
 */
private class PdfSession(val fd: ParcelFileDescriptor, val renderer: PdfRenderer)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    pdfUri: Uri?,
    onClose: () -> Unit
) {
    if (pdfUri == null) return

    val context = LocalContext.current
    var pageCount by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var session by remember(pdfUri) { mutableStateOf<PdfSession?>(null) }

    LaunchedEffect(pdfUri) {
        isLoading = true
        error = null
        val loaded = withContext(Dispatchers.IO) {
            runCatching {
                val fd = context.contentResolver.openFileDescriptor(pdfUri, "r")
                    ?: return@runCatching null
                try {
                    PdfSession(fd, PdfRenderer(fd))
                } catch (e: Exception) {
                    runCatching { fd.close() }
                    throw e
                }
            }.getOrNull()
        }
        session = loaded
        pageCount = loaded?.renderer?.pageCount ?: 0
        if (loaded == null) error = context.getString(R.string.pdf_open_error)
        isLoading = false
    }

    DisposableEffect(Unit) {
        onDispose {
            session?.let { open ->
                runCatching { open.renderer.close() }
                runCatching { open.fd.close() }
            }
        }
    }

    BackHandler(onBack = onClose)

    Scaffold(
        topBar = {
            PdfViewerTopBar(
                pageCount = pageCount,
                onClose = onClose
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = stringResource(R.string.loading_pdf),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = error ?: stringResource(R.string.error_occurred, ""),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        TextButton(onClick = onClose) {
                            Text(
                                text = stringResource(R.string.back),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            items = List(pageCount) { it },
                            key = { index, _ -> index }
                        ) { index, _ ->
                            PdfPageCard(
                                renderer = session?.renderer,
                                pageIndex = index,
                                totalPages = pageCount,
                                contentDescription = stringResource(R.string.page_number, index + 1)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PdfViewerTopBar(
    pageCount: Int,
    onClose: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.pdf_viewer),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            if (pageCount > 0) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Text(
                        text = "$pageCount ${stringResource(R.string.pages)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun PdfPageCard(
    renderer: PdfRenderer?,
    pageIndex: Int,
    totalPages: Int,
    contentDescription: String
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loadError by remember { mutableStateOf(false) }

    LaunchedEffect(pageIndex) {
        if (renderer != null) {
            withContext(Dispatchers.IO) {
                try {
                    synchronized(renderer) {
                        val page = renderer.openPage(pageIndex)
                        try {
                            val scale = (TARGET_WIDTH_DP.dp.value * 3f / page.width).coerceIn(0.5f, 2f)
                            val targetWidth = (page.width * scale).toInt().coerceAtMost(MAX_PDF_DIMENSION)
                            val targetHeight = (page.height * scale).toInt().coerceAtMost(MAX_PDF_DIMENSION)
                            val bmp = Bitmap.createBitmap(
                                targetWidth.coerceAtLeast(1),
                                targetHeight.coerceAtLeast(1),
                                Bitmap.Config.ARGB_8888
                            )
                            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            bitmap = bmp
                        } finally {
                            page.close()
                        }
                    }
                } catch (e: Exception) {
                    loadError = true
                }
            }
        }
    }

    DisposableEffect(pageIndex) {
        onDispose {
            // Compose çizim hattı bitmap'i bir sonraki karede kullanabilir; recycle() çökme riski taşır.
            bitmap = null
        }
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = stringResource(R.string.page_x_of_y, pageIndex + 1, totalPages),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            if (scale > 1f) {
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = "${(scale * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        val currentBitmap = bitmap
        if (currentBitmap != null && !loadError) {
            var gestureModifier = Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                        }
                    }
                )
            }
            if (scale > 1f) {
                gestureModifier = gestureModifier.then(
                    Modifier.pointerInput(scale) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) {
                                val maxPan = 300f * (scale - 1)
                                offset = Offset(
                                    x = (offset.x + pan.x).coerceIn(-maxPan, maxPan),
                                    y = (offset.y + pan.y).coerceIn(-maxPan, maxPan)
                                )
                            } else {
                                offset = Offset.Zero
                            }
                        }
                    }
                )
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .then(gestureModifier),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Image(
                    bitmap = currentBitmap.asImageBitmap(),
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                )
            }
        } else if (loadError) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.page_load_error),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        if (scale > 1f) {
            TextButton(
                onClick = {
                    scale = 1f
                    offset = Offset.Zero
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = stringResource(R.string.reset_zoom),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

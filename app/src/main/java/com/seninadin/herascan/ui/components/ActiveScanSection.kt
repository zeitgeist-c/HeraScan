@file:Suppress("SpellCheckingInspection")
package com.seninadin.herascan.ui.components

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.seninadin.herascan.MainViewModel
import com.seninadin.herascan.R
import com.seninadin.herascan.ScanUiState
import com.seninadin.herascan.utils.savePdfToDevice
import com.seninadin.herascan.utils.shareFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// ════════════════════════════════════════════════════════════════
//  Yardımcı Bileşenler
// ════════════════════════════════════════════════════════════════

/** Bölüm üstü küçük başlık — örn. "AKTİF TARAMA" */
@Composable
internal fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.08.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 2.dp, bottom = 8.dp)
    )
}

/** Sayfa küçük resmi — tarama kartı içinde */
@Composable
private fun PageThumbnail(
    uri: Uri,
    index: Int,
    isFirst: Boolean,
    context: Context
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            // Kart çerçevesi
            Card(
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    width = if (isFirst) 2.dp else 1.dp,
                    color = if (isFirst) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.size(72.dp, 100.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(uri)
                        .crossfade(true)
                        .size(144, 200)
                        .build(),
                    contentDescription = stringResource(R.string.scanned_page, index + 1),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // Sayfa numarası rozeti
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isFirst) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isFirst) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.page_number, index + 1),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** PDF dosyasını gösteren yeşil banner */
@Composable
private fun PdfBanner(
    documentName: String,
    pageCount: Int,
    onView: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // PDF ikonu
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
            // İsim ve bilgi
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$documentName.pdf",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$pageCount sayfa",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            // Görüntüle butonu
            FilledTonalButton(
                onClick = onView,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                modifier = Modifier.height(30.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text(
                    text = stringResource(R.string.view_pdf),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/** Aksiyon kartı için stil türü */
private enum class ActionStyle { Primary, Secondary, Outlined }

/** 2×2 ızgarasındaki aksiyon kartı */
@Composable
private fun ActionCard(
    icon: ImageVector,
    label: String,
    subtitle: String,
    style: ActionStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val containerColor = when (style) {
        ActionStyle.Primary   -> MaterialTheme.colorScheme.primary
        ActionStyle.Secondary -> MaterialTheme.colorScheme.secondaryContainer
        ActionStyle.Outlined  -> MaterialTheme.colorScheme.surface
    }
    val labelColor = when (style) {
        ActionStyle.Primary   -> MaterialTheme.colorScheme.onPrimary
        ActionStyle.Secondary -> MaterialTheme.colorScheme.onSecondaryContainer
        ActionStyle.Outlined  -> MaterialTheme.colorScheme.onSurface
    }
    val iconTint = when (style) {
        ActionStyle.Primary   -> MaterialTheme.colorScheme.onPrimary
        ActionStyle.Secondary -> MaterialTheme.colorScheme.secondary
        ActionStyle.Outlined  -> MaterialTheme.colorScheme.primary
    }
    val iconBg = when (style) {
        ActionStyle.Primary   -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)
        ActionStyle.Secondary -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        ActionStyle.Outlined  -> MaterialTheme.colorScheme.primaryContainer
    }
    val borderColor = when (style) {
        ActionStyle.Primary   -> MaterialTheme.colorScheme.primary
        ActionStyle.Secondary -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
        ActionStyle.Outlined  -> MaterialTheme.colorScheme.outlineVariant
    }
    val subtitleColor = when (style) {
        ActionStyle.Primary   -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.65f)
        else                  -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        enabled = enabled,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = labelColor,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = subtitleColor,
                maxLines = 1
            )
        }
    }
}

/** Amber renkli OCR tam genişlik butonu */
@Composable
private fun OcrActionRow(
    uiState: ScanUiState,
    viewModel: MainViewModel
) {
    Surface(
        onClick = {
            if (uiState.imageUris.size > 1) viewModel.extractTextFromAllImages()
            else uiState.imageUris.firstOrNull()?.let { viewModel.extractTextFromImage(it) }
        },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)),
        enabled = !uiState.isOcrLoading,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Amber ikon kutusu
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isOcrLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            // Metin
            Column(modifier = Modifier.weight(1f)) {
                val title = if (uiState.imageUris.size > 1)
                    stringResource(R.string.read_all_pages, uiState.imageUris.size)
                else
                    stringResource(R.string.read_text)
                val subtitle = if (uiState.isOcrLoading && uiState.ocrTotal > 1)
                    "${stringResource(R.string.extracting_all)} (${uiState.ocrProgress}/${uiState.ocrTotal})"
                else
                    "OCR ile metin çıkar · yapay zeka destekli"

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Ana Bileşen
// ════════════════════════════════════════════════════════════════

@Composable
fun ActiveScanSection(
    uiState: ScanUiState,
    viewModel: MainViewModel,
    context: Context,
    scope: CoroutineScope
) {
    // ── Bölüm Başlığı ─────────────────────────────────────────────
    SectionLabel(stringResource(R.string.current_scan))

    // ── Tarama Kartı ──────────────────────────────────────────────
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Kart başlığı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.documentName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${uiState.imageUris.size} Sayfa",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Sayfa küçük resimleri
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(uiState.imageUris) { index, uri ->
                    PageThumbnail(
                        uri = uri,
                        index = index,
                        isFirst = index == 0,
                        context = context
                    )
                }
            }

            // PDF Banner
            if (uiState.pdfUri != null) {
                PdfBanner(
                    documentName = uiState.documentName,
                    pageCount = uiState.imageUris.size,
                    onView = { viewModel.showPdfViewer() },
                    modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                )
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    // ── Aksiyon Bölümü ────────────────────────────────────────────
    SectionLabel("İşlemler")   // strings.xml'e R.string.actions olarak ekleyin

    val pdfNotFoundMsg = stringResource(R.string.pdf_not_found)

    // Satır 1: PDF Paylaş · Cihaza Kaydet
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActionCard(
            icon = Icons.Default.Share,
            label = stringResource(R.string.share_pdf),
            subtitle = "Uygulama seç",
            style = ActionStyle.Primary,
            modifier = Modifier.weight(1f),
            onClick = {
                uiState.pdfUri?.let { uri ->
                    scope.launch {
                        val result = shareFile(context, uri, "application/pdf")
                        if (result.isSuccess) {
                            result.getOrNull()?.let { context.startActivity(it) }
                        } else {
                            val error = result.exceptionOrNull()?.message
                            Toast.makeText(
                                context,
                                context.getString(R.string.share_error, error),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } ?: Toast.makeText(context, pdfNotFoundMsg, Toast.LENGTH_SHORT).show()
            }
        )
        ActionCard(
            icon = Icons.Default.CheckCircle,
            label = stringResource(R.string.save_pdf),
            subtitle = "İndirilenler",
            style = ActionStyle.Secondary,
            modifier = Modifier.weight(1f),
            onClick = {
                scope.launch {
                    val result = savePdfToDevice(context, uiState.pdfUri)
                    if (result.isSuccess) {
                        val fileName = result.getOrNull()
                        Toast.makeText(
                            context,
                            context.getString(R.string.save_success, fileName),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        val error = result.exceptionOrNull()?.message
                        Toast.makeText(
                            context,
                            error ?: context.getString(R.string.save_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }

    Spacer(Modifier.height(8.dp))

    // Satır 2: Görsel Paylaş · PDF Önizle
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActionCard(
            icon = Icons.Default.Image,
            label = stringResource(R.string.share_image),
            subtitle = "İlk sayfa",
            style = ActionStyle.Outlined,
            modifier = Modifier.weight(1f),
            onClick = {
                if (uiState.imageUris.isNotEmpty()) {
                    scope.launch {
                        val result = shareFile(context, uiState.imageUris.first(), "image/jpeg")
                        if (result.isSuccess) {
                            result.getOrNull()?.let { context.startActivity(it) }
                        } else {
                            val error = result.exceptionOrNull()?.message
                            Toast.makeText(
                                context,
                                context.getString(R.string.share_error, error),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        )
        ActionCard(
            icon = Icons.Default.PictureAsPdf,
            label = stringResource(R.string.view_pdf),
            subtitle = "Uygulama içi",
            style = ActionStyle.Outlined,
            enabled = uiState.pdfUri != null,
            modifier = Modifier.weight(1f),
            onClick = { viewModel.showPdfViewer() }
        )
    }

    Spacer(Modifier.height(8.dp))

    // OCR Butonu
    OcrActionRow(uiState = uiState, viewModel = viewModel)
}

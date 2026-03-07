package com.seninadin.herascan.ui.components

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.seninadin.herascan.MainViewModel
import com.seninadin.herascan.R
import com.seninadin.herascan.ScanUiState
import com.seninadin.herascan.utils.savePdfToDevice
import com.seninadin.herascan.utils.shareFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ActiveScanSection(
    uiState: ScanUiState, 
    viewModel: MainViewModel, 
    context: Context,
    scope: CoroutineScope
) {
    Text(
        stringResource(R.string.current_scan),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(12.dp))

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        itemsIndexed(uiState.imageUris) { index, uri ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.size(150.dp, 210.dp)
                ) {
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(context)
                            .data(uri)
                            .crossfade(true)
                            .size(300, 420) // Bellek için küçültülmüş render
                            .build(),
                        contentDescription = stringResource(R.string.scanned_page, index + 1),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        stringResource(R.string.page_number, index + 1),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val pdfNotFoundMsg = stringResource(R.string.pdf_not_found)
            Button(
                onClick = {
                    uiState.pdfUri?.let { uri ->
                        scope.launch {
                            val result = shareFile(context, uri, "application/pdf")
                            if (result.isSuccess) {
                                val intent = result.getOrNull()
                                if (intent != null) {
                                    context.startActivity(intent)
                                }
                            } else {
                                val error = result.exceptionOrNull()?.message
                                Toast.makeText(context, context.getString(R.string.share_error, error), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } ?: Toast.makeText(context, pdfNotFoundMsg, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.share_pdf))
            }

            Button(
                onClick = { 
                    scope.launch {
                        val result = savePdfToDevice(context, uiState.pdfUri)
                        if (result.isSuccess) {
                            val fileName = result.getOrNull()
                            Toast.makeText(context, context.getString(R.string.save_success, fileName), Toast.LENGTH_LONG).show()
                        } else {
                            val error = result.exceptionOrNull()?.message
                            Toast.makeText(context, error ?: context.getString(R.string.save_failed), Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.save_pdf))
            }

            if (uiState.pdfUri != null) {
                OutlinedButton(
                    onClick = { viewModel.showPdfViewer() },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.view_pdf))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (uiState.imageUris.isNotEmpty()) {
                        scope.launch {
                            val result = shareFile(context, uiState.imageUris.first(), "image/jpeg")
                            if (result.isSuccess) {
                                val intent = result.getOrNull()
                                if (intent != null) {
                                    context.startActivity(intent)
                                }
                            } else {
                                val error = result.exceptionOrNull()?.message
                                Toast.makeText(context, context.getString(R.string.share_error, error), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.share_image))
            }

            OutlinedButton(
                onClick = {
                    uiState.imageUris.firstOrNull()?.let {
                        viewModel.extractTextFromImage(it)
                    }
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isOcrLoading
            ) {
                if (uiState.isOcrLoading && uiState.ocrTotal == 1) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp), 
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.extracting_text))
                } else {
                    Icon(Icons.Default.TextSnippet, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.read_text))
                }
            }
        }

        if (uiState.imageUris.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.extractTextFromAllImages() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isOcrLoading
            ) {
                if (uiState.isOcrLoading && uiState.ocrTotal > 1) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("${stringResource(R.string.extracting_all)} (${uiState.ocrProgress}/${uiState.ocrTotal})")
                } else {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.read_all_pages, uiState.imageUris.size))
                }
            }
        }
    }
}

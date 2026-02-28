package com.seninadin.herascan.ui.components

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.seninadin.herascan.R
import com.seninadin.herascan.db.ScanHistory

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistorySection(history: List<ScanHistory>, onDelete: (ScanHistory) -> Unit) {
    var scanToDelete by remember { mutableStateOf<ScanHistory?>(null) }

    Text(
        stringResource(R.string.history_scans),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(12.dp))
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(history, key = { it.id }) { scan ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box {
                    ElevatedCard(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .size(100.dp, 140.dp)
                            .combinedClickable(
                                onClick = { /* Detay görünümü eklenebilir */ },
                                onLongClick = { scanToDelete = scan }
                            )
                    ) {
                        if (scan.thumbnailUri != null) {
                            AsyncImage(
                                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                    .data(Uri.parse(scan.thumbnailUri))
                                    .crossfade(true)
                                    .size(200, 280) // Thumbnail boyutu için agresif downsampling
                                    .build(),
                                contentDescription = scan.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant), 
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    
                    IconButton(
                        onClick = { scanToDelete = scan },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Sil", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = scan.title,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(100.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (scanToDelete != null) {
        AlertDialog(
            onDismissRequest = { scanToDelete = null },
            title = { Text(stringResource(R.string.delete_document)) },
            text = { Text(stringResource(R.string.delete_document_confirm, scanToDelete?.title ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(scanToDelete!!)
                        scanToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { scanToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

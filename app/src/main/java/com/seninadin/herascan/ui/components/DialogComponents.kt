package com.seninadin.herascan.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.seninadin.herascan.MainViewModel
import com.seninadin.herascan.R
import com.seninadin.herascan.ScanUiState

@Composable
fun EmptyStateSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.DocumentScanner,
            contentDescription = "Boş Durum",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.empty_state_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.empty_state_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun OcrResultContent(extractedText: String, context: Context) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            stringResource(R.string.extracted_text),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false) 
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(16.dp)
        ) {
            Text(
                text = extractedText, 
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.align(Alignment.End)
        ) {
            val copyMsg = stringResource(R.string.text_copied)
            OutlinedButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("HeraScan OCR", extractedText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, copyMsg, Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.copy))
            }
            
            val shareChooser = stringResource(R.string.share_text_chooser)
            Button(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, extractedText)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, shareChooser))
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.share))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun RenameDocumentDialog(uiState: ScanUiState, viewModel: MainViewModel, context: Context) {
    val savedDefaultMsg = stringResource(R.string.saved_default_name)
    val savedHistoryMsg = stringResource(R.string.saved_to_history)

    AlertDialog(
        onDismissRequest = {
            viewModel.saveCurrentScanToHistory()
        },
        title = { Text(stringResource(R.string.rename_document), style = MaterialTheme.typography.titleMedium) },
        text = {
            OutlinedTextField(
                value = uiState.documentName,
                onValueChange = { viewModel.updateDocumentName(it) },
                label = { Text(stringResource(R.string.document_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(onClick = {
                viewModel.saveCurrentScanToHistory()
            }) {
                Text(stringResource(R.string.save))
            }
        }
    )
}

@file:Suppress("SpellCheckingInspection")
package com.seninadin.herascan.ui.components

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PersistableBundle
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seninadin.herascan.MainViewModel
import com.seninadin.herascan.R
import com.seninadin.herascan.ScanUiState
import com.seninadin.herascan.utils.DateFormats
import com.seninadin.herascan.utils.resolveDocumentName
import com.seninadin.herascan.utils.saveTextToFile
import com.seninadin.herascan.utils.rememberStoragePermissionGate
import kotlinx.coroutines.launch
import java.util.Date

// ════════════════════════════════════════════════════════════════
//  Empty-state screen
// ════════════════════════════════════════════════════════════════

@Composable
fun EmptyStateSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Illustration
        EmptyStateIllustration()

        Spacer(Modifier.height(28.dp))

        Text(
            text = stringResource(R.string.empty_state_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_state_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 260.dp)
        )

        Spacer(Modifier.height(24.dp))

        // Info chip
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = stringResource(R.string.empty_state_tip),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/** Empty-state illustration drawn with Compose */
@Composable
private fun EmptyStateIllustration() {
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Shadow
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(width = 80.dp, height = 8.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.outlineVariant)
        )

        // Arka belge
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(x = 8.dp, y = (-12).dp)
                .size(width = 60.dp, height = 78.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(6.dp)
                    )
                )
        )

        // Ana belge
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-8).dp)
                .size(width = 66.dp, height = 86.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surface)
                .then(
                    Modifier.border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(6.dp)
                    )
                )
        ) {
            // Document lines
            Column(
                modifier = Modifier.padding(start = 10.dp, top = 20.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                repeat(5) { i ->
                    val widths = listOf(0.85f, 0.70f, 0.90f, 0.60f, 0.80f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(widths[i])
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }
        }

        // Camera icon (top right)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  OCR result content (shown inside the bottom sheet)
// ════════════════════════════════════════════════════════════════

@Composable
fun OcrResultContent(
    extractedText: String,
    documentName: String,
    context: Context
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 32.dp)
    ) {
        // Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.TextSnippet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = stringResource(R.string.extracted_text),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Metin kutusu
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 280.dp)
        ) {
            Text(
                text = extractedText,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
            )
        }

        Spacer(Modifier.height(20.dp))

        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val copyMsg = stringResource(R.string.text_copied)
            val legacyNotice = stringResource(R.string.clipboard_legacy_notice)

            // Kopyala
            OutlinedButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("HeraScan OCR", extractedText)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // Sensitive OCR text like ID/IBAN numbers must not appear in clipboard previews.
                        clip.description.extras = PersistableBundle().apply {
                            putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                        }
                    }
                    clipboard.setPrimaryClip(clip)
                    val msg = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        copyMsg
                    } else {
                        "$copyMsg $legacyNotice"
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                },
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.weight(1f).height(44.dp)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.copy),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Share
            val shareChooser = stringResource(R.string.share_text_chooser)
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, extractedText)
                    }
                    context.startActivity(Intent.createChooser(intent, shareChooser))
                },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.weight(1f).height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.share),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        val saveMsg = stringResource(R.string.save_text_success)
        val saveDeniedMsg = stringResource(R.string.save_permission_denied)
        val defaultName = stringResource(R.string.default_doc_name)
        val runSaveWithPermission = rememberStoragePermissionGate(
            onDenied = { Toast.makeText(context, saveDeniedMsg, Toast.LENGTH_SHORT).show() }
        )
        OutlinedButton(
            onClick = {
                runSaveWithPermission {
                    scope.launch {
                        val base = resolveDocumentName(documentName, defaultName)
                        val fileName = "${base}_${DateFormats.formatFileTimestamp(Date())}"
                        val result = saveTextToFile(context, extractedText, fileName)
                        if (result.isSuccess) {
                            Toast.makeText(context, saveMsg, Toast.LENGTH_SHORT).show()
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
            },
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Icon(
                Icons.Default.Save,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.save_text),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Document naming dialog
// ════════════════════════════════════════════════════════════════

@Composable
fun RenameDocumentDialog(
    uiState: ScanUiState,
    viewModel: MainViewModel,
    context: Context
) {
    AlertDialog(
        onDismissRequest = { viewModel.dismissRenameDialog() },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.rename_document),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.rename_document_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            OutlinedTextField(
                value = uiState.documentName,
                onValueChange = { viewModel.updateDocumentName(it) },
                label = {
                    Text(
                        text = stringResource(R.string.document_name),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { viewModel.saveCurrentScanToHistory() },
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.save),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { viewModel.dismissRenameDialog() }
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

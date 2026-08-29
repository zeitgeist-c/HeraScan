package com.seninadin.herascan.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.seninadin.herascan.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

private const val TEMP_FILE_MAX_AGE_HOURS = 24L

fun cleanupTempFiles(context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        val pdfFolder = File(context.cacheDir, "pdfs")
        val imagesFolder = File(context.cacheDir, "images")
        
        val folders = listOf(pdfFolder, imagesFolder).filter { it.exists() }
        val maxAgeMillis = TimeUnit.HOURS.toMillis(TEMP_FILE_MAX_AGE_HOURS)
        val now = System.currentTimeMillis()
        
        folders.forEach { folder ->
            folder.listFiles()?.forEach { file ->
                if (now - file.lastModified() > maxAgeMillis) {
                    file.delete()
                }
            }
        }
    }
}

fun clearAllTempFiles(context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        val pdfFolder = File(context.cacheDir, "pdfs")
        val imagesFolder = File(context.cacheDir, "images")
        
        listOf(pdfFolder, imagesFolder).forEach { folder ->
            if (folder.exists()) {
                folder.deleteRecursively()
            }
        }
    }
}

/**
 * Dosya paylaşımı için geçici bir URI oluşturur ve Paylaşım Intent'i döner.
 */
suspend fun shareFile(
    context: Context,
    uri: Uri,
    mimeType: String,
    documentName: String
): Result<Intent> {
    return withContext(Dispatchers.IO) {
        try {
            val extension = if (mimeType == "application/pdf") ".pdf" else ".jpg"
            val folderName = if (mimeType == "application/pdf") "pdfs" else "images"
            
            val tempFolder = File(context.cacheDir, folderName)
            if (!tempFolder.exists()) tempFolder.mkdirs()

            val tempFile = File(tempFolder, documentName.toSafeFileName(extension))
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            val providerUri = FileProvider.getUriForFile(
                context,
                "${context.applicationContext.packageName}.fileprovider",
                tempFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, providerUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            Result.success(Intent.createChooser(shareIntent, context.getString(R.string.share_file_chooser)))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

/**
 * OCR metnini TXT dosyası olarak cihaza kaydeder.
 */
suspend fun saveTextToFile(context: Context, text: String, fileName: String): Result<String> {
    return withContext(Dispatchers.IO) {
        try {
            val actualFileName = if (fileName.endsWith(".txt")) fileName else "$fileName.txt"
            val resolver = context.contentResolver

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, actualFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/HeraScan")
                }
            }

            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(text.toByteArray(Charsets.UTF_8))
                }
                Result.success(actualFileName)
            } else {
                Result.failure(Exception(context.getString(R.string.save_failed)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception(context.getString(R.string.save_error, e.message)))
        }
    }
}

/**
 * Belirtilen URI'deki PDF'i cihazın Belgeler (Documents) klasörüne kaydeder.
 */
suspend fun savePdfToDevice(
    context: Context,
    sourceUri: Uri?,
    documentName: String
): Result<String> {
    if (sourceUri == null) {
        return Result.failure(Exception(context.getString(R.string.save_pdf_not_found)))
    }

    return withContext(Dispatchers.IO) {
        try {
            val fileName = documentName.toSafeFileName(".pdf")
            val resolver = context.contentResolver
            
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/HeraScan")
                }
            }

            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    resolver.openInputStream(sourceUri)?.use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Result.success(fileName)
            } else {
                Result.failure(Exception(context.getString(R.string.save_failed)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception(context.getString(R.string.save_error, e.message)))
        }
    }
}

internal fun String.toSafeFileName(extension: String): String {
    val normalizedExtension = if (extension.startsWith(".")) extension else ".$extension"
    val nameWithoutExtension = if (endsWith(normalizedExtension, ignoreCase = true)) {
        dropLast(normalizedExtension.length)
    } else {
        this
    }
    val safeName = nameWithoutExtension
        .trim()
        .replace(Regex("[\\\\/:*?\"<>|]"), "_")
        .trimEnd('.', ' ')
        .ifBlank { "HeraScan" }

    return "$safeName$normalizedExtension"
}

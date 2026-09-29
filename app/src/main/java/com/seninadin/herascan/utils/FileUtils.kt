package com.seninadin.herascan.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.seninadin.herascan.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

private const val TEMP_FILE_MAX_AGE_HOURS = 24L
private const val TAG = "FileUtils"

/** Paylaşım için kopyalanan kaynağın üst sınırı (100 MB). */
private const val SHARE_SOURCE_MAX_BYTES = 100L * 1024 * 1024

/**
 * Boş belge adını tek noktada çözer (DB başlığı ↔ dosya adı sözleşmesi).
 * Saf fonksiyon — birim testli.
 */
fun resolveDocumentName(raw: String, fallback: String): String {
    val trimmed = raw.trim()
    return trimmed.ifBlank { fallback }
}

/**
 * Eski temp dosyalarını temizler. Çağıranın scope'unda çalışır
 * (sahipsiz CoroutineScope kullanılmaz).
 */
suspend fun cleanupTempFiles(context: Context) {
    withContext(Dispatchers.IO) {
        val folders = listOf(
            File(context.cacheDir, "pdfs"),
            File(context.cacheDir, "images")
        ).filter { it.exists() }
        val maxAgeMillis = TimeUnit.HOURS.toMillis(TEMP_FILE_MAX_AGE_HOURS)
        val now = System.currentTimeMillis()

        folders.forEach { folder ->
            folder.listFiles()?.forEach { file ->
                runCatching {
                    if (now - file.lastModified() > maxAgeMillis) file.delete()
                }.onFailure { e ->
                    Logger.e(TAG, "Temp dosya silinemedi: ${file.name}", e)
                }
            }
        }
    }
}

/**
 * Kaynak URI'yi app-private cache'e kopyalar (geçmiş kayıtlarının
 * cihaz yeniden başlatıldıktan sonra da açılması için).
 * Dönüş: kopyanın content URI'si, ya da kopya başarısızsa null.
 */
suspend fun copyUriToPrivateStorage(
    context: Context,
    sourceUri: Uri,
    folderName: String,
    fileName: String
): Uri? {
    return withContext(Dispatchers.IO) {
        runCatching {
            val folder = File(context.cacheDir, folderName)
            if (!folder.exists()) folder.mkdirs()
            val dest = File(folder, fileName)
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(dest).use { output -> input.copyTo(output) }
            } ?: return@runCatching null
            FileProvider.getUriForFile(
                context,
                "${context.applicationContext.packageName}.fileprovider",
                dest
            )
        }.onFailure { e ->
            Logger.e(TAG, "Private kopya başarısız: $folderName/$fileName", e)
        }.getOrNull()
    }
}

private fun querySourceSize(context: Context, uri: Uri): Long? {
    return runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val size = cursor.getLong(0)
                    if (size >= 0) size else null
                } else null
            }
    }.getOrNull()
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
            val size = querySourceSize(context, uri)
            if (size != null && size > SHARE_SOURCE_MAX_BYTES) {
                return@withContext Result.failure(
                    Exception(context.getString(R.string.save_failed))
                )
            }
            val mime = context.contentResolver.getType(uri) ?: mimeType
            val extension = if (mime == "application/pdf") ".pdf" else ".jpg"
            val folderName = if (mime == "application/pdf") "pdfs" else "images"

            val tempFolder = File(context.cacheDir, folderName)
            if (!tempFolder.exists()) tempFolder.mkdirs()

            val baseName = resolveDocumentName(documentName, "HeraScan")
            val uniqueName = "${baseName}_${UUID.randomUUID().toString().take(8)}".toSafeFileName(extension)
            val tempFile = File(tempFolder, uniqueName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(
                Exception(context.getString(R.string.save_failed))
            )

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
            Logger.e(TAG, "Paylaşım hazırlanamadı", e)
            Result.failure(Exception(context.getString(R.string.save_failed)))
        }
    }
}

/**
 * MediaStore yazma kalıbı: insert → yaz → rollback.
 * `saveTextToFile` ve `savePdfToDevice` ortak kullanır (DUP-02).
 */
private suspend fun insertMediaFile(
    context: Context,
    displayName: String,
    mimeType: String,
    write: suspend (OutputStream) -> Unit
): Result<Unit> {
    return withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/HeraScan")
                }
            }

            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            val outputStream = uri?.let { resolver.openOutputStream(it) }
            if (uri != null && outputStream != null) {
                outputStream.use { write(it) }
                Result.success(Unit)
            } else {
                if (uri != null) resolver.delete(uri, null, null)
                Result.failure(Exception(context.getString(R.string.save_failed)))
            }
        } catch (e: Exception) {
            Logger.e(TAG, "MediaStore yazma başarısız: $displayName", e)
            Result.failure(Exception(context.getString(R.string.save_failed)))
        }
    }
}

/**
 * OCR metnini TXT dosyası olarak cihaza kaydeder.
 */
suspend fun saveTextToFile(context: Context, text: String, fileName: String): Result<String> {
    val actualFileName = if (fileName.endsWith(".txt")) fileName else "$fileName.txt"
    return insertMediaFile(context, actualFileName, "text/plain") { output ->
        output.write(text.toByteArray(Charsets.UTF_8))
    }.map { actualFileName }
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
            val fileName = resolveDocumentName(documentName, "HeraScan").toSafeFileName(".pdf")
            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return@withContext Result.failure(
                    Exception(context.getString(R.string.save_pdf_not_found))
                )

            inputStream.use { input ->
                insertMediaFile(context, fileName, "application/pdf") { output ->
                    input.copyTo(output)
                }.map { fileName }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "PDF kaydetme başarısız", e)
            Result.failure(Exception(context.getString(R.string.save_failed)))
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

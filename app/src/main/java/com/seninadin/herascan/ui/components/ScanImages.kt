package com.seninadin.herascan.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import com.seninadin.herascan.R

/**
 * Coil görsel isteği tek noktadan (DUP-04): crossfade + sabit boyut.
 * [data] Uri ya da String olabilir.
 */
@Composable
fun rememberScanImageRequest(data: Any?, width: Int, height: Int): ImageRequest {
    val context = LocalContext.current
    return remember(data, width, height) {
        ImageRequest.Builder(context)
            .data(data)
            .crossfade(true)
            .size(width, height)
            .build()
    }
}

/**
 * Paylaşım sonucu tek noktadan (DUP-03): başarıda chooser'ı açar,
 * hatada sabit kullanıcı mesajı gösterir (iç detay sızdırmaz).
 */
fun Context.launchShareResult(result: Result<Intent>) {
    if (result.isSuccess) {
        result.getOrNull()?.let { startActivity(it) }
    } else {
        Toast.makeText(
            this,
            getString(R.string.share_error, getString(R.string.operation_failed)),
            Toast.LENGTH_SHORT
        ).show()
    }
}

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
 * Coil image requests from one place (DUP-04): crossfade + fixed size.
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
 * Share result from one place (DUP-03): opens the chooser on success,
 * shows a fixed user message on failure (no internals leak).
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

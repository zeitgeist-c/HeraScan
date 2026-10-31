package com.seninadin.herascan.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Returns a gate requesting the WRITE_EXTERNAL_STORAGE runtime permission
 * needed for MediaStore writes on pre-API-29 devices.
 *
 * - API >= 29 or already granted: the action runs directly.
 * - Otherwise it is requested; the pending action runs when granted, [onDenied] when denied.
 */
@Composable
fun rememberStoragePermissionGate(onDenied: () -> Unit): ((() -> Unit) -> Unit) {
    val context = LocalContext.current
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val action = pendingAction
        pendingAction = null
        if (granted) action?.invoke() else onDenied()
    }

    val gate: (() -> Unit) -> Unit = remember(context) {
        { action ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                action()
            } else {
                pendingAction = action
                launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }
    return gate
}

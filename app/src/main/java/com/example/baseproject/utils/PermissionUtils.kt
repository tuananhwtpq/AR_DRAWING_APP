package com.example.baseproject.utils

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {
    fun checkCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkRecordAudioPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(
        mActivity: Activity,
        permission: String,
        permissionLauncher: ActivityResultLauncher<String>,
        goToSettingLauncher: ActivityResultLauncher<Intent>
    ) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
            goToSetting(mActivity, goToSettingLauncher)
        } else {
            permissionLauncher.launch(permission)
        }
    }

    private fun goToSetting(mContext: Context, launcher: ActivityResultLauncher<Intent>) {
        try {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", mContext.packageName, null)
                launcher.launch(this)
            }
        } catch (e: ActivityNotFoundException) {
            Log.e("TAG", "showDialogGoToSetting: ${e.message}")
        }
    }

    fun checkNotiPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }
}
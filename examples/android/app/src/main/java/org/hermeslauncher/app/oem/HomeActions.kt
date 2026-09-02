package org.hermeslauncher.app.oem

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.Manifest
import android.widget.Toast
import androidx.core.content.ContextCompat
import org.hermeslauncher.app.R
import org.hermeslauncher.app.launcher.DoubleTapAction

object HomeActions {
    @Volatile
    private var torchOn: Boolean = false

    fun onDoubleTap(context: Context, action: DoubleTapAction, requestCamera: () -> Unit) {
        when (action) {
            DoubleTapAction.OFF -> Unit
            DoubleTapAction.FLASHLIGHT -> {
                if (!toggleTorch(context)) {
                    requestCamera()
                }
            }
            DoubleTapAction.LOCK -> lockOrPrompt(context)
        }
    }

    fun toggleTorch(context: Context): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        val cameras = context.getSystemService(CameraManager::class.java) ?: return true
        val id = cameras.cameraIdList.firstOrNull() ?: return true
        val next = !torchOn
        return runCatching {
            cameras.setTorchMode(id, next)
            torchOn = next
            true
        }.getOrDefault(true)
    }

    fun setTorch(context: Context, on: Boolean) {
        if (!on && !torchOn) {
            return
        }
        val cameras = context.getSystemService(CameraManager::class.java) ?: return
        val id = cameras.cameraIdList.firstOrNull() ?: return
        runCatching { cameras.setTorchMode(id, on) }
        torchOn = on
    }

    fun lockOrPrompt(context: Context) {
        val admin = ComponentName(context, HermesDeviceAdmin::class.java)
        val dpm = context.getSystemService(DevicePolicyManager::class.java) ?: return
        if (dpm.isAdminActive(admin)) {
            runCatching { dpm.lockNow() }
            return
        }
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                context.getString(R.string.device_admin_explain),
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            runCatching { context.startActivity(intent) }
        } else {
            Toast.makeText(context, R.string.device_admin_explain, Toast.LENGTH_SHORT).show()
        }
    }
}

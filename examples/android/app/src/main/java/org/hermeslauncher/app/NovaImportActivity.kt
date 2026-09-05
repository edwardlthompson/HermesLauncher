package org.hermeslauncher.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hermeslauncher.app.workspace.NovaImportApply

/** SAF picker so HOME (Launcher3) can import a Nova `.novabackup` without a Compose Activity Result. */
class NovaImportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val picker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) {
                finish()
                return@registerForActivityResult
            }
            lifecycleScope.launch {
                val placed = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.use { input ->
                        NovaImportApply.applyStream(
                            application as HermesApplication,
                            input,
                            requireLauncher = false,
                        )
                    }
                }
                val message = if (placed == null) {
                    getString(R.string.backup_nova_fail)
                } else {
                    getString(R.string.backup_nova_ok, placed)
                }
                Toast.makeText(this@NovaImportActivity, message, Toast.LENGTH_LONG).show()
                finish()
            }
        }
        if (savedInstanceState == null) {
            picker.launch(arrayOf("application/zip", "application/octet-stream", "*/*"))
        }
    }
}

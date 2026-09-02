package org.hermeslauncher.app.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.hermeslauncher.app.R
import org.hermeslauncher.app.oem.OemFamily
import org.hermeslauncher.app.oem.RepairPolicy
import org.hermeslauncher.app.ui.theme.SpacingMd
import org.hermeslauncher.app.ui.theme.SpacingSm

@Composable
fun RepairBanner(
    oem: OemFamily,
    onRepair: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val body = when (RepairPolicy.primaryStep(oem)) {
        "samsung_never_sleeping" -> stringResource(R.string.oem_body_samsung)
        "xiaomi_autostart" -> stringResource(R.string.oem_body_xiaomi)
        else -> stringResource(R.string.oem_body_generic)
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Column(modifier = Modifier.padding(SpacingMd)) {
            Text(
                text = stringResource(R.string.oem_banner_title, oemLabel(oem)),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = SpacingSm, bottom = SpacingSm),
            )
            Button(onClick = onRepair) {
                Text(stringResource(R.string.oem_repair_action))
            }
        }
    }
}

@Composable
private fun oemLabel(oem: OemFamily): String {
    val res = when (oem) {
        OemFamily.ONEPLUS -> R.string.oem_name_oneplus
        OemFamily.SAMSUNG -> R.string.oem_name_samsung
        OemFamily.XIAOMI -> R.string.oem_name_xiaomi
        OemFamily.PIXEL -> R.string.oem_name_pixel
        OemFamily.LINEAGE -> R.string.oem_name_lineage
        OemFamily.OTHER -> R.string.oem_name_other
    }
    return stringResource(res)
}

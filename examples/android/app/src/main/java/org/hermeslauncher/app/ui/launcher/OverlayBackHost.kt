package org.hermeslauncher.app.ui.launcher

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

/** Launcher is not a ComponentActivity; FilterBar's BackHandler needs an owner. */
@Composable
fun OverlayBackHost(content: @Composable () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val owner = remember(lifecycleOwner) {
        object : OnBackPressedDispatcherOwner, LifecycleOwner by lifecycleOwner {
            override val onBackPressedDispatcher = OnBackPressedDispatcher()
        }
    }
    CompositionLocalProvider(LocalOnBackPressedDispatcherOwner provides owner, content = content)
}

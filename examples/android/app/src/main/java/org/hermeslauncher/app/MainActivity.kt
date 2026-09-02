package org.hermeslauncher.app

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.hermeslauncher.app.about.AppUpdatePreferences
import org.hermeslauncher.app.crashcapture.CrashCapture
import org.hermeslauncher.app.display.WindowRefresh
import org.hermeslauncher.app.network.NetworkStatusMonitor
import org.hermeslauncher.app.launcher.HomePulse
import org.hermeslauncher.app.oem.HomeActions
import org.hermeslauncher.app.ui.HermesApp
import org.hermeslauncher.app.ui.theme.ThemePreferences
import org.hermeslauncher.app.vault.HermesNotificationListener
import org.hermeslauncher.app.widgets.WidgetHostController

class MainActivity : ComponentActivity() {
    private var networkStatusMonitor: NetworkStatusMonitor? = null
    private lateinit var widgetController: WidgetHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setBackgroundDrawableResource(android.R.color.transparent)
        CrashCapture.install(applicationContext)
        val app = application as HermesApplication
        widgetController = WidgetHostController(
            activity = this,
            host = app.widgetHost,
            store = app.widgetStore,
            scope = lifecycleScope,
        )
        val themePreferences = ThemePreferences(applicationContext)
        val appUpdatePreferences = AppUpdatePreferences(applicationContext)
        networkStatusMonitor = NetworkStatusMonitor(applicationContext).also { it.start() }

        lifecycleScope.launch {
            appUpdatePreferences.clearPendingRestart()
            appUpdatePreferences.ensureInstalledFormat()
        }

        setContent {
            HermesApp(
                context = this,
                scope = lifecycleScope,
                themePreferences = themePreferences,
                appUpdatePreferences = appUpdatePreferences,
                networkStatusMonitor = networkStatusMonitor!!,
                widgetController = widgetController,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val cats = intent.categories ?: emptySet()
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) &&
            HomePulse.isHome(intent.action, cats)
        ) {
            (application as HermesApplication).homePulse.tryEmit(Unit)
        }
    }

    override fun onStart() {
        super.onStart()
        WindowRefresh.applyTo(this)
        val app = application as HermesApplication
        app.widgetHost.startListening()
        NotificationListenerService.requestRebind(
            ComponentName(this, HermesNotificationListener::class.java),
        )
        lifecycleScope.launch { app.feeds.refresh() }
    }

    override fun onStop() {
        HomeActions.setTorch(this, false)
        (application as HermesApplication).widgetHost.stopListening()
        super.onStop()
    }

    override fun onDestroy() {
        networkStatusMonitor?.stop()
        super.onDestroy()
    }
}

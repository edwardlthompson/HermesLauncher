package org.hermeslauncher.app

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
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
import org.hermeslauncher.app.widgets.WidgetHostTick

class MainActivity : ComponentActivity() {
    private var networkStatusMonitor: NetworkStatusMonitor? = null
    private lateinit var widgetController: WidgetHostController
    private var tickRegistered = false
    private val timeTick = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != Intent.ACTION_TIME_TICK) {
                return
            }
            tickWidgets()
        }
    }

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
        val cats = intent.categories
        val home = HomePulse.isHome(intent.action, cats) || intent.hasCategory(Intent.CATEGORY_HOME)
        Log.i(TAG, "onNewIntent action=${intent.action} cats=$cats hasHome=${intent.hasCategory(Intent.CATEGORY_HOME)} home=$home")
        if (home) {
            val sent = (application as HermesApplication).homePulse.tryEmit(Unit)
            Log.i(TAG, "onNewIntent emit=$sent")
        }
    }

    override fun onStart() {
        super.onStart()
        WindowRefresh.applyTo(this)
        val app = application as HermesApplication
        runCatching { app.widgetHost.startListening() }
        if (!tickRegistered) {
            ContextCompat.registerReceiver(
                this,
                timeTick,
                IntentFilter(Intent.ACTION_TIME_TICK),
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
            tickRegistered = true
        }
        tickWidgets()
        NotificationListenerService.requestRebind(
            ComponentName(this, HermesNotificationListener::class.java),
        )
        lifecycleScope.launch { app.feeds.refresh() }
    }

    override fun onStop() {
        if (tickRegistered) {
            unregisterReceiver(timeTick)
            tickRegistered = false
        }
        HomeActions.setTorch(this, false)
        super.onStop()
    }

    override fun onDestroy() {
        networkStatusMonitor?.stop()
        super.onDestroy()
    }

    private fun tickWidgets() {
        val host = (application as HermesApplication).widgetHost
        WidgetHostTick.poke(this, host.appWidgetIds)
    }

    companion object {
        private const val TAG: String = "HermesHome"
    }
}

package org.hermeslauncher.app

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.view.View
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherState
import com.android.launcher3.logging.StatsLogManager.LauncherEvent
import com.android.launcher3.statemanager.StateManager
import com.android.launcher3.util.TouchController
import com.android.launcher3.util.IntArray
import com.android.launcher3.util.IntSet
import com.android.launcher3.AbstractFloatingView
import com.android.launcher3.views.OptionsPopupView
import com.android.systemui.plugins.shared.LauncherOverlayManager
import org.hermeslauncher.app.l3.HermesSwipeController
import org.hermeslauncher.app.l3.L3InstantSwipe
import org.hermeslauncher.app.l3.HomeAgainSearch
import org.hermeslauncher.app.l3.L3GestureHost
import org.hermeslauncher.app.l3.L3Live
import org.hermeslauncher.app.l3.L3WidgetTick
import org.hermeslauncher.app.ui.launcher.WallpaperIntents
import org.hermeslauncher.app.workspace.HermesPages

class HermesLauncherActivity : Launcher() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        L3Live.attach(this)
        stateManager.addStateListener(object : StateManager.StateListener<LauncherState> {
            override fun onStateTransitionStart(toState: LauncherState) = Unit
            override fun onStateTransitionComplete(finalState: LauncherState) {
                if (finalState == LauncherState.ALL_APPS) {
                    L3GestureHost.afterAllApps(this@HermesLauncherActivity)
                }
            }
        })
        handleArticleExtra(intent)
    }

    override fun onStart() {
        super.onStart()
        L3WidgetTick.attach(this)
    }

    override fun onStop() {
        L3WidgetTick.detach(this)
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        NotificationListenerService.requestRebind(
            ComponentName(this, org.hermeslauncher.app.vault.HermesNotificationListener::class.java),
        )
    }

    override fun onDestroy() {
        L3Live.detach()
        super.onDestroy()
    }

    override fun createTouchControllers(): Array<TouchController> {
        return arrayOf(dragController, L3InstantSwipe(this), HermesSwipeController(this))
    }

    override fun onNewIntent(intent: Intent) {
        val openSearch = HomeAgainSearch.shouldOpen(
            alreadyOnHome = HomeAgainSearch.alreadyOnHome(this, intent),
            inNormal = isInState(LauncherState.NORMAL),
            onInbox = HomeAgainSearch.onInbox(this),
            floatingOpen = AbstractFloatingView.getTopOpenView(this) != null,
            actionMain = Intent.ACTION_MAIN == intent.action,
        )
        super.onNewIntent(intent)
        handleArticleExtra(intent)
        if (openSearch) {
            HomeAgainSearch.show(this)
        }
    }

    private fun handleArticleExtra(intent: Intent?) {
        val id = intent?.getStringExtra(org.hermeslauncher.app.feeds.FeedNotify.EXTRA_ARTICLE_ID) ?: return
        (application as HermesApplication).pendingArticleId.value = id
        runCatching { workspace.snapToPage(0) }
    }

    override fun getDefaultOverlay(): LauncherOverlayManager {
        return object : LauncherOverlayManager {}
    }

    override fun bindScreens(orderedScreenIds: IntArray) {
        super.bindScreens(orderedScreenIds)
        HermesPages.ensure(workspace)
    }

    override fun finishBindingItems(pagesBoundFirst: IntSet) {
        super.finishBindingItems(pagesBoundFirst)
        HermesPages.ensure(workspace)
        workspace.moveToDefaultScreen()
        L3WidgetTick.poke(this)
    }

    override fun showDefaultOptions(x: Float, y: Float) {
        val items = OptionsPopupView.getOptions(this)
        val settings = if (items.isNotEmpty()) items.removeAt(items.lastIndex) else null
        items.add(
            OptionsPopupView.OptionItem(
                this,
                R.string.home_option_live_wallpaper,
                com.android.launcher3.R.drawable.ic_palette,
                LauncherEvent.IGNORE,
                View.OnLongClickListener {
                    WallpaperIntents.startLiveOrToast(this)
                    true
                },
            ),
        )
        items.add(
            OptionsPopupView.OptionItem(
                this,
                R.string.home_option_icon,
                com.android.launcher3.R.drawable.ic_apps,
                LauncherEvent.IGNORE,
                View.OnLongClickListener {
                    stateManager.goToState(LauncherState.ALL_APPS)
                    true
                },
            ),
        )
        items.add(
            OptionsPopupView.OptionItem(
                this,
                R.string.about_open,
                android.R.drawable.ic_menu_info_details,
                LauncherEvent.IGNORE,
                View.OnLongClickListener {
                    startActivity(HermesSettingsActivity.intent(this, org.hermeslauncher.app.ui.settings.SettingsSection.ABOUT))
                    true
                },
            ),
        )
        if (settings != null) {
            items.add(settings)
        }
        OptionsPopupView.show(this, getPopupTarget(x, y), items, false)
    }
}

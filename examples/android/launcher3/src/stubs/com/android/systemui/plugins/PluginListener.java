package com.android.systemui.plugins;

import android.content.Context;

/** Stub of SystemUI PluginCoreLib (not shipped; privileged). */
public interface PluginListener<T extends Plugin> {
    void onPluginConnected(T plugin, Context context);

    default void onPluginDisconnected(T plugin) {}
}

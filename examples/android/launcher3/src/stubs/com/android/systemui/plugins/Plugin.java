package com.android.systemui.plugins;

import android.content.Context;

/** Stub of SystemUI PluginCoreLib (not shipped; privileged). */
public interface Plugin {
    default void onCreate(Context sysuiContext, Context pluginContext) {}

    default void onDestroy() {}
}

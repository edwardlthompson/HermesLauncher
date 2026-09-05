package com.android.launcher3;

import android.content.ComponentName;
import android.content.Context;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class to filter out components from various lists
 */
public class AppFilter {

    private final Set<ComponentName> mFilteredComponents;

    /** Extra package hide list (Hermes drawer blacklist). */
    public static volatile java.util.function.Predicate<String> sHiddenPackage = pkg -> false;

    public AppFilter(Context context) {
        mFilteredComponents = Arrays.stream(
                context.getResources().getStringArray(R.array.filtered_components))
                .map(ComponentName::unflattenFromString)
                .collect(Collectors.toSet());
    }

    public boolean shouldShowApp(ComponentName app) {
        if (app != null && sHiddenPackage.test(app.getPackageName())) {
            return false;
        }
        return !mFilteredComponents.contains(app);
    }
}

package com.android.launcher3.icons;

import static android.content.Intent.ACTION_MANAGED_PROFILE_ADDED;
import static android.content.Intent.ACTION_MANAGED_PROFILE_REMOVED;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.SparseLongArray;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.android.launcher3.icons.cache.BaseIconCache;

/**
 * User-app compatible SimpleIconCache (AOSP calls {@code UserHandle.getIdentifier()} which is hidden).
 */
public class SimpleIconCache extends BaseIconCache {

    private static SimpleIconCache sIconCache = null;
    private static final Object CACHE_LOCK = new Object();

    private final SparseLongArray mUserSerialMap = new SparseLongArray(2);
    private final UserManager mUserManager;

    public SimpleIconCache(Context context, String dbFileName, Looper bgLooper, int iconDpi,
            int iconPixelSize, boolean inMemoryCache) {
        super(context, dbFileName, bgLooper, iconDpi, iconPixelSize, inMemoryCache);
        mUserManager = context.getSystemService(UserManager.class);
        IntentFilter filter = new IntentFilter(ACTION_MANAGED_PROFILE_ADDED);
        filter.addAction(ACTION_MANAGED_PROFILE_REMOVED);
        ContextCompat.registerReceiver(context, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                resetUserCache();
            }
        }, filter, null, new Handler(bgLooper), ContextCompat.RECEIVER_EXPORTED);
    }

    @Override
    protected long getSerialNumberForUser(@NonNull UserHandle user) {
        synchronized (mUserSerialMap) {
            int key = user.hashCode();
            int index = mUserSerialMap.indexOfKey(key);
            if (index >= 0) {
                return mUserSerialMap.valueAt(index);
            }
            long serial = mUserManager.getSerialNumberForUser(user);
            mUserSerialMap.put(key, serial);
            return serial;
        }
    }

    private void resetUserCache() {
        synchronized (mUserSerialMap) {
            mUserSerialMap.clear();
        }
    }

    @Override
    protected boolean isInstantApp(@NonNull ApplicationInfo info) {
        return mContext.getPackageManager().isInstantApp(info.packageName);
    }

    @NonNull
    @Override
    public BaseIconFactory getIconFactory() {
        return IconFactory.obtain(mContext);
    }

    public static SimpleIconCache getIconCache(Context context) {
        synchronized (CACHE_LOCK) {
            if (sIconCache != null) {
                return sIconCache;
            }
            boolean inMemoryCache =
                    context.getResources().getBoolean(R.bool.simple_cache_enable_im_memory);
            String dbFileName = context.getString(R.string.cache_db_name);
            HandlerThread bgThread = new HandlerThread("simple-icon-cache");
            bgThread.start();
            sIconCache = new SimpleIconCache(context.getApplicationContext(), dbFileName,
                    bgThread.getLooper(), context.getResources().getConfiguration().densityDpi,
                    context.getResources().getDimensionPixelSize(R.dimen.default_icon_bitmap_size),
                    inMemoryCache);
            return sIconCache;
        }
    }
}

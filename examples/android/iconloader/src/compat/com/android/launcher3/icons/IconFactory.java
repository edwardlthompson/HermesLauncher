package com.android.launcher3.icons;

import android.content.Context;

/** Thread-safe pool of {@link BaseIconFactory}. Copied from AOSP src_full_lib. */
public class IconFactory extends BaseIconFactory {

    private static final Object sPoolSync = new Object();
    private static IconFactory sPool;
    private static int sPoolId = 0;

    public static IconFactory obtain(Context context) {
        int poolId;
        synchronized (sPoolSync) {
            if (sPool != null) {
                IconFactory m = sPool;
                sPool = m.next;
                m.next = null;
                return m;
            }
            poolId = sPoolId;
        }
        return new IconFactory(
            context,
            context.getResources().getConfiguration().densityDpi,
            context.getResources().getDimensionPixelSize(R.dimen.default_icon_bitmap_size),
            poolId
        );
    }

    public static void clearPool() {
        synchronized (sPoolSync) {
            sPool = null;
            sPoolId++;
        }
    }

    private final int mPoolId;
    private IconFactory next;

    private IconFactory(Context context, int fillResIconDpi, int iconBitmapSize, int poolId) {
        super(context, fillResIconDpi, iconBitmapSize);
        mPoolId = poolId;
    }

    public void recycle() {
        synchronized (sPoolSync) {
            if (sPoolId != mPoolId) {
                return;
            }
            clear();
            next = sPool;
            sPool = this;
        }
    }

    @Override
    public void close() {
        recycle();
    }
}

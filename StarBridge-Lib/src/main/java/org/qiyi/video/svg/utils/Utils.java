package org.qiyi.video.svg.utils;

import android.os.Looper;

/**
 * Created by wangallen on 2018/3/27.
 */

public final class Utils {

    private Utils() {
    }

    public static boolean isOnBackgroundThread() {
        return !isOnMainThread();
    }

    public static boolean isOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

}

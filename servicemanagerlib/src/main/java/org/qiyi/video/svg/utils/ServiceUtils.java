package org.qiyi.video.svg.utils;

import android.content.Context;
import android.content.Intent;

/**
 * Created by wangallen on 2018/2/26.
 */

public class ServiceUtils {

    private ServiceUtils() {
    }

    /**
     * 考虑到Android 8.0在后台调用startService时会抛出IllegalStateException
     *
     * @param context
     * @param intent
     */
    public static void startServiceSafely(Context context, Intent intent) {
        if (null == context) {
            return;
        }
        try {
            context.startService(intent);
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }
    }

}

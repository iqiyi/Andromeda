package org.qiyi.video.svg.log;

import android.util.Log;

/**
 * Created by wangallen on 2018/1/15.
 */

public class Logger {

    private static final String TAG = "ServiceManager";

    public static void i(String msg) {
        Log.i(TAG, msg);
    }

    public static void d(String msg) {
        Log.d(TAG, msg);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }

}

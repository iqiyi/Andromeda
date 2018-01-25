package org.qiyi.video.svg.utils;

import android.app.ActivityManager;
import android.content.Context;

/**
 * Created by wangallen on 2018/1/25.
 */

public class ProcessUtils {

    public static boolean isMainProcess(Context context) {
        String processName = getCurrentProcessName(context);
        if (processName.equals(context.getPackageName())) {
            return true;
        }
        return false;
    }

    public static String getCurrentProcessName(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo processInfo : activityManager.getRunningAppProcesses()) {
            if (processInfo.pid == android.os.Process.myPid()) {
                return processInfo.processName;
            }
        }
        return null;
    }

}

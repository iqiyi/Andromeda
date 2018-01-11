package wang.imallen.blog.servicemanager;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import wang.imallen.blog.applemodule.EatAppleImpl;
import org.qiyi.video.svg.ServiceManager;
import org.qiyi.video.svg.config.Constants;

/**
 * Created by wangallen on 2018/1/8.
 */

public class CustomApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (isMainProcess()) {
            ServiceManager.getInstance().registerLocalService(Constants.APPLE_MODULE, new EatAppleImpl());
            //TODO 让ServiceManager启动GuardService
        }
    }

    private boolean isMainProcess() {
        String processName = getCurrentProcessName();
        if (processName.equals(getPackageName())) {
            return true;
        }
        return false;
    }

    public String getCurrentProcessName() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
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

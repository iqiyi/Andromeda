package wang.imallen.blog.servicemanager.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Created by guofeng05 on 2018/6/5.
 */

public class ToastUtil {
    private static Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void postToast(final Context context, final String str) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

}

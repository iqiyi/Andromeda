package wang.imallen.blog.servicemanager.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

public class ToastUtils {

    public static void init(Context context){
        appContext=context;
    }

    private static Context appContext;

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static Toast mToast;

    private static Runnable CANCEL_RUN = new Runnable() {
        @Override public void run() {
            mToast.cancel();
        }
    };

    private ToastUtils() {
    }


    public static void showShortToast(CharSequence msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }

    public static void showLongToast(CharSequence msg) {
        showToast(msg, Toast.LENGTH_LONG);
    }

    private static void showToast(CharSequence msg, int duration) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        HANDLER.removeCallbacks(CANCEL_RUN);
        if (null == mToast) {
            mToast = Toast.makeText(appContext, msg, duration);
        } else {
            mToast.setDuration(duration);
            mToast.setText(msg);
        }
        HANDLER.postDelayed(CANCEL_RUN, duration == Toast.LENGTH_SHORT ? 1000 : 3000);
        mToast.show();
    }
}

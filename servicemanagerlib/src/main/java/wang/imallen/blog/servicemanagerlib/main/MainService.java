package wang.imallen.blog.servicemanagerlib.main;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import wang.imallen.blog.servicemanagerlib.config.Constants;

public class MainService extends Service {


    public MainService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String module = intent.getStringExtra(Constants.KEY_MODULE);
        IBinder binder = intent.getParcelableExtra(Constants.KEY_BINDER_WRAPPER);
        if (binder != null) {
            MainServiceManager.getInstance().registerIBinder(module, binder);
        }

        return super.onStartCommand(intent, flags, startId);
    }
    */
}

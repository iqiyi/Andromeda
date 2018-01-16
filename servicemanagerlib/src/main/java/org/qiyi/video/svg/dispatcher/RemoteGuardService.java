package org.qiyi.video.svg.dispatcher;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


//TODO 在主进程启动时是调用bindService()还是startService()来将RemoteServiceManager注册到主进程呢?
public class RemoteGuardService extends Service {

    private static final String TAG = "ServiceManager";

    public static final String REMOTE_GUARD_SERIVCE_MODULE = "RemoteGuardServiceModule";

    public static final String BIND_U_SELF = "BindYourSelf";

    public RemoteGuardService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return ServiceDispatcher.getInstance(this).asBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand,action:" + intent.getAction() + ",pid:" + android.os.Process.myPid() + ",thread:" + Thread.currentThread().getName());
        /*
        if (BIND_U_SELF.equals(intent.getAction())) {
            BinderWrapper binderWrapper = intent.getParcelableExtra(Constants.KEY_BINDER_WRAPPER);
            if (binderWrapper != null) {
                Log.d(TAG,"binderWrapper is not null!");
                IServiceRegister serviceRegister = IServiceRegister.Stub.asInterface(binderWrapper.getBinder());
                if (serviceRegister != null) {
                    try {
                        serviceRegister.registerRemoteService(REMOTE_GUARD_SERIVCE_MODULE,
                                ServiceDispatcher.getInstance(this).asBinder());
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        */
        return super.onStartCommand(intent, flags, startId);
    }
}

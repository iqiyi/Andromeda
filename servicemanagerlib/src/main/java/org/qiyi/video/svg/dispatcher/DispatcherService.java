package org.qiyi.video.svg.dispatcher;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import org.qiyi.video.svg.BinderWrapper;
import org.qiyi.video.svg.IDispatcherRegister;
import org.qiyi.video.svg.config.Constants;
import org.qiyi.video.svg.log.Logger;

public class DispatcherService extends Service {
    public DispatcherService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        Logger.d("DispatcherService-->onStartCommand,action:" + intent.getAction());
        if (Constants.DISPATCH_ACTION.equals(intent.getAction())) {

            BinderWrapper wrapper = intent.getParcelableExtra(Constants.KEY_DISPATHCER_REGISTER_WRAPPER);

            //BinderWrapper messengerWrapper = intent.getParcelableExtra(Constants.KEY_MESSENGER_BINDER_WRAPPER);
            BinderWrapper businessWrapper = intent.getParcelableExtra(Constants.KEY_BUSINESS_BINDER_WRAPPER);

            String serviceCanonicalName = intent.getStringExtra(Constants.KEY_SERVICE_NAME);
            //int pid = intent.getIntExtra(Constants.KEY_PID, -1);

            try {
                if (TextUtils.isEmpty(serviceCanonicalName)) {
                    Logger.e("service canonical name is null");
                } else {
                    ServiceDispatcher.getInstance(this).registerRemoteService(serviceCanonicalName, businessWrapper.getBinder());
                }
                //注意这里其实是不会有RemoteException的，因为这里并不是远程调用，只是恰好这个接口既用于远程调用也用于同进程调用
                //ServiceDispatcher.getInstance().registerRemoteService(serviceCanonicalName,pid);
                //TODO 这里好像有点问题，实际上我们并不是要注册IDispatcherRegister.Stub的IBinder,而是要注册IMessenger.Stub的IBinder
                //ServiceDispatcher.getInstance().registerRemoteServiceWithBinder(serviceCanonicalName, wrapper.getBinder(), pid);

            } catch (RemoteException ex) {
                ex.printStackTrace();
            } finally {

                IDispatcherRegister dispatcherRegister = IDispatcherRegister.Stub.asInterface(wrapper.getBinder());
                if (dispatcherRegister != null) {
                    Logger.d("now register to RemoteServiceManager");
                    try {
                        dispatcherRegister.registerDispatcher(ServiceDispatcher.getInstance(this).asBinder());
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Logger.d("IdspatcherRegister IBinder is null");
                }

            }

        }


        return super.onStartCommand(intent, flags, startId);
    }

}

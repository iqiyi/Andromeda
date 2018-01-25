package org.qiyi.video.svg.dispatcher;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import org.qiyi.video.svg.BinderWrapper;
import org.qiyi.video.svg.IRemoteTransfer;
import org.qiyi.video.svg.config.Constants;
import org.qiyi.video.svg.event.Event;
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
        //TODO 其实这里应该叫REGISTER_SERVICE_ACTION更合适
        if (Constants.DISPATCH_SERVICE_ACTION.equals(intent.getAction())) {
            registerRemoteService(intent);
        } else if (Constants.DISPATCH_EVENT_ACTION.equals(intent.getAction())) {
            publishEvent(intent);
        }


        return super.onStartCommand(intent, flags, startId);
    }

    private void publishEvent(Intent intent) {
        BinderWrapper remoteTransferWrapper = intent.getParcelableExtra(Constants.KEY_REMOTE_TRANSFER_WRAPPER);
        int pid = intent.getIntExtra(Constants.KEY_PID, -1);
        IBinder remoteTransferBinder = remoteTransferWrapper.getBinder();
        registerAndReverseRegister(pid, remoteTransferBinder);
        Event event = intent.getParcelableExtra(Constants.KEY_EVENT);
        try {
            Dispatcher.getInstance(this).publish(event);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 注册和反向注册
     *
     * @param pid
     * @param transterBinder
     */
    private void registerAndReverseRegister(int pid, IBinder transterBinder) {
        Logger.d("DispatcherService-->registerAndReverseRegister,pid=" + pid);
        IRemoteTransfer remoteTransfer = IRemoteTransfer.Stub.asInterface(transterBinder);

        Dispatcher.getInstance(this).registerRemoteTransfer(pid, transterBinder);

        if (remoteTransfer != null) {
            Logger.d("now register to RemoteTransfer");
            try {
                remoteTransfer.registerDispatcher(Dispatcher.getInstance(this).asBinder());
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        } else {
            Logger.d("IdspatcherRegister IBinder is null");
        }
    }

    private void registerRemoteService(Intent intent) {

        BinderWrapper wrapper = intent.getParcelableExtra(Constants.KEY_REMOTE_TRANSFER_WRAPPER);
        BinderWrapper businessWrapper = intent.getParcelableExtra(Constants.KEY_BUSINESS_BINDER_WRAPPER);
        String serviceCanonicalName = intent.getStringExtra(Constants.KEY_SERVICE_NAME);
        int pid = intent.getIntExtra(Constants.KEY_PID, -1);
        try {
            if (TextUtils.isEmpty(serviceCanonicalName)) {
                Logger.e("service canonical name is null");
            } else {
                Dispatcher.getInstance(this).registerRemoteService(serviceCanonicalName, businessWrapper.getBinder());
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } finally {
            registerAndReverseRegister(pid, wrapper.getBinder());
        }

    }


}

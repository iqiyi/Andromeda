package wang.imallen.blog.applemodule;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import wang.imallen.blog.servicemanagerlib.BinderWrapper;
import wang.imallen.blog.servicemanagerlib.IRemoteServiceManager;
import wang.imallen.blog.servicemanagerlib.config.Constants;
import wang.imallen.blog.servicemanagerlib.config.ServiceActionPolicyImpl;

public class DeliverAppleService extends Service {

    private static final String APPLE_MODULE = "apple";

    private DeliverAppleNative deliverAppleNative = new DeliverAppleNative();

    public DeliverAppleService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return deliverAppleNative.asBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ServiceActionPolicyImpl.getInstance().getFetchServiceAction(APPLE_MODULE).equals(intent.getAction())) {
            BinderWrapper binderWrapper = intent.getParcelableExtra(Constants.KEY_BINDER_WRAPPER);
            IBinder binder = binderWrapper.getBinder();
            IRemoteServiceManager proxy = IRemoteServiceManager.Stub.asInterface(binder);
            if (proxy != null) {
                try {
                    proxy.registerRemoteService(APPLE_MODULE, deliverAppleNative.asBinder());
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}

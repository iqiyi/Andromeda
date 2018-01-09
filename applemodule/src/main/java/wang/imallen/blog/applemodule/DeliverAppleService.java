package wang.imallen.blog.applemodule;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import org.qiyi.video.svg.BinderWrapper;
import org.qiyi.video.svg.IServiceDispatcher;
import org.qiyi.video.svg.config.Constants;
import org.qiyi.video.svg.config.ServiceActionPolicyImpl;

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
            IServiceDispatcher proxy = IServiceDispatcher.Stub.asInterface(binder);

            String moduleName=intent.getStringExtra("Module");
            //TODO 根据moduleName找到对应的IBinder，这部分可能需要gradle插件来生成相应代码

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

package org.qiyi.video.svg.stub;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.qiyi.video.svg.config.Constants;

public class CommuStubService extends Service {

    private static final String TAG = "ServiceRouter";

    public CommuStubService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand,pid:" + android.os.Process.myPid() + ",action:" + intent.getAction() + ",serviceName:" + intent.getStringExtra(Constants.KEY_SERVICE_NAME));
        /*
        //if (Constants.APPLE_PROCESS_SEVICE_ACTION.equals(intent.getAction())) {
            BinderWrapper binderWrapper = intent.getParcelableExtra(Constants.KEY_BINDER_WRAPPER);
            IBinder binder = binderWrapper.getBinder();
            IServiceDispatcher proxy = IServiceDispatcher.Stub.asInterface(binder);

            String serviceName = intent.getStringExtra(Constants.KEY_SERVICE_NAME);
            //TODO 根据moduleName找到对应的IBinder，这部分可能需要gradle插件来生成相应代码

            if (proxy != null) {
                try {
                    IInterface stubImpl = ServiceRouter.getInstance().getStubService(serviceName);
                    if(stubImpl==null){
                        Log.d(TAG,"stubImpl is null");
                        proxy.registerRemoteService(serviceName,null);
                    }else{
                        Log.d(TAG,"stubImpl is not null");
                        //TODO 直接这样是不行的，要使用asBinder
                        proxy.registerRemoteService(serviceName, stubImpl.asBinder());
                    }

                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        //}
        */

        return super.onStartCommand(intent, flags, startId);
    }

    public static class CommuStubService0 extends CommuStubService {
    }

    public static class CommuStubService1 extends CommuStubService {
    }

    public static class CommuStubService2 extends CommuStubService {
    }

    public static class CommuStubService3 extends CommuStubService {
    }

    public static class CommuStubService4 extends CommuStubService {
    }

    public static class CommuStubService5 extends CommuStubService {
    }

    public static class CommuStubService6 extends CommuStubService {
    }

}

package org.qiyi.video.svg.stub;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.qiyi.video.svg.ICommuStub;
import org.qiyi.video.svg.config.Constants;

public class CommuStubService extends Service {

    private static final String TAG = "ServiceRouter";

    public CommuStubService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO 返回null和返回有实际含义的binder,会不会造成oom_adj值不一样？
        return new ICommuStub.Stub() {
            @Override
            public void commu(Bundle args) throws RemoteException {
                //do nothing now
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand,pid:" + android.os.Process.myPid() + ",action:" + intent.getAction() + ",serviceName:" + intent.getStringExtra(Constants.KEY_SERVICE_NAME));

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

    public static class CommuStubService7 extends CommuStubService {
    }

    public static class CommuStubService8 extends CommuStubService {
    }

    public static class CommuStubService9 extends CommuStubService {
    }

    public static class CommuStubService10 extends CommuStubService {
    }

    public static class CommuStubService11 extends CommuStubService {
    }

    public static class CommuStubService12 extends CommuStubService {
    }

    public static class CommuStubService13 extends CommuStubService {
    }


}

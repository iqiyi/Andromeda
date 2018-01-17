package org.qiyi.video.svg.callback;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;

import org.qiyi.video.svg.IPCCallback;

/**
 * Created by wangallen on 2018/1/17.
 */

public abstract class BaseCallback extends IPCCallback.Stub {

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public final void onSuccess(final Bundle result) throws RemoteException {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onSucceed(result);
            }
        });
    }

    @Override
    public final void onFail(final String reason) throws RemoteException {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onFailed(reason);
            }
        });
    }

    public abstract void onSucceed(Bundle result);

    public abstract void onFailed(String reason);
}

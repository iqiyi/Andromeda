package org.qiyi.video.svg.dispatcher.service;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.qiyi.video.svg.bean.BinderBean;
import org.qiyi.video.svg.log.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangallen on 2018/1/8.
 */
public class ServiceDispatcher implements IServiceDispatcher {

    private static final String TAG = "StarBridge";

    public ServiceDispatcher() {
    }

    private Map<String, BinderBean> remoteBinderCache = new ConcurrentHashMap<>();

    @Override
    public BinderBean getTargetBinder(String serviceCanonicalName) throws RemoteException {
        Log.d(TAG, "ServiceDispatcher-->getTargetBinder,serivceName:" + serviceCanonicalName + ",pid:" + android.os.Process.myPid() + ",thread:" + Thread.currentThread().getName());
        BinderBean bean = remoteBinderCache.get(serviceCanonicalName);
        if (null == bean) {
            //TODO 这里是不是要在出现这种情况时通过startService()来启动进程?
            return null;
        } else {
            return bean;
        }
    }

    @Override
    public void registerRemoteService(final String serviceCanonicalName, String processName,
                                      IBinder binder) throws RemoteException {
        Log.d(TAG, "ServiceDispatcher-->registerStubService,serviceCanonicalName:" + serviceCanonicalName + ",pid:" + android.os.Process.myPid() + ",thread:" + Thread.currentThread().getName());
        if (binder != null) {
            binder.linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    Logger.d("ServiceDispatcher-->binderDied,serviceCanonicalName:" + serviceCanonicalName);
                    remoteBinderCache.remove(serviceCanonicalName);
                }
            }, 0);
            remoteBinderCache.put(serviceCanonicalName, new BinderBean(binder, processName));
            Log.d(TAG, "binder is not null");
        } else {
            Log.d(TAG, "binder is null");
        }
    }

    @Override
    public void removeBinderCache(String serviceCanonicalName) {
        remoteBinderCache.remove(serviceCanonicalName);
    }
}

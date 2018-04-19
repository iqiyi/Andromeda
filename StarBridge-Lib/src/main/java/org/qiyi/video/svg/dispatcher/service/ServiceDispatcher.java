package org.qiyi.video.svg.dispatcher.service;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.qiyi.video.svg.StarBridge;
import org.qiyi.video.svg.backup.EmergencyHandler;
import org.qiyi.video.svg.backup.IEmergencyHandler;
import org.qiyi.video.svg.bean.BinderBean;
import org.qiyi.video.svg.log.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangallen on 2018/1/8.
 */
public class ServiceDispatcher implements IServiceDispatcher {

    private static final String TAG = "StarBridge";

    private IEmergencyHandler emergencyHandler;

    public ServiceDispatcher() {
        emergencyHandler = new EmergencyHandler();
    }

    private Map<String, BinderBean> remoteBinderCache = new ConcurrentHashMap<>();

    @Override
    public BinderBean getTargetBinder(String serviceCanonicalName) throws RemoteException {
        Log.d(TAG, "ServiceDispatcher-->getTargetBinder,serivceName:" + serviceCanonicalName + ",pid:" + android.os.Process.myPid() + ",thread:" + Thread.currentThread().getName());
        BinderBean bean = remoteBinderCache.get(serviceCanonicalName);
        if (null == bean) {
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
                    BinderBean bean = remoteBinderCache.remove(serviceCanonicalName);
                    if (bean != null) {
                        emergencyHandler.handleBinderDied(StarBridge.getAppContext(), bean.getProcessName());
                    }
                }
            }, 0);
            remoteBinderCache.put(serviceCanonicalName, new BinderBean(binder, processName));
            Logger.d("ServiceDispatcher-->registerRemoteService(),binder is not null");
        } else {
            Log.d(TAG, "ServiceDispatcher-->registerRemoteService(),binder is null");
        }
    }

    @Override
    public void removeBinderCache(String serviceCanonicalName) {
        remoteBinderCache.remove(serviceCanonicalName);
    }
}

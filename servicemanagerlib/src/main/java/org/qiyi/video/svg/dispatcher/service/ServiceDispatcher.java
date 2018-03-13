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
//TODO 要将自己注册到主进程的ServiceManager中
//TODO 这个是不是叫ServiceDispatcher, ServiceRegistryCenter或者RemoteServiceCenter更合适呢？那样每个进程就可以有一个RemoteServiceManager，从架构上会更清晰
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

    //TODO 还要把binder与serviceName绑定在一起，才能找到对应的类调用asInterface()方法
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

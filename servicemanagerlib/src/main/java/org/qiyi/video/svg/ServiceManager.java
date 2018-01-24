package org.qiyi.video.svg;

import android.content.Context;
import android.os.IBinder;

import org.qiyi.video.svg.event.Event;
import org.qiyi.video.svg.event.EventListener;
import org.qiyi.video.svg.local.LocalServiceManager;
import org.qiyi.video.svg.remote.RemoteServiceManager;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by wangallen on 2018/1/8.
 */

//注意:这个ServiceManager可能会存在于很多进程中，不要以为只有主进程才有
public class ServiceManager implements IServiceManager {

    private static final String TAG = "ServiceManager";

    private static ServiceManager sInstance;

    private static AtomicBoolean initFlag = new AtomicBoolean(false);

    public static void init(Context context) {
        if (initFlag.get() || context == null) {
            return;
        }
        RemoteServiceManager.init(context.getApplicationContext());
        initFlag.set(true);
    }

    public static ServiceManager getInstance() {
        if (null == sInstance) {
            synchronized (ServiceManager.class) {
                if (null == sInstance) {
                    sInstance = new ServiceManager();
                }
            }
        }
        return sInstance;
    }

    private ServiceManager() {
    }

    @Override
    public void registerLocalService(Class serviceClass, Object serviceImpl) {
        LocalServiceManager.getInstance().registerService(serviceClass.getCanonicalName(), serviceImpl);
    }

    @Override
    public void registerLocalService(String serviceCanonicalName, Object serviceImpl) {
        LocalServiceManager.getInstance().registerService(serviceCanonicalName, serviceImpl);
    }

    @Override
    public void unregisterLocalService(Class serviceClass) {
        LocalServiceManager.getInstance().unregisterService(serviceClass.getCanonicalName());
    }

    @Override
    public void unregisterLocalService(String serivceCanonicalName) {
        LocalServiceManager.getInstance().unregisterService(serivceCanonicalName);
    }

    @Override
    public void registerRemoteService(Class serviceClass, IBinder stubBinder) {
        RemoteServiceManager.getInstance().registerStubService(serviceClass.getCanonicalName(), stubBinder);
    }

    @Override
    public void registerRemoteService(String serviceCanonicalName, IBinder stubBinder) {
        RemoteServiceManager.getInstance().registerStubService(serviceCanonicalName, stubBinder);
    }

    @Override
    public Object getLocalService(Class serviceClass) {
        return LocalServiceManager.getInstance().getLocalService(serviceClass.getCanonicalName());
    }

    @Override
    public IBinder getRemoteService(Class serviceClass) {
        return RemoteServiceManager.getInstance().getRemoteService(serviceClass.getCanonicalName());
    }

    @Override
    public Object getLocalService(String serviceCanonicalName) {

        return LocalServiceManager.getInstance().getLocalService(serviceCanonicalName);
    }

    @Override
    public IBinder getRemoteService(String serviceCanonicalName) {
        return RemoteServiceManager.getInstance().getRemoteService(serviceCanonicalName);
    }

    //TODO 如果同时对多个Event感兴趣呢？是不是就要注册多次?

    @Override
    public void subscribeEvent(String name, EventListener listener) {
        RemoteServiceManager.getInstance().subscribeEvent(name, listener);
    }

    @Override
    public void unsubscribeEvent(EventListener listener) {
        RemoteServiceManager.getInstance().unsubscribeEvent(listener);
    }

    @Override
    public void publishEvent(Event event) {

    }
}

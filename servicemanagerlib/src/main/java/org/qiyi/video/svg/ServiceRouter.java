package org.qiyi.video.svg;

import android.content.Context;
import android.os.IBinder;

import org.qiyi.video.svg.event.Event;
import org.qiyi.video.svg.event.EventListener;
import org.qiyi.video.svg.local.LocalServiceRouter;
import org.qiyi.video.svg.transfer.RemoteTransfer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by wangallen on 2018/1/8.
 */

//注意:这个ServiceManager可能会存在于很多进程中，不要以为只有主进程才有
public class ServiceRouter implements IServiceRouter {

    private static final String TAG = "ServiceRouter";

    private static ServiceRouter sInstance;

    private static AtomicBoolean initFlag = new AtomicBoolean(false);

    public static void init(Context context) {
        if (initFlag.get() || context == null) {
            return;
        }
        RemoteTransfer.init(context.getApplicationContext());
        initFlag.set(true);
    }

    public static ServiceRouter getInstance() {
        if (null == sInstance) {
            synchronized (ServiceRouter.class) {
                if (null == sInstance) {
                    sInstance = new ServiceRouter();
                }
            }
        }
        return sInstance;
    }

    private ServiceRouter() {
    }

    @Override
    public void registerLocalService(Class serviceClass, Object serviceImpl) {
        LocalServiceRouter.getInstance().registerService(serviceClass.getCanonicalName(), serviceImpl);
    }

    @Override
    public void registerLocalService(String serviceCanonicalName, Object serviceImpl) {
        LocalServiceRouter.getInstance().registerService(serviceCanonicalName, serviceImpl);
    }

    @Override
    public void unregisterLocalService(Class serviceClass) {
        LocalServiceRouter.getInstance().unregisterService(serviceClass.getCanonicalName());
    }

    @Override
    public void unregisterLocalService(String serivceCanonicalName) {
        LocalServiceRouter.getInstance().unregisterService(serivceCanonicalName);
    }

    @Override
    public void registerRemoteService(Class serviceClass, IBinder stubBinder) {
        RemoteTransfer.getInstance().registerStubService(serviceClass.getCanonicalName(), stubBinder);
    }

    @Override
    public void registerRemoteService(String serviceCanonicalName, IBinder stubBinder) {
        RemoteTransfer.getInstance().registerStubService(serviceCanonicalName, stubBinder);
    }

    @Override
    public Object getLocalService(Class serviceClass) {
        return LocalServiceRouter.getInstance().getLocalService(serviceClass.getCanonicalName());
    }

    @Override
    public IBinder getRemoteService(Class serviceClass) {
        return RemoteTransfer.getInstance().getRemoteService(serviceClass.getCanonicalName());
    }

    @Override
    public Object getLocalService(String serviceCanonicalName) {
        return LocalServiceRouter.getInstance().getLocalService(serviceCanonicalName);
    }

    @Override
    public IBinder getRemoteService(String serviceCanonicalName) {
        return RemoteTransfer.getInstance().getRemoteService(serviceCanonicalName);
    }

    //TODO 如果同时对多个Event感兴趣呢？是不是就要注册多次?

    @Override
    public void subscribe(String name, EventListener listener) {
        RemoteTransfer.getInstance().subscribeEvent(name, listener);
    }

    @Override
    public void unsubscribe(EventListener listener) {
        RemoteTransfer.getInstance().unsubscribeEvent(listener);
    }

    @Override
    public void publish(Event event) {
        RemoteTransfer.getInstance().publish(event);
    }
}

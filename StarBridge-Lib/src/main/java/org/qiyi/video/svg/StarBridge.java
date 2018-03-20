package org.qiyi.video.svg;

//import android.arch.lifecycle.LifecycleOwner;

import android.content.Context;
import android.os.IBinder;
import android.text.TextUtils;

import org.qiyi.video.svg.event.Event;
import org.qiyi.video.svg.event.EventListener;
import org.qiyi.video.svg.local.LocalServiceHub;
import org.qiyi.video.svg.transfer.RemoteTransfer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by wangallen on 2018/1/8.
 */
//TODO 可以考虑学习ArchComponents那样，把ServiceRouter的初始化放在ContentProvider中，而且设置multiprocess="true"的话，只需要一个ContentProvider. 这样就不需要用户来进行初始化了，可以使这个库更方便使用!
//注意:这个StarBridge可能会存在于很多进程中，不要以为只有主进程才有
//Step 1:实现全部接口对象在Application中注册
//Step 2:实现全部接口对象在用户指定位置处注册

public class StarBridge implements IStarBridge {

    private static final String TAG = "StarBridge";

    private static StarBridge sInstance;

    private static AtomicBoolean initFlag = new AtomicBoolean(false);

    public static void init(Context context) {
        if (initFlag.get() || context == null) {
            return;
        }
        RemoteTransfer.init(context.getApplicationContext());
        initFlag.set(true);
    }

    public static StarBridge getInstance() {
        if (null == sInstance) {
            synchronized (StarBridge.class) {
                if (null == sInstance) {
                    sInstance = new StarBridge();
                }
            }
        }
        return sInstance;
    }

    private StarBridge() {
    }

    @Override
    public void registerLocalService(Class serviceClass, Object serviceImpl) {
        if (null == serviceClass || null == serviceImpl) {
            return;
        }
        LocalServiceHub.getInstance().registerService(serviceClass.getCanonicalName(), serviceImpl);
    }

    //考虑到混淆，直接写类的完整路径名容易导致两边不一致，所以不推荐使用这种方式!
    @Deprecated
    @Override
    public void registerLocalService(String serviceCanonicalName, Object serviceImpl) {
        if (TextUtils.isEmpty(serviceCanonicalName) || null == serviceImpl) {
            return;
        }
        LocalServiceHub.getInstance().registerService(serviceCanonicalName, serviceImpl);
    }

    @Override
    public void unregisterLocalService(Class serviceClass) {
        if (null == serviceClass) {
            return;
        }
        LocalServiceHub.getInstance().unregisterService(serviceClass.getCanonicalName());
    }

    //考虑到混淆，不推荐使用这种方式!
    @Deprecated
    @Override
    public void unregisterLocalService(String serivceCanonicalName) {
        if (TextUtils.isEmpty(serivceCanonicalName)) {
            return;
        }
        LocalServiceHub.getInstance().unregisterService(serivceCanonicalName);
    }

    @Override
    public <T extends IBinder> void registerRemoteService(Class serviceClass, T stubBinder) {
        if (null == serviceClass || null == stubBinder) {
            return;
        }
        RemoteTransfer.getInstance().registerStubService(serviceClass.getCanonicalName(), stubBinder);
    }

    //考虑到混淆，不推荐使用这种方式
    @Deprecated
    @Override
    public <T extends IBinder> void registerRemoteService(String serviceCanonicalName, T stubBinder) {
        if (TextUtils.isEmpty(serviceCanonicalName) || null == stubBinder) {
            return;
        }
        RemoteTransfer.getInstance().registerStubService(serviceCanonicalName, stubBinder);
    }

    @Override
    public void unregisterRemoteService(Class serviceClass) {
        if (null == serviceClass) {
            return;
        }
        RemoteTransfer.getInstance().unregisterStubService(serviceClass.getCanonicalName());
    }

    @Deprecated
    @Override
    public void unregisterRemoteService(String serviceCanonicalName) {
        if (TextUtils.isEmpty(serviceCanonicalName)) {
            return;
        }
        RemoteTransfer.getInstance().unregisterStubService(serviceCanonicalName);
    }

    @Override
    public <T> T getLocalService(Class serviceClass) {
        if (null == serviceClass) {
            return null;
        }
        return (T) LocalServiceHub.getInstance().getLocalService(serviceClass.getCanonicalName());
    }

    @Override
    public IBinder getRemoteService(Class serviceClass) {
        if (null == serviceClass) {
            return null;
        }
        return RemoteTransfer.getInstance().getRemoteService(serviceClass.getCanonicalName());
    }

    @Deprecated
    @Override
    public <T> T getLocalService(String serviceCanonicalName) {
        if (TextUtils.isEmpty(serviceCanonicalName)) {
            return null;
        }
        return (T) LocalServiceHub.getInstance().getLocalService(serviceCanonicalName);
    }

    @Deprecated
    @Override
    public IBinder getRemoteService(String serviceCanonicalName) {
        if (TextUtils.isEmpty(serviceCanonicalName)) {
            return null;
        }
        return RemoteTransfer.getInstance().getRemoteService(serviceCanonicalName);
    }

    @Override
    public void unbind(Class<?> serviceClass) {
        if (null == serviceClass) {
            return;
        }
        RemoteTransfer.getInstance().unbind(serviceClass.getCanonicalName());
    }

    @Deprecated
    @Override
    public void unbind(String serviceCanonicalName) {
        if (TextUtils.isEmpty(serviceCanonicalName)) {
            return;
        }
        RemoteTransfer.getInstance().unbind(serviceCanonicalName);
    }

    @Override
    public void subscribe(String name, EventListener listener) {
        if (TextUtils.isEmpty(name)) {
            return;
        }
        RemoteTransfer.getInstance().subscribeEvent(name, listener);
    }

    @Override
    public void unsubscribe(EventListener listener) {
        if (null == listener) {
            return;
        }
        RemoteTransfer.getInstance().unsubscribeEvent(listener);
    }

    @Override
    public void publish(Event event) {
        if (null == event) {
            return;
        }
        RemoteTransfer.getInstance().publish(event);
    }
}

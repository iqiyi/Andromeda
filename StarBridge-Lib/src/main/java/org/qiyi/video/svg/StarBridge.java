package org.qiyi.video.svg;

//import android.arch.lifecycle.LifecycleOwner;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;

import org.qiyi.video.svg.event.Event;
import org.qiyi.video.svg.event.EventListener;
import org.qiyi.video.svg.local.LocalServiceHub;
import org.qiyi.video.svg.remote.IRemoteManager;
import org.qiyi.video.svg.remote.RemoteManagerRetriever;
import org.qiyi.video.svg.transfer.RemoteTransfer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by wangallen on 2018/1/8.
 */
//TODO 可以考虑学习ArchComponents那样，把ServiceRouter的初始化放在ContentProvider中，而且设置multiprocess="true"的话，只需要一个ContentProvider. 这样就不需要用户来进行初始化了，可以使这个库更方便使用!
//注意:这个StarBridge可能会存在于很多进程中，不要以为只有主进程才有
//Step 1:实现全部接口对象在Application中注册
//Step 2:实现全部接口对象在用户指定位置处注册

//TODO 为了使用更简便，要写成static方法，这样用户就不用写StarBridge.getInstance().xx()了,只要写StarBridge.xx()即可。
public class StarBridge {

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

    private RemoteManagerRetriever remoteManagerRetriever;

    private StarBridge() {
        this.remoteManagerRetriever = new RemoteManagerRetriever();
    }

    public static void registerLocalService(Class serviceClass, Object serviceImpl) {
        if (null == serviceClass || null == serviceImpl) {
            return;
        }
        LocalServiceHub.getInstance().registerService(serviceClass.getCanonicalName(), serviceImpl);
    }

    //考虑到混淆，直接写类的完整路径名容易导致两边不一致，所以不推荐使用这种方式!
    @Deprecated
    public static void registerLocalService(String serviceCanonicalName, Object serviceImpl) {
        if (TextUtils.isEmpty(serviceCanonicalName) || null == serviceImpl) {
            return;
        }
        LocalServiceHub.getInstance().registerService(serviceCanonicalName, serviceImpl);
    }

    public static <T> T getLocalService(Class serviceClass) {
        if (null == serviceClass) {
            return null;
        }
        return (T) LocalServiceHub.getInstance().getLocalService(serviceClass.getCanonicalName());
    }

    @Deprecated
    public static <T> T getLocalService(String serviceCanonicalName) {
        if (TextUtils.isEmpty(serviceCanonicalName)) {
            return null;
        }
        return (T) LocalServiceHub.getInstance().getLocalService(serviceCanonicalName);
    }

    public static void unregisterLocalService(Class serviceClass) {
        if (null == serviceClass) {
            return;
        }
        LocalServiceHub.getInstance().unregisterService(serviceClass.getCanonicalName());
    }

    //考虑到混淆，不推荐使用这种方式!
    @Deprecated
    public static void unregisterLocalService(String serivceCanonicalName) {
        if (TextUtils.isEmpty(serivceCanonicalName)) {
            return;
        }
        LocalServiceHub.getInstance().unregisterService(serivceCanonicalName);
    }

    public static <T extends IBinder> void registerRemoteService(Class serviceClass, T stubBinder) {
        if (null == serviceClass || null == stubBinder) {
            return;
        }
        RemoteTransfer.getInstance().registerStubService(serviceClass.getCanonicalName(), stubBinder);
    }

    //考虑到混淆，不推荐使用这种方式
    @Deprecated
    public static <T extends IBinder> void registerRemoteService(String serviceCanonicalName, T stubBinder) {
        if (TextUtils.isEmpty(serviceCanonicalName) || null == stubBinder) {
            return;
        }
        RemoteTransfer.getInstance().registerStubService(serviceCanonicalName, stubBinder);
    }

    public static void unregisterRemoteService(Class serviceClass) {
        if (null == serviceClass) {
            return;
        }
        RemoteTransfer.getInstance().unregisterStubService(serviceClass.getCanonicalName());
    }

    @Deprecated
    public static void unregisterRemoteService(String serviceCanonicalName) {
        if (TextUtils.isEmpty(serviceCanonicalName)) {
            return;
        }
        RemoteTransfer.getInstance().unregisterStubService(serviceCanonicalName);
    }


    public static IRemoteManager with(android.app.Fragment fragment) {
        return getRetriever().get(fragment.getActivity());
    }

    public static IRemoteManager with(Fragment fragment) {
        return getRetriever().get(fragment);
    }

    public static IRemoteManager with(Activity activity) {
        return getRetriever().get(activity);
    }

    public static IRemoteManager with(Context context) {
        return getRetriever().get(context);
    }

    public static IRemoteManager with(View view){
        return getRetriever().get(view);
    }

    private static RemoteManagerRetriever getRetriever() {
        //Preconditions.checkNotNull(context,"context cannote be null in getRetriever(Context)");
        return StarBridge.getInstance().getRemoteManagerRetriever();
    }


    //////////////////start of non-static methods////////////////////////////

    public RemoteManagerRetriever getRemoteManagerRetriever() {
        return remoteManagerRetriever;
    }


    ////////////////end of non-static methods/////////////////////////////

    /*
    @Override
    public IBinder getRemoteService(Class serviceClass) {
        if (null == serviceClass) {
            return null;
        }
        return RemoteTransfer.getInstance().getRemoteService(serviceClass.getCanonicalName());
    }

    @Deprecated
    public IBinder getRemoteService(String serviceCanonicalName) {
        if (TextUtils.isEmpty(serviceCanonicalName)) {
            return null;
        }
        return RemoteTransfer.getInstance().getRemoteService(serviceCanonicalName);
    }
    */


    //TODO 这两个是放在StarBridge中还是RemoteManager中还需要再讨论一下
    public static void unbind(Class<?> serviceClass) {
        if (null == serviceClass) {
            return;
        }
        RemoteTransfer.getInstance().unbind(serviceClass.getCanonicalName());
    }

    @Deprecated
    public static void unbind(String serviceCanonicalName) {
        if (TextUtils.isEmpty(serviceCanonicalName)) {
            return;
        }
        RemoteTransfer.getInstance().unbind(serviceCanonicalName);
    }

    //TODO 这个是否也需要与生命周期关联起来呢？然后比如在onDestroy()时自动unsubscribe()掉。感觉最好还是要!
    //TODO 但是这样的话有个问题，就是使用上不统一，因为subscribe()是放在RemoteManager中，而unsubscribe()和publish()方法却是放在StarBridge中
    //TODO 其实这个问题在注册远程服务和使用远程服务时也存在，所以是不是要换一种使用方法呢?
    public static void subscribe(String name, EventListener listener) {
        if (TextUtils.isEmpty(name)) {
            return;
        }
        RemoteTransfer.getInstance().subscribeEvent(name, listener);
    }

    public static void unsubscribe(EventListener listener) {
        if (null == listener) {
            return;
        }
        RemoteTransfer.getInstance().unsubscribeEvent(listener);
    }

    public static void publish(Event event) {
        if (null == event) {
            return;
        }
        RemoteTransfer.getInstance().publish(event);
    }
}

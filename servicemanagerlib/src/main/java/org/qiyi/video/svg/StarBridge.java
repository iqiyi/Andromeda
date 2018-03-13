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
//注意:这个ServiceManager可能会存在于很多进程中，不要以为只有主进程才有
//Step 1:实现全部接口对象在Application中注册
//Step 2:实现全部接口对象在用户指定位置处注册

/**
 * //TODO 初步构思
 * 1.利用注解@Local和@Remote分别标识本地服务和远程服务
 * 2.增加一个@XProvide注解(用来修饰static方法)，如果有这个注解，
 * 表示对象的生成不能直接new一个空的构造方法来完成，可能需要Context之类等其他的参数，
 * 反正就让业务开发自己提供对象就行了。如果某个接口实现没有@XProvide这个注解，就表示它可以通过new一个默认构造方法获取。
 * //TODO 但是现在有一个问题就是如果用户需要借助注入点的某些参数才能完成对象的初始化，特别是Local接口的实现，那这里就不好写了，而这种情况也是比较常见的吧!
 * <p>
 * 3.先利用注解解释器将相应的信息放到某个类的static信息中，然后利用gradle插件+javaassist在合适的位置插入代码
 * <p>
 * 4.如果javassit没有类似aspectj那种include和exclude的选项的话，就自己封装一个增强型的javassist
 */
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

    @Override
    public void unregisterLocalService(String serivceCanonicalName) {
        if (TextUtils.isEmpty(serivceCanonicalName)) {
            return;
        }
        LocalServiceHub.getInstance().unregisterService(serivceCanonicalName);
    }

    //TODO 这里最好也是让用户直接注册实现了接口的对象即可，然后我们从中取出IBinder
    @Override
    public <T extends IBinder> void registerRemoteService(Class serviceClass, T stubBinder) {
        if (null == serviceClass || null == stubBinder) {
            return;
        }
        RemoteTransfer.getInstance().registerStubService(serviceClass.getCanonicalName(), stubBinder);
    }

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

    @Override
    public <T> T getLocalService(String serviceCanonicalName) {
        if (TextUtils.isEmpty(serviceCanonicalName)) {
            return null;
        }
        return (T) LocalServiceHub.getInstance().getLocalService(serviceCanonicalName);
    }

    //TODO 考虑是否直接返回Proxy对象呢？采用反射的方式，不然使用起来太不方便了!
    @Override
    public IBinder getRemoteService(String serviceCanonicalName) {
        if (TextUtils.isEmpty(serviceCanonicalName)) {
            return null;
        }
        return RemoteTransfer.getInstance().getRemoteService(serviceCanonicalName);
    }

    @Override
    public void unbind(String serviceCanonicalName) {
        if (TextUtils.isEmpty(serviceCanonicalName)) {
            return;
        }
        RemoteTransfer.getInstance().unbind(serviceCanonicalName);
    }

    //TODO 如果同时对多个Event感兴趣呢？是不是就要注册多次?

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

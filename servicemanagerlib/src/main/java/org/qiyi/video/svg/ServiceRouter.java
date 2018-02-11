package org.qiyi.video.svg;

import android.arch.lifecycle.LifecycleOwner;
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
//TODO 可以考虑学习ArchComponents那样，把ServiceRouter的初始化放在ContentProvider中，而且设置multiprocess="true"的话，只需要一个ContentProvider. 这样就不需要用户来进行初始化了，可以使这个库更方便使用!
//注意:这个ServiceManager可能会存在于很多进程中，不要以为只有主进程才有
//Step 1:实现全部接口对象在Application中注册
//Step 2:实现全部接口对象在用户指定位置处注册

/** //TODO 初步构思
 * 1.利用注解@Local和@Remote分别标识本地服务和远程服务
 * 2.增加一个@XProvide注解(用来修饰static方法)，如果有这个注解，
 * 表示对象的生成不能直接new一个空的构造方法来完成，可能需要Context之类等其他的参数，
 * 反正就让业务开发自己提供对象就行了。如果某个接口实现没有@XProvide这个注解，就表示它可以通过new一个默认构造方法获取。
 * //TODO 但是现在有一个问题就是如果用户需要借助注入点的某些参数才能完成对象的初始化，特别是Local接口的实现，那这里就不好写了，而这种情况也是比较常见的吧!
 *
 * 3.先利用注解解释器将相应的信息放到某个类的static信息中，然后利用gradle插件+javaassist在合适的位置插入代码
 *
 *
 */
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
    //TODO 这里最好也是让用户直接注册实现了接口的对象即可，然后我们从中取出IBinder
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

    //TODO 考虑是否直接返回Proxy对象呢？采用反射的方式，不然使用起来太不方便了!
    @Override
    public IBinder getRemoteService(String serviceCanonicalName) {
        return RemoteTransfer.getInstance().getRemoteService(serviceCanonicalName);
    }

    @Override
    public IBinder getRemoteService(LifecycleOwner owner, Class serviceClass) {
        return RemoteTransfer.getInstance().getRemoteService(owner, serviceClass.getCanonicalName());
    }

    @Override
    public IBinder getRemoteService(LifecycleOwner owner, String serviceCanonicalName) {
        return RemoteTransfer.getInstance().getRemoteService(owner, serviceCanonicalName);
    }

    @Override
    public void unbind(String serviceCanonicalName) {
        RemoteTransfer.getInstance().unbind(serviceCanonicalName);
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

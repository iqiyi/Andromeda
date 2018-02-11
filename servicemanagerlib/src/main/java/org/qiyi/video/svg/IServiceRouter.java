package org.qiyi.video.svg;

import android.arch.lifecycle.LifecycleOwner;
import android.os.IBinder;

import org.qiyi.video.svg.event.Event;
import org.qiyi.video.svg.event.EventListener;

/**
 * Created by wangallen on 2018/1/8.
 */
//TODO 后期要考虑一个Module下分很多个interfaces的情况，即一对多。因为可能一个Module也很复杂，需要几个不同的业务分别实现各自的接口
public interface IServiceRouter {

    //TODO 这里是不是要将resiter改成add呢？不然容易跟下面的注册事件引起混淆!
    void registerLocalService(Class serviceClass, Object serviceImpl);

    void registerLocalService(String serviceCanonicalName, Object serviceImpl);

    void unregisterLocalService(Class serviceClass);

    void unregisterLocalService(String serviceName);

    void registerRemoteService(Class serviceClass, IBinder stubBinder);

    void registerRemoteService(String serviceCanonicalName, IBinder stubBinder);
    //TODO 不仅要支持懒加载，也要支持业务方主动注册!
    //void registerStubService(String serivceCanonicalName, Binder stubImpl);

    Object getLocalService(Class serviceClass);

    //只能用于同进程通信,所以支持的返回值和参数类型都不受限制
    Object getLocalService(String serivceCanonicalName);
    //<T> T getLocalService(String serivceCanonicalName);

    //TODO 那对于这种什么都不传递的，是不是只能调用ApplicationContext去bind了呢？
    IBinder getRemoteService(Class serviceClass);

    //有了LifecycleOwner之后就可以利用LifecycleOwner.getLifecycle()来获取LifecycleRegistry,然后调用LifecycleRegistry.addListener()即可增加监听器
    IBinder getRemoteService(LifecycleOwner owner, Class serviceClass);

    //TODO 是不是改成apiCanonicalName更不容易引起误解呢?
    //既可用于IPC,也可用于同一个进程通信,所以返回值和参数类型受AIDL的限制
    IBinder getRemoteService(String serviceCanonicalName);

    IBinder getRemoteService(LifecycleOwner owner, String serviceCanonicalName);

    //TODO 要传递什么参数呢？Activity还是什么? 还是说什么都不用传？最好是什么都不用传，就是bind时使用ApplicationContext, unbind时也使用ApplicationContext，这样就
    //TODO 其实最好就是有一个unbind()接口，调用一次就可以把当前类中用到过的所有服务都unbind掉
    void unbind(String canonicalName);

    void subscribe(String name, EventListener listener);

    void unsubscribe(EventListener listener);

    void publish(Event event);

}

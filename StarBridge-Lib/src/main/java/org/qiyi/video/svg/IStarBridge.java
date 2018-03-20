package org.qiyi.video.svg;

//import android.arch.lifecycle.LifecycleOwner;

import android.os.IBinder;

import org.qiyi.video.svg.event.Event;
import org.qiyi.video.svg.event.EventListener;

/**
 * Created by wangallen on 2018/1/8.
 */
public interface IStarBridge {

    void registerLocalService(Class serviceClass, Object serviceImpl);

    void registerLocalService(String serviceCanonicalName, Object serviceImpl);

    <T> T getLocalService(Class serviceClass);

    //只能用于同进程通信,所以支持的返回值和参数类型都不受限制
    <T> T getLocalService(String serivceCanonicalName);

    /**
     * 服务提供方才能调用
     *
     * @param serviceClass
     */
    void unregisterLocalService(Class serviceClass);

    void unregisterLocalService(String serviceName);

    <T extends IBinder> void registerRemoteService(Class serviceClass, T stubBinder);

    <T extends IBinder> void registerRemoteService(String serviceCanonicalName, T stubBinder);

    void unregisterRemoteService(Class serviceClass);

    void unregisterRemoteService(String serviceCanonicalName);

    IBinder getRemoteService(Class serviceClass);

    //有了LifecycleOwner之后就可以利用LifecycleOwner.getLifecycle()来获取LifecycleRegistry,然后调用LifecycleRegistry.addListener()即可增加监听器
    //IBinder getRemoteService(LifecycleOwner owner, Class serviceClass);

    //TODO 是不是改成apiCanonicalName更不容易引起误解呢?
    //既可用于IPC,也可用于同一个进程通信,所以返回值和参数类型受AIDL的限制
    IBinder getRemoteService(String serviceCanonicalName);

    //IBinder getRemoteService(LifecycleOwner owner, String serviceCanonicalName);

    void unbind(Class<?> serviceClass);

    void unbind(String canonicalName);

    void subscribe(String name, EventListener listener);

    void unsubscribe(EventListener listener);

    void publish(Event event);

}

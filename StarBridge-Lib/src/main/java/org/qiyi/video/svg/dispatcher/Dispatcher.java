package org.qiyi.video.svg.dispatcher;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import org.qiyi.video.svg.IDispatcher;
import org.qiyi.video.svg.bean.BinderBean;
import org.qiyi.video.svg.config.Constants;
import org.qiyi.video.svg.dispatcher.event.EventDispatcher;
import org.qiyi.video.svg.dispatcher.event.IEventDispatcher;
import org.qiyi.video.svg.dispatcher.service.IServiceDispatcher;
import org.qiyi.video.svg.dispatcher.service.ServiceDispatcher;
import org.qiyi.video.svg.event.Event;

/**
 * Created by wangallen on 2018/1/24.
 */

public class Dispatcher extends IDispatcher.Stub {

    public static Dispatcher sInstance;

    public static Dispatcher getInstance() {
        if (null == sInstance) {
            synchronized (Dispatcher.class) {
                if (null == sInstance) {
                    sInstance = new Dispatcher();
                }
            }
        }
        return sInstance;
    }

    private IServiceDispatcher serviceDispatcher;

    private IEventDispatcher eventDispatcher;

    private Dispatcher() {
        serviceDispatcher = new ServiceDispatcher();
        eventDispatcher = new EventDispatcher();
    }

    //给同进程的DispatcherService调用的,因而没必要写进接口里
    public void registerRemoteTransfer(int pid, IBinder transferBinder) {
        if (pid < 0) {
            return;
        }
        eventDispatcher.registerRemoteTransfer(pid, transferBinder);
    }

    @Override
    public BinderBean getTargetBinder(String serviceCanonicalName) throws RemoteException {
        return serviceDispatcher.getTargetBinder(serviceCanonicalName);
    }

    @Override
    public IBinder fetchTargetBinder(String uri) throws RemoteException {
        return null;
    }

    @Override
    public void registerRemoteService(String serviceCanonicalName, String processName, IBinder binder) throws RemoteException {
        serviceDispatcher.registerRemoteService(serviceCanonicalName, processName, binder);
    }

    @Override
    public void unregisterRemoteService(String serviceCanonicalName) throws RemoteException {
        serviceDispatcher.removeBinderCache(serviceCanonicalName);
        //然后让EventDispatcher通知各个进程清除缓存
        eventDispatcher.unregisterRemoteService(serviceCanonicalName);
    }

    @Override
    public void publish(Event event) throws RemoteException {
        eventDispatcher.publish(event);
    }

}

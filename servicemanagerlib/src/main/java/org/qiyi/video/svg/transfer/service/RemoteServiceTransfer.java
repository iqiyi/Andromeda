package org.qiyi.video.svg.transfer.service;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import org.qiyi.video.svg.BinderWrapper;
import org.qiyi.video.svg.IDispatcher;
import org.qiyi.video.svg.IRemoteTransfer;
import org.qiyi.video.svg.config.Constants;
import org.qiyi.video.svg.dispatcher.DispatcherService;
import org.qiyi.video.svg.log.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangallen on 2018/1/25.
 */

public class RemoteServiceTransfer {

    /**
     * 本地的Binder,需要给其他进程使用的,key为inteface的完整名称
     */
    //TODO 这个还是改成让他们注册IBinder,这样子也能保持注册和取出来的是一样的东西
    private Map<String, IBinder> stubBinderCache = new ConcurrentHashMap<>();

    private Map<String, IBinder> remoteBinderCache = new ConcurrentHashMap<>();

    public void registerStubService(String serviceCanonicalName, IBinder stubBinder,
                                    Context context, IDispatcher dispatcherProxy, IRemoteTransfer.Stub stub) {
        stubBinderCache.put(serviceCanonicalName, stubBinder);
        if (dispatcherProxy == null) {
            BinderWrapper wrapper = new BinderWrapper(stub.asBinder());
            Intent intent = new Intent(context, DispatcherService.class);
            intent.setAction(Constants.DISPATCH_SERVICE_ACTION);
            intent.putExtra(Constants.KEY_REMOTE_TRANSFER_WRAPPER, wrapper);
            intent.putExtra(Constants.KEY_BUSINESS_BINDER_WRAPPER, new BinderWrapper(stubBinder));
            intent.putExtra(Constants.KEY_SERVICE_NAME, serviceCanonicalName);
            intent.putExtra(Constants.KEY_PID, android.os.Process.myPid());
            context.startService(intent);
        } else {
            try {
                dispatcherProxy.registerRemoteService(serviceCanonicalName, stubBinder);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    public IBinder getIBinderFromCache(String serviceCanonicalName) {
        if (stubBinderCache.get(serviceCanonicalName) != null) {
            return stubBinderCache.get(serviceCanonicalName);
        }

        if (remoteBinderCache.get(serviceCanonicalName) != null) {
            return remoteBinderCache.get(serviceCanonicalName);
        }
        return null;
    }

    public IBinder getAndSaveIBinder(String serviceName,IDispatcher dispatcherProxy) {
        try {
            IBinder binder = dispatcherProxy.getTargetBinder(serviceName);
            Logger.d("get IBinder from ServiceDispatcher");
            remoteBinderCache.put(serviceName, binder);
            return binder;
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }

        return null;
    }

}

package org.qiyi.video.svg.transfer.service;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import org.qiyi.video.svg.BinderWrapper;
import org.qiyi.video.svg.IDispatcher;
import org.qiyi.video.svg.IRemoteTransfer;
import org.qiyi.video.svg.bean.BinderBean;
import org.qiyi.video.svg.config.Constants;
import org.qiyi.video.svg.dispatcher.DispatcherService;
import org.qiyi.video.svg.log.Logger;
import org.qiyi.video.svg.utils.ProcessUtils;
import org.qiyi.video.svg.utils.ServiceUtils;

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

    private Map<String, BinderBean> remoteBinderCache = new ConcurrentHashMap<>();

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
            intent.putExtra(Constants.KEY_PROCESS_NAME, ProcessUtils.getProcessName(context));
            ServiceUtils.startServiceSafely(context, intent);
        } else {
            try {
                dispatcherProxy.registerRemoteService(serviceCanonicalName,
                        ProcessUtils.getProcessName(context), stubBinder);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    public BinderBean getIBinderFromCache(Context context, String serviceCanonicalName) {
        //TODO 如果是自己进程或者主进程，就不要进行bind操作了
        if (stubBinderCache.get(serviceCanonicalName) != null) {
            return new BinderBean(stubBinderCache.get(serviceCanonicalName),
                    ProcessUtils.getProcessName(context));
        }

        if (remoteBinderCache.get(serviceCanonicalName) != null) {
            return remoteBinderCache.get(serviceCanonicalName);
        }
        return null;
    }

    public BinderBean getAndSaveIBinder(final String serviceName, IDispatcher dispatcherProxy) {
        try {
            BinderBean binderBean = dispatcherProxy.getTargetBinder(serviceName);
            //TODO 这里要处理linkToDeath的问题!
            try {
                binderBean.getBinder().linkToDeath(new IBinder.DeathRecipient() {
                    @Override
                    public void binderDied() {
                        remoteBinderCache.remove(serviceName);
                    }
                }, 0);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
            Logger.d("get IBinder from ServiceDispatcher");
            remoteBinderCache.put(serviceName, binderBean);
            return binderBean;
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }

        return null;
    }

}

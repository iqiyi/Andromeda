/*
 * Copyright (c) 2018-present, iQIYI, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *        1. Redistributions of source code must retain the above copyright notice,
 *        this list of conditions and the following disclaimer.
 *
 *        2. Redistributions in binary form must reproduce the above copyright notice,
 *        this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *        3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived
 *        from this software without specific prior written permission.
 *
 *        THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 *        INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *        IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 *        OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *        OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *        OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *        EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
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
    private Map<String, IBinder> stubBinderCache = new ConcurrentHashMap<>();

    private Map<String, BinderBean> remoteBinderCache = new ConcurrentHashMap<>();

    public void registerStubServiceLocked(String serviceCanonicalName, IBinder stubBinder,
                                          Context context, IDispatcher dispatcherProxy, IRemoteTransfer.Stub stub) {
        stubBinderCache.put(serviceCanonicalName, stubBinder);
        if (dispatcherProxy == null) {
            BinderWrapper wrapper = new BinderWrapper(stub.asBinder());
            Intent intent = new Intent(context, DispatcherService.class);
            intent.setAction(Constants.DISPATCH_REGISTER_SERVICE_ACTION);
            intent.putExtra(Constants.KEY_REMOTE_TRANSFER_WRAPPER, wrapper);
            intent.putExtra(Constants.KEY_BUSINESS_BINDER_WRAPPER, new BinderWrapper(stubBinder));
            intent.putExtra(Constants.KEY_SERVICE_NAME, serviceCanonicalName);
            setProcessInfo(intent, context);
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

    private void setProcessInfo(Intent intent, Context context) {
        intent.putExtra(Constants.KEY_PID, android.os.Process.myPid());
        intent.putExtra(Constants.KEY_PROCESS_NAME, ProcessUtils.getProcessName(context));
    }

    /**
     * 思考:其实是不是不用这么麻烦，直接利用事件通知机制进行通知就可以了吧？
     * 可以是可以，但是逻辑上就不那么清晰了，而且要写很多ugly的if语句，可读性和可维护性也差了。
     *
     * @param serviceCanonicalName
     * @param context
     * @param dispatcherProxy
     */
    public void unregisterStubServiceLocked(String serviceCanonicalName, Context context, IDispatcher dispatcherProxy) {
        //第一步，清除本地的缓存
        clearStubBinderCache(serviceCanonicalName);
        //第二步，通知Dispatcher,然后让Dispatcher通知各进程
        if (null == dispatcherProxy) {
            Intent intent = new Intent(context, DispatcherService.class);
            intent.setAction(Constants.DISPATCH_UNREGISTER_SERVICE_ACTION);
            intent.putExtra(Constants.KEY_SERVICE_NAME, serviceCanonicalName);
            ServiceUtils.startServiceSafely(context, intent);
        } else {
            try {
                dispatcherProxy.unregisterRemoteService(serviceCanonicalName);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    public BinderBean getIBinderFromCache(Context context, String serviceCanonicalName) {
        //如果是自己进程或者主进程，就不要进行bind操作了
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
            if (null == binderBean) {
                return null;
            }
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

    /**
     * 清除本地的IBinder缓存
     *
     * @param serviceName
     */
    public void clearStubBinderCache(String serviceName) {
        stubBinderCache.remove(serviceName);
    }

    public void clearRemoteBinderCacheLocked(String serviceName) {
        remoteBinderCache.remove(serviceName);
    }

}

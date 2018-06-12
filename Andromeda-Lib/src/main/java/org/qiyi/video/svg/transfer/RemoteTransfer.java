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
package org.qiyi.video.svg.transfer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;

import org.qiyi.video.svg.BinderWrapper;
import org.qiyi.video.svg.IDispatcher;
import org.qiyi.video.svg.IRemoteTransfer;
import org.qiyi.video.svg.bean.BinderBean;
import org.qiyi.video.svg.config.Constants;
import org.qiyi.video.svg.cursor.DispatcherCursor;
import org.qiyi.video.svg.dispatcher.DispatcherProvider;
import org.qiyi.video.svg.dispatcher.DispatcherService;
import org.qiyi.video.svg.event.Event;
import org.qiyi.video.svg.event.EventListener;
import org.qiyi.video.svg.log.Logger;
import org.qiyi.video.svg.transfer.event.EventTransfer;
import org.qiyi.video.svg.transfer.event.IEventTransfer;
import org.qiyi.video.svg.transfer.service.IRemoteServiceTransfer;
import org.qiyi.video.svg.transfer.service.RemoteServiceTransfer;
import org.qiyi.video.svg.utils.IOUtils;
import org.qiyi.video.svg.utils.ServiceUtils;

/**
 * Created by wangallen on 2018/1/9.
 */
public class RemoteTransfer extends IRemoteTransfer.Stub implements IRemoteServiceTransfer, IEventTransfer {

    public static final int MAX_WAIT_TIME = 600;

    private static RemoteTransfer sInstance;

    //TODO 这样做有个弊端，就是没做到懒加载
    public static void init(Context context) {
        getInstance().setContext(context);

        getInstance().sendRegisterInfo();
    }

    public static RemoteTransfer getInstance() {
        if (null == sInstance) {
            synchronized (RemoteTransfer.class) {
                if (null == sInstance) {
                    sInstance = new RemoteTransfer();
                }
            }
        }
        return sInstance;
    }

    private Context context;

    private IDispatcher dispatcherProxy;

    private RemoteServiceTransfer serviceTransfer;
    private EventTransfer eventTransfer;

    private RemoteTransfer() {
        serviceTransfer = new RemoteServiceTransfer();
        eventTransfer = new EventTransfer();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    //让ServiceDispatcher反向注册到当前进程
    private synchronized void sendRegisterInfo() {
        if (dispatcherProxy == null) {
            //后面考虑还是采用"has-a"的方式会更好
            BinderWrapper wrapper = new BinderWrapper(this.asBinder());
            Intent intent = new Intent(context, DispatcherService.class);
            intent.setAction(Constants.DISPATCH_REGISTER_SERVICE_ACTION);
            intent.putExtra(Constants.KEY_REMOTE_TRANSFER_WRAPPER, wrapper);
            intent.putExtra(Constants.KEY_PID, android.os.Process.myPid());
            ServiceUtils.startServiceSafely(context, intent);
        }
    }

    private void initDispatchProxyLocked() {
        if (null == dispatcherProxy) {
            IBinder dispatcherBinder = getIBinderFromProvider();
            if (null != dispatcherBinder) {
                Logger.d("the binder from provider is not null");
                dispatcherProxy = IDispatcher.Stub.asInterface(dispatcherBinder);
                registerCurrentTransfer();
            }
        }
        if (null == dispatcherProxy) {
            sendRegisterInfo();
            try {
                wait(MAX_WAIT_TIME);
            } catch (InterruptedException ex) {
                Logger.e("Attention! Wait out of time!");
                ex.printStackTrace();
            }
        }
    }

    @Override
    public synchronized BinderBean getRemoteServiceBean(String serviceCanonicalName) {
        Logger.d("RemoteTransfer-->getRemoteServiceBean,pid=" + android.os.Process.myPid() + ",thread:" + Thread.currentThread().getName());
        BinderBean cacheBinderBean = serviceTransfer.getIBinderFromCache(context, serviceCanonicalName);
        if (cacheBinderBean != null) {
            return cacheBinderBean;
        }
        initDispatchProxyLocked();
        if (serviceTransfer == null || dispatcherProxy == null) {
            return null;
        }
        return serviceTransfer.getAndSaveIBinder(serviceCanonicalName, dispatcherProxy);
    }

    private void registerCurrentTransfer() {
        try {
            dispatcherProxy.registerRemoteTransfer(android.os.Process.myPid(), this.asBinder());
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    private Uri getDispatcherProviderUri() {
        return Uri.parse("content://" + context.getPackageName() + "." + DispatcherProvider.URI_SUFFIX + "/main");
    }

    private IBinder getIBinderFromProvider() {
        Logger.d("RemoteTransfer-->getIBinderFromProvider()");
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(getDispatcherProviderUri(), DispatcherProvider.PROJECTION_MAIN,
                    null, null, null);
            if (cursor == null) {
                return null;
            }
            return DispatcherCursor.stripBinder(cursor);
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    @Override
    public synchronized void registerStubService(String serviceCanonicalName, IBinder stubBinder) {
        initDispatchProxyLocked();
        serviceTransfer.registerStubServiceLocked(serviceCanonicalName, stubBinder, context, dispatcherProxy, this);
    }

    /**
     * 要注销本进程的某个服务,注意它与unregisterRemoteService()的区别!
     * 这个方法表示要注销本进程的某个服务
     *
     * @param serviceCanonicalName
     */
    @Override
    public synchronized void unregisterStubService(String serviceCanonicalName) {
        initDispatchProxyLocked();
        serviceTransfer.unregisterStubServiceLocked(serviceCanonicalName, context, dispatcherProxy);
    }

    ///////////////start of event/////////////////////

    @Override
    public synchronized void subscribeEvent(String name, EventListener listener) {
        eventTransfer.subscribeEventLocked(name, listener);
    }

    @Override
    public synchronized void unsubscribeEvent(EventListener listener) {
        eventTransfer.unsubscribeEventLocked(listener);
    }

    @Override
    public synchronized void publish(Event event) {
        initDispatchProxyLocked();
        eventTransfer.publishLocked(event, dispatcherProxy, this, context);
    }

    ////////////////end of event///////////////////////////

    @Override
    public synchronized void registerDispatcher(IBinder dispatcherBinder) throws RemoteException {
        Logger.d("RemoteTransfer-->registerDispatcher");
        //一般从发出注册信息到这里回调就6ms左右，所以绝大部分时候走的都是这个逻辑。
        dispatcherBinder.linkToDeath(new IBinder.DeathRecipient() {
            @Override
            public void binderDied() {
                Logger.d("RemoteTransfer-->dispatcherBinder binderDied");
                resetDispatcherProxy();
            }
        }, 0);
        dispatcherProxy = IDispatcher.Stub.asInterface(dispatcherBinder);
        notifyAll();
    }

    private synchronized void resetDispatcherProxy() {
        dispatcherProxy = null;
    }

    /**
     * 接收到来自Dispatcher的通知，如果本地有相应的IBinder,就要清除
     *
     * @param serviceCanonicalName
     * @throws RemoteException
     */
    @Override
    public synchronized void unregisterRemoteService(String serviceCanonicalName) throws RemoteException {
        Logger.d("RemoteTransfer-->unregisterRemoteServiceLocked,pid:" + android.os.Process.myPid() + ",serviceName:" + serviceCanonicalName);
        serviceTransfer.clearRemoteBinderCacheLocked(serviceCanonicalName);
    }

    @Override
    public synchronized void notify(Event event) throws RemoteException {
        eventTransfer.notifyLocked(event);
    }

}

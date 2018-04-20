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
import android.os.IBinder;
import android.os.RemoteException;

import org.qiyi.video.svg.BinderWrapper;
import org.qiyi.video.svg.IDispatcher;
import org.qiyi.video.svg.IRemoteTransfer;
import org.qiyi.video.svg.bean.BinderBean;
import org.qiyi.video.svg.config.Constants;
import org.qiyi.video.svg.cursor.DispatcherCursor;
import org.qiyi.video.svg.dispatcher.Dispatcher;
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
import org.qiyi.video.svg.utils.ProcessUtils;
import org.qiyi.video.svg.utils.ServiceUtils;

/**
 * Created by wangallen on 2018/1/9.
 */
public class RemoteTransfer extends IRemoteTransfer.Stub implements IRemoteServiceTransfer, IEventTransfer {

    private static RemoteTransfer sInstance;

    //TODO 这样做有个弊端，就是没做到懒加载
    public static void init(Context context) {
        getInstance().setContext(context);

        //如果是主进程就可以直接调用ServiceDispatcher
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

    private final Object lock = new Object();

    private RemoteServiceTransfer serviceTransfer;
    private EventTransfer eventTransfer;

    private RemoteTransfer() {
        serviceTransfer = new RemoteServiceTransfer();
        eventTransfer = new EventTransfer();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    //让ServiceDispatcher注册到当前进程
    public void sendRegisterInfo() {

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

    @Override
    public BinderBean getRemoteServiceBean(String serviceCanonicalName) {
        Logger.d("RemoteTransfer-->getRemoteServiceBean,pid=" + android.os.Process.myPid() + ",thread:" + Thread.currentThread().getName());
        return getIBinder(serviceCanonicalName);
    }

    private BinderBean getIBinder(String serviceName) {
        Logger.d("RemoteTransfer-->getIBinder()");
        // 首先检查是否就在本地!这非常重要，否则有可能导致死锁!
        BinderBean cacheBinderBean = serviceTransfer.getIBinderFromCache(context, serviceName);
        if (cacheBinderBean != null) {
            return cacheBinderBean;
        }
        Logger.d("RemoteTransfer-->getIBinder(),start to wait");

        synchronized (lock) {

            if (null == dispatcherProxy) {
                IBinder dispatcherBinder = getIBinderFromProvider();
                if (null != dispatcherBinder) {
                    dispatcherProxy = IDispatcher.Stub.asInterface(dispatcherBinder);
                    registerCurrentTransfer();
                }
            }

            //停靠等待的这种情况是可以不用注册的，因为说明sendRegisterInfo()成功了
            if (null == dispatcherProxy) {
                sendRegisterInfo();
                try {
                    lock.wait(3000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        Logger.d("RemoteTransfer-->getIBinder(),end of wait");
        if (serviceTransfer == null || dispatcherProxy == null) {
            return null;
        }
        return serviceTransfer.getAndSaveIBinder(serviceName, dispatcherProxy);
    }

    private void registerCurrentTransfer() {
        try {
            dispatcherProxy.registerRemoteTransfer(android.os.Process.myPid(), this.asBinder());
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    private IBinder getIBinderFromProvider() {
        Logger.d("RemoteTransfer-->getIBinderFromProvider()");
        Cursor cursor = null;
        try {

            cursor = context.getContentResolver().query(DispatcherProvider.URI, DispatcherProvider.PROJECTION_MAIN,
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
        serviceTransfer.registerStubService(serviceCanonicalName, stubBinder, context, dispatcherProxy, this);
    }

    /**
     * 要注销本进程的某个服务,注意它与unregisterRemoteService()的区别!
     * 这个方法表示要注销本进程的某个服务
     *
     * @param serviceCanonicalName
     */
    @Override
    public synchronized void unregisterStubService(String serviceCanonicalName) {
        serviceTransfer.unregisterStubService(serviceCanonicalName, context, dispatcherProxy);
    }

    ///////////////start of event/////////////////////

    @Override
    public synchronized void subscribeEvent(String name, EventListener listener) {
        eventTransfer.subscribeEvent(name, listener);
    }

    @Override
    public synchronized void unsubscribeEvent(EventListener listener) {
        eventTransfer.unsubscribeEvent(listener);
    }

    @Override
    public synchronized void publish(Event event) {
        eventTransfer.publish(event, dispatcherProxy, this, context);
    }

    ////////////////end of event///////////////////////////

    @Override
    public void registerDispatcher(IBinder dispatcherBinder) throws RemoteException {
        Logger.d("RemoteTransfer-->registerDispatcher");
        //一般从发出注册信息到这里回调就6ms左右，所以绝大部分时候走的都是这个逻辑。
        dispatcherBinder.linkToDeath(new IBinder.DeathRecipient() {
            @Override
            public void binderDied() {
                Logger.d("RemoteTransfer-->dispatcherBinder binderDied");
                dispatcherProxy = null;
            }
        }, 0);
        //这里实现IServiceRegister仅仅是为了给RemoteServiceManager提供注册到当前进程的机会
        dispatcherProxy = IDispatcher.Stub.asInterface(dispatcherBinder);
        synchronized (lock) {
            //由于只有一个地方使用到lock,所以这里使用notify()和notifyAll()的效果是一样的
            lock.notifyAll();
        }
    }

    /**
     * 接收到来自Dispatcher的通知，如果本地有相应的IBinder,就要清除
     *
     * @param serviceCanonicalName
     * @throws RemoteException
     */
    @Override
    public void unregisterRemoteService(String serviceCanonicalName) throws RemoteException {
        Logger.d("RemoteTransfer-->unregisterRemoteService,pid:" + android.os.Process.myPid() + ",serviceName:" + serviceCanonicalName);
        serviceTransfer.clearRemoteBinderCache(serviceCanonicalName);
    }

    @Override
    public void notify(Event event) throws RemoteException {
        eventTransfer.notify(event);
    }

}

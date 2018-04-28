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
package org.qiyi.video.svg.dispatcher;

import android.os.IBinder;
import android.os.RemoteException;

import org.qiyi.video.svg.IDispatcher;
import org.qiyi.video.svg.bean.BinderBean;
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

    //给同进程的DispatcherService调用的和远程调用
    @Override
    public synchronized void registerRemoteTransfer(int pid, IBinder transferBinder) {
        if (pid < 0) {
            return;
        }
        eventDispatcher.registerRemoteTransferLocked(pid, transferBinder);
    }


    @Override
    public synchronized BinderBean getTargetBinder(String serviceCanonicalName) throws RemoteException {
        return serviceDispatcher.getTargetBinderLocked(serviceCanonicalName);
    }

    @Override
    public synchronized IBinder fetchTargetBinder(String uri) throws RemoteException {
        //作为保留接口，后面可能会用到
        return null;
    }

    @Override
    public synchronized void registerRemoteService(String serviceCanonicalName, String processName, IBinder binder) throws RemoteException {
        serviceDispatcher.registerRemoteServiceLocked(serviceCanonicalName, processName, binder);
    }

    @Override
    public synchronized void unregisterRemoteService(String serviceCanonicalName) throws RemoteException {
        serviceDispatcher.removeBinderCacheLocked(serviceCanonicalName);
        //然后让EventDispatcher通知各个进程清除缓存
        eventDispatcher.unregisterRemoteServiceLocked(serviceCanonicalName);
    }

    @Override
    public synchronized void publish(Event event) throws RemoteException {
        eventDispatcher.publishLocked(event);
    }

}

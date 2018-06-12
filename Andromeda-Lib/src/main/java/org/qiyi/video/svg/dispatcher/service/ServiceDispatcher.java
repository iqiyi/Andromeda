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
package org.qiyi.video.svg.dispatcher.service;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.qiyi.video.svg.Andromeda;
import org.qiyi.video.svg.backup.EmergencyHandler;
import org.qiyi.video.svg.backup.IEmergencyHandler;
import org.qiyi.video.svg.bean.BinderBean;
import org.qiyi.video.svg.log.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangallen on 2018/1/8.
 */
public class ServiceDispatcher implements IServiceDispatcher {

    private static final String TAG = "Andromeda";

    private IEmergencyHandler emergencyHandler;

    public ServiceDispatcher() {
        emergencyHandler = new EmergencyHandler();
    }

    private Map<String, BinderBean> remoteBinderCache = new ConcurrentHashMap<>();

    @Override
    public BinderBean getTargetBinderLocked(String serviceCanonicalName) throws RemoteException {
        Log.d(TAG, "ServiceDispatcher-->getTargetBinderLocked,serivceName:" + serviceCanonicalName + ",pid:" + android.os.Process.myPid() + ",thread:" + Thread.currentThread().getName());
        BinderBean bean = remoteBinderCache.get(serviceCanonicalName);
        if (null == bean) {
            return null;
        } else {
            return bean;
        }
    }

    @Override
    public void registerRemoteServiceLocked(final String serviceCanonicalName, String processName,
                                            IBinder binder) throws RemoteException {
        Log.d(TAG, "ServiceDispatcher-->registerRemoteServiceLocked,serviceCanonicalName:" + serviceCanonicalName + ",pid:" + android.os.Process.myPid() + ",thread:" + Thread.currentThread().getName());
        if (binder != null) {
            binder.linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    Logger.d("ServiceDispatcher-->binderDied,serviceCanonicalName:" + serviceCanonicalName);
                    BinderBean bean = remoteBinderCache.remove(serviceCanonicalName);
                    //实际上这里是还没实现线程同步，但是并不会影响执行结果，所以其实下面这句就没有同步的必要。
                    if (bean != null) {
                        emergencyHandler.handleBinderDied(Andromeda.getAppContext(), bean.getProcessName());
                    }
                }
            }, 0);
            remoteBinderCache.put(serviceCanonicalName, new BinderBean(binder, processName));
            Logger.d("ServiceDispatcher-->registerRemoteServiceLocked(),binder is not null");
        } else {
            Log.d(TAG, "ServiceDispatcher-->registerRemoteServiceLocked(),binder is null");
        }
    }

    @Override
    public void removeBinderCacheLocked(String serviceCanonicalName) {
        remoteBinderCache.remove(serviceCanonicalName);
    }
}

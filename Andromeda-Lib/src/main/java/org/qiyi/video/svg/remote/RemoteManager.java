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
package org.qiyi.video.svg.remote;

import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;

import org.qiyi.video.svg.bean.BinderBean;
import org.qiyi.video.svg.life.Lifecycle;
import org.qiyi.video.svg.life.LifecycleListener;
import org.qiyi.video.svg.log.Logger;
import org.qiyi.video.svg.transfer.RemoteTransfer;
import org.qiyi.video.svg.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by wangallen on 2018/3/26.
 */

public class RemoteManager implements IRemoteManager, LifecycleListener {

    private Lifecycle lifecycle;

    private Handler handler = new Handler(Looper.getMainLooper());

    private Context appContext;

    private List<String> commuStubServiceNames = new ArrayList<>();

    public RemoteManager(Context context, final Lifecycle lifecycle) {
        this.appContext = context;
        this.lifecycle = lifecycle;

        if (Utils.isOnBackgroundThread()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    lifecycle.addListener(RemoteManager.this);
                }
            });
        } else {
            lifecycle.addListener(this);
        }

    }

    @Override
    public IBinder getRemoteService(Class<?> serviceClass) {
        if (null == serviceClass) {
            return null;
        }
        return getRemoteService(serviceClass.getCanonicalName());
    }

    @Override
    public synchronized IBinder getRemoteService(String serviceCanonicalName) {
        Logger.d(this.toString() + "-->getRemoteService,serviceName:" + serviceCanonicalName);
        if (TextUtils.isEmpty(serviceCanonicalName)) {
            return null;
        }
        BinderBean binderBean = RemoteTransfer.getInstance().getRemoteServiceBean(serviceCanonicalName);
        if (binderBean == null) {
            Logger.e("Found no binder for "+serviceCanonicalName+"! Please check you have register implementation for it or proguard reasons!");
            return null;
        }
        String commuStubServiceName = ConnectionManager.getInstance().bindAction(appContext, binderBean.getProcessName());
        commuStubServiceNames.add(commuStubServiceName);
        return binderBean.getBinder();
    }

    @Override
    public void onStart() {
        Logger.d(this.toString() + "-->onStart()");
    }

    @Override
    public void onStop() {
        Logger.d(this.toString() + "-->onStop()");
    }

    @Override
    public void onDestroy() {
        Logger.d(this.toString() + "-->onDestroy()");
        ConnectionManager.getInstance().unbindAction(appContext, commuStubServiceNames);
    }
}

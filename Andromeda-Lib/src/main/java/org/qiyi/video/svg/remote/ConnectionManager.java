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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.qiyi.video.svg.bean.ConnectionBean;
import org.qiyi.video.svg.log.Logger;
import org.qiyi.video.svg.utils.StubServiceMatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by wangallen on 2018/3/29.
 */

public class ConnectionManager {

    private static ConnectionManager instance;

    public static ConnectionManager getInstance() {
        if (null == instance) {
            synchronized (ConnectionManager.class) {
                if (null == instance) {
                    instance = new ConnectionManager();
                }
            }
        }
        return instance;
    }

    //key是对方进程的占坑Service名称
    private Map<String, ConnectionBean> connectionCache = new HashMap<>();

    private ConnectionManager() {
    }

    private String getCommuStubServiceName(Intent intent) {
        if (intent.getComponent() == null) {
            return null;
        }
        return intent.getComponent().getClassName();
    }

    //这里不能按照serviceCanonicalName来区分，而是要按照target service来划分，如果targetService一样，那就没必要再绑定
    public synchronized String bindAction(Context context, String serverProcessName) {
        Logger.d("ConnectionManager-->bindAction,serverProcessName:" + serverProcessName);
        Intent intent = StubServiceMatcher.matchIntent(context, serverProcessName);
        if (null == intent) {
            Logger.d("match intent is null");
            return null;
        }

        String commuStubServiceName = getCommuStubServiceName(intent);
        ConnectionBean bean = connectionCache.get(commuStubServiceName);
        if (null == bean) {
            ServiceConnection connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Logger.d("onServiceConnected,name:" + name.getShortClassName());
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Logger.d("onServiceDisconnected,name:" + name.getShortClassName());
                }
            };
            bean = new ConnectionBean(connection);
            connectionCache.put(commuStubServiceName, bean);
            Logger.d("really start to bind");
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
        } else {
            bean.increaseRef();
        }
        return commuStubServiceName;
    }

    public synchronized void unbindAction(Context context, Set<String> commuStubServiceNames) {
        Logger.d("ConnectionManager-->unbindAction");
        for (String stubServiceName : commuStubServiceNames) {
            Logger.d("unbindAction, stubServiceName:" + stubServiceName);
            ConnectionBean bean = connectionCache.get(stubServiceName);
            if (bean == null) {
                return;
            }
            bean.decreaseRef();
            if (bean.getRefCount() < 1) {
                Logger.d("really unbind " + stubServiceName);
                context.unbindService(bean.getServiceConnection());
            }
        }
    }


}

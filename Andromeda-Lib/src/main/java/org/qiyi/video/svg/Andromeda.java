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
package org.qiyi.video.svg;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;

import org.qiyi.video.svg.event.Event;
import org.qiyi.video.svg.event.EventListener;
import org.qiyi.video.svg.local.LocalServiceHub;
import org.qiyi.video.svg.remote.ConnectionManager;
import org.qiyi.video.svg.remote.IRemoteManager;
import org.qiyi.video.svg.remote.RemoteManagerRetriever;
import org.qiyi.video.svg.transfer.RemoteTransfer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by wangallen on 2018/1/8.
 */
public class Andromeda {

    private static final String TAG = "Andromeda";

    private static Andromeda sInstance;

    private static Context appContext;

    private static AtomicBoolean initFlag = new AtomicBoolean(false);

    public static void init(Context context) {
        if (initFlag.get() || context == null) {
            return;
        }
        appContext = context.getApplicationContext();
        RemoteTransfer.init(context.getApplicationContext());
        initFlag.set(true);
    }

    public static Andromeda getInstance() {
        if (null == sInstance) {
            synchronized (Andromeda.class) {
                if (null == sInstance) {
                    sInstance = new Andromeda();
                }
            }
        }
        return sInstance;
    }

    private RemoteManagerRetriever remoteManagerRetriever;

    private Andromeda() {
        this.remoteManagerRetriever = new RemoteManagerRetriever();
    }

    public static Context getAppContext() {
        return appContext;
    }

    public static void registerLocalService(Class serviceClass, Object serviceImpl) {
        if (null == serviceClass || null == serviceImpl) {
            return;
        }
        LocalServiceHub.getInstance().registerService(serviceClass.getCanonicalName(), serviceImpl);
    }

    //考虑到混淆，直接写类的完整路径名容易导致两边不一致，所以不推荐使用这种方式!
    @Deprecated
    public static void registerLocalService(String serviceCanonicalName, Object serviceImpl) {
        if (TextUtils.isEmpty(serviceCanonicalName) || null == serviceImpl) {
            return;
        }
        LocalServiceHub.getInstance().registerService(serviceCanonicalName, serviceImpl);
    }

    public static <T> T getLocalService(Class serviceClass) {
        if (null == serviceClass) {
            return null;
        }
        return (T) LocalServiceHub.getInstance().getLocalService(serviceClass.getCanonicalName());
    }

    @Deprecated
    public static <T> T getLocalService(String serviceCanonicalName) {
        if (TextUtils.isEmpty(serviceCanonicalName)) {
            return null;
        }
        return (T) LocalServiceHub.getInstance().getLocalService(serviceCanonicalName);
    }

    public static void unregisterLocalService(Class serviceClass) {
        if (null == serviceClass) {
            return;
        }
        LocalServiceHub.getInstance().unregisterService(serviceClass.getCanonicalName());
    }

    //考虑到混淆，不推荐使用这种方式!
    @Deprecated
    public static void unregisterLocalService(String serivceCanonicalName) {
        if (TextUtils.isEmpty(serivceCanonicalName)) {
            return;
        }
        LocalServiceHub.getInstance().unregisterService(serivceCanonicalName);
    }

    public static <T extends IBinder> void registerRemoteService(Class serviceClass, T stubBinder) {
        if (null == serviceClass || null == stubBinder) {
            return;
        }
        RemoteTransfer.getInstance().registerStubService(serviceClass.getCanonicalName(), stubBinder);
    }

    //考虑到混淆，不推荐使用这种方式
    @Deprecated
    public static <T extends IBinder> void registerRemoteService(String serviceCanonicalName, T stubBinder) {
        if (TextUtils.isEmpty(serviceCanonicalName) || null == stubBinder) {
            return;
        }
        RemoteTransfer.getInstance().registerStubService(serviceCanonicalName, stubBinder);
    }

    public static void unregisterRemoteService(Class serviceClass) {
        if (null == serviceClass) {
            return;
        }
        RemoteTransfer.getInstance().unregisterStubService(serviceClass.getCanonicalName());
    }

    @Deprecated
    public static void unregisterRemoteService(String serviceCanonicalName) {
        if (TextUtils.isEmpty(serviceCanonicalName)) {
            return;
        }
        RemoteTransfer.getInstance().unregisterStubService(serviceCanonicalName);
    }


    public static IRemoteManager with(android.app.Fragment fragment) {
        return getRetriever().get(fragment);
    }

    public static IRemoteManager with(Fragment fragment) {
        return getRetriever().get(fragment);
    }

    public static IRemoteManager with(FragmentActivity fragmentActivity) {
        return getRetriever().get(fragmentActivity);
    }

    public static IRemoteManager with(Activity activity) {
        return getRetriever().get(activity);
    }

    public static IRemoteManager with(Context context) {
        return getRetriever().get(context);
    }

    public static IRemoteManager with(View view) {
        return getRetriever().get(view);
    }

    private static RemoteManagerRetriever getRetriever() {
        //Preconditions.checkNotNull(context,"context cannote be null in getRetriever(Context)");
        return Andromeda.getInstance().getRemoteManagerRetriever();
    }


    public RemoteManagerRetriever getRemoteManagerRetriever() {
        return remoteManagerRetriever;
    }


    ////////////////end of non-static methods/////////////////////////////

    public static void unbind(Class<?> serviceClass) {
        unbind(serviceClass.getCanonicalName());
    }

    @Deprecated
    public static void unbind(String serviceCanonicalName) {
        if (TextUtils.isEmpty(serviceCanonicalName)) {
            return;
        }
        List<String> serviceNames = new ArrayList<>();
        serviceNames.add(serviceCanonicalName);
        ConnectionManager.getInstance().unbindAction(appContext, serviceNames);
    }

    public static void unbind(Set<Class<?>> serviceClasses) {
        if (null == serviceClasses || serviceClasses.size() < 1) {
            return;
        }
        List<String> serviceNames = new ArrayList<>();
        for (Class<?> clazz : serviceClasses) {
            serviceNames.add(clazz.getCanonicalName());
        }
        ConnectionManager.getInstance().unbindAction(appContext, serviceNames);
    }

    public static void subscribe(String name, EventListener listener) {
        if (TextUtils.isEmpty(name)) {
            return;
        }
        RemoteTransfer.getInstance().subscribeEvent(name, listener);
    }

    public static void unsubscribe(EventListener listener) {
        if (null == listener) {
            return;
        }
        RemoteTransfer.getInstance().unsubscribeEvent(listener);
    }

    public static void publish(Event event) {
        if (null == event) {
            return;
        }
        RemoteTransfer.getInstance().publish(event);
    }
}

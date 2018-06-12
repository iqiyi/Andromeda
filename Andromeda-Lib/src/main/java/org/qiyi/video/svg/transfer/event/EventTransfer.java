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
package org.qiyi.video.svg.transfer.event;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.text.TextUtils;

import org.qiyi.video.svg.BinderWrapper;
import org.qiyi.video.svg.IDispatcher;
import org.qiyi.video.svg.IRemoteTransfer;
import org.qiyi.video.svg.config.Constants;
import org.qiyi.video.svg.dispatcher.DispatcherService;
import org.qiyi.video.svg.event.Event;
import org.qiyi.video.svg.event.EventListener;
import org.qiyi.video.svg.log.Logger;
import org.qiyi.video.svg.utils.ServiceUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangallen on 2018/1/25.
 */

public class EventTransfer {

    private Map<String, List<WeakReference<EventListener>>> eventListeners = new HashMap<>();

    public void subscribeEventLocked(String name, EventListener listener) {
        Logger.d("RemoteTransfer-->subscribe,name:" + name);
        if (TextUtils.isEmpty(name) || listener == null) {
            return;
        }
        if (null == eventListeners.get(name)) {
            List<WeakReference<EventListener>> list = new ArrayList<>();
            eventListeners.put(name, list);
        }
        eventListeners.get(name).add(new WeakReference<>(listener));
    }

    public void unsubscribeEventLocked(EventListener listener) {
        for (Map.Entry<String, List<WeakReference<EventListener>>> entry : eventListeners.entrySet()) {
            List<WeakReference<EventListener>> listeners = entry.getValue();
            for (WeakReference<EventListener> weakRef : listeners) {
                if (listener == weakRef.get()) {
                    listeners.remove(weakRef);
                    break;
                }
            }
        }
    }


    public void publishLocked(Event event, IDispatcher dispatcherProxy, IRemoteTransfer.Stub stub, Context context) {
        Logger.d("EventTransfer-->publishLocked,event.name:" + event.getName());
        if (null == dispatcherProxy) {
            BinderWrapper wrapper = new BinderWrapper(stub.asBinder());
            Intent intent = new Intent(context, DispatcherService.class);
            intent.setAction(Constants.DISPATCH_EVENT_ACTION);
            intent.putExtra(Constants.KEY_REMOTE_TRANSFER_WRAPPER, wrapper);
            intent.putExtra(Constants.KEY_EVENT, event);
            intent.putExtra(Constants.KEY_PID, android.os.Process.myPid());
            ServiceUtils.startServiceSafely(context, intent);
        } else {
            try {
                dispatcherProxy.publish(event);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void notifyLocked(Event event) {
        Logger.d("EventTransfer-->notifyLocked,pid:" + android.os.Process.myPid() + ",event.name:" + event.getName());
        List<WeakReference<EventListener>> listeners = eventListeners.get(event.getName());
        if (listeners == null) {
            Logger.d("There is no listeners for " + event.getName() + " in pid " + android.os.Process.myPid());
            return;
        }
        for (int i = listeners.size() - 1; i >= 0; --i) {
            WeakReference<EventListener> listenerRef = listeners.get(i);
            if (listenerRef.get() == null) {
                listeners.remove(i);
            } else {
                listenerRef.get().onNotify(event);
            }
        }
    }
}

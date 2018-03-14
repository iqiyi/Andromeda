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

    public void subscribeEvent(String name, EventListener listener) {
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

    public void unsubscribeEvent(EventListener listener) {
        //TODO 直接遍历的话，效率会不会有点低?
        for (Map.Entry<String, List<WeakReference<EventListener>>> entry : eventListeners.entrySet()) {
            List<WeakReference<EventListener>> listeners = entry.getValue();
            for (WeakReference<EventListener> weakRef : listeners) {
                if (listener == weakRef) {
                    listeners.remove(weakRef);
                    break;
                }
            }
        }
    }

    public void publish(Event event, IDispatcher dispatcherProxy, IRemoteTransfer.Stub stub, Context context) {
        Logger.d("EventTransfer-->publish,event.name:" + event.getName());
        if (null == dispatcherProxy) {
            BinderWrapper wrapper = new BinderWrapper(stub.asBinder());
            Intent intent = new Intent(context, DispatcherService.class);
            intent.setAction(Constants.DISPATCH_REGISTER_SERVICE_ACTION);
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

    public void notify(Event event) {
        Logger.d("EventTransfer-->notify,pid:" + android.os.Process.myPid() + ",event.name:" + event.getName());
        List<WeakReference<EventListener>> listeners = eventListeners.get(event.getName());

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

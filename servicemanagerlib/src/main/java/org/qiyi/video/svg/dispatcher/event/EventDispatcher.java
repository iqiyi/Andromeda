package org.qiyi.video.svg.dispatcher.event;

import android.os.IBinder;
import android.os.RemoteException;

import org.qiyi.video.svg.IRemoteTransfer;
import org.qiyi.video.svg.event.Event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangallen on 2018/1/24.
 */

public class EventDispatcher implements IEventDispatcher {

    //private List<IBinder> transferBinders = new ArrayList<>();
    private Map<Integer, IBinder> transferBinders = new ConcurrentHashMap<>();

    @Override
    public void registerRemoteTransfer(final int pid, IBinder transferBinder) {
        if (transferBinder == null) {
            return;
        }
        try {
            transferBinder.linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    transferBinders.remove(pid);
                }
            }, 0);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } finally {
            transferBinders.put(pid, transferBinder);
        }

    }

    @Override
    public void publish(Event event) throws RemoteException {

        RemoteException ex = null;

        for (Map.Entry<Integer, IBinder> entry : transferBinders.entrySet()) {
            IRemoteTransfer transfer = IRemoteTransfer.Stub.asInterface(entry.getValue());
            //对于这种情况，如果有一个出现RemoteException,也不能就停下吧?
            if (null != transfer) {
                try {
                    transfer.notify(event);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    ex = e;
                }
            }
        }

        if (null != ex) {
            throw ex;
        }

    }
}

package org.qiyi.video.svg.dispatcher.event;

import android.os.IBinder;
import android.os.RemoteException;

import org.qiyi.video.svg.event.Event;

/**
 * Created by wangallen on 2018/1/24.
 */

public interface IEventDispatcher {

    void registerRemoteTransfer(int pid, IBinder transferBinder);

    void publish(Event event) throws RemoteException;
}

package org.qiyi.video.svg.dispatcher.service;

import android.os.IBinder;
import android.os.RemoteException;

/**
 * Created by wangallen on 2018/1/24.
 */

public interface IServiceDispatcher {

    IBinder getTargetBinder(String serviceCanonicalName) throws RemoteException;

    void registerRemoteService(String serviceCanonicalName, IBinder binder) throws RemoteException;

    void unregisterRemoteService(String serviceCanonicalName) throws RemoteException;

}

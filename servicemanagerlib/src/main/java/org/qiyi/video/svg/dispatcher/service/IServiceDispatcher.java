package org.qiyi.video.svg.dispatcher.service;

import android.os.IBinder;
import android.os.RemoteException;

import org.qiyi.video.svg.bean.BinderBean;

/**
 * Created by wangallen on 2018/1/24.
 */

public interface IServiceDispatcher {

    //IBinder getTargetBinder(String serviceCanonicalName) throws RemoteException;
    BinderBean getTargetBinder(String serviceCanonicalName) throws RemoteException;

    void registerRemoteService(String serviceCanonicalName, String processName, IBinder binder) throws RemoteException;

    //void unregisterRemoteService(String serviceCanonicalName) throws RemoteException;
    void removeBinderCache(String serviceCanonicalName);

}

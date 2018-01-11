package org.qiyi.video.svg.remote;

import android.content.Context;
import android.os.Binder;
import android.os.IInterface;

/**
 * Created by wangallen on 2018/1/9.
 */

public interface IRemoteServiceManager {

    Object getRemoteService(String serviceCanonicalName, Context context);

    //TODO 这个可能要等事件通知机制做好之后才能实现，因为需要通知所有用到的Client
    void registerStubService(String serviceCanonicalName, IInterface stubImpl);

    IInterface getStubService(String serviceCanonicalName);
    //void unregisterService(String module);

}

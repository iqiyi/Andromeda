package org.qiyi.video.svg.remote;

import android.os.IBinder;
import android.os.IInterface;

/**
 * Created by wangallen on 2018/1/9.
 */

public interface IRemoteServiceManager {

    IBinder getRemoteService(String serviceCanonicalName);

    //TODO 这个可能要等事件通知机制做好之后才能实现，因为需要通知所有用到的Client
    //其实是registerStubService
    void registerStubService(String serviceCanonicalName, IBinder stubBinder);

}

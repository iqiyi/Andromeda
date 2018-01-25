package org.qiyi.video.svg.transfer.service;

import android.os.IBinder;

/**
 * Created by wangallen on 2018/1/9.
 */

public interface IRemoteServiceTransfer {

    IBinder getRemoteService(String serviceCanonicalName);

    //TODO 这个可能要等事件通知机制做好之后才能实现，因为需要通知所有用到的Client
    //其实是registerStubService
    void registerStubService(String serviceCanonicalName, IBinder stubBinder);

}

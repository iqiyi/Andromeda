package org.qiyi.video.svg.remote;

import android.os.IBinder;

/**
 * Created by wangallen on 2018/3/26.
 */

public interface IRemoteManager {

    IBinder getRemoteService(Class<?> serviceClass);

    @Deprecated
    IBinder getRemoteService(String serviceCanonicalName);

    /*
    void unbind(Class<?> serviceClass);

    @Deprecated
    void unbind(String serviceCanonicalName);
    */

}

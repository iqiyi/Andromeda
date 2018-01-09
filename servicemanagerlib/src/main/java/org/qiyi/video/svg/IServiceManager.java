package org.qiyi.video.svg;

import android.content.Context;

/**
 * Created by wangallen on 2018/1/8.
 */
//TODO 后期要考虑一个Module下分很多个interfaces的情况，即一对多。因为可能一个Module也很复杂，需要几个不同的业务分别实现各自的接口
public interface IServiceManager {

    void registerLocalService(String module, Object serviceImpl);

    void unregisterLocalService(String module, Object serviceImpl);

    //TODO 不仅要支持懒加载，也要支持业务方主动注册!
    //void registerRemoteService(String module,Object serivceImpl);

    //只能用于同进程通信,所以支持的返回值和参数类型都不受限制
    Object getLocalService(String module);

    //既可用于IPC,也可用于同一个进程通信,所以返回值和参数类型受AIDL的限制
    Object getRemoteService(String module, Context context);

}

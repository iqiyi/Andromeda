package org.qiyi.video.svg.local;

/**
 * Created by wangallen on 2018/1/8.
 */

public interface ILocalServiceHub {

    Object getLocalService(String module);

    void registerService(String module, Object serviceImpl);

    void unregisterService(String module);

}

package org.qiyi.video.svg.local;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangallen on 2018/1/8.
 */

public class LocalServiceHub implements ILocalServiceHub {

    private static LocalServiceHub sInstance;

    public static LocalServiceHub getInstance() {
        if (null == sInstance) {
            synchronized (LocalServiceHub.class) {
                if (null == sInstance) {
                    sInstance = new LocalServiceHub();
                }
            }
        }
        return sInstance;
    }

    private Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    private LocalServiceHub() {
    }


    @Override
    public Object getLocalService(String module) {
        return serviceMap.get(module);
    }

    @Override
    public void registerService(String module, Object serviceImpl) {
        serviceMap.put(module, serviceImpl);
    }

    @Override
    public void unregisterService(String module) {
        serviceMap.remove(module);
    }
}

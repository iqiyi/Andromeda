package wang.imallen.blog.servicemanagerlib.local;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangallen on 2018/1/8.
 */

public class LocalServiceManager implements ILocalServiceManager {

    private static LocalServiceManager sInstance;

    public static LocalServiceManager getInstance() {
        if (null == sInstance) {
            synchronized (LocalServiceManager.class) {
                if (null == sInstance) {
                    sInstance = new LocalServiceManager();
                }
            }
        }
        return sInstance;
    }

    private Map<String, Object> serviceMap = new ConcurrentHashMap<>();

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

package wang.imallen.blog.servicemanagerlib.current;

import android.os.IBinder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangallen on 2018/1/8.
 */
//TODO 这个类和MainServiceManager都是多余的
public class CurrentServiceManager implements ICurrentServiceManager {

    private Map<String, IBinder> binderMap = new ConcurrentHashMap<>();

    @Override
    public void registerIBinder(String module, IBinder binder) {
        binderMap.put(module, binder);
    }

    @Override
    public void unregisterIBinder(String module) {
        binderMap.remove(module);
    }
}

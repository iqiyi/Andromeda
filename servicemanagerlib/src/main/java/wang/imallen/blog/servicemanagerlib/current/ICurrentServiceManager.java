package wang.imallen.blog.servicemanagerlib.current;

import android.os.IBinder;

/**
 * Created by wangallen on 2018/1/8.
 */

public interface ICurrentServiceManager {
    void registerIBinder(String module, IBinder binder);

    void unregisterIBinder(String module);
}

package wang.imallen.blog.servicemanagerlib.main;

import android.os.IBinder;

/**
 * Created by wangallen on 2018/1/8.
 */

public interface IMainServiceManager {

    //TODO 但是，要怎样可以直接获取接口呢?目前只能返回Object,因为泛型还没这么强大
    <T> T getRemoteService(String module);

    //IBinder getRemoteService(String module);

    void registerIBinder(String module, IBinder binder);

    void unregisterIBinder(String module);

}

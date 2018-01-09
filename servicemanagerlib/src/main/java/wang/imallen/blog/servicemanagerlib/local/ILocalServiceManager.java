package wang.imallen.blog.servicemanagerlib.local;

/**
 * Created by wangallen on 2018/1/8.
 */

public interface ILocalServiceManager {

    Object getLocalService(String module);

    void registerService(String module, Object serviceImpl);

    void unregisterService(String module);

}

package wang.imallen.blog.servicemanagerlib.config;

/**
 * Created by wangallen on 2018/1/8.
 */

public interface ServiceActionPolicy {

    /**
     * 根据module名称获取到对应Module用来通信的那个Service的IBinder的action
     *
     * @param module
     * @return
     */
    String getFetchServiceAction(String module);

    /**
     * 让对方向自己注册的action
     * @param module
     * @return
     */
    String getRegisterServiceAction(String module);
}

package wang.imallen.blog.servicemanagerlib;

import android.content.Context;

/**
 * Created by wangallen on 2018/1/8.
 */

public interface IServiceManager {

    //只能用于同进程通信,所以支持的返回值和参数类型都不受限制
    Object getLocalService(String module);

    //既可用于IPC,也可用于同一个进程通信,所以返回值和参数类型受AIDL的限制
    Object getRemoteService(String module,Context context);

}

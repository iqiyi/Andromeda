package org.qiyi.video.svg.utils;

import android.content.Context;
import android.content.Intent;

import org.qiyi.video.svg.stub.CommuStubService;

/**
 * Created by wangallen on 2018/1/30.
 */
//TODO 这部分代码要改成自动生成，而且它要与Manifest文件的编辑相对应
public class MatchStubServiceHelper {

    /**
     * 服务进程的名称，如果是主进程就不用bind了!
     *
     * @param serviceName
     * @param serverProcessName
     * @return
     */
    public static Intent matchIntent(Context context, String serviceName, String serverProcessName) {
        //如果是对方是主进程，则不需要bind,因为不用担心主进程被杀掉
        if (context.getPackageName().equals(serverProcessName)) {
            return null;
        }
        //如果对方跟当前进程是同一进程，也不需要进行bind
        String currentProName = ProcessUtils.getProcessName(context);
        if (null == currentProName || currentProName.equals(serverProcessName)) {
            return null;
        }
        //TODO 要根据processName获取到StubService的名称，然后获取Intent,这部分代码后面可能需要在编译时生成
        if (serverProcessName.endsWith(":apple")) {
            return new Intent(context, CommuStubService.CommuStubService0.class);
        } else if (serverProcessName.endsWith(":webview")) {
            return new Intent(context, CommuStubService.CommuStubService1.class);
        } else if (serverProcessName.endsWith("pushservice")) {
            return new Intent(context, CommuStubService.CommuStubService2.class);
        } else if (serverProcessName.endsWith(":bdservice_v1")) {
            return new Intent(context, CommuStubService.CommuStubService3.class);
        } else if (serverProcessName.endsWith(":silk")) {
            return new Intent(context, CommuStubService.CommuStubService4.class);
        } else if (serverProcessName.endsWith(":plugin1")) {
            return new Intent(context, CommuStubService.CommuStubService5.class);
        } else if (serverProcessName.endsWith(":plugin2")) {
            return new Intent(context, CommuStubService.CommuStubService6.class);
        } else if (serverProcessName.endsWith(":downloader")) {
            return new Intent(context, CommuStubService.CommuStubService7.class);
        }

        return null;
    }

}

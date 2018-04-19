package org.qiyi.video.svg.utils;

import android.content.Context;
import android.content.Intent;

import org.qiyi.video.svg.log.Logger;

/**
 * Created by wangallen on 2018/1/30.
 */
public class StubServiceMatcher {

    /**
     * 服务进程的名称，如果是主进程就不用bind了!
     *
     * @param serverProcessName
     * @return
     */
    public static Intent matchIntent(Context context, String serverProcessName) {
        //如果是对方是主进程，则不需要bind,因为不用担心主进程被杀掉
        if (context.getPackageName().equals(serverProcessName)) {
            return null;
        }
        //如果对方跟当前进程是同一进程，也不需要进行bind
        String currentProName = ProcessUtils.getProcessName(context);
        if (null == currentProName || currentProName.equals(serverProcessName)) {
            return null;
        }
        String resultProName = serverProcessName;
        if (resultProName.startsWith(context.getPackageName())) {
            int index = resultProName.lastIndexOf(":");
            //要考虑到有些进程名称不包含":"
            if (index > 0) {
                resultProName = resultProName.substring(index);
            }
        }
        Logger.d("StubServiceMatcher-->matchIntent(),resultProName:" + resultProName);
        Object targetObj = getTargetService(resultProName);
        if (null == targetObj) {
            return null;
        }
        Class targetServiceClass = (Class) targetObj;
        return new Intent(context, targetServiceClass);
    }

    /**
     * gradle插件会修改这个方法，插入类似如下代码:
     * Map hashMap = new HashMap();
     * hashMap.put(":guard", CommuStubService0.class);
     * hashMap.put(":banana", CommuStubService1.class);
     * hashMap.put("com.android.apple", CommuStubService2.class);
     * hashMap.put(":test4", CommuStubService3.class);
     * hashMap.put("com.android.test5", CommuStubService4.class);
     * hashMap.put(":apple", CommuStubService5.class);
     * hashMap.put(":tea", CommuStubService6.class);
     * hashMap.put("com.android.test6", CommuStubService7.class);
     * hashMap.put(":test3", CommuStubService8.class);
     * hashMap.put(":test2", CommuStubService9.class);
     * hashMap.put(":test1", CommuStubService10.class);
     * if(matchedServices.get($1)!=null)return matchedServices.get($1);
     * return null;
     *
     * @param proName
     * @return
     */
    //由于javassist不支持泛型，故不能返回Class,只能返回Object
    private static Object getTargetService(String proName) {

        return null;
    }


}

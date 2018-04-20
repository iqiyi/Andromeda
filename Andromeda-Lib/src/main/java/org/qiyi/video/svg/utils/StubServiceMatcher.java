/*
* Copyright (c) 2018-present, iQIYI, Inc. All rights reserved.
*
* Redistribution and use in source and binary forms, with or without modification,
* are permitted provided that the following conditions are met:
*
*        1. Redistributions of source code must retain the above copyright notice,
*        this list of conditions and the following disclaimer.
*
*        2. Redistributions in binary form must reproduce the above copyright notice,
*        this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
*
*        3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived
*        from this software without specific prior written permission.
*
*        THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
*        INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
*        IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
*        OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
*        OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
*        OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
*        EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
*/
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

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
package org.qiyi.video.svg.stub;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.qiyi.video.svg.ICommuStub;
import org.qiyi.video.svg.config.Constants;

public class CommuStubService extends Service {

    private static final String TAG = "Andromeda";

    public CommuStubService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ICommuStub.Stub() {
            @Override
            public void commu(Bundle args) throws RemoteException {
                //do nothing now
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand,pid:" + android.os.Process.myPid() + ",action:" + intent.getAction() + ",serviceName:" + intent.getStringExtra(Constants.KEY_SERVICE_NAME));

        //这样可以使Service所在进程的保活效果好一点
        return Service.START_STICKY;
    }

    public static class CommuStubService0 extends CommuStubService {
    }

    public static class CommuStubService1 extends CommuStubService {
    }

    public static class CommuStubService2 extends CommuStubService {
    }

    public static class CommuStubService3 extends CommuStubService {
    }

    public static class CommuStubService4 extends CommuStubService {
    }

    public static class CommuStubService5 extends CommuStubService {
    }

    public static class CommuStubService6 extends CommuStubService {
    }

    public static class CommuStubService7 extends CommuStubService {
    }

    public static class CommuStubService8 extends CommuStubService {
    }

    public static class CommuStubService9 extends CommuStubService {
    }

    public static class CommuStubService10 extends CommuStubService {
    }

    public static class CommuStubService11 extends CommuStubService {
    }

    public static class CommuStubService12 extends CommuStubService {
    }

    public static class CommuStubService13 extends CommuStubService {
    }

    public static class CommuStubService14 extends CommuStubService {
    }


}

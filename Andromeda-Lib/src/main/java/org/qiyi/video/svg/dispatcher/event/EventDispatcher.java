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
package org.qiyi.video.svg.dispatcher.event;

import android.os.IBinder;
import android.os.RemoteException;

import org.qiyi.video.svg.IRemoteTransfer;
import org.qiyi.video.svg.event.Event;
import org.qiyi.video.svg.log.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangallen on 2018/1/24.
 */

public class EventDispatcher implements IEventDispatcher {

    private Map<Integer, IBinder> transferBinders = new ConcurrentHashMap<>();

    @Override
    public void registerRemoteTransferLocked(final int pid, IBinder transferBinder) {
        Logger.d("EventDispatcher-->registerRemoteTransferLocked,pid:" + pid);
        if (transferBinder == null) {
            return;
        }
        try {
            transferBinder.linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    transferBinders.remove(pid);
                }
            }, 0);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } finally {
            transferBinders.put(pid, transferBinder);
        }

    }

    @Override
    public void publishLocked(Event event) throws RemoteException {
        Logger.d("EventDispatcher-->publishLocked,event.name:" + event.getName());
        RemoteException ex = null;
        for (Map.Entry<Integer, IBinder> entry : transferBinders.entrySet()) {
            IRemoteTransfer transfer = IRemoteTransfer.Stub.asInterface(entry.getValue());
            //对于这种情况，如果有一个出现RemoteException,也不能就停下吧?
            if (null != transfer) {
                try {
                    transfer.notify(event);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    ex = e;
                }
            }
        }
        if (null != ex) {
            throw ex;
        }

    }

    @Override
    public void unregisterRemoteServiceLocked(String serviceCanonicalName) throws RemoteException {
        Logger.d("EventDispatcher-->unregisterRemoteServiceLocked,serviceCanonicalName:" + serviceCanonicalName);
        RemoteException e = null;
        for (Map.Entry<Integer, IBinder> entry : transferBinders.entrySet()) {
            IRemoteTransfer transfer = IRemoteTransfer.Stub.asInterface(entry.getValue());
            if (null != transfer) {
                try {
                    transfer.unregisterRemoteService(serviceCanonicalName);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                    e = ex;
                }
            }
        }
        if (null != e) {
            throw e;
        }
    }
}

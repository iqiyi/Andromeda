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
package org.qiyi.video.svg.life;

import org.qiyi.video.svg.log.Logger;
import org.qiyi.video.svg.utils.CollectionUtils;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Created by wangallen on 2018/3/27.
 */

public class ActivityFragLifecycle implements Lifecycle {

    private final Set<LifecycleListener> lifecycleListeners = Collections.newSetFromMap(new WeakHashMap<LifecycleListener, Boolean>());

    private boolean isStarted;
    private boolean isDestroyed;

    @Override
    public void addListener(LifecycleListener listener) {
        Logger.d(this.toString() + "-->addListener,isDestroyed:" + isDestroyed + ",isStarted:" + isStarted);
        lifecycleListeners.add(listener);

        if (isDestroyed) {
            listener.onDestroy();
        } else if (isStarted) {
            listener.onStart();
        } else {
            listener.onStop();
        }
    }

    @Override
    public void removeListener(LifecycleListener listener) {
        lifecycleListeners.remove(listener);
    }

    public void onStart() {
        Logger.d(this.toString() + "-->onStart()");
        isStarted = true;
        for (LifecycleListener listener : CollectionUtils.getSnapshot(lifecycleListeners)) {
            listener.onStart();
        }
    }

    public void onStop() {
        Logger.d(this.toString() + "-->onStop()");
        isStarted = false;
        for (LifecycleListener listener : CollectionUtils.getSnapshot(lifecycleListeners)) {
            listener.onStop();
        }
    }

    public void onDestroy() {
        Logger.d(this.toString() + "-->onDestroy()");
        isDestroyed = true;
        for (LifecycleListener listener : CollectionUtils.getSnapshot(lifecycleListeners)) {
            listener.onDestroy();
        }
    }
}

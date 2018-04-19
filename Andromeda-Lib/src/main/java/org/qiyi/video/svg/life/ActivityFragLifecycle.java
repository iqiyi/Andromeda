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
            //TODO 这个判断好像会有点问题吧?这会导致在刚添加时就调用一次onStop()
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

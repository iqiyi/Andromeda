package org.qiyi.video.svg.life;

import org.qiyi.video.svg.utils.CollectionUtils;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Created by wangallen on 2018/3/27.
 */

public class ActivityFragmentLifecycle implements Lifecycle {

    private final Set<LifecycleListener> lifecycleListeners = Collections.newSetFromMap(new WeakHashMap<LifecycleListener, Boolean>());

    private boolean isStarted;
    private boolean isDestroyed;

    @Override
    public void addListener(LifecycleListener listener) {
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
        isStarted = true;
        for (LifecycleListener listener : CollectionUtils.getSnapshot(lifecycleListeners)) {
            listener.onStart();
        }
    }

    public void onStop() {
        isStarted = false;
        for (LifecycleListener listener : CollectionUtils.getSnapshot(lifecycleListeners)) {
            listener.onStop();
        }
    }

    public void onDestroy() {
        isDestroyed = true;
        for (LifecycleListener listener : CollectionUtils.getSnapshot(lifecycleListeners)) {
            listener.onDestroy();
        }
    }
}

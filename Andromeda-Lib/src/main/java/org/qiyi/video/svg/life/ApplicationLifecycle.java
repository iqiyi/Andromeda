package org.qiyi.video.svg.life;

/**
 * Created by wangallen on 2018/3/27.
 */

public class ApplicationLifecycle implements Lifecycle{

    //TODO 其实完善一点的话，Application也有onStop()和onDestroy()的时候吧，比如这个进程被kill掉了

    @Override
    public void addListener(LifecycleListener listener) {
        listener.onStart();
    }

    @Override
    public void removeListener(LifecycleListener listener) {

    }
}

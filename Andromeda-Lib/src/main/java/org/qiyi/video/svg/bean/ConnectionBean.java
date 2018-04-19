package org.qiyi.video.svg.bean;

import android.content.ServiceConnection;

/**
 * Created by wangallen on 2018/3/29.
 */

public class ConnectionBean {

    private ServiceConnection serviceConnection;

    private int refCount;

    public ConnectionBean(ServiceConnection connection) {
        this.serviceConnection = connection;
        this.refCount = 1;
    }

    public void increaseRef() {
        ++refCount;
    }

    public void decreaseRef() {
        --refCount;
    }

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public int getRefCount() {
        return refCount;
    }
}

package org.qiyi.video.svg.remote;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.qiyi.video.svg.bean.ConnectionBean;
import org.qiyi.video.svg.log.Logger;
import org.qiyi.video.svg.utils.StubServiceMatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by wangallen on 2018/3/29.
 */

public class ConnectionManager {

    private static ConnectionManager instance;

    public static ConnectionManager getInstance() {
        if (null == instance) {
            synchronized (ConnectionManager.class) {
                if (null == instance) {
                    instance = new ConnectionManager();
                }
            }
        }
        return instance;
    }

    //key是对方进程的占坑Service名称
    private Map<String, ConnectionBean> connectionCache = new HashMap<>();

    private ConnectionManager() {
    }

    private String getCommuStubServiceName(Intent intent) {
        if (intent.getComponent() == null) {
            return null;
        }
        return intent.getComponent().getClassName();
    }

    //这里不能按照serviceCanonicalName来区分，而是要按照target service来划分，如果targetService一样，那就没必要再绑定
    public String bindAction(Context context, String serverProcessName) {
        Logger.d("ConnectionManager-->bindAction,serverProcessName:" + serverProcessName);
        Intent intent = StubServiceMatcher.matchIntent(context, serverProcessName);
        if (null == intent) {
            Logger.d("match intent is null");
            return null;
        }
        //TODO 要考虑一下getCommuStubServiceName()会不会耗时太多
        String commuStubServiceName = getCommuStubServiceName(intent);
        ConnectionBean bean = connectionCache.get(commuStubServiceName);
        if (null == bean) {
            ServiceConnection connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Logger.d("onServiceConnected,name:" + name.getShortClassName());
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Logger.d("onServiceDisconnected,name:" + name.getShortClassName());
                }
            };
            bean = new ConnectionBean(connection);
            connectionCache.put(commuStubServiceName, bean);
            Logger.d("really start to bind");
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
        } else {
            bean.increaseRef();
        }
        return commuStubServiceName;
    }

    public synchronized void unbindAction(Context context, Set<String> commuStubServiceNames) {
        Logger.d("ConnectionManager-->unbindAction");
        for (String stubServiceName : commuStubServiceNames) {
            Logger.d("unbindAction, stubServiceName:" + stubServiceName);
            ConnectionBean bean = connectionCache.get(stubServiceName);
            if (bean == null) {
                return;
            }
            bean.decreaseRef();
            if (bean.getRefCount() < 1) {
                Logger.d("really unbind " + stubServiceName);
                context.unbindService(bean.getServiceConnection());
            }
        }
    }


}

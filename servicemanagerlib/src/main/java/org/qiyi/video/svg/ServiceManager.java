package org.qiyi.video.svg;

import android.content.Context;

import org.qiyi.video.svg.local.ILocalServiceManager;
import org.qiyi.video.svg.local.LocalServiceManager;
import org.qiyi.video.svg.remote.RemoteServiceManager;

/**
 * Created by wangallen on 2018/1/8.
 */

//注意:这个ServiceManager可能会存在于很多进程中，不要以为只有主进程才有
public class ServiceManager implements IServiceManager {

    private static final String TAG = "ServiceManager";

    private static ServiceManager sInstance;

    private ILocalServiceManager localServiceManager = LocalServiceManager.getInstance();

    public static ServiceManager getInstance() {
        if (null == sInstance) {
            synchronized (ServiceManager.class) {
                if (null == sInstance) {
                    sInstance = new ServiceManager();
                }
            }
        }
        return sInstance;
    }

    private ServiceManager() {
    }

    @Override
    public void registerLocalService(String module, Object serviceImpl) {
        LocalServiceManager.getInstance().registerService(module, serviceImpl);
    }

    @Override
    public void unregisterLocalService(String module, Object serviceImpl) {
        LocalServiceManager.getInstance().unregisterService(module);
    }

    @Override
    public Object getLocalService(String module) {
        return LocalServiceManager.getInstance().getLocalService(module);
    }

    @Override
    public Object getRemoteService(String module, Context context) {
        return RemoteServiceManager.getInstance().getRemoteService(module, context);
    }
}

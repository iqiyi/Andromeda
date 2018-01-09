package wang.imallen.blog.servicemanagerlib;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import wang.imallen.blog.moduleexportlib.apple.DeliverAppleStub;
import wang.imallen.blog.servicemanagerlib.config.Constants;
import wang.imallen.blog.servicemanagerlib.local.ILocalServiceManager;
import wang.imallen.blog.servicemanagerlib.local.LocalServiceManager;
import wang.imallen.blog.servicemanagerlib.dispatcher.RemoteGuardService;
import wang.imallen.blog.servicemanagerlib.remote.RemoteServiceManager;

/**
 * Created by wangallen on 2018/1/8.
 */

//TODO 注意:这个ServiceManager可能会存在于很多进程中，不要以为只有主进程才有
//public class ServiceManager extends Binder implements IServiceManager, IServiceRegister, IInterface
public class ServiceManager implements IServiceManager{

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
    public Object getLocalService(String module) {
        return LocalServiceManager.getInstance().getLocalService(module);
    }

    @Override
    public Object getRemoteService(String module,Context context) {
        return RemoteServiceManager.getInstance().getRemoteService(module,context);
    }
}

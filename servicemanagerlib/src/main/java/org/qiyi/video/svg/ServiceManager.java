package org.qiyi.video.svg;

import android.content.Context;
import android.os.IBinder;

import org.qiyi.video.svg.local.LocalServiceManager;
import org.qiyi.video.svg.remote.RemoteServiceManager;

/**
 * Created by wangallen on 2018/1/8.
 */

//注意:这个ServiceManager可能会存在于很多进程中，不要以为只有主进程才有
public class ServiceManager implements IServiceManager {

    private static final String TAG = "ServiceManager";

    private static ServiceManager sInstance;

    public static void init(Context context) {
        RemoteServiceManager.init(context);
    }

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
    public void registerLocalService(Class serviceClass, Object serviceImpl) {
        LocalServiceManager.getInstance().registerService(serviceClass.getCanonicalName(), serviceImpl);
    }

    @Override
    public void registerLocalService(String module, Object serviceImpl) {
        LocalServiceManager.getInstance().registerService(module, serviceImpl);
    }


    @Override
    public void unregisterLocalService(Class serviceClass) {
        LocalServiceManager.getInstance().unregisterService(serviceClass.getCanonicalName());
    }

    @Override
    public void unregisterLocalService(String serivceCanonicalName) {
        LocalServiceManager.getInstance().unregisterService(serivceCanonicalName);
    }

    @Override
    public void registerRemoteService(Class serviceClass, IBinder stubBinder) {
        RemoteServiceManager.getInstance().registerStubService(serviceClass.getCanonicalName(), stubBinder);
    }

    @Override
    public Object getLocalService(Class serviceClass) {
        return LocalServiceManager.getInstance().getLocalService(serviceClass.getCanonicalName());
    }

    @Override
    public IBinder getRemoteService(Class serviceClass) {
        return RemoteServiceManager.getInstance().getRemoteService(serviceClass.getCanonicalName());
    }
    /*
    @Override
    public IInterface getStubService(String serviceCanonicalName) {
        return RemoteServiceManager.getInstance().getStubService(serviceCanonicalName);
    }
    */

    @Override
    public Object getLocalService(String serviceCanonicalName) {

        /*
        IBinder calculateBinder=null;
        ICalculate calculate=ICalculate.Stub.asInterface(calculateBinder);
        IPCCallback.Stub callback=new IPCCallback.Stub() {
            @Override
            public void onSucceed(String result) throws RemoteException {
                //Binder线程中回调

            }

            @Override
            public void onFail(String reason) throws RemoteException {

            }
        };
        try{
            calculate.sum(1,2,callback);
        }catch(RemoteException ex){

        }
        */


        return LocalServiceManager.getInstance().getLocalService(serviceCanonicalName);
    }

    @Override
    public IBinder getRemoteService(String serviceCanonicalName) {
        return RemoteServiceManager.getInstance().getRemoteService(serviceCanonicalName);
    }

}

package wang.imallen.blog.servicemanagerlib.main;

import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import wang.imallen.blog.servicemanagerlib.IRemoteServiceManager;
import wang.imallen.blog.servicemanagerlib.IServiceRegister;
import wang.imallen.blog.servicemanagerlib.remote.RemoteGuardService;

/**
 * Created by wangallen on 2018/1/8.
 */
//TODO 考虑将这个MainServiceManager和LocalServiceManager合二为一，为增加可读性和代码简洁性
public class MainServiceManager implements IMainServiceManager, IServiceRegister {

    private static MainServiceManager sInstance;

    public static MainServiceManager getInstance() {
        if (null == sInstance) {
            synchronized (MainServiceManager.class) {
                if (null == sInstance) {
                    sInstance = new MainServiceManager();
                }
            }
        }
        return sInstance;
    }

    //private Map<String, IBinder> binderMap = new ConcurrentHashMap<>();

    private IRemoteServiceManager remoteServiceManagerProxy;

    /////////////////////////TODO 这个接口目前只用于注册RemoteServiceManager的IBinder
    //TODO 这个接口名称是不是应该叫registerRemoteServiceManagerBinder更合适呢?
    @Override
    public void registerRemoteService(String module, IBinder binder) throws RemoteException {
        if (RemoteGuardService.REMOTE_GUARD_SERIVCE_MODULE.equals(module)) {
            remoteServiceManagerProxy = IRemoteServiceManager.Stub.asInterface(binder);
        }
    }

    //TODO 这个方法是多余的
    @Override
    public void unregisterRemoteService(String module) throws RemoteException {

    }
    ///////////////////////////TODO


    @Override
    public IBinder asBinder() {
        return null;
    }

    @Override
    public void registerIBinder(String module, IBinder binder) {
        if (remoteServiceManagerProxy != null) {
            try {
                remoteServiceManagerProxy.registerRemoteService(module, binder);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void unregisterIBinder(String module) {
        if(remoteServiceManagerProxy!=null){
            try{
                remoteServiceManagerProxy.unregisterRemoteService(module);
            }catch(RemoteException ex){
                ex.printStackTrace();
            }
        }
    }

    /////////////////
    @Override
    public IBinder getRemoteService(String module) {
        return null;
    }
}

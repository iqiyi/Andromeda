package wang.imallen.blog.servicemanagerlib.remote;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import wang.imallen.blog.moduleexportlib.apple.DeliverAppleStub;
import wang.imallen.blog.servicemanagerlib.BinderWrapper;
import wang.imallen.blog.servicemanagerlib.IServiceDispatcher;
import wang.imallen.blog.servicemanagerlib.IServiceRegister;
import wang.imallen.blog.servicemanagerlib.config.Constants;
import wang.imallen.blog.servicemanagerlib.dispatcher.RemoteGuardService;

/**
 * Created by wangallen on 2018/1/9.
 */

public class RemoteServiceManager extends IServiceRegister.Stub implements IRemoteServiceManager {

    private static final String TAG = "ServiceManager";

    private static RemoteServiceManager sInstance;

    public static RemoteServiceManager getInstance() {
        if (null == sInstance) {
            synchronized (RemoteServiceManager.class) {
                if (null == sInstance) {
                    sInstance = new RemoteServiceManager();
                }
            }
        }
        return sInstance;
    }

    private IServiceDispatcher serviceDispatcherProxy;
    private Map<String, IBinder> binderCache = new ConcurrentHashMap<>();
    private final Object lock = new Object();


    private RemoteServiceManager(){}

    //TODO 是在这里传context好呢?还是在初始化时传context更好?
    @Override
    public Object getRemoteService(String module, Context context) {
        Log.d(TAG, "ServiceManager-->getRemoteService,pid=" + android.os.Process.myPid() + ",thread:" + Thread.currentThread().getName());

        //TODO 这里需要利用由AnnotationProcessor生成的代码的一个类,在那个类中可将IBinder转换为Interface
        IBinder binder = getIBinder(module, context);
        //TODO 这部分代码后面要改成自动生成，或者是利用反射来调用asInterface
        switch (module) {
            case Constants.APPLE_MODULE: {
                return DeliverAppleStub.asInterface(binder);
            }
            case Constants.CHERRY_MODULE: {
                //TODO 暂时先不实现cherry的，后面再补上
            }
            default:
                break;
        }
        return null;
    }

    private IBinder getIBinder(String module, Context context) {
        if (binderCache.get(module) != null) {
            return binderCache.get(module);
        }

        context = context.getApplicationContext();

        synchronized (lock) {
            if (null == serviceDispatcherProxy) {
                BinderWrapper wrapper = new BinderWrapper(this.asBinder());
                Intent intent = new Intent(context, RemoteGuardService.class);
                intent.setAction(RemoteGuardService.BIND_U_SELF);
                intent.putExtra(Constants.KEY_BINDER_WRAPPER, wrapper);
                context.startService(intent);

                try {
                    lock.wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        try {
            IBinder binder = serviceDispatcherProxy.getTargetBinder(module);
            binderCache.put(module, binder);
            return binder;
        } catch (RemoteException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    ///////////////////
    @Override
    public void registerRemoteService(String module, IBinder binder) throws RemoteException {
        Log.d(TAG, "ServiceManager-->registerRemoteService,module:" + module + ",pid=" + android.os.Process.myPid() + ",thread:" + Thread.currentThread().getName());
        //这里实现IServiceRegister仅仅是为了给RemoteServiceManager提供注册到当前进程的机会
        if (Constants.REMOTE_GUARD_SERVICE_MODULE.equals(module)) {
            //TODO 还要加上linkToDeath这个监听
            serviceDispatcherProxy = IServiceDispatcher.Stub.asInterface(binder);
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    @Override
    public void unregisterRemoteService(String module) throws RemoteException {
        //ServiceManager里不需要这个回调
    }
}

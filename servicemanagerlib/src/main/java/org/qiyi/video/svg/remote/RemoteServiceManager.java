package org.qiyi.video.svg.remote;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.util.Log;

import org.qiyi.video.svg.BinderWrapper;
import org.qiyi.video.svg.IServiceDispatcher;
import org.qiyi.video.svg.IServiceRegister;
import org.qiyi.video.svg.config.Constants;
import org.qiyi.video.svg.dispatcher.RemoteGuardService;
import org.qiyi.video.svg.helper.MatchPolicy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    /**
     * 本地的Binder,需要给其他进程使用的,key为inteface的完整名称
     */
    private Map<String, IInterface> localStubCache = new ConcurrentHashMap<>();
    //private Map<String, Binder> localStubCache = new ConcurrentHashMap<>();

    private Map<String, IBinder> remoteBinderCache = new ConcurrentHashMap<>();
    private final Object lock = new Object();


    private RemoteServiceManager() {
    }

    //TODO 是在这里传context好呢?还是在初始化时传context更好?
    @Override
    public Object getRemoteService(String serviceName, Context context) {
        Log.d(TAG, "ServiceManager-->getRemoteService,pid=" + android.os.Process.myPid() + ",thread:" + Thread.currentThread().getName());

        //TODO 这里需要利用由AnnotationProcessor生成的代码的一个类,在那个类中可将IBinder转换为Interface
        IBinder binder = getIBinder(serviceName, context);
        //TODO 这部分代码后面要改成自动生成，可以利用注解解释器或者gradle插件来生成
        //TODO 目前的考虑是参与编译的就自动生成代码，不参与编译的只能用反射来调用asInterface了
        // GlobalInterface.asInterface();
        return MatchPolicy.asInterface(serviceName, binder);
    }

    private IBinder getIBinder(String serviceName, Context context) {
        if (remoteBinderCache.get(serviceName) != null) {
            return remoteBinderCache.get(serviceName);
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
            //TODO 这里是要改成获取更多的信息，比如类名，还是改成调用AsInterfaceHelper呢?
            //TODO 好像对于插件只能是通过反射来做，所以还是要获取实现类的完整名称,所以需要两者结合的方式! 而且注意这个是不能缓存的，因为你不确定当前这个调用是否一定跟服务端在不同的进程!
            IBinder binder = serviceDispatcherProxy.getTargetBinder(serviceName);
            Log.d(TAG, "get IBinder from ServiceDispatcher");
            remoteBinderCache.put(serviceName, binder);
            return binder;
        } catch (RemoteException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    //注意这个和registerRemoteService的区别，这里其实只是register本进程中有IPC能力的接口
    @Override
    public void registerStubService(String serviceCanonicalName, IInterface stubImpl) {
        localStubCache.put(serviceCanonicalName, stubImpl);
    }

    @Override
    public IInterface getStubService(String serviceCanonicalName) {
        return localStubCache.get(serviceCanonicalName);
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

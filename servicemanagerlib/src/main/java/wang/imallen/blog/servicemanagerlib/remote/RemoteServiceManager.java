package wang.imallen.blog.servicemanagerlib.remote;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import wang.imallen.blog.servicemanagerlib.BinderWrapper;
import wang.imallen.blog.servicemanagerlib.IRemoteServiceManager;
import wang.imallen.blog.servicemanagerlib.config.Constants;
import wang.imallen.blog.servicemanagerlib.config.ServiceActionPolicy;
import wang.imallen.blog.servicemanagerlib.config.ServiceActionPolicyImpl;

/**
 * Created by wangallen on 2018/1/8.
 */
//TODO 要将自己注册到主进程的ServiceManager中
//TODO 这个是不是叫ServiceDispatcher, ServiceRegistryCenter或者RemoteServiceCenter更合适呢？那样每个进程就可以有一个RemoteServiceManager，从架构上会更清晰
public class RemoteServiceManager extends IRemoteServiceManager.Stub {

    private static final String TAG = "ServiceManager";

    private static RemoteServiceManager sInstance;

    public static RemoteServiceManager getInstance(Context context) {
        if (null == sInstance) {
            synchronized (RemoteServiceManager.class) {
                if (null == sInstance) {
                    sInstance = new RemoteServiceManager(context);
                }
            }
        }
        return sInstance;
    }

    private ServiceActionPolicy serviceActionPolicy;
    private Context context;

    private RemoteServiceManager(Context context) {
        this.context = context;
        serviceActionPolicy = ServiceActionPolicyImpl.getInstance();
    }

    private Map<String, IBinder> binderMap = new ConcurrentHashMap<>();

    private String waitingModule;

    private final Object lock = new Object();

    @Override
    public IBinder getTargetBinder(String module) throws RemoteException {
        Log.d(TAG, "RemoteServiceManager-->getTargetBinder,pid:" + android.os.Process.myPid() + ",thread:" + Thread.currentThread().getName());
        IBinder binder = binderMap.get(module);
        if (binder != null) {
            return binder;
        }
        //TODO 如果还没有的话，就需要同步去取
        //TODO 取的方法就是利用startService(),并且我们根据协议，只利用module即可获得action(如规则为qiyi://module//action),然后便可发送命令给相应Module的Service
        //TODO 之后，对应Module的Service接受到命令之后，就把自己的IBinder放在里面再寄回来，然后唤醒进程，从而实现阻塞调用。
        //TODO 需要讨论的是这里用bindService更好还是startService更好，因为都是要利用线程等待
        //TODO 为了保险起见，这里还要加上一个超时机制，比如设置为5s钟，如果还没返回的话，就直接notify了
        Intent intent = new Intent();
        //TODO 要采用这种启动方式吗?
        //TODO 看来只传递一个module名称还是信息太少了，后面可能还是要传递一个ModuleInfo或者ModuleBean之类的,里面包含了完整的target service的名称
        //ComponentName componentName=new ComponentName(context,)
        //intent.setComponent(componentName);
        intent.setPackage(context.getPackageName());
        intent.setAction(serviceActionPolicy.getFetchServiceAction(module));
        BinderWrapper wrapper = new BinderWrapper(this);
        intent.putExtra(Constants.KEY_BINDER_WRAPPER, wrapper);
        context.startService(intent);
        this.waitingModule = module;

        //TODO 可能还要增加一个timeout机制
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        return binderMap.get(module);
    }

    @Override
    public IBinder fetchTargetBinder(String uri) throws RemoteException {
        //TODO 暂时还不确定是否要支持uri
        return null;
    }

    @Override
    public void registerRemoteService(String module, IBinder binder) throws RemoteException {
        Log.d(TAG, "RemoteServiceManager-->registerRemoteService,pid:" + android.os.Process.myPid() + ",thread:" + Thread.currentThread().getName());
        binderMap.put(module, binder);
        if (module.equals(waitingModule)) {
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    @Override
    public void unregisterRemoteService(String module) throws RemoteException {
        binderMap.remove(module);
    }
}

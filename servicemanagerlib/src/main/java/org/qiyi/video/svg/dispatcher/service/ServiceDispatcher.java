package org.qiyi.video.svg.dispatcher.service;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.qiyi.video.svg.log.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangallen on 2018/1/8.
 */
//TODO 要将自己注册到主进程的ServiceManager中
//TODO 这个是不是叫ServiceDispatcher, ServiceRegistryCenter或者RemoteServiceCenter更合适呢？那样每个进程就可以有一个RemoteServiceManager，从架构上会更清晰
public class ServiceDispatcher implements IServiceDispatcher {

    private static final String TAG = "ServiceManager";

    private Context context;

    public ServiceDispatcher(Context context) {
        this.context = context;
        //serviceActionPolicy = ServiceActionPolicyImpl.getInstance();
    }

    private Map<String, IBinder> remoteBinderCache = new ConcurrentHashMap<>();

    private String waitingServiceName;

    private final Object lock = new Object();

    @Override
    public IBinder getTargetBinder(String serviceName) {
        Log.d(TAG, "ServiceDispatcher-->getTargetBinder,serivceName:" + serviceName + ",pid:" + android.os.Process.myPid() + ",thread:" + Thread.currentThread().getName());
        IBinder binder = remoteBinderCache.get(serviceName);
        return binder;
        /*
        if (binder != null) {
            return binder;
        }
        //TODO 如果还没有的话，就需要同步去取
        //TODO 取的方法就是利用startService(),并且我们根据协议，只利用module即可获得action(如规则为qiyi://serviceName//action),然后便可发送命令给相应Module的Service
        //TODO 之后，对应Module的Service接受到命令之后，就把自己的IBinder放在里面再寄回来，然后唤醒进程，从而实现阻塞调用。
        //TODO 需要讨论的是这里用bindService更好还是startService更好，因为都是要利用线程等待
        //TODO 为了保险起见，这里还要加上一个超时机制，比如设置为5s钟，如果还没返回的话，就直接notify了
        Intent intent = new Intent();
        //TODO 要采用这种启动方式吗?
        //TODO 看来只传递一个module名称还是信息太少了，后面可能还是要传递一个ModuleInfo或者ModuleBean之类的,里面包含了完整的target service的名称
        //ComponentName componentName=new ComponentName(context,)
        //intent.setComponent(componentName);
        intent.setPackage(context.getPackageName());

        intent.setAction(MatchPolicy.getServiceAction(serviceName));
        //intent.setAction(serviceActionPolicy.getFetchServiceAction(serviceName));
        intent.putExtra(Constants.KEY_SERVICE_NAME, serviceName);
        BinderWrapper wrapper = new BinderWrapper(this);
        intent.putExtra(Constants.KEY_BINDER_WRAPPER, wrapper);

        this.waitingServiceName = serviceName;
        //TODO Service的Context又去启动Service,会不会不行？
        context.startService(intent);

        //TODO 可能还要增加一个timeout机制
        synchronized (lock) {
            try {
                lock.wait(5000);
                //lock.wait();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        Log.d(TAG, "now return to RemoteServiceManager in request process");
        //TODO 要增加判空操作
        return remoteBinderCache.get(serviceName);
        */
    }

    //TODO 还要把binder与serviceName绑定在一起，才能找到对应的类调用asInterface()方法
    @Override
    public void registerRemoteService(final String serviceCanonicalName, IBinder binder) throws RemoteException {
        Log.d(TAG, "ServiceDispatcher-->registerStubService,serviceCanonicalName:" + serviceCanonicalName + ",pid:" + android.os.Process.myPid() + ",thread:" + Thread.currentThread().getName());
        if (binder != null) {
            binder.linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    Logger.d("ServiceDispatcher-->binderDied,serviceCanonicalName:" + serviceCanonicalName);
                    remoteBinderCache.remove(serviceCanonicalName);
                }
            }, 0);
            remoteBinderCache.put(serviceCanonicalName, binder);
            Log.d(TAG, "binder is not null");
        } else {
            Log.d(TAG, "binder is null");
        }
    }

    @Override
    public void unregisterRemoteService(String serviceCanonicalName) throws RemoteException {
        remoteBinderCache.remove(serviceCanonicalName);
    }
}

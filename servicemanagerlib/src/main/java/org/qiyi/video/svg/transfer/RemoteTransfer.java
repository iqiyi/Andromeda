package org.qiyi.video.svg.transfer;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import org.qiyi.video.svg.BinderWrapper;
import org.qiyi.video.svg.IDispatcher;
import org.qiyi.video.svg.IRemoteTransfer;
import org.qiyi.video.svg.bean.BinderBean;
import org.qiyi.video.svg.config.Constants;
import org.qiyi.video.svg.dispatcher.Dispatcher;
import org.qiyi.video.svg.dispatcher.DispatcherService;
import org.qiyi.video.svg.event.Event;
import org.qiyi.video.svg.event.EventListener;
import org.qiyi.video.svg.log.Logger;
import org.qiyi.video.svg.transfer.event.EventTransfer;
import org.qiyi.video.svg.transfer.event.IEventTransfer;
import org.qiyi.video.svg.transfer.service.IRemoteServiceTransfer;
import org.qiyi.video.svg.transfer.service.RemoteServiceTransfer;
import org.qiyi.video.svg.utils.MatchStubServiceHelper;
import org.qiyi.video.svg.utils.ProcessUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangallen on 2018/1/9.
 */
//TODO 注意:所有startService()的地方都要加上try...catch,因为有些手机在后台如果startService()会抛出异常,比如Oppo
public class RemoteTransfer extends IRemoteTransfer.Stub implements IRemoteServiceTransfer, IEventTransfer {

    private static RemoteTransfer sInstance;

    public static void init(Context context) {
        getInstance().setContext(context);

        //TODO 如果是主进程就可以直接调用ServiceDispatcher
        getInstance().sendRegisterInfo();
    }

    public static RemoteTransfer getInstance() {
        if (null == sInstance) {
            synchronized (RemoteTransfer.class) {
                if (null == sInstance) {
                    sInstance = new RemoteTransfer();
                }
            }
        }
        return sInstance;
    }

    private Context context;

    private IDispatcher dispatcherProxy;

    private final Object lock = new Object();

    private RemoteServiceTransfer serviceTransfer;
    private EventTransfer eventTransfer;

    private RemoteTransfer() {
        serviceTransfer = new RemoteServiceTransfer();
        eventTransfer = new EventTransfer();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    //让ServiceDispatcher注册到当前进程
    public void sendRegisterInfo() {
        if (ProcessUtils.isMainProcess(context)) {
            //KP 如果是主进程就走捷径,不然直接杀进程时会导致crash
            dispatcherProxy = Dispatcher.getInstance(context);
            //但是这样的话还是有问题，因为没有把自己注册进去,所以还要直接注册
            Dispatcher.getInstance(context).registerRemoteTransfer(android.os.Process.myPid(), this.asBinder());
            return;
        }

        if (dispatcherProxy == null) {
            //后面考虑还是采用"has-a"的方式会更好
            BinderWrapper wrapper = new BinderWrapper(this.asBinder());
            Intent intent = new Intent(context, DispatcherService.class);
            intent.setAction(Constants.DISPATCH_SERVICE_ACTION);
            intent.putExtra(Constants.KEY_REMOTE_TRANSFER_WRAPPER, wrapper);
            intent.putExtra(Constants.KEY_PID, android.os.Process.myPid());
            context.startService(intent);
        }
    }

    //TODO 是在这里传context好呢?还是在初始化时传context更好?
    @Override
    public IBinder getRemoteService(String serviceName) {
        Logger.d("RemoteTransfer-->getRemoteService,pid=" + android.os.Process.myPid() + ",thread:" + Thread.currentThread().getName());
        //TODO 这里就不只要获取IBinder,还要获取到对方的processName,这样才能进行bind操作
        BinderBean binderBean = getIBinder(serviceName);
        //TODO 要进行bind操作
        bindAction(serviceName, binderBean.getProcessName());
        return binderBean.getBinder();
    }

    private void unbindAction(String serviceName) {
        ServiceConnection connection = connectionCache.get(serviceName);
        if (connection == null) {
            return;
        }
        context.unbindService(connection);
    }

    private Map<String, ServiceConnection> connectionCache = new ConcurrentHashMap<>();

    //TODO 是让当前进程去bind呢?还是只让主进程去bind?要考虑到当前进程可能是在插件进程，同时对方可能是主进程这种情况？
    private void bindAction(String serviceName, String serverProcessName) {
        //如果是主进程或者跟当前进程在同一个进程，MatchStubServiceHelper.matchIntent()就会返回null
        Intent intent = MatchStubServiceHelper.matchIntent(context, serviceName, serverProcessName);
        if (null == intent) {
            return;
        }
        ServiceConnection connection = connectionCache.get(serviceName);
        if (null == connection) {
            connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Logger.d("onServiceConnected");
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Logger.d("onServiceDisconnected");
                }
            };
            connectionCache.put(serviceName, connection);
        }
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
    }

    @Override
    public IBinder getRemoteService(final LifecycleOwner owner, final String serviceCanonicalName) {
        BinderBean binderBean = getIBinder(serviceCanonicalName);
        if (owner != null) {
            bindAction(serviceCanonicalName, binderBean.getProcessName());
            //TODO 那什么时候remove掉呢?
            owner.getLifecycle().addObserver(new LifecycleObserver() {
                @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
                public void onStop() {
                    unbindAction(serviceCanonicalName);
                    owner.getLifecycle().removeObserver(this);
                }
            });
        }
        return binderBean.getBinder();
    }

    //TODO 这个是不是应该为unbind(String serviceCanonicalName);
    @Override
    public void unbind(String serviceCanonicalName) {
        unbindAction(serviceCanonicalName);
    }

    private BinderBean getIBinder(String serviceName) {
        Logger.d("RemoteTransfer-->getIBinder()");
        //KP 首先检查是否就在本地!这非常重要，否则有可能导致死锁!

        //TODO bindAction看来还是要放到RemoteServiceTransfer中
        BinderBean cacheBinderBean = serviceTransfer.getIBinderFromCache(context, serviceName);
        if (cacheBinderBean != null) {
            return cacheBinderBean;
        }
        Logger.d("RemoteTransfer-->getIBinder(),start to wait");
        //TODO 这部分逻辑是不是要先去掉呢?
        synchronized (lock) {
            if (null == dispatcherProxy) {
                sendRegisterInfo();
                try {
                    lock.wait(3000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        Logger.d("RemoteTransfer-->getIBinder(),end of wait");
        return serviceTransfer.getAndSaveIBinder(serviceName, dispatcherProxy);
    }

    //注意这个和registerRemoteService的区别，这里其实只是register本进程中有IPC能力的接口,它的名字其实叫registerStubService更合适
    //TODO 考虑还是在每个进程的Application中进行初始化，这样有两个目的，一个是获取Context,就不用后面每次调用都传递Context;另外一个是通过startService让ServiceDispatcher反向注册到当前进程
    @Override
    public void registerStubService(String serviceCanonicalName, IBinder stubBinder) {
        serviceTransfer.registerStubService(serviceCanonicalName, stubBinder, context, dispatcherProxy, this);
    }

    ///////////////start of event/////////////////////

    @Override
    public synchronized void subscribeEvent(String name, EventListener listener) {
        eventTransfer.subscribeEvent(name, listener);
    }

    @Override
    public synchronized void unsubscribeEvent(EventListener listener) {
        eventTransfer.unsubscribeEvent(listener);
    }

    @Override
    public void publish(Event event) {
        eventTransfer.publish(event, dispatcherProxy, this, context);
    }

    ////////////////end of event///////////////////////////

    @Override
    public void registerDispatcher(IBinder dispatcherBinder) throws RemoteException {
        Logger.d("RemoteTransfer-->registerDispatcher");
        dispatcherBinder.linkToDeath(new IBinder.DeathRecipient() {
            @Override
            public void binderDied() {
                Logger.d("RemoteTransfer-->dispatcherBinder binderDied");
                dispatcherProxy = null;
            }
        }, 0);
        //这里实现IServiceRegister仅仅是为了给RemoteServiceManager提供注册到当前进程的机会
        dispatcherProxy = IDispatcher.Stub.asInterface(dispatcherBinder);
        synchronized (lock) {
            //由于只有一个地方使用到lock,所以这里使用notify()和notifyAll()的效果是一样的
            lock.notifyAll();
        }
    }

    @Override
    public void notify(Event event) throws RemoteException {
        eventTransfer.notify(event);
    }

}

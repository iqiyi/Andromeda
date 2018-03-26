package org.qiyi.video.svg.transfer;

//import android.arch.lifecycle.Lifecycle;
//import android.arch.lifecycle.LifecycleObserver;
//import android.arch.lifecycle.LifecycleOwner;
//import android.arch.lifecycle.OnLifecycleEvent;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.IBinder;
import android.os.RemoteException;

import org.qiyi.video.svg.BinderWrapper;
import org.qiyi.video.svg.IDispatcher;
import org.qiyi.video.svg.IRemoteTransfer;
import org.qiyi.video.svg.bean.BinderBean;
import org.qiyi.video.svg.config.Constants;
import org.qiyi.video.svg.cursor.DispatcherCursor;
import org.qiyi.video.svg.dispatcher.Dispatcher;
import org.qiyi.video.svg.dispatcher.DispatcherProvider;
import org.qiyi.video.svg.dispatcher.DispatcherService;
import org.qiyi.video.svg.event.Event;
import org.qiyi.video.svg.event.EventListener;
import org.qiyi.video.svg.log.Logger;
import org.qiyi.video.svg.transfer.event.EventTransfer;
import org.qiyi.video.svg.transfer.event.IEventTransfer;
import org.qiyi.video.svg.transfer.service.IRemoteServiceTransfer;
import org.qiyi.video.svg.transfer.service.RemoteServiceTransfer;
import org.qiyi.video.svg.utils.IOUtils;
import org.qiyi.video.svg.utils.ProcessUtils;
import org.qiyi.video.svg.utils.ServiceUtils;
import org.qiyi.video.svg.utils.StubServiceMatcher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangallen on 2018/1/9.
 */
public class RemoteTransfer extends IRemoteTransfer.Stub implements IRemoteServiceTransfer, IEventTransfer {

    private static RemoteTransfer sInstance;

    public static void init(Context context) {
        getInstance().setContext(context);

        //如果是主进程就可以直接调用ServiceDispatcher
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
            //如果是主进程就走捷径,不然直接杀进程时会导致crash
            dispatcherProxy = Dispatcher.getInstance();
            //但是这样的话还是有问题，因为没有把自己注册进去,所以还要直接注册
            Dispatcher.getInstance().registerRemoteTransfer(android.os.Process.myPid(), this.asBinder());
            return;
        }

        if (dispatcherProxy == null) {
            //后面考虑还是采用"has-a"的方式会更好
            BinderWrapper wrapper = new BinderWrapper(this.asBinder());
            Intent intent = new Intent(context, DispatcherService.class);
            intent.setAction(Constants.DISPATCH_REGISTER_SERVICE_ACTION);
            intent.putExtra(Constants.KEY_REMOTE_TRANSFER_WRAPPER, wrapper);
            intent.putExtra(Constants.KEY_PID, android.os.Process.myPid());
            ServiceUtils.startServiceSafely(context, intent);
        }
    }

    @Override
    public IBinder getRemoteService(String serviceName) {
        Logger.d("RemoteTransfer-->getRemoteService,pid=" + android.os.Process.myPid() + ",thread:" + Thread.currentThread().getName());
        BinderBean binderBean = getIBinder(serviceName);
        if (null == binderBean) {
            return null;
        }
        bindAction(serviceName, binderBean.getProcessName());
        return binderBean.getBinder();
    }
    //TODO 实际上这样的缓存是不是有点问题，考虑到进程对进程，那connectionCache的key是不是应该为serverProcessName更合适呢？
    private void unbindAction(String serviceName) {
        ServiceConnection connection = connectionCache.get(serviceName);
        if (connection == null) {
            return;
        }
        context.unbindService(connection);
    }

    private Map<String, ServiceConnection> connectionCache = new ConcurrentHashMap<>();

    private synchronized void bindAction(String serviceName, String serverProcessName) {
        //如果是主进程或者跟当前进程在同一个进程，StubServiceMatcher.matchIntent()就会返回null
        Intent intent = StubServiceMatcher.matchIntent(context, serverProcessName);
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
    /*
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
                //TODO 这部分后面要移除!
                @Override
                public void onStateChange() {

                }
            });
        }
        return binderBean.getBinder();
    }
    */

    //TODO 这里，其实由于ServiceConnection是我们自己创建的，不会有Activity或Fragment中成员的操作，所以不存在内存泄露问题;
    //TODO 另一方面，用的是Application Context,所以其实这里即使不进行unbind()也不要紧的，顶多就是让Service所在进程的oom_adj值高一点，但是并不会导致内存泄露问题。
    //TODO 可以考虑采用这样一个策略，即如果绑定到新的Service,那么就unbind掉之前的ServiceConnection;
    //TODO 另外一种策略就是每新增一个ServiceConnection,就添加一个Monitor,然后比如30分钟内没有被使用就unbind掉
    @Override
    public synchronized void unbind(String serviceCanonicalName) {
        unbindAction(serviceCanonicalName);
    }

    private BinderBean getIBinder(String serviceName) {
        Logger.d("RemoteTransfer-->getIBinder()");
        // 首先检查是否就在本地!这非常重要，否则有可能导致死锁!
        BinderBean cacheBinderBean = serviceTransfer.getIBinderFromCache(context, serviceName);
        if (cacheBinderBean != null) {
            return cacheBinderBean;
        }
        Logger.d("RemoteTransfer-->getIBinder(),start to wait");

        synchronized (lock) {

            if (null == dispatcherProxy) {
                IBinder dispatcherBinder = getIBinderFromProvider();
                if (null != dispatcherBinder) {
                    dispatcherProxy = IDispatcher.Stub.asInterface(dispatcherBinder);
                    registerCurrentTransfer();
                }
            }

            //停靠等待的这种情况是可以不用注册的，因为说明sendRegisterInfo()成功了
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
        if (serviceTransfer == null||dispatcherProxy==null) {
            return null;
        }
        return serviceTransfer.getAndSaveIBinder(serviceName, dispatcherProxy);
    }

    private void registerCurrentTransfer(){
        try {
            dispatcherProxy.registerRemoteTransfer(android.os.Process.myPid(), this.asBinder());
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    private IBinder getIBinderFromProvider() {
        Logger.d("RemoteTransfer-->getIBinderFromProvider()");
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(DispatcherProvider.URI, DispatcherProvider.PROJECTION_MAIN,
                    null, null, null);
            if (cursor == null) {
                return null;
            }
            return DispatcherCursor.stripBinder(cursor);
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    //注意这个和registerRemoteService的区别，这里其实只是register本进程中有IPC能力的接口,它的名字其实叫registerStubService更合适
    @Override
    public synchronized void registerStubService(String serviceCanonicalName, IBinder stubBinder) {
        serviceTransfer.registerStubService(serviceCanonicalName, stubBinder, context, dispatcherProxy, this);
    }

    /**
     * 要注销本进程的某个服务,注意它与unregisterRemoteService()的区别!
     * 这个方法表示要注销本进程的某个服务
     *
     * @param serviceCanonicalName
     */
    @Override
    public synchronized void unregisterStubService(String serviceCanonicalName) {
        serviceTransfer.unregisterStubService(serviceCanonicalName, context, dispatcherProxy);
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
    public synchronized void publish(Event event) {
        eventTransfer.publish(event, dispatcherProxy, this, context);
    }

    ////////////////end of event///////////////////////////

    @Override
    public void registerDispatcher(IBinder dispatcherBinder) throws RemoteException {
        Logger.d("RemoteTransfer-->registerDispatcher");
        //一般从发出注册信息到这里回调就6ms左右，所以绝大部分时候走的都是这个逻辑。
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

    /**
     * 接收到来自Dispatcher的通知，如果本地有相应的IBinder,就要清除
     *
     * @param serviceCanonicalName
     * @throws RemoteException
     */
    @Override
    public void unregisterRemoteService(String serviceCanonicalName) throws RemoteException {
        Logger.d("RemoteTransfer-->unregisterRemoteService,pid:" + android.os.Process.myPid() + ",serviceName:" + serviceCanonicalName);
        serviceTransfer.clearRemoteBinderCache(serviceCanonicalName);
    }

    @Override
    public void notify(Event event) throws RemoteException {
        eventTransfer.notify(event);
    }

}

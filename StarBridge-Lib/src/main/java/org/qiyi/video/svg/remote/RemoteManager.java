package org.qiyi.video.svg.remote;

import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;

import org.qiyi.video.svg.bean.BinderBean;
import org.qiyi.video.svg.life.Lifecycle;
import org.qiyi.video.svg.life.LifecycleListener;
import org.qiyi.video.svg.log.Logger;
import org.qiyi.video.svg.transfer.RemoteTransfer;
import org.qiyi.video.svg.utils.Utils;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by wangallen on 2018/3/26.
 */

public class RemoteManager implements IRemoteManager, LifecycleListener {

    private Lifecycle lifecycle;
    private IRemoteManagerTreeNode treeNode;

    private Handler handler = new Handler(Looper.getMainLooper());

    private Context appContext;

    private Set<String> commuStubServiceNames = new HashSet<>();

    public RemoteManager(Context context, final Lifecycle lifecycle, IRemoteManagerTreeNode treeNode) {
        this.appContext = context;
        this.lifecycle = lifecycle;
        this.treeNode = treeNode;

        if (Utils.isOnBackgroundThread()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    lifecycle.addListener(RemoteManager.this);
                }
            });
        } else {
            lifecycle.addListener(this);
        }

    }

    @Override
    public IBinder getRemoteService(Class<?> serviceClass) {
        if (null == serviceClass) {
            return null;
        }
        return getRemoteService(serviceClass.getCanonicalName());
    }

    //TODO 是在这里调用onStart()好呢？还是说像Glide那样依照Fragment的onStart()来呢?
    //TODO 还是要在这里调用onStart()比较好，因为Fragment或者Activity进行onStart()时，不一定要获取远程服务，此时
    @Override
    public synchronized IBinder getRemoteService(String serviceCanonicalName) {
        Logger.d(this.toString() + "-->getRemoteService,serviceName:" + serviceCanonicalName);
        if (TextUtils.isEmpty(serviceCanonicalName)) {
            return null;
        }
        BinderBean binderBean = RemoteTransfer.getInstance().getRemoteServiceBean(serviceCanonicalName);
        String commuStubServiceName = ConnectionManager.getInstance().bindAction(appContext, binderBean.getProcessName());
        commuStubServiceNames.add(commuStubServiceName);
        return binderBean.getBinder();
    }

    @Override
    public void onStart() {
        Logger.d(this.toString() + "-->onStart()");
    }

    @Override
    public void onStop() {
        Logger.d(this.toString() + "-->onStop()");
    }

    @Override
    public void onDestroy() {
        //TODO unbindAction
        Logger.d(this.toString() + "-->onDestroy()");
        ConnectionManager.getInstance().unbindAction(appContext, commuStubServiceNames);
    }
}

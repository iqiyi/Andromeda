package wang.imallen.blog.servicemanager;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.qiyi.video.starbridge_annotations.local.LBind;
import org.qiyi.video.starbridge_annotations.local.LRegister;
import org.qiyi.video.svg.IPCCallback;
import org.qiyi.video.svg.ServiceRouter;
import org.qiyi.video.svg.callback.BaseCallback;
import org.qiyi.video.svg.event.Event;
import org.qiyi.video.svg.event.EventListener;
import org.qiyi.video.svg.log.Logger;

import wang.imallen.blog.applemodule.CheckAppleImpl;
import wang.imallen.blog.applemodule.EventActivity;
import wang.imallen.blog.applemodule.LocalServiceDemo;
import wang.imallen.blog.applemodule.RemoteServiceDemo;
import wang.imallen.blog.moduleexportlib.apple.IBuyApple;
import wang.imallen.blog.moduleexportlib.apple.ICheckApple;
import wang.imallen.blog.moduleexportlib.event.EventConstants;

public class MainActivity extends AppCompatActivity implements EventListener {

    private static final String TAG = "ServiceRouter";

    @LBind(ICheckPear.class)
    private ICheckPear checkPear = new CheckPearImpl();

    @LBind(ICheckApple.class)
    private ICheckApple checkApple;

    /*
    @LBind
    private ICheckApple checkApple2;


    @LBind
    private ICheckApple checkApple3;
    */

    /*
    //为了测试重载方法
    private class Apple {

        @LRegister({ICheckApple.class, ICheckPear.class})
        String getDesc(String userId) {
            return "This is an apple";
        }

        @LRegister({ICheckApple.class, ICheckPear.class})
        String getDesc() {
            return "Big Apple";
        }

        @LRegister(ICheckApple.class)
        int getWeight(String userId) {
            return 10;
        }

        @LRegister(ICheckApple.class)
        int getWeight(String userId, int money) {
            return 10 * money;
        }

    }
    */
    //TODO 为什么@LRegister在@Override之下时，Processor就采集不到?
    @LRegister(ICheckApple.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkApple= CheckAppleImpl.getInstance();

        findViewById(R.id.showLocalServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LocalServiceDemo.class));
            }
        });

        findViewById(R.id.showRemoteServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RemoteServiceDemo.class));
            }
        });

        findViewById(R.id.subscribeEventBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //订阅事件
                ServiceRouter.getInstance().subscribe(EventConstants.APPLE_EVENT, MainActivity.this);
            }
        });

        findViewById(R.id.publishEventBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("Result", "gave u five apples!");
                ServiceRouter.getInstance().publish(new Event(EventConstants.APPLE_EVENT, bundle));
            }
        });

        findViewById(R.id.gotoEventBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EventActivity.class));
            }
        });

        findViewById(R.id.unsubscribeEventBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //取消订阅
                ServiceRouter.getInstance().unsubscribe(MainActivity.this);
            }
        });

    }

    @LRegister(ICheckPear.class)
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onNotify(Event event) {
        String name = event.getName();
        Logger.d("MainActivity-->event name:" + name);
        if (event.getData() == null) {
            return;
        }
        String result = event.getData().getString("Result");
        Logger.d("MainActivity-->event result:" + result);
    }

    //使用方式一:只要实现BaseCallback这个抽象类即可，在主线程回调
    private void useBuyAppleService() {
        IBuyApple buyApple = IBuyApple.Stub.asInterface(ServiceRouter.getInstance().getRemoteService(IBuyApple.class));
        try {
            //buyApple.buyApple(10, new MyCallback());
            buyApple.buyAppleOnNet(10, new BaseCallback() {
                @Override
                public void onSucceed(Bundle result) {
                    Log.d(TAG, "BuyApple-->onSuccess,thread:" + Thread.currentThread().getName() + ",result:" + result.getInt("Result"));
                }

                @Override
                public void onFailed(String reason) {
                    Log.d(TAG, "BuyApple-->onFail,thread:" + Thread.currentThread().getName() + ",reason:" + reason);
                }
            });

        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    //使用方式二:自己去继承IPCCallback.Stub，在Binder线程回调
    private class MyCallback extends IPCCallback.Stub {
        @Override
        public void onSuccess(Bundle result) throws RemoteException {
            Log.d(TAG, "BuyApple-->onSuccess,result:" + result.getInt("Result"));
        }

        @Override
        public void onFail(String reason) throws RemoteException {
            Log.d(TAG, "BuyApple-->onFail,reason:" + reason);
        }
    }


}

package wang.imallen.blog.servicemanager;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.qiyi.video.svg.Andromeda;
import org.qiyi.video.svg.IPCCallback;
import org.qiyi.video.svg.callback.BaseCallback;
import org.qiyi.video.svg.event.Event;
import org.qiyi.video.svg.event.EventListener;
import org.qiyi.video.svg.log.Logger;

import wang.imallen.blog.applemodule.event.EventActivity;
import wang.imallen.blog.applemodule.local.LocalServiceDemo;
import wang.imallen.blog.applemodule.remote.RemoteServiceDemo;
import wang.imallen.blog.cherrymodule.CherryActivity;
import wang.imallen.blog.moduleexportlib.apple.IBuyApple;
import wang.imallen.blog.moduleexportlib.event.EventConstants;
import wang.imallen.blog.servicemanager.annotation.local.RegLocalServiceByAnnoActivity;
import wang.imallen.blog.servicemanager.annotation.remote.RegRemoteServiceByAnnoActivity;
import wang.imallen.blog.servicemanager.lifecycle.LifecycleTestActivity;

public class MainActivity extends AppCompatActivity implements EventListener {

    private static final String TAG = "Andromeda";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.showLocalServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LocalServiceDemo.class));
            }
        });

        findViewById(R.id.showLocalServiceByAnno).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegLocalServiceByAnnoActivity.class));
            }
        });

        findViewById(R.id.showRemoteServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RemoteServiceDemo.class));
            }
        });

        findViewById(R.id.showRemoteServiceByAnno).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegRemoteServiceByAnnoActivity.class));
            }
        });

        findViewById(R.id.showServiceInOtherModule).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CherryActivity.class));
            }
        });

        findViewById(R.id.subscribeEventBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //订阅事件
                Andromeda.getInstance().subscribe(EventConstants.APPLE_EVENT, MainActivity.this);
            }
        });

        findViewById(R.id.publishEventBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("Result", "gave u five apples!");
                Andromeda.getInstance().publish(new Event(EventConstants.APPLE_EVENT, bundle));
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
                Andromeda.getInstance().unsubscribe(MainActivity.this);
            }
        });


        findViewById(R.id.lifecycleTestBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LifecycleTestActivity.class));
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onNotify(Event event) {
        String name = event.getName();
        Toast.makeText(this, "get event whose name is " + name, Toast.LENGTH_SHORT).show();
        Logger.d("MainActivity-->event name:" + name);
        if (event.getData() == null) {
            return;
        }
        String result = event.getData().getString("Result");
        Logger.d("MainActivity-->event result:" + result);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //使用方式一:只要实现BaseCallback这个抽象类即可，在主线程回调
    private void useBuyAppleService() {
        //IBuyApple buyApple = IBuyApple.Stub.asInterface(Andromeda.getInstance().getRemoteService(IBuyApple.class));
        IBuyApple buyApple = IBuyApple.Stub.asInterface(Andromeda.with(this).getRemoteService(IBuyApple.class));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

package wang.imallen.blog.servicemanager;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.qiyi.video.svg.IPCCallback;
import org.qiyi.video.svg.ServiceManager;
import org.qiyi.video.svg.callback.BaseCallback;

import wang.imallen.blog.applemodule.LocalServiceDemo;
import wang.imallen.blog.applemodule.RemoteServiceDemo;
import wang.imallen.blog.moduleexportlib.apple.IBuyApple;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ServiceManager";

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

        findViewById(R.id.showRemoteServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RemoteServiceDemo.class));
            }
        });

    }

    //使用方式一:只要实现BaseCallback这个抽象类即可，在主线程回调
    private void useBuyAppleService() {
        IBuyApple buyApple = IBuyApple.Stub.asInterface(ServiceManager.getInstance().getRemoteService(IBuyApple.class));
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

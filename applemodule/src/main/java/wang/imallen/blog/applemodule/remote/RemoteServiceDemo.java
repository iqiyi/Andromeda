package wang.imallen.blog.applemodule.remote;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import org.qiyi.video.svg.StarBridge;
import org.qiyi.video.svg.callback.BaseCallback;

import wang.imallen.blog.applemodule.service.BuyAppleImpl;
import wang.imallen.blog.applemodule.R;
import wang.imallen.blog.moduleexportlib.apple.IBuyApple;

public class RemoteServiceDemo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apple);

        findViewById(R.id.registerRemoteServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StarBridge.getInstance().registerRemoteService(IBuyApple.class, BuyAppleImpl.getInstance());
                Toast.makeText(RemoteServiceDemo.this, "just registered remote service for IBuyApple interface", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.useRemoteInSameProcessBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useRemoteServiceInSameProcess();
            }
        });

        findViewById(R.id.unregisterRemoteServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StarBridge.getInstance().unregisterRemoteService(IBuyApple.class);
                Toast.makeText(RemoteServiceDemo.this, "just unregistered remote service for IBuyApple interface", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.useRemoteInOtherProcessBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RemoteServiceDemo.this, BananaActivity.class));
            }
        });

    }

    /**
     * 在同一个进程中使用远程服务，需要注意的是:
     * 1.本地服务只能在本进程使用
     * 2.远程服务既可以在本进程使用也可在其他进程中使用，当在本进程使用时会
     */
    private void useRemoteServiceInSameProcess() {
        IBinder buyAppleBinder = StarBridge.getInstance().getRemoteService(IBuyApple.class);
        if (null == buyAppleBinder) {
            Toast.makeText(RemoteServiceDemo.this, "buyAppleBinder is null! May be the service has been cancelled!", Toast.LENGTH_SHORT).show();
            return;
        }
        IBuyApple buyApple = IBuyApple.Stub.asInterface(buyAppleBinder);
        if (null != buyApple) {
            try {
                int appleNum = buyApple.buyAppleInShop(10);
                Toast.makeText(RemoteServiceDemo.this, "got remote service in the same process(:apple),appleNum:" + appleNum, Toast.LENGTH_SHORT).show();

                buyApple.buyAppleOnNet(10, new BaseCallback() {
                    @Override
                    public void onSucceed(Bundle result) {
                        int appleNum = result.getInt("Result", 0);
                        Toast.makeText(RemoteServiceDemo.this,
                                "got remote service with callback in the same process(:apple),appleNum:" + appleNum, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(String reason) {
                        Toast.makeText(RemoteServiceDemo.this, "got remote service failed with callback!", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }


}

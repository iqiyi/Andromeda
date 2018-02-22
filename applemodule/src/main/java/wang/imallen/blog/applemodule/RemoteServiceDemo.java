package wang.imallen.blog.applemodule;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import org.qiyi.video.starbridge_annotations.local.LBind;
import org.qiyi.video.starbridge_annotations.remote.RBind;
import org.qiyi.video.starbridge_annotations.remote.RRegister;
import org.qiyi.video.svg.ServiceRouter;
import org.qiyi.video.svg.callback.BaseCallback;
import org.qiyi.video.svg.event.Event;

import wang.imallen.blog.moduleexportlib.apple.IBuyApple;
import wang.imallen.blog.moduleexportlib.event.EventConstants;

public class RemoteServiceDemo extends AppCompatActivity {

    //TODO 这样看的话，是不是LBind的值也可以为空，如果它为空的话就默认是它声明的那个接口
    @RBind(IBuyApple.class)
    private IBuyApple buyApple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apple);

        findViewById(R.id.registerRemoteServiceBtn).setOnClickListener(new View.OnClickListener() {

            @RRegister(services = IBuyApple.class)
            @Override
            public void onClick(View v) {
                //ServiceRouter.getInstance().registerRemoteService(IBuyApple.class, BuyAppleImpl.getInstance().asBinder());
                //在这里完成buyApple的实例化
                buyApple=BuyAppleImpl.getInstance();

                Toast.makeText(RemoteServiceDemo.this, "just registered remote service for IBuyApple interface", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.useRemoteInSameProcessBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useRemoteServiceInSameProcess();
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
        IBinder buyAppleBinder = ServiceRouter.getInstance().getRemoteService(IBuyApple.class);
        if (null == buyAppleBinder) {
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

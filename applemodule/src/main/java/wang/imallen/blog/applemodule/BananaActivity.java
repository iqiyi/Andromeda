package wang.imallen.blog.applemodule;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import org.qiyi.video.svg.ServiceRouter;
import org.qiyi.video.svg.callback.BaseCallback;
import org.qiyi.video.svg.log.Logger;

import wang.imallen.blog.moduleexportlib.apple.IBuyApple;

public class BananaActivity extends AppCompatActivity {

    //TODO 目前的方案是服务提供方需要3个注解:
    //TODO  第一个注解是加在服务的实现类上
    //TODO 第二个注解是加在变量的声明上
    //TODO 第三个注解是加在变量具体要注册的方法里，比如Application的onCreate()中
    //TODO 不过这是针对最复杂的情况，对于更简单的情形，比如直接通过默认构造方法或者带一个Context的方法就可以生成对象，并且在Application注入的，要做到只需要一个注解即可完成注入。

    //TODO 在注入点加上参数信息，要区分方法参数和类的成员参数
    //TODO 或者就是通过反射来获取对象，只需要让业务方告诉我们变量名
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banana);

        findViewById(R.id.useRemoteServiceBtn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useBuyAppleInShop();
            }
        });

        findViewById(R.id.useRemoteServiceBtn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useBuyAppleOnNet();
            }
        });

    }

    private void useBuyAppleInShop() {
        IBinder buyAppleBinder = ServiceRouter.getInstance().getRemoteService(IBuyApple.class);
        if (null == buyAppleBinder) {
            return;
        }
        IBuyApple buyApple = IBuyApple.Stub.asInterface(buyAppleBinder);
        if (null != buyApple) {
            try {
                int appleNum = buyApple.buyAppleInShop(10);
                Toast.makeText(BananaActivity.this, "got remote service in other process(:banana),appleNum:" + appleNum, Toast.LENGTH_SHORT).show();

            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void useBuyAppleOnNet() {
        IBinder buyAppleBinder = ServiceRouter.getInstance().getRemoteService(IBuyApple.class);
        if (null == buyAppleBinder) {
            return;
        }
        IBuyApple buyApple = IBuyApple.Stub.asInterface(buyAppleBinder);
        if (null != buyApple) {
            try {
                buyApple.buyAppleOnNet(10, new BaseCallback() {
                    @Override
                    public void onSucceed(Bundle result) {
                        int appleNum = result.getInt("Result", 0);
                        Logger.d("got remote service with callback in other process(:banana),appleNum:" + appleNum);
                        Toast.makeText(BananaActivity.this,
                                "got remote service with callback in other process(:banana),appleNum:" + appleNum, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(String reason) {
                        Logger.e("buyAppleOnNet failed,reason:" + reason);
                        Toast.makeText(BananaActivity.this, "got remote service failed with callback!", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }
}

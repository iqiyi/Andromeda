package wang.imallen.blog.applemodule;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import org.qiyi.video.svg.ServiceManager;
import org.qiyi.video.svg.callback.BaseCallback;
import org.qiyi.video.svg.log.Logger;

import wang.imallen.blog.moduleexportlib.apple.IBuyApple;

public class BananaActivity extends AppCompatActivity {

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
        IBinder buyAppleBinder = ServiceManager.getInstance().getRemoteService(IBuyApple.class);
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
        IBinder buyAppleBinder = ServiceManager.getInstance().getRemoteService(IBuyApple.class);
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

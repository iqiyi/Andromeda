package wang.imallen.blog.cherrymodule;

import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.qiyi.video.svg.StarBridge;
import org.qiyi.video.svg.callback.BaseCallback;

import wang.imallen.blog.moduleexportlib.apple.IBuyApple;
import wang.imallen.blog.moduleexportlib.apple.ICheckApple;

public class CherryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cherry);


        findViewById(R.id.useLocalServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useLocalService();
            }
        });

        findViewById(R.id.useRemoteServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useRemoteService();
            }
        });

    }

    private void useLocalService(){
        ICheckApple checkApple = StarBridge.getInstance().getLocalService(ICheckApple.class);
        if (checkApple != null) {
            int calories = checkApple.getAppleCalories(3);
            String desc = checkApple.getAppleDescription(2);
            Toast.makeText(this,
                    "got ICheckApple service,calories:" + calories + ",description:" + desc, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "found no ICheckApple service, it may be cancelled!", Toast.LENGTH_SHORT).show();
        }
    }

    private void useRemoteService(){
        //IBinder buyAppleBinder = StarBridge.getInstance().getRemoteService(IBuyApple.class);
        IBinder buyAppleBinder=StarBridge.with(this).getRemoteService(IBuyApple.class);
        if (null == buyAppleBinder) {
            Toast.makeText(this, "buyAppleBinder is null! Maybe the service has not been registered or been cancelled!", Toast.LENGTH_SHORT).show();
            return;
        }
        IBuyApple buyApple = IBuyApple.Stub.asInterface(buyAppleBinder);
        if (null != buyApple) {
            try {
                int appleNum = buyApple.buyAppleInShop(10);
                Toast.makeText(this, "got remote service in the same process(:apple),appleNum:" + appleNum, Toast.LENGTH_SHORT).show();

                buyApple.buyAppleOnNet(10, new BaseCallback() {
                    @Override
                    public void onSucceed(Bundle result) {
                        int appleNum = result.getInt("Result", 0);
                        Toast.makeText(CherryActivity.this,
                                "got remote service with callback in the same process(:apple),appleNum:" + appleNum, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(String reason) {
                        Toast.makeText(CherryActivity.this, "got remote service failed with callback!", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }
}

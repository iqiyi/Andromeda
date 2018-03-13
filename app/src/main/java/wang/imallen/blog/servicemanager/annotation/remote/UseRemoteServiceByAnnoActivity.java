package wang.imallen.blog.servicemanager.annotation.remote;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import org.qiyi.video.starbridge_annotations.remote.RGet;
import org.qiyi.video.starbridge_annotations.remote.RInject;
import org.qiyi.video.svg.callback.BaseCallback;

import wang.imallen.blog.moduleexportlib.apple.IBuyApple;
import wang.imallen.blog.servicemanager.R;

/**
 * 展示通过注解完成远程服务的注入过程
 */
public class UseRemoteServiceByAnnoActivity extends AppCompatActivity {

    @RInject
    private IBuyApple buyAppleService;

    @RGet(IBuyApple.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_use_remote_service_by_anno);

        findViewById(R.id.useBuyAppleServiceBtn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyAppleInShop();
            }
        });

        findViewById(R.id.useBuyAppleServiceBtn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyAppleOnNet();
            }
        });
    }

    private void buyAppleInShop() {
        if (buyAppleService != null) {
            try {
                int appleNum = buyAppleService.buyAppleInShop(10);
                Toast.makeText(this, "got remote service in the process(:tea),appleNum:" + appleNum, Toast.LENGTH_SHORT).show();

            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void buyAppleOnNet() {
        if (buyAppleService != null) {
            try {
                buyAppleService.buyAppleOnNet(10, new BaseCallback() {
                    @Override
                    public void onSucceed(Bundle result) {
                        int appleNum = result.getInt("Result", 0);
                        Toast.makeText(UseRemoteServiceByAnnoActivity.this,
                                "got remote service with callback in process(:tea),appleNum:" + appleNum, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(String reason) {
                        Toast.makeText(UseRemoteServiceByAnnoActivity.this, "got remote service failed with callback!", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }


}

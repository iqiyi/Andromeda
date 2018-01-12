package wang.imallen.blog.servicemanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import wang.imallen.blog.moduleexportlib.apple.IDeliverApple;
import wang.imallen.blog.moduleexportlib.apple.IEatApple;
import org.qiyi.video.svg.ServiceManager;
import org.qiyi.video.svg.config.Constants;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ServiceManager";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        findViewById(R.id.gotoAppleActivityBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,AppleActivity.class));
            }
        });

    }

    private void useLocalService() {
        IEatApple eatApple =  ServiceManager.getInstance().getLocalService(Constants.APPLE_MODULE);
        if (eatApple != null) {
            eatApple.eatApple(23);
        }else{
            Log.d(TAG,"IEatApple service is null");
        }
    }

    private void useRemoteService() {
        IDeliverApple deliverApple = (IDeliverApple) ServiceManager.getInstance().getRemoteService(IDeliverApple.class.getCanonicalName(),this);
        if (deliverApple != null) {
            int appleNum = deliverApple.getApple(10);
            Log.d(TAG, "getApple() result:" + appleNum);
        }else{
            Log.d(TAG,"proxy is null!");
        }
    }

}

package wang.imallen.blog.servicemanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import wang.imallen.blog.moduleexportlib.apple.IDeliverApple;
import wang.imallen.blog.moduleexportlib.apple.IEatApple;
import wang.imallen.blog.servicemanagerlib.ServiceManager;
import wang.imallen.blog.servicemanagerlib.config.Constants;

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

    }

    private void useLocalService() {
        IEatApple eatApple = (IEatApple) ServiceManager.getInstance().getLocalService(Constants.APPLE_MODULE);
        if (eatApple != null) {
            eatApple.eatApple(23);
        }else{
            Log.d(TAG,"IEatApple service is null");
        }
    }

    private void useRemoteService() {
        IDeliverApple deliverApple = (IDeliverApple) ServiceManager.getInstance().getRemoteService(Constants.APPLE_MODULE,this);
        if (deliverApple != null) {
            int appleNum = deliverApple.getApple(10);
            Log.d(TAG, "getApple() result:" + appleNum);
        }
    }

}

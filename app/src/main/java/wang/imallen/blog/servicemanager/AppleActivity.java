package wang.imallen.blog.servicemanager;

import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.qiyi.video.svg.ServiceManager;

import wang.imallen.blog.applemodule.DeliverAppleNative;
import wang.imallen.blog.moduleexportlib.apple.DeliverAppleStub;
import wang.imallen.blog.moduleexportlib.apple.IDeliverApple;

public class AppleActivity extends AppCompatActivity {

    private static final String TAG = "ServiceManager";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apple);

        findViewById(R.id.registerStubServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServiceManager.getInstance().registerRemoteService(IDeliverApple.class, new DeliverAppleNative());
            }
        });

        findViewById(R.id.useRemoteServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useRemoteService();
            }
        });
    }


    private void useRemoteService() {
        //IDeliverApple deliverApple = (IDeliverApple) ServiceManager.getInstance().getRemoteService(Constants.APPLE_MODULE, this);
        IBinder deliverAppleBinder = ServiceManager.getInstance().getRemoteService(IDeliverApple.class.getCanonicalName());
        IDeliverApple deliverApple = DeliverAppleStub.asInterface(deliverAppleBinder);
        if (deliverApple != null) {
            int appleNum = deliverApple.getApple(10);
            Log.d(TAG, "getApple() result:" + appleNum);
        }
    }

}

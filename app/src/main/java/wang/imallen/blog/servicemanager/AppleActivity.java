package wang.imallen.blog.servicemanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.qiyi.video.svg.ServiceManager;
import org.qiyi.video.svg.config.Constants;

import wang.imallen.blog.moduleexportlib.apple.IDeliverApple;

public class AppleActivity extends AppCompatActivity {

    private static final String TAG = "ServiceManager";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apple);

        findViewById(R.id.useRemoteServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useRemoteService();
            }
        });
    }

    private void useRemoteService() {
        IDeliverApple deliverApple = (IDeliverApple) ServiceManager.getInstance().getRemoteService(Constants.APPLE_MODULE, this);
        if (deliverApple != null) {
            int appleNum = deliverApple.getApple(10);
            Log.d(TAG, "getApple() result:" + appleNum);
        }
    }

}

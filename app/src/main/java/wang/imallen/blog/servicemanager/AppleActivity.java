package wang.imallen.blog.servicemanager;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.qiyi.video.svg.IPCCallback;
import org.qiyi.video.svg.ServiceManager;

import wang.imallen.blog.applemodule.DeliverAppleNative;
import wang.imallen.blog.moduleexportlib.apple.DeliverAppleStub;
import wang.imallen.blog.moduleexportlib.apple.IBuyApple;
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

        findViewById(R.id.registerBuyAppleServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServiceManager.getInstance().registerRemoteService(IBuyApple.class, new BuyAppleNative());
            }
        });

        findViewById(R.id.useRemoteServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useRemoteService();
            }
        });


    }

    private class BuyAppleNative extends IBuyApple.Stub {

        @Override
        public void buyApple(int userId, IPCCallback callback) throws RemoteException {
            Log.d(TAG, "BuyAppleNative-->buyApple,userId:" + userId);
            Bundle result = new Bundle();
            if (userId == 10) {
                result.putInt("Result", 20);
                callback.onSuccess(result);
            } else if (userId == 20) {
                result.putInt("Result", 30);
                callback.onSuccess(result);
            } else {
                callback.onFail("Sorry, u are not authorized!");
            }
        }
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

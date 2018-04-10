package wang.imallen.blog.servicemanager.lifecycle.frag;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.qiyi.video.svg.StarBridge;
import org.qiyi.video.svg.log.Logger;

import wang.imallen.blog.moduleexportlib.apple.IBuyApple;
import wang.imallen.blog.servicemanager.R;

public class SupportFragActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frag);

        findViewById(R.id.showSupportFragBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSupportFrag();
            }
        });

        findViewById(R.id.useRemoteServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useBuyAppleService();
            }
        });

    }

    private void useBuyAppleService() {
        IBinder binder = StarBridge.with(this).getRemoteService(IBuyApple.class);
        if (binder == null) {
            return;
        }
        IBuyApple buyApple = IBuyApple.Stub.asInterface(binder);
        if (buyApple == null) {
            return;
        }
        try {
            buyApple.buyAppleInShop(29);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d("SupportFragActivity-->onDestroy()");
    }

    private void showSupportFrag() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,
                CustomSupportFragment.newInstance()).commitAllowingStateLoss();
    }
}

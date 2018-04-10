package wang.imallen.blog.servicemanager.lifecycle.frag;

import android.app.Activity;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;

import org.qiyi.video.svg.StarBridge;
import org.qiyi.video.svg.log.Logger;

import wang.imallen.blog.moduleexportlib.apple.IBuyApple;
import wang.imallen.blog.servicemanager.R;

public class FragActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_frag);

        //TODO 这样做是有问题的，在FragmentActivity中只能管理support v4 Fragment，而不能管理android.app.Fragment
        findViewById(R.id.showFragBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFrag();
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

    private void showFrag() {
        getFragmentManager().beginTransaction().replace(R.id.frameLayout,
                CustomFragment.newInstance()).commitAllowingStateLoss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d("FragActivity-->onDestroy()");
    }

}

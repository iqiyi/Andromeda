package wang.imallen.blog.servicemanager.lifecycle;

import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.qiyi.video.svg.Andromeda;

import wang.imallen.blog.moduleexportlib.apple.IBuyApple;
import wang.imallen.blog.servicemanager.R;

public class ViewTestActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_test);

        textView=findViewById(R.id.textView);

        useBuyAppleService();
    }

    private void useBuyAppleService() {
        IBinder binder = Andromeda.with(textView).getRemoteService(IBuyApple.class);
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
}

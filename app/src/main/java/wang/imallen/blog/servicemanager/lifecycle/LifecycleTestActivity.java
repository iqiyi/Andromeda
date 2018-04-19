package wang.imallen.blog.servicemanager.lifecycle;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.qiyi.video.svg.Andromeda;

import wang.imallen.blog.moduleexportlib.apple.IBuyApple;
import wang.imallen.blog.servicemanager.R;
import wang.imallen.blog.servicemanager.lifecycle.frag.FragActivity;
import wang.imallen.blog.servicemanager.lifecycle.frag.SupportFragActivity;

public class LifecycleTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lifecycle_test);

        findViewById(R.id.fragmentTestBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LifecycleTestActivity.this, FragActivity.class));
            }
        });

        findViewById(R.id.supportFragmentTestBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LifecycleTestActivity.this, SupportFragActivity.class));
            }
        });

        findViewById(R.id.viewPagerTestBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LifecycleTestActivity.this, ViewPagerActivity.class));
            }
        });

        findViewById(R.id.fragmentActivityTestBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LifecycleTestActivity.this, SupportFragActivity.class));
            }
        });

        findViewById(R.id.activityTestBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LifecycleTestActivity.this, FragActivity.class));
            }
        });

        findViewById(R.id.contextTestBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useBuyAppleService();
            }
        });

        findViewById(R.id.viewTestBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LifecycleTestActivity.this, ViewTestActivity.class));
            }
        });
    }

    private void useBuyAppleService() {
        IBinder binder = Andromeda.with(this.getApplicationContext()).getRemoteService(IBuyApple.class);
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

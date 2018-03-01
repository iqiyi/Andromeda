package wang.imallen.blog.applemodule;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.qiyi.video.starbridge_annotations.local.LGet;
import org.qiyi.video.starbridge_annotations.local.LInject;
import org.qiyi.video.starbridge_annotations.remote.RGet;
import org.qiyi.video.starbridge_annotations.remote.RInject;

import wang.imallen.blog.moduleexportlib.apple.IBuyApple;
import wang.imallen.blog.moduleexportlib.apple.ICheckApple;
import wang.imallen.blog.moduleexportlib.cherry.IBuyCherry;

public class InjectDemoActivity extends AppCompatActivity {

    @LInject
    private ICheckApple checkApple;

    @RInject
    private IBuyApple buyApple;

    @RInject
    private IBuyCherry buyCherry;

    @LGet({ICheckApple.class})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inject_demo);
    }

    @RGet({IBuyApple.class, IBuyCherry.class})
    @Override
    protected void onStart() {
        super.onStart();
    }
}

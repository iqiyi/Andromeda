package wang.imallen.blog.servicemanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.qiyi.video.starbridge_annotations.local.LBind;
import org.qiyi.video.starbridge_annotations.local.LRegister;
import org.qiyi.video.starbridge_annotations.local.LUnRegister;
import org.qiyi.video.starbridge_annotations.remote.RBind;
import org.qiyi.video.starbridge_annotations.remote.RRegister;

import wang.imallen.blog.moduleexportlib.apple.IBuyApple;
import wang.imallen.blog.moduleexportlib.apple.ICheckApple;
import wang.imallen.blog.moduleexportlib.cherry.IBuyCherry;

public class DetailActivity extends AppCompatActivity {

    @LBind
    private ICheckApple checkApple;

    @LBind
    private ICheckPear checkPear;

    @RBind(IBuyApple.class)
    private IBuyApple.Stub buyApple;

    @RBind(IBuyCherry.class)
    private IBuyCherry.Stub buyCherry;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
    }

    @LRegister({ICheckApple.class, ICheckPear.class})
    @Override
    protected void onStart() {
        super.onStart();
    }

    @RRegister({IBuyApple.class, IBuyCherry.class})
    @Override
    protected void onResume() {
        super.onResume();
    }

    @LUnRegister(ICheckApple.class)
    @Override
    protected void onStop() {
        super.onStop();
    }
}

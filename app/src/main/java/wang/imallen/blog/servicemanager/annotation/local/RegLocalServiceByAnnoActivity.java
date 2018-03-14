package wang.imallen.blog.servicemanager.annotation.local;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.qiyi.video.starbridge_annotations.local.LBind;
import org.qiyi.video.starbridge_annotations.local.LRegister;
import org.qiyi.video.starbridge_annotations.local.LUnRegister;

import wang.imallen.blog.applemodule.service.CheckAppleImpl;
import wang.imallen.blog.moduleexportlib.apple.ICheckApple;
import wang.imallen.blog.servicemanager.CheckPearImpl;
import wang.imallen.blog.servicemanager.ICheckPear;
import wang.imallen.blog.servicemanager.R;

/**
 * 展示通过注解完成本地服务的注册过程
 */
public class RegLocalServiceByAnnoActivity extends AppCompatActivity {


    @LBind(ICheckApple.class)
    private ICheckApple checkApple;

    @LBind
    private ICheckPear checkPear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_service_annotation_demo);
        checkPear = new CheckPearImpl();

        findViewById(R.id.goToUseBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegLocalServiceByAnnoActivity.this, UseLocalServiceByAnnoActivity.class));
            }
        });

    }

    @LRegister(ICheckApple.class)
    @Override
    protected void onStart() {
        super.onStart();
        checkApple = CheckAppleImpl.getInstance();
    }

    @LRegister(ICheckPear.class)
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @LUnRegister({ICheckApple.class, ICheckPear.class})
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

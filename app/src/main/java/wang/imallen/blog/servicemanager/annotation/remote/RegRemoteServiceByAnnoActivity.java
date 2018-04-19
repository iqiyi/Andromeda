package wang.imallen.blog.servicemanager.annotation.remote;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


import wang.imallen.blog.applemodule.service.BuyAppleImpl;
import wang.imallen.blog.moduleexportlib.apple.IBuyApple;
import wang.imallen.blog.servicemanager.R;

/**
 * 展示通过注解完成远程服务的注册过程
 */
public class RegRemoteServiceByAnnoActivity extends AppCompatActivity {

    private IBuyApple buyApple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_remote_service_by_anno);

        findViewById(R.id.goToUseBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegRemoteServiceByAnnoActivity.this, UseRemoteServiceByAnnoActivity.class));
            }
        });

        //给buyApple赋值
        buyApple = BuyAppleImpl.getInstance();
    }

    //在这里会通过代码插入完成远程服务的注册
    @Override
    protected void onStart() {
        super.onStart();
    }

    //注意:这里只是为了使用注解注销远程服务的用法，实际中要谨慎使用注销操作，因为可能导致服务使用方出错
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

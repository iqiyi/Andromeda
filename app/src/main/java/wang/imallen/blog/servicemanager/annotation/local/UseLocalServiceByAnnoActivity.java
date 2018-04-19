package wang.imallen.blog.servicemanager.annotation.local;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.qiyi.video.starbridge_annotations.local.LGet;
import org.qiyi.video.starbridge_annotations.local.LInject;

import wang.imallen.blog.moduleexportlib.apple.ICheckApple;
import wang.imallen.blog.servicemanager.ICheckPear;
import wang.imallen.blog.servicemanager.R;

/**
 * 展示通过注解完成服务的注入，比如在这里是在onCreate()中进行注入
 */
public class UseLocalServiceByAnnoActivity extends AppCompatActivity {

    @LInject
    private ICheckApple checkAppleService;

    @LInject
    private ICheckPear checkPearService;


    @LGet({ICheckApple.class,ICheckPear.class})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_use_local_service_by_anno);

        findViewById(R.id.useAppleServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAppleService != null) {
                    int calories = checkAppleService.getAppleCalories(3);
                    String desc = checkAppleService.getAppleDescription(2);
                    Toast.makeText(UseLocalServiceByAnnoActivity.this,
                            "got ICheckApple service,calories:" + calories + ",description:" + desc, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UseLocalServiceByAnnoActivity.this, "found no ICheckApple service, it may be cancelled!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.usePearServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPearService != null) {
                    int calories = checkPearService.getCalories(5);
                    String desc = checkPearService.getPearDesc(3);
                    Toast.makeText(UseLocalServiceByAnnoActivity.this,
                            "got ICheckPear service,calories:" + calories + ",description:" + desc, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UseLocalServiceByAnnoActivity.this, "found no ICheckPear service, it may be cancelled!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}

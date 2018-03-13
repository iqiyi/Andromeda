package wang.imallen.blog.servicemanager.annotation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import org.qiyi.video.starbridge_annotations.local.LBind;
import org.qiyi.video.starbridge_annotations.local.LRegister;
import org.qiyi.video.starbridge_annotations.local.LUnRegister;
import org.qiyi.video.svg.StarBridge;

import wang.imallen.blog.applemodule.CheckAppleImpl;
import wang.imallen.blog.moduleexportlib.apple.ICheckApple;
import wang.imallen.blog.servicemanager.CheckPearImpl;
import wang.imallen.blog.servicemanager.ICheckPear;
import wang.imallen.blog.servicemanager.R;

public class LocalServiceAnnotationDemo extends AppCompatActivity {


    @LBind(ICheckApple.class)
    private ICheckApple checkApple;

    @LBind
    private ICheckPear checkPear;

    //TODO 是不是采用两个Fragment来举例更好呢？就是在一个Fragment中进行注册，然后在另一个Fragment中就能使用了。
    //TODO 确实要这样，写一个实现了此UI的基类即可!
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_service_annotation_demo);
        checkPear = new CheckPearImpl();

        findViewById(R.id.useAppleServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ICheckApple appleService = StarBridge.getInstance().getLocalService(ICheckApple.class);
                if (appleService != null) {
                    int calories = appleService.getAppleCalories(3);
                    String desc = appleService.getAppleDescription(2);
                    Toast.makeText(LocalServiceAnnotationDemo.this,
                            "got ICheckApple service,calories:" + calories + ",description:" + desc, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LocalServiceAnnotationDemo.this, "found no ICheckApple service, it may be cancelled!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.usePearServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ICheckPear pearService = StarBridge.getInstance().getLocalService(ICheckPear.class.getCanonicalName());
                if (pearService != null) {
                    int calories = pearService.getCalories(5);
                    String desc = pearService.getPearDesc(3);
                    Toast.makeText(LocalServiceAnnotationDemo.this,
                            "got ICheckPear service,calories:" + calories + ",description:" + desc, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LocalServiceAnnotationDemo.this, "found no ICheckPear service, it may be cancelled!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @LRegister(ICheckApple.class)
    @Override
    protected void onStart() {
        super.onStart();
        checkApple = CheckAppleImpl.getInstance();
    }

    //TODO 为什么@LRegister在@Override之下时，Processor就采集不到?
    @LRegister(ICheckPear.class)  //TODO 为什么这里会插入失败呢?
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

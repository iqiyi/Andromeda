package wang.imallen.blog.applemodule;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import org.qiyi.video.svg.ServiceManager;

import wang.imallen.blog.moduleexportlib.apple.ICheckApple;

public class LocalServiceDemo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_service_demo);

        findViewById(R.id.registerLocalServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServiceManager.getInstance().registerLocalService(ICheckApple.class, CheckAppleImpl.getInstance());
                Toast.makeText(LocalServiceDemo.this, "registered ICheckApple service", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.useLocalServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ICheckApple checkApple = (ICheckApple) ServiceManager.getInstance().getLocalService(ICheckApple.class);
                if (checkApple != null) {
                    int calories = checkApple.getAppleCalories(3);
                    String desc = checkApple.getAppleDescription(2);
                    Toast.makeText(LocalServiceDemo.this,
                            "got ICheckApple service,calories:" + calories + ",description:" + desc, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

package wang.imallen.blog.applemodule;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import org.qiyi.video.starbridge_annotations.local.LBind;
import org.qiyi.video.starbridge_annotations.local.LGet;
import org.qiyi.video.starbridge_annotations.local.LInject;
import org.qiyi.video.starbridge_annotations.local.LRegister;
import org.qiyi.video.starbridge_annotations.local.LUnRegister;
import org.qiyi.video.svg.ServiceRouter;

import wang.imallen.blog.moduleexportlib.apple.ICheckApple;

public class LocalServiceDemo extends AppCompatActivity {

    //以接口类型声明的，LBind的值可写可不写
    /*
    //@LBind(ICheckApple.class)
    @LBind //值为空时，注解解释器会以checkApple的声明类型作为要注册的服务类，即ICheckApple.class
    private ICheckApple checkApple;
    */

    /** 此时，由于声明的类型是CheckAppleImpl而不是ICheckApple,所以LBind这个注解的值必须填写，否则注册信息会出错，从而导致运行时调用方找不到服务的情况出现
    @LBind(ICheckApple.class)
    private CheckAppleImpl checkApple;
    */

    @LBind(ICheckApple.class)
    ICheckApple myCheckApple;

    //TODO 考虑改一下注解名称，将@LInject, @LGet分别改为@Local和@LInject，那样更好理解!
    @LInject
    ICheckApple checkApple;

    //TODO 表示要在这里注入ICheckApple的实例，那是不是最好再加上一个"checkApple"呢?不然万一在几个地方都要用到呢?
    @LGet(ICheckApple.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_service_demo);

        findViewById(R.id.registerLocalServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            @LRegister(ICheckApple.class) //通过注解来完成注入
            public void onClick(View v) {

                //checkApple=CheckAppleImpl.getInstance();

                ServiceRouter.getInstance().registerLocalService(ICheckApple.class, CheckAppleImpl.getInstance());
                Toast.makeText(LocalServiceDemo.this, "registered ICheckApple service", Toast.LENGTH_SHORT).show();

                //TODO 这部分代码会通过gradle插件插入
                //ServiceRouter.getInstance().registerLocalService(ICheckApple.class,CheckAppleImpl.getInstance());
            }
        });

        findViewById(R.id.useLocalServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ICheckApple checkApple = (ICheckApple) ServiceRouter.getInstance().getLocalService(ICheckApple.class);
                if (checkApple != null) {
                    int calories = checkApple.getAppleCalories(3);
                    String desc = checkApple.getAppleDescription(2);
                    Toast.makeText(LocalServiceDemo.this,
                            "got ICheckApple service,calories:" + calories + ",description:" + desc, Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.unregisterLocalServiceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            @LUnRegister(ICheckApple.class) //通过注解完成本地服务的注销操作
            public void onClick(View v) {

                ServiceRouter.getInstance().unregisterLocalService(ICheckApple.class);
            }
        });

    }

    @LRegister(ICheckApple.class)
    @Override
    protected void onStart() {
        super.onStart();
    }


}

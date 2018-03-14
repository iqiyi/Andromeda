package wang.imallen.blog.applemodule.event;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.qiyi.video.svg.StarBridge;
import org.qiyi.video.svg.event.Event;
import org.qiyi.video.svg.event.EventListener;
import org.qiyi.video.svg.log.Logger;

import wang.imallen.blog.applemodule.R;
import wang.imallen.blog.moduleexportlib.event.EventConstants;

public class EventActivity extends AppCompatActivity implements EventListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        //测试弱引用是否起作用
        StarBridge.getInstance().subscribe(EventConstants.APPLE_EVENT, this);

        findViewById(R.id.publishEventBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("Result", "gave u five apples!");
                StarBridge.getInstance().publish(new Event(EventConstants.APPLE_EVENT, bundle));
            }
        });

    }

    @Override
    public void onNotify(Event event) {
        Logger.d("EventActivity-->onNotify,event.name:" + event.getName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d("onDestroy");
    }
}

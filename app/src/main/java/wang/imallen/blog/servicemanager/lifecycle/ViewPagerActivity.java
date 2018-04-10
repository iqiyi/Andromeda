package wang.imallen.blog.servicemanager.lifecycle;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import wang.imallen.blog.servicemanager.R;
import wang.imallen.blog.servicemanager.lifecycle.frag.CustomFragmentAdapter;
import wang.imallen.blog.servicemanager.lifecycle.frag.MyPicFrag;
import wang.imallen.blog.servicemanager.lifecycle.frag.MyTextFrag;
import wang.imallen.blog.servicemanager.lifecycle.frag.PicFrag;
import wang.imallen.blog.servicemanager.lifecycle.frag.TextFrag;

public class ViewPagerActivity extends AppCompatActivity {

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);

        viewPager=findViewById(R.id.viewPager);

        List<Fragment>fragments=new ArrayList<>();
        fragments.add(new TextFrag());
        fragments.add(new PicFrag());
        fragments.add(new MyTextFrag());
        fragments.add(new MyPicFrag());
        CustomFragmentAdapter adapter=new CustomFragmentAdapter(getSupportFragmentManager(),fragments);
        viewPager.setAdapter(adapter);

        viewPager.setOffscreenPageLimit(2);

    }
}

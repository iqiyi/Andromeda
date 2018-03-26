package wang.imallen.blog.servicemanager.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import wang.imallen.blog.servicemanager.R;

public class TestActivity1 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test1);
    }

    public static class TestActivity2 extends TestActivity1{}


    public static class TestActivity3 extends TestActivity1{}

    public static class TestActivity4 extends TestActivity1{}

    public static class TestActivity5 extends TestActivity1{}

    public static class TestActivity6 extends TestActivity1{}

}

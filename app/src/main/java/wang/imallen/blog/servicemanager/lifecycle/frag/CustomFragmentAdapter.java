package wang.imallen.blog.servicemanager.lifecycle.frag;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by wangallen on 2018/4/10.
 */

public class CustomFragmentAdapter extends FragmentPagerAdapter{

    private List<Fragment>fragments;

    public CustomFragmentAdapter(FragmentManager fm,List<Fragment>fragments){
        super(fm);
        this.fragments=fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments==null?null:fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments==null?0:fragments.size();
    }
}

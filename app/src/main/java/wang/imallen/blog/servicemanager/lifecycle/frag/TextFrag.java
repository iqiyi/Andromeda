package wang.imallen.blog.servicemanager.lifecycle.frag;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.qiyi.video.svg.log.Logger;

import wang.imallen.blog.servicemanager.R;

/**
 * Created by wangallen on 2018/4/10.
 */

public class TextFrag extends Fragment{

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_layout, container, false);

        TextView textView = rootView.findViewById(R.id.textView);
        textView.setText("This is a TextView in TextFrag");

        return rootView;
    }

    @Override
    public void onStart() {
        Logger.d("TextFrag-->onStart()");
        super.onStart();
    }

    @Override
    public void onResume() {
        Logger.d("TextFrag-->onResume()");
        super.onResume();
    }

    @Override
    public void onPause() {
        Logger.d("TextFrag-->onPause()");
        super.onPause();
    }

    @Override
    public void onStop() {
        Logger.d("TextFrag-->onStop()");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Logger.d("TextFrag-->onDestroyView()");
        super.onDestroyView();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        Logger.d("TextFrag-->setuserVisibleHint()");
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Logger.d("TextFrag-->onHiddenChanged()");
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroy() {
        Logger.d("TextFrag-->onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}

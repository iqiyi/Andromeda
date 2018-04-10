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

public class MyTextFrag extends Fragment {

    private static final String TAG = "MyTextFrag";

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
        textView.setText("This is a TextView in MyTextFrag");

        return rootView;
    }

    @Override
    public void onStart() {
        Logger.d(TAG + "-->onStart()");
        super.onStart();
    }

    @Override
    public void onResume() {
        Logger.d(TAG + "-->onResume()");
        super.onResume();
    }

    @Override
    public void onPause() {
        Logger.d(TAG + "-->onPause()");
        super.onPause();
    }

    @Override
    public void onStop() {
        Logger.d(TAG + "-->onStop()");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Logger.d(TAG + "-->onDestroyView()");
        super.onDestroyView();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        Logger.d(TAG + "-->setuserVisibleHint()");
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Logger.d(TAG + "-->onHiddenChanged()");
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroy() {
        Logger.d(TAG + "-->onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}

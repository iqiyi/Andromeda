package wang.imallen.blog.servicemanager.lifecycle.frag;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.qiyi.video.svg.StarBridge;
import org.qiyi.video.svg.log.Logger;

import wang.imallen.blog.moduleexportlib.apple.IBuyApple;
import wang.imallen.blog.servicemanager.R;

/**
 * Created by wangallen on 2018/4/9.
 */

public class CustomSupportFragment extends Fragment{

    public static CustomSupportFragment newInstance(){
        return new CustomSupportFragment();
    }

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
        textView.setText("This is a TextView in CustomSupportFragment");

        //KP 此时当前Fragment还不可见，这就是RemoteManager在初始时会被调用一次onStop()的原因.
        //KP 而到后面创建的root fragment,是由activity的可见性来决定的，而Activity此时已经可见，所以它的lifecycle会调用onStart();
        //useBuyAppleService();

        return rootView;
    }

    private void useBuyAppleService() {
        IBinder binder = StarBridge.with(this).getRemoteService(IBuyApple.class);
        if (binder == null) {
            return;
        }
        IBuyApple buyApple = IBuyApple.Stub.asInterface(binder);
        if (buyApple == null) {
            return;
        }
        try {
            buyApple.buyAppleInShop(29);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public void onStart() {
        Logger.d("CustomSupportFragment-->onStart()");
        super.onStart();
    }

    @Override
    public void onResume() {
        useBuyAppleService();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}

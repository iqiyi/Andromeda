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
import android.widget.ImageView;

import org.qiyi.video.svg.StarBridge;
import org.qiyi.video.svg.log.Logger;

import wang.imallen.blog.moduleexportlib.apple.IBuyApple;
import wang.imallen.blog.servicemanager.R;

/**
 * Created by wangallen on 2018/4/10.
 */

public class MyPicFrag extends Fragment{

    private static final String TAG = "MyPicFrag";

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
        View rootView = inflater.inflate(R.layout.pic_layout, container, false);

        ImageView imageView=rootView.findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.ghoul);

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
        useBuyAppleService();
        super.onResume();
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
    public void onPause() {
        Logger.d(TAG + "-->onPause()");
        super.onPause();
    }

    @Override
    public void onStop() {
        Logger.d(TAG + "-->onStop()");
        super.onStop();
    }

    //在Fragment不可见时，回调的是onDestroyView()而不是onDestroy()，不过由于它仍旧在缓存中，所以此时确实不能释放连接，而是要等到onDestroy()时再释放
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

package wang.imallen.blog.servicemanager.lifecycle.frag;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.qiyi.video.svg.Andromeda;
import org.qiyi.video.svg.log.Logger;

import wang.imallen.blog.moduleexportlib.apple.IBuyApple;
import wang.imallen.blog.servicemanager.R;

/**
 * Created by wangallen on 2018/4/9.
 */

public class CustomFragment extends Fragment {

    public static CustomFragment newInstance() {
        return new CustomFragment();
    }

    @Override
    public void onAttach(Context context) {
        Logger.d("CustomFragment-->onAttach()");
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.d("CustomFragment-->onCreate()");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        Logger.d("CustomFragment-->onCreateView()");

        View rootView = inflater.inflate(R.layout.frag_layout, container, false);

        TextView textView = rootView.findViewById(R.id.textView);
        textView.setText("This is a TextView in CustomFragment");

        return rootView;
    }

    private void useBuyAppleService() {
        IBinder binder = Andromeda.with(this).getRemoteService(IBuyApple.class);
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
        super.onStart();
        Logger.d("CustomFragment-->onStart()");
        useBuyAppleService();
    }

    @Override
    public void onResume() {
        Logger.d("CustomFragment-->onResume()");
        super.onResume();
    }

    @Override
    public void onPause() {
        Logger.d("CustomFragment-->onPause()");
        super.onPause();
    }

    @Override
    public void onStop() {
        Logger.d("CustomFragment-->onStop()");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Logger.d("CustomFragment-->onDestroyView()");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Logger.d("CustomFragment-->onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Logger.d("CustomFragment-->onDetach()");
        super.onDetach();
    }
}

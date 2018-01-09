package wang.imallen.blog.applemodule;

import android.util.Log;

import wang.imallen.blog.moduleexportlib.apple.IDeliverApple;
import wang.imallen.blog.serviceannotation.Remote;

/**
 * Created by wangallen on 2018/1/8.
 */
@Remote
public class DeliveryAppleImpl implements IDeliverApple {

    private static final String TAG = "ServiceManager";

    @Override
    public int getApple(int userId) {
        return 13;
    }

    @Override
    public void sendApple(int saleId, int appleNum) {
        Log.d(TAG, "send " + appleNum + " apples");
    }
}

package wang.imallen.blog.applemodule;

import android.os.Bundle;
import android.os.RemoteException;

import org.qiyi.video.svg.IPCCallback;
import org.qiyi.video.svg.log.Logger;

import wang.imallen.blog.moduleexportlib.apple.IBuyApple;

/**
 * Created by wangallen on 2018/1/18.
 */

public class BuyAppleImpl extends IBuyApple.Stub {

    private static BuyAppleImpl instance;

    public static BuyAppleImpl getInstance() {
        if (null == instance) {
            synchronized (BuyAppleImpl.class) {
                if (null == instance) {
                    instance = new BuyAppleImpl();
                }
            }
        }
        return instance;
    }

    private BuyAppleImpl() {
    }

    @Override
    public int buyAppleInShop(int userId) throws RemoteException {
        Logger.d("BuyAppleImpl-->buyAppleInShop,userId:" + userId);
        if (userId == 10) {
            return 20;
        } else if (userId == 20) {
            return 30;
        } else {
            return -1;
        }
    }

    @Override
    public void buyAppleOnNet(int userId, IPCCallback callback) throws RemoteException {
        Logger.d("BuyAppleImpl-->buyAppleOnNet,userId:" + userId);


        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }


        Bundle result = new Bundle();
        if (userId == 10) {
            result.putInt("Result", 20);
            callback.onSuccess(result);
        } else if (userId == 20) {
            result.putInt("Result", 30);
            callback.onSuccess(result);
        } else {
            callback.onFail("Sorry, u are not authorized!");
        }
    }

}

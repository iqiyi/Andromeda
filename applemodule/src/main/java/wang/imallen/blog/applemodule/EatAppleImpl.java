package wang.imallen.blog.applemodule;

import android.util.Log;

import wang.imallen.blog.moduleexportlib.apple.IEatApple;

/**
 * Created by wangallen on 2018/1/8.
 */

public class EatAppleImpl implements IEatApple{

    private static final String TAG="ServiceManager";

    @Override
    public void eatApple(int userId) {
        switch (userId){
            case 10:{
                Log.d(TAG,"user 10 can eat one apple only!");
                break;
            }
            case 100:{
                Log.d(TAG,"user 100 can eat 10 apples at most!");
                break;
            }
            default:{
                Log.d(TAG,"user "+userId+" can eat 6 apples!");
                break;
            }
        }
    }

    @Override
    public int getAppleCalorie(int appleNum) {
        return 230;
    }
}

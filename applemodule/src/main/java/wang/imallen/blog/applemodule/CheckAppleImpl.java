package wang.imallen.blog.applemodule;

import wang.imallen.blog.moduleexportlib.apple.ICheckApple;

/**
 * Created by wangallen on 2018/1/18.
 */

public class CheckAppleImpl implements ICheckApple {

    private static CheckAppleImpl instance;

    public static CheckAppleImpl getInstance() {
        if (null == instance) {
            synchronized (CheckAppleImpl.class) {
                if (null == instance) {
                    instance = new CheckAppleImpl();
                }
            }
        }
        return instance;
    }

    private CheckAppleImpl() {
    }

    @Override
    public int getAppleCalories(int appleNum) {
        return appleNum * 50;
    }

    @Override
    public String getAppleDescription(int appleType) {
        switch (appleType) {
            case 1: {
                return "Yellow apple";
            }
            case 2: {
                return "Green apple";
            }
            case 3: {
                return "Red apple";
            }
            default:
                return "Unknown apple";
        }
    }
}

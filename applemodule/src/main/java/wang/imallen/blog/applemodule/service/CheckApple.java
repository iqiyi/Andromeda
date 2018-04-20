package wang.imallen.blog.applemodule.service;

import wang.imallen.blog.moduleexportlib.apple.ICheckApple;

/**
 * Created by wangallen on 2018/1/18.
 */

public class CheckApple implements ICheckApple {

    private static CheckApple instance;

    public CheckApple() {
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

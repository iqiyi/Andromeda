package wang.imallen.blog.servicemanager;

/**
 * Created by wangallen on 2018/2/12.
 */

public class CheckPearImpl implements ICheckPear {
    @Override
    public int getAppleCalories(int pearNum) {
        return pearNum * 50;
    }

    @Override
    public String getPearDesc(int pearType) {
        return "Big pear!";
    }
}

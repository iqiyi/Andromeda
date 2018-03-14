package org.qiyi.video.svg.utils;

/**
 * Created by wangallen on 2018/1/30.
 */

public class ProcessImpl {

    private static ProcessImpl instance;

    public static ProcessImpl getInstance(){
        if(null==instance){
            synchronized (ProcessImpl.class){
                if(null==instance){
                    instance=new ProcessImpl();
                }
            }
        }
        return instance;
    }

    private String processName;

    private ProcessImpl(){}

}

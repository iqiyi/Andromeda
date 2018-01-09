package wang.imallen.blog.servicemanagerlib.config;

/**
 * Created by wangallen on 2018/1/8.
 */

public class ServiceActionPolicyImpl implements ServiceActionPolicy {

    private static ServiceActionPolicyImpl sInstance;

    public static ServiceActionPolicyImpl getInstance(){
        if(null==sInstance){
            synchronized (ServiceActionPolicyImpl.class){
                if(null==sInstance){
                    sInstance=new ServiceActionPolicyImpl();
                }
            }
        }
        return sInstance;
    }

    private ServiceActionPolicyImpl(){}

    private static final String SCHEMA = "qiyi";
    private static final String REGISTER = "register";
    private static final String FETCH = "fetch";



    @Override
    public String getFetchServiceAction(String module) {
        return SCHEMA + "://" + module + "/" + FETCH;
    }

    @Override
    public String getRegisterServiceAction(String module) {
        return SCHEMA + "://" + module + "/" + REGISTER;
    }

}

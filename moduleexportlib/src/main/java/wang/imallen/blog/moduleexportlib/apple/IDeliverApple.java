package wang.imallen.blog.moduleexportlib.apple;

//import wang.imallen.blog.serviceannotation.Remote;

/**
 * Created by wangallen on 2018/1/8.
 */
//@Remote
public interface IDeliverApple {

    int getApple(int userId);

    void sendApple(int saleId, int appleNum);

}

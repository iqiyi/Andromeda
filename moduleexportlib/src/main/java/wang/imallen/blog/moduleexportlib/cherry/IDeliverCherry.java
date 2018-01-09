package wang.imallen.blog.moduleexportlib.cherry;

import java.util.List;

import wang.imallen.blog.serviceannotation.Remote;

/**
 * Created by wangallen on 2018/1/8.
 */
@Remote
public interface IDeliverCherry {

    List<Cherry> getCherries(int userId);

    //void sendCherry(int saleId, List<Cherry> cherries);

}

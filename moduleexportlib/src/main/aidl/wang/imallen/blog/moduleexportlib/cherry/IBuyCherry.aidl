// IBuyCherry.aidl
package wang.imallen.blog.moduleexportlib.cherry;
import org.qiyi.video.svg.IPCCallback;
// Declare any non-default types here with import statements

interface IBuyCherry {

    int buyCherryInShop(int userId);
    void buyCherryOnNet(int userId,IPCCallback callback);

}

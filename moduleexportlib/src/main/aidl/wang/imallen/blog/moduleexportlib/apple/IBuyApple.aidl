// IBuyApple.aidl
package wang.imallen.blog.moduleexportlib.apple;
import org.qiyi.video.svg.IPCCallback;

interface IBuyApple {
    int buyAppleInShop(int userId);
    void buyAppleOnNet(int userId,IPCCallback callback);
}

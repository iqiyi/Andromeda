// Callback.aidl
package org.qiyi.video.svg;
import android.os.Bundle;
// Declare any non-default types here with import statements

interface IPCCallback {
   void onSuccess(in Bundle result);
   void onFail(String reason);
}

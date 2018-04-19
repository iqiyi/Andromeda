package org.qiyi.video.svg.backup;

import android.content.Context;

/**
 * Created by wangallen on 2018/4/18.
 */

public interface IEmergencyHandler {
    void handleBinderDied(Context context, String serverProcessName);
}

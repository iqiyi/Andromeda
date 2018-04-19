package org.qiyi.video.svg.remote;

import java.util.Collections;
import java.util.Set;

/**
 * Created by wangallen on 2018/3/28.
 */

public class EmptyRemoteManagerTreeNode implements IRemoteManagerTreeNode {
    @Override
    public Set<IRemoteManager> getDescendants() {
        return Collections.emptySet();
    }
}

package org.qiyi.video.svg.fragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.os.Build;

import org.qiyi.video.svg.StarBridge;
import org.qiyi.video.svg.life.ActivityFragmentLifecycle;
import org.qiyi.video.svg.log.Logger;
import org.qiyi.video.svg.remote.IRemoteManager;
import org.qiyi.video.svg.remote.IRemoteManagerTreeNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by wangallen on 2018/3/27.
 */

public class RemoteManagerFragment extends Fragment {

    private IRemoteManager remoteManager;

    private Fragment parentFragmentHint;

    private RemoteManagerFragment rootRemoteManagerFragment;

    private final ActivityFragmentLifecycle lifecycle;

    private final Set<RemoteManagerFragment> childRemoteManagerFrags = new HashSet<>();

    private final FragmentRemoteManagerTreeNode remoteManagerTreeNode = new FragmentRemoteManagerTreeNode();

    public RemoteManagerFragment() {
        this(new ActivityFragmentLifecycle());
    }

    @SuppressLint("ValidFragment")
    public RemoteManagerFragment(ActivityFragmentLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    /**
     * Sets a hint for which fragment is our parent which allows the fragment to return correct
     * information about its parents before pending fragment transactions have been executed.
     *
     * @param parentFragmentHint
     */
    public void setParentFragmentHint(Fragment parentFragmentHint) {
        Logger.d(this.toString() + "-->setParentFragmentHint()");
        this.parentFragmentHint = parentFragmentHint;
        if (parentFragmentHint != null && parentFragmentHint.getActivity() != null) {
            registerFragmentWithRoot(parentFragmentHint.getActivity());
        }
    }

    private void registerFragmentWithRoot(Activity activity) {
        Logger.d(this.toString() + "-->registerFragmentWithRoot()");
        unregisterFragmentWithRoot();
        //TODO 这里是不是要先判断一下rootRemoteManagerFragment是否为空呢?
        rootRemoteManagerFragment = StarBridge.getInstance().getRemoteManagerRetriever().getRemoteManagerFragment(activity);
        if (!equals(rootRemoteManagerFragment)) {
            rootRemoteManagerFragment.addChildRemoteManagerFragment(this);
        }
    }

    private void addChildRemoteManagerFragment(RemoteManagerFragment child) {
        childRemoteManagerFrags.add(child);
    }

    private void removeChildRemoteManagerFragment(RemoteManagerFragment child) {
        childRemoteManagerFrags.remove(child);
    }

    private void unregisterFragmentWithRoot() {
        if (rootRemoteManagerFragment != null) {
            rootRemoteManagerFragment.removeChildRemoteManagerFragment(this);
            rootRemoteManagerFragment = null;
        }
    }

    public IRemoteManager getRemoteManager() {
        return remoteManager;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            registerFragmentWithRoot(activity);
        } catch (IllegalStateException e) {
            // OnAttach can be called after the activity is destroyed, see #497.
            Logger.e("Unable to register fragment with root");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        lifecycle.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        lifecycle.onStop();
    }

    //TODO 可能要放在onDestoryView()中会更好，因为在ViewPager中管理的Fragment,会先调用onDestoryView(),但是可能一直都不会调用onDestory()
    @Override
    public void onDestroy() {
        super.onDestroy();
        lifecycle.onDestroy();
        unregisterFragmentWithRoot();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        unregisterFragmentWithRoot();
    }

    @Override
    public String toString() {
        return super.toString() + "{parent=" + getParentFragmentUsingHint() + "}";
    }

    Set<RemoteManagerFragment> getDescendantRemoteManagerFragments() {
        if (this.equals(rootRemoteManagerFragment)) {
            return Collections.unmodifiableSet(childRemoteManagerFrags);
        } else if (rootRemoteManagerFragment == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //Pre JB MR1 doesn't allow us to get the parent fragment so we can't introspect hierarchy, so just return an empty set
            return Collections.emptySet();
        } else {
            Set<RemoteManagerFragment> descendants = new HashSet<>();
            for (RemoteManagerFragment fragment : rootRemoteManagerFragment.getDescendantRemoteManagerFragments()) {
                if (isDescendant(fragment.getParentFragment())) {
                    descendants.add(fragment);
                }
            }
            return Collections.unmodifiableSet(descendants);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private boolean isDescendant(Fragment fragment) {
        Fragment root = getParentFragment();
        Fragment parentFragment;
        while ((parentFragment = fragment.getParentFragment()) != null) {
            if (parentFragment.equals(root)) {
                return true;
            }
            fragment = fragment.getParentFragment();
        }
        return false;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private Fragment getParentFragmentUsingHint() {
        final Fragment fragment;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            fragment = getParentFragment();
        } else {
            fragment = null;
        }
        return fragment != null ? fragment : parentFragmentHint;
    }

    private class FragmentRemoteManagerTreeNode implements IRemoteManagerTreeNode {
        @Override
        public Set<IRemoteManager> getDescendants() {
            Set<RemoteManagerFragment> descendantFragments = getDescendantRemoteManagerFragments();
            Set<IRemoteManager> descendants = new HashSet<>(descendantFragments.size());
            for (RemoteManagerFragment fragment : descendantFragments) {
                if (fragment.getRemoteManager() != null) {
                    descendants.add(fragment.getRemoteManager());
                }
            }
            return descendants;
        }
    }

    public ActivityFragmentLifecycle getLifecycle() {
        return lifecycle;
    }

    public FragmentRemoteManagerTreeNode getRemoteManagerTreeNode() {
        return remoteManagerTreeNode;
    }

    public void setRemoteManager(IRemoteManager remoteManager) {
        this.remoteManager = remoteManager;
    }
}

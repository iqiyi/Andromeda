package org.qiyi.video.svg.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import org.qiyi.video.svg.StarBridge;
import org.qiyi.video.svg.life.ActivityFragLifecycle;
import org.qiyi.video.svg.log.Logger;
import org.qiyi.video.svg.remote.IRemoteManager;
import org.qiyi.video.svg.remote.IRemoteManagerTreeNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by wangallen on 2018/3/27.
 */

public class SupportRemoteManagerFragment extends Fragment {

    private final ActivityFragLifecycle lifecycle;

    private SupportRemoteManagerFragment rootRequestManagerFragment;

    private Fragment parentFragmentHint;

    private IRemoteManager remoteManager;

    private final Set<SupportRemoteManagerFragment> childRemoteManagerFrags = new HashSet<>();

    private final IRemoteManagerTreeNode remoteManagerTreeNode = new SupportFragTreeNode();

    public SupportRemoteManagerFragment() {
        this(new ActivityFragLifecycle());
    }

    @SuppressLint("ValidFragment")
    public SupportRemoteManagerFragment(ActivityFragLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    /**
     * Sets a hint for which fragment is our parent which allows fragment to return correct
     * information about its parents before pending fragment transactions have been executed.
     *
     * @param parentFragmentHint
     */
    public void setParentFragmentHint(Fragment parentFragmentHint) {
        Logger.d("SupportRemoteManagerFragment-->setParentFragmentHint()");
        this.parentFragmentHint = parentFragmentHint;
        if (parentFragmentHint != null && parentFragmentHint.getActivity() != null) {
            registerFragmentWithRoot(parentFragmentHint.getActivity());
        }
    }

    private void registerFragmentWithRoot(FragmentActivity activity) {
        Logger.d("SupportRemoteManagerFragment-->registerFragmentWithRoot()");
        unregisterFragmentWithRoot();
        rootRequestManagerFragment = StarBridge.getInstance().getRemoteManagerRetriever().getSupportRemoteManagerFragment(activity);
        if (!equals(rootRequestManagerFragment)) {
            rootRequestManagerFragment.addChildRemoteManagerFragment(this);
        }
    }

    private void addChildRemoteManagerFragment(SupportRemoteManagerFragment child) {
        childRemoteManagerFrags.add(child);
    }

    private void removeChildRemoteManagerFragment(SupportRemoteManagerFragment child) {
        childRemoteManagerFrags.remove(child);
    }

    private void unregisterFragmentWithRoot() {
        if (rootRequestManagerFragment != null) {
            rootRequestManagerFragment.removeChildRemoteManagerFragment(this);
            rootRequestManagerFragment = null;
        }
    }

    Set<SupportRemoteManagerFragment> getDescendantRequestManagerFrags() {
        if (rootRequestManagerFragment == null) {
            return Collections.emptySet();
        } else if (this.equals(rootRequestManagerFragment)) {
            return Collections.unmodifiableSet(childRemoteManagerFrags);
        } else {
            Set<SupportRemoteManagerFragment> descendants = new HashSet<>();
            for (SupportRemoteManagerFragment frag : rootRequestManagerFragment.getDescendantRequestManagerFrags()) {
                if (isDescendant(frag.getParentFragmentUsingHint())) {
                    descendants.add(frag);
                }
            }
            return Collections.unmodifiableSet(descendants);
        }
    }

    private boolean isDescendant(Fragment fragment) {
        Fragment root = getParentFragmentUsingHint();
        Fragment parentFrag;
        while ((parentFrag = fragment.getParentFragment()) != null) {
            if (parentFrag.equals(root)) {
                return true;
            }
            fragment = fragment.getParentFragment();
        }
        return false;
    }

    private Fragment getParentFragmentUsingHint() {
        Fragment fragment = getParentFragment();
        return fragment != null ? fragment : parentFragmentHint;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            registerFragmentWithRoot(getActivity());
        } catch (IllegalStateException e) {
            //OnAttach can be called after the activity is destroyed, see #497
            Logger.e("Unable to register fragment with root:" + e.toString());
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

    //TODO 是选择onDestroy()好呢，还是onDestoryView()好呢?
    @Override
    public void onDestroy() {
        super.onDestroy();
        lifecycle.onDestroy();
        unregisterFragmentWithRoot();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parentFragmentHint = null;
        unregisterFragmentWithRoot();
    }

    public ActivityFragLifecycle getLifecycle() {
        return lifecycle;
    }

    public IRemoteManager getRemoteManager() {
        return remoteManager;
    }

    public void setRemoteManager(IRemoteManager remoteManager) {
        this.remoteManager = remoteManager;
    }

    public IRemoteManagerTreeNode getRemoteManagerTreeNode() {
        return remoteManagerTreeNode;
    }

    private class SupportFragTreeNode implements IRemoteManagerTreeNode {
        @Override
        public Set<IRemoteManager> getDescendants() {
            Set<SupportRemoteManagerFragment> descendantFrags = getDescendantRequestManagerFrags();
            Set<IRemoteManager> descendants = new HashSet<>(descendantFrags.size());
            for (SupportRemoteManagerFragment fragment : descendantFrags) {
                if (fragment.getRemoteManager() != null) {
                    descendants.add(fragment.getRemoteManager());
                }
            }
            return descendants;
        }
    }

}

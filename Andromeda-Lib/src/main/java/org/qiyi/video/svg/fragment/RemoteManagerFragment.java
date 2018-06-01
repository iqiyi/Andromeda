/*
 * Copyright (c) 2018-present, iQIYI, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *        1. Redistributions of source code must retain the above copyright notice,
 *        this list of conditions and the following disclaimer.
 *
 *        2. Redistributions in binary form must reproduce the above copyright notice,
 *        this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *        3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived
 *        from this software without specific prior written permission.
 *
 *        THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 *        INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *        IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 *        OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *        OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *        OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *        EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.qiyi.video.svg.fragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.os.Build;

import org.qiyi.video.svg.Andromeda;
import org.qiyi.video.svg.life.ActivityFragLifecycle;
import org.qiyi.video.svg.log.Logger;
import org.qiyi.video.svg.remote.IRemoteManager;

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

    private final ActivityFragLifecycle lifecycle;

    private final Set<RemoteManagerFragment> childRemoteManagerFrags = new HashSet<>();

    public RemoteManagerFragment() {
        this(new ActivityFragLifecycle());
    }

    @SuppressLint("ValidFragment")
    public RemoteManagerFragment(ActivityFragLifecycle lifecycle) {
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
        rootRemoteManagerFragment = Andromeda.getInstance().getRemoteManagerRetriever().getRemoteManagerFragment(activity);
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

    public ActivityFragLifecycle getLifecycle() {
        return lifecycle;
    }

    public void setRemoteManager(IRemoteManager remoteManager) {
        this.remoteManager = remoteManager;
    }
}

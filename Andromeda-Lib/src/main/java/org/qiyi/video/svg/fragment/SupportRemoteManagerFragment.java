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
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

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

public class SupportRemoteManagerFragment extends Fragment {

    private final ActivityFragLifecycle lifecycle;

    private SupportRemoteManagerFragment rootRequestManagerFragment;

    private Fragment parentFragmentHint;

    private IRemoteManager remoteManager;

    private final Set<SupportRemoteManagerFragment> childRemoteManagerFrags = new HashSet<>();

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
        rootRequestManagerFragment = Andromeda.getInstance().getRemoteManagerRetriever().getSupportRemoteManagerFragment(activity);
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

}

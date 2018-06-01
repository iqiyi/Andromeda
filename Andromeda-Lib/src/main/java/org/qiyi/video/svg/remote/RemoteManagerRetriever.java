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
package org.qiyi.video.svg.remote;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.ArrayMap;
import android.view.View;

import org.qiyi.video.svg.fragment.RemoteManagerFragment;
import org.qiyi.video.svg.fragment.SupportRemoteManagerFragment;
import org.qiyi.video.svg.life.ApplicationLifecycle;
import org.qiyi.video.svg.log.Logger;
import org.qiyi.video.svg.utils.Preconditions;
import org.qiyi.video.svg.utils.Utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangallen on 2018/3/27.
 */

public class RemoteManagerRetriever implements IRemoteManagerRetriever, Handler.Callback {

    private static final String FRAG_TAG = "org.qiyi.video.svg.remote";

    private static final int REMOVE_FRAG_MANAGER = 1;
    private static final int REMOVE_SUPPORT_FRAG_MANAGER = 2;

    private static final String KEY_FRAG_INDEX = "KeyFragIndex";

    private final Handler handler;

    /**
     * The top application level RemoteManager
     */
    private volatile RemoteManager applicationManager;

    private final Map<android.app.FragmentManager, RemoteManagerFragment> pendingRemoteManagerFrags = new HashMap<>();

    private final Map<FragmentManager, SupportRemoteManagerFragment> pendingSupportRemoteManagerFrags = new HashMap<>();

    //Objects used to find Fragments and Activities containing views
    private final ArrayMap<View, Fragment> tempViewToSupportFragment = new ArrayMap<>();
    private final ArrayMap<View, android.app.Fragment> tempViewToFragment = new ArrayMap<>();
    private final Bundle tempBundle = new Bundle();


    public RemoteManagerRetriever() {
        handler = new Handler(Looper.getMainLooper(), this);
    }

    @Override
    public IRemoteManager get(Fragment fragment) {
        Preconditions.checkNotNull(fragment.getActivity(), "you cannot start a load on a fragment before it is attached to Activity or after it is destroyed!");
        if (Utils.isOnBackgroundThread()) {
            return get(fragment.getActivity().getApplicationContext());
        } else {
            FragmentManager fm = fragment.getChildFragmentManager();
            return getSupportFragmentManager(fragment.getActivity(), fm, fragment, fragment.isVisible());
        }
    }

    @Override
    public IRemoteManager get(android.app.Fragment fragment) {
        if (fragment.getActivity() == null) {
            throw new IllegalArgumentException("You cannot start bind action on a fragment before it is attached");
        }
        if (Utils.isOnBackgroundThread() || Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return get(fragment.getActivity().getApplicationContext());
        } else {
            android.app.FragmentManager fm = fragment.getChildFragmentManager();
            return getRemoteFragmentManager(fragment.getActivity(), fm, fragment, fragment.isVisible());
        }
    }

    private IRemoteManager getSupportFragmentManager(Context context, FragmentManager fm, Fragment parentHint, boolean isParentVisible) {
        SupportRemoteManagerFragment current = getSupportRemoteManagerFragment(fm, parentHint, isParentVisible);
        IRemoteManager remoteManager = current.getRemoteManager();
        if (remoteManager == null) {
            remoteManager = new RemoteManager(context.getApplicationContext(), current.getLifecycle());
            current.setRemoteManager(remoteManager);
        }
        return remoteManager;
    }

    private IRemoteManager getRemoteFragmentManager(Context context, android.app.FragmentManager fm, android.app.Fragment parentHint,
                                                    boolean isParentVisible) {
        RemoteManagerFragment current = getRemoteManagerFragment(fm, parentHint, isParentVisible);
        IRemoteManager remoteManager = current.getRemoteManager();
        if (remoteManager == null) {
            remoteManager = new RemoteManager(context.getApplicationContext(), current.getLifecycle());
            current.setRemoteManager(remoteManager);
        }
        return remoteManager;
    }

    private SupportRemoteManagerFragment getSupportRemoteManagerFragment(FragmentManager fm,
                                                                         Fragment parentHint, boolean isParentVisible) {
        SupportRemoteManagerFragment currentFrag = (SupportRemoteManagerFragment) fm.findFragmentByTag(FRAG_TAG);
        if (currentFrag == null) {
            currentFrag = pendingSupportRemoteManagerFrags.get(fm);
            if (currentFrag == null) {
                currentFrag = new SupportRemoteManagerFragment();
                currentFrag.setParentFragmentHint(parentHint);
                //如果parentHint可见的话就马上调用onStart()
                if (isParentVisible) {
                    currentFrag.getLifecycle().onStart();
                }
                pendingSupportRemoteManagerFrags.put(fm, currentFrag);
                fm.beginTransaction().add(currentFrag, FRAG_TAG).commitAllowingStateLoss();
                handler.obtainMessage(REMOVE_SUPPORT_FRAG_MANAGER, fm).sendToTarget();
            }
        }
        return currentFrag;
    }

    private RemoteManagerFragment getRemoteManagerFragment(android.app.FragmentManager fm,
                                                           android.app.Fragment parentHint, boolean isParentVisible) {
        RemoteManagerFragment current = (RemoteManagerFragment) fm.findFragmentByTag(FRAG_TAG);
        if (current == null) {
            current = pendingRemoteManagerFrags.get(fm);
            if (current == null) {
                current = new RemoteManagerFragment();
                current.setParentFragmentHint(parentHint);
                if (isParentVisible) {
                    current.getLifecycle().onStart();
                }
                pendingRemoteManagerFrags.put(fm, current);
                fm.beginTransaction().add(current, FRAG_TAG).commitAllowingStateLoss();
                handler.obtainMessage(REMOVE_FRAG_MANAGER, fm).sendToTarget();
            }
        }
        return current;
    }

    public SupportRemoteManagerFragment getSupportRemoteManagerFragment(FragmentActivity activity) {
        return getSupportRemoteManagerFragment(activity.getSupportFragmentManager(),
                null, isActivityVisible(activity));
    }

    public RemoteManagerFragment getRemoteManagerFragment(Activity activity) {
        return getRemoteManagerFragment(activity.getFragmentManager(), null, isActivityVisible(activity));
    }

    private static boolean isActivityVisible(Activity activity) {
        return !activity.isFinishing();
    }

    @Override
    public IRemoteManager get(FragmentActivity fragActivity) {
        if (Utils.isOnBackgroundThread()) {
            return get(fragActivity.getApplicationContext());
        } else {
            assertNotDestroyed(fragActivity);
            FragmentManager fm = fragActivity.getSupportFragmentManager();
            return getSupportFragmentManager(fragActivity, fm, null, isActivityVisible(fragActivity));
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static void assertNotDestroyed(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed()) {
            throw new IllegalArgumentException("You cannot start a load for a destroyed activity");
        }
    }

    @Override
    public IRemoteManager get(Activity activity) {
        if (Utils.isOnBackgroundThread()) {
            return get(activity.getApplicationContext());
        } else {
            assertNotDestroyed(activity);
            android.app.FragmentManager fm = activity.getFragmentManager();
            return getRemoteFragmentManager(activity, fm, null, isActivityVisible(activity));
        }
    }

    @Override
    public IRemoteManager get(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("You cannot start bind action on a null Context");
        } else if (Utils.isOnMainThread() && !(context instanceof Application)) {
            if (context instanceof FragmentActivity) {
                return get((FragmentActivity) context);
            } else if (context instanceof Activity) {
                return get((Activity) context);
            } else if (context instanceof ContextWrapper) {
                return get(((ContextWrapper) context).getBaseContext());
            }
        }
        return getApplicationManager(context);
    }

    private RemoteManager getApplicationManager(Context context) {
        if (applicationManager == null) {
            synchronized (this) {
                if (applicationManager == null) {
                    applicationManager = new RemoteManager(context.getApplicationContext(),
                            new ApplicationLifecycle());
                }
            }
        }
        return applicationManager;
    }

    @Override
    public IRemoteManager get(View view) {
        if (Utils.isOnBackgroundThread()) {
            return get(view.getContext().getApplicationContext());
        }
        Preconditions.checkNotNull(view);
        Preconditions.checkNotNull(view.getContext(), "Unable to obtain a request manager for a view without a Context");
        Activity activity = findActivity(view.getContext());
        //The view might be somewhere else, like a service
        if (activity == null) {
            return get(view.getContext().getApplicationContext());
        }

        //Support Fragments.
        //Althouth the user might have non-support Fragments attached to FragmentActivity, searching
        //for non-support Fragments is so expensive pre 0 and that should be rare enough that we
        //prefer to just fall back to the Activity directly.
        if (activity instanceof FragmentActivity) {
            Fragment fragment = findSupportFragment(view, (FragmentActivity) activity);
            return fragment != null ? get(fragment) : get(activity);
        }

        //Standard Fragments.
        android.app.Fragment fragment = findFragment(view, activity);
        if (fragment == null) {
            return get(activity);
        }
        return get(fragment);
    }

    private Activity findActivity(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return findActivity(((ContextWrapper) context).getBaseContext());
        } else {
            return null;
        }
    }

    private android.app.Fragment findFragment(View target, Activity activity) {
        tempViewToFragment.clear();
        findAllFragmentsWithViews(activity.getFragmentManager(), tempViewToFragment);

        android.app.Fragment result = null;

        View activityRoot = activity.findViewById(android.R.id.content);
        View current = target;
        while (!current.equals(activityRoot)) {
            result = tempViewToFragment.get(current);
            if (result != null) {
                break;
            }
            if (current.getParent() instanceof View) {
                current = (View) current.getParent();
            } else {
                break;
            }
        }
        tempViewToFragment.clear();
        return result;
    }

    private void findAllFragmentsWithViews(android.app.FragmentManager fragmentManager,
                                           ArrayMap<View, android.app.Fragment> result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            for (android.app.Fragment fragment : fragmentManager.getFragments()) {
                if (fragment.getView() != null) {
                    result.put(fragment.getView(), fragment);
                    findAllFragmentsWithViews(fragment.getChildFragmentManager(), result);
                }
            }
        } else {
            findAllFragmentsWithViewsPreO(fragmentManager, result);
        }
    }

    private void findAllFragmentsWithViewsPreO(android.app.FragmentManager fragmentManager,
                                               ArrayMap<View, android.app.Fragment> result) {
        int index = 0;
        while (true) {
            tempBundle.putInt(KEY_FRAG_INDEX, index++);
            android.app.Fragment fragment = null;
            try {
                fragment = fragmentManager.getFragment(tempBundle, KEY_FRAG_INDEX);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (fragment == null) {
                break;
            }
            if (fragment.getView() != null) {
                result.put(fragment.getView(), fragment);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    findAllFragmentsWithViews(fragment.getChildFragmentManager(), result);
                }
            }
        }
    }

    private Fragment findSupportFragment(View target, FragmentActivity activity) {
        tempViewToSupportFragment.clear();
        findAllSupportFragmentsWithViews(activity.getSupportFragmentManager().getFragments(), tempViewToSupportFragment);
        Fragment result = null;
        View activityRoot = activity.findViewById(android.R.id.content);
        View current = target;
        while (!current.equals(activityRoot)) {
            result = tempViewToSupportFragment.get(current);
            if (result != null) {
                break;
            }
            if (current.getParent() instanceof View) {
                current = (View) current.getParent();
            } else {
                break;
            }
        }
        tempViewToSupportFragment.clear();
        return result;
    }

    private static void findAllSupportFragmentsWithViews(Collection<Fragment> topLevelFragments,
                                                         Map<View, Fragment> result) {
        if (topLevelFragments == null) {
            return;
        }
        for (Fragment fragment : topLevelFragments) {
            //getFragments() in the support FragmentManager may contain null values, see#1991
            if (fragment == null || fragment.getView() == null) {
                continue;
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;
        Object removed = null;
        Object key = null;
        switch (msg.what) {
            case REMOVE_FRAG_MANAGER:
                android.app.FragmentManager fm = (android.app.FragmentManager) msg.obj;
                key = fm;
                removed = pendingRemoteManagerFrags.remove(fm);
                break;
            case REMOVE_SUPPORT_FRAG_MANAGER:
                FragmentManager supportFm = (FragmentManager) msg.obj;
                key = supportFm;
                removed = pendingSupportRemoteManagerFrags.remove(supportFm);
                break;
            default:
                handled = false;
                break;
        }
        if (handled && removed == null) {
            Logger.d("Failed to remove expected remote manager fragment, manager:" + key);
        }
        return handled;
    }


}

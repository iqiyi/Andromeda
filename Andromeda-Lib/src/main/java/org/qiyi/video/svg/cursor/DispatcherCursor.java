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
package org.qiyi.video.svg.cursor;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import org.qiyi.video.svg.BinderWrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangallen on 2018/3/20.
 */

public class DispatcherCursor extends MatrixCursor {

    public static final String KEY_BINDER_WRAPPER = "KeyBinderWrapper";

    private static Map<String, DispatcherCursor> cursorCache = new ConcurrentHashMap<>();

    public static final String[] DEFAULT_COLUMNS = {"col"};

    private Bundle binderExtras = new Bundle();

    public DispatcherCursor(String[] columnNames, IBinder binder) {
        super(columnNames);
        binderExtras.putParcelable(KEY_BINDER_WRAPPER, new BinderWrapper(binder));
    }

    @Override
    public Bundle getExtras() {
        return binderExtras;
    }

    public static DispatcherCursor generateCursor(IBinder binder) {
        try {
            DispatcherCursor cursor;
            cursor = cursorCache.get(binder.getInterfaceDescriptor());
            if (cursor != null) {
                return cursor;
            }
            cursor = new DispatcherCursor(DEFAULT_COLUMNS, binder);
            cursorCache.put(binder.getInterfaceDescriptor(), cursor);
            return cursor;
        } catch (RemoteException ex) {
            return null;
        }
    }

    public static IBinder stripBinder(Cursor cursor) {
        if (null == cursor) {
            return null;
        }
        Bundle bundle = cursor.getExtras();
        bundle.setClassLoader(BinderWrapper.class.getClassLoader());
        BinderWrapper binderWrapper = bundle.getParcelable(KEY_BINDER_WRAPPER);
        return null != binderWrapper ? binderWrapper.getBinder() : null;
    }

}

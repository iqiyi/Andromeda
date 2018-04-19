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

    private static Map<String, DispatcherCursor> cursorMap = new ConcurrentHashMap<>();

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
            DispatcherCursor cursor = cursorMap.get(binder.getInterfaceDescriptor());
            cursor = new DispatcherCursor(DEFAULT_COLUMNS, binder);
            cursorMap.put(binder.getInterfaceDescriptor(), cursor);
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

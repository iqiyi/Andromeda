package org.qiyi.video.svg.dispatcher;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.qiyi.video.svg.cursor.DispatcherCursor;
import org.qiyi.video.svg.log.Logger;


/**
 * Created by wangallen on 2018/3/19.
 */

public class DispatcherProvider extends ContentProvider {

    public static final String PROJECTION_MAIN[] = {"main"};

    public static final Uri URI = Uri.parse("content://org.qiyi.video.svg.dispatcher/main");

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Logger.d("DispatcherProvider-->query,uri:" + uri.getAuthority());
        return DispatcherCursor.generateCursor(Dispatcher.getInstance().asBinder());
    }


    @Override
    public String getType(Uri uri) {
        return null;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}

package org.qiyi.video.svg.utils;

import android.database.Cursor;

/**
 * Created by wangallen on 2018/3/20.
 */

public final class IOUtils {

    private IOUtils() {
    }

    public static void closeQuietly(Cursor cursor) {
        try {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

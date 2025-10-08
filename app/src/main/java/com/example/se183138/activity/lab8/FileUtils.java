package com.example.se183138.activity.lab8;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Tiện ích làm việc với tệp tin/URI.
 * Lưu ý: Cách lấy đường dẫn thực từ Uri có thể khác nhau giữa các thiết bị/phiên bản Android.
 */
public class FileUtils {
    /**
     * Trả về đường dẫn file hệ thống từ một Uri ảnh trong MediaStore.
     * @return Đường dẫn tuyệt đối hoặc null nếu không truy vấn được
     */
    public static String getPath(Context context, Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }
}

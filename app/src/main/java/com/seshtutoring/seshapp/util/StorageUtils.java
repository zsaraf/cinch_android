package com.seshtutoring.seshapp.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.nfc.Tag;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by nadavhollander on 7/21/15.
 */
public class StorageUtils {
    private static final String TAG = StorageUtils.class.getName();

    public static File storeTempImage(Context context, Bitmap image, String fileName) {
        File file = null;

        try {
            file = File.createTempFile(fileName, null, context.getCacheDir());
            file.deleteOnExit();

            FileOutputStream fos = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "Error accessing file: " + e.getMessage());
        }

        return file;
    }
}

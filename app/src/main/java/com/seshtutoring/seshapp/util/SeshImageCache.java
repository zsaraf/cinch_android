package com.seshtutoring.seshapp.util;

import android.content.Context;

import com.squareup.picasso.LruCache;

/**
 * Created by nadavhollander on 8/27/15.
 */
public class SeshImageCache {
    private static LruCache cache;

    public static LruCache sharedInstance(Context context) {
        if (cache == null) {
            cache = new LruCache(context);
        }

        return cache;
    }
}

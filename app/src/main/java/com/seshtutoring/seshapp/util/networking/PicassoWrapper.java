package com.seshtutoring.seshapp.util.networking;

import android.content.Context;

import com.seshtutoring.seshapp.util.SeshImageCache;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * Created by lillioetting on 9/9/15.
 */
public class PicassoWrapper {

    private static PicassoWrapper mInstance;
    private static Context mContext;
    public Picasso picasso;

    private PicassoWrapper(Context context) {
        mContext = context;

        //make Picasso builder
        OkHttpClient picassoClient = new OkHttpClient();
//        picassoClient.interceptors().add(new Interceptor() {
//            @Override
//            public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
//                Request newRequest = chain.request().newBuilder()
//                        .build();
//                return chain.proceed(newRequest);
//            }
//        });
        Picasso.Builder builder = new Picasso.Builder(mContext);

        this.picasso = builder
                .downloader(new OkHttpDownloader(picassoClient))
                .memoryCache(SeshImageCache.sharedInstance(mContext)).build();
    }

    public static synchronized PicassoWrapper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PicassoWrapper(context);
        }
        return mInstance;
    }


}

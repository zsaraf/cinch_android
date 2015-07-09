package com.seshtutoring.seshapp.util.networking;

import android.content.Context;

/**
 * Singleton stub for storing token
 * @TODO implement
 */
public class SeshAuthManager {
    private static SeshAuthManager mInstance;
    private Context mContext;
    private String sessionStub;

    private SeshAuthManager(Context context) {
        this.mContext = context;
    }

    public static SeshAuthManager sharedManager(Context context) {
        if (mInstance == null) {
            mInstance = new SeshAuthManager(context);
        }
        return mInstance;
    }

    // Stub implementation to make tests for SeshNetworking work
    public String getAccessToken() {
        if (sessionStub == null) {
            return "abcdefg";
        }
        return sessionStub;
    }
}

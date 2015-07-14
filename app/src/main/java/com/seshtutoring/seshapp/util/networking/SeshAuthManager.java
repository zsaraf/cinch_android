package com.seshtutoring.seshapp.util.networking;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Singleton stub for storing token
 */
public class SeshAuthManager {
    private static final String SESSION_ID_SHARED_PREFERENCES = "session_id_shared_preferences";
    private static final String SESSION_ID_KEY = "session_id";
    private static SeshAuthManager mInstance;
    private Context mContext;
    private String sessionId;

    private SeshAuthManager(Context context) {
        this.mContext = context;
    }

    public static SeshAuthManager sharedManager(Context context) {
        if (mInstance == null) {
            mInstance = new SeshAuthManager(context);
        }
        return mInstance;
    }

    public boolean isValidSession() {
        return getAccessToken() == null ? false : true;
    }

    // Stub implementation to make tests for SeshNetworking work
    public String getAccessToken() {
        if (sessionId == null) {
            SharedPreferences keyStore = mContext.getSharedPreferences(SESSION_ID_SHARED_PREFERENCES, 0);
            sessionId = keyStore.getString(SESSION_ID_KEY, null);
        }
        return sessionId;
    }

    public void foundSessionId(String sessionId) {
        SharedPreferences keyStore = mContext.getSharedPreferences(SESSION_ID_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = keyStore.edit();
        editor.putString(SESSION_ID_KEY, sessionId);
        editor.commit();
    }
}
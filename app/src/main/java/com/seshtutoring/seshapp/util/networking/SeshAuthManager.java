package com.seshtutoring.seshapp.util.networking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.view.SeshActivity;

/**
 * Singleton stub for storing token
 */
public class SeshAuthManager {
    private static final String SESSION_ID_SHARED_PREFERENCES = "session_id_shared_preferences";
    private static final String SESSION_ID_KEY = "session_id";
    private static SeshAuthManager mInstance;
    private Context mContext;
    private String sessionId;
    private boolean invalidated;

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

        invalidated = false;
    }

    public void clearSession() {
        SharedPreferences keyStore = mContext.getSharedPreferences(SESSION_ID_SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = keyStore.edit();
        editor.remove(SESSION_ID_KEY);
        editor.commit();

        this.sessionId = null;
    }

    public void invalidateSession() {
        if (!invalidated) {
            invalidated = true;
            User.logoutUserLocally(mContext);
            Intent showErrorDialog = new Intent(SeshActivity.INVALID_SESSION_ID_ACTION);
            mContext.sendBroadcast(showErrorDialog);
        }
    }
}

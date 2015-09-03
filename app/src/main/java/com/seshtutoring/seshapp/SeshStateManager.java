package com.seshtutoring.seshapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.services.UserInfoFetcher;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.view.InSeshActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.SeshActivity;
import com.stripe.android.compat.AsyncTask;

import org.joda.time.Period;

import java.util.List;

/**
 * Created by nadavhollander on 7/30/15.
 */
public class SeshStateManager {
    private final static String TAG = SeshStateManager.class.getName();

    public enum SeshState {
        NONE("SeshStateNone"), IN_SESH("SeshStateInSesh");

        public String identifier;

        SeshState(String identifier) {
            this.identifier = identifier;
        }
    }

    private static SeshStateManager mInstance;
    private SeshState currentSeshState;
    private boolean displayUpdateNeeded;
    private Context mContext;

    public SeshStateManager(Context context) {
        this.mContext = context;
        this.displayUpdateNeeded = false;
    }

    public static SeshState getCurrentSeshState(Context context) {
        return sharedInstance(context).currentSeshState;
    }

    public void updateSeshState(String seshStateIdentifier) {
        SeshState newSeshState;
        switch (seshStateIdentifier) {
            case "SeshStateNone":
                newSeshState = SeshState.NONE;
                break;
            case "SeshStateInSesh":
                newSeshState = SeshState.IN_SESH;
                break;
            default:
                newSeshState = SeshState.NONE;
                break;
        }

//        if (newSeshState != currentSeshState) {
//            displayUpdateNeeded = true;
//        }

        updateSeshState(newSeshState);
        Log.d(TAG, "Sesh state updated to " + currentSeshState.name());

    }

    public void updateSeshState(SeshState seshState) {
        this.currentSeshState = seshState;
        displayActivityForSeshStateUpdate();
    }

    public void displayActivityForSeshStateUpdate() {
        Log.d(TAG, "Displaying Activity for Current State: " + currentSeshState.toString());

        ApplicationLifecycleTracker applicationLifecycleTracker
                = ApplicationLifecycleTracker.sharedInstance(mContext);
        SeshActivity foregroundActivity = (SeshActivity) applicationLifecycleTracker.getActivityInForeground();
        switch (currentSeshState) {
            case IN_SESH:
                if (foregroundActivity != null &&
                        !foregroundActivity.isInSeshActivity()) {
                    if (Sesh.getCurrentSesh() == null) {
                        UserInfoFetcher seshInfoFetcher = new UserInfoFetcher(mContext);
                        seshInfoFetcher.fetch(new UserInfoFetcher.UserInfoSavedListener() {
                            @Override
                            public void onUserInfoSaved(User user) {
                                startInSeshActivity();
                            }
                        });
                    } else {
                        startInSeshActivity();
                    }
                }
                break;
            case NONE:
                if (foregroundActivity != null &&
                        foregroundActivity.isInSeshActivity()) {
                    startMainContainerActivity();
                }
                break;
        }

        displayUpdateNeeded = false;
    }

    private void startInSeshActivity() {
        Log.d(TAG, "STARTING IN SESH ACTIVITY");
        Intent intent = new Intent(mContext, InSeshActivity.class);
        Activity foregroundActivity = ApplicationLifecycleTracker.sharedInstance(mContext).getActivityInForeground();
        foregroundActivity.startActivity(intent);
        foregroundActivity.overridePendingTransition(R.anim.slide_up, R.anim.hold);
    }

    private void startMainContainerActivity() {
        Intent intent = new Intent(mContext, MainContainerActivity.class);
        ApplicationLifecycleTracker.sharedInstance(mContext).getActivityInForeground().startActivity(intent);
    }

    public static SeshStateManager sharedInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SeshStateManager(context);
        }
        return mInstance;
    }

    public static class VerifySeshStateAsyncTask extends AsyncTask<Object, Void, Void> {
        private Context mContext;
        private boolean inSesh;

        @Override
        public Void doInBackground(Object... params) {
            inSesh = false;

            this.mContext = (Context) params[0];
            User user = (User) params[1];

            List<Sesh> openSeshes = user.getOpenSeshes();
            for (Sesh sesh : openSeshes) {
                if (sesh.hasStarted) {
                    inSesh = true;
                }
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            super.onPostExecute(result);
            SeshStateManager seshStateManager = SeshStateManager.sharedInstance(mContext);
            if (inSesh && seshStateManager.currentSeshState != SeshState.IN_SESH) {
                seshStateManager.updateSeshState(SeshState.IN_SESH);
            }
        }
    }
 }

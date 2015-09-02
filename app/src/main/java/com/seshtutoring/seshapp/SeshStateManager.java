package com.seshtutoring.seshapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.services.SeshInfoFetcher;
import com.seshtutoring.seshapp.services.SeshInfoFetcher.FetchUpdateListener;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.view.InSeshActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.SeshActivity;

import org.joda.time.Period;

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
        this.currentSeshState = SeshState.NONE;
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

        if (newSeshState != currentSeshState) {
            displayUpdateNeeded = true;
        }

        currentSeshState = newSeshState;
        Log.d(TAG, "Sesh state updated to " + currentSeshState.name());
    }

    public void displayActivityForSeshStateUpdate() {
        Log.d(TAG, "Displaying Activity for Current State: " + currentSeshState.toString());

        if (!displayUpdateNeeded) return;

        ApplicationLifecycleTracker applicationLifecycleTracker
                = ApplicationLifecycleTracker.sharedInstance(mContext);
        SeshActivity foregroundActivity = (SeshActivity) applicationLifecycleTracker.getActivityInForeground();
        switch (currentSeshState) {
            case IN_SESH:
                if (!foregroundActivity.isInSeshActivity()) {
                    if (Sesh.getCurrentSesh() == null) {
                        SeshInfoFetcher seshInfoFetcher = new SeshInfoFetcher(mContext);
                        seshInfoFetcher.fetch(new FetchUpdateListener() {
                            @Override
                            public void onFetchUpdate() {
                                startInSeshActivity();
                            }
                        });
                    } else {
                        startInSeshActivity();
                    }
                }
                break;
            case NONE:
                if (foregroundActivity.isInSeshActivity()) {
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
 }

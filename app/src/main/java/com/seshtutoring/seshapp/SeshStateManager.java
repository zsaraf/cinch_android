package com.seshtutoring.seshapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.services.PeriodicFetchBroadcastReceiver;
import com.seshtutoring.seshapp.services.PeriodicFetchBroadcastReceiver.FetchUpdateListener;
import com.seshtutoring.seshapp.services.SeshInfoFetcher;
import com.seshtutoring.seshapp.services.SeshStateFetcher;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.view.InSeshActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity;

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
    private Context mContext;

    public SeshStateManager(Context context) {
        this.mContext = context;
        this.currentSeshState = SeshState.NONE;
    }

    public static SeshState getCurrentSeshState(Context context) {
        return sharedInstance(context).currentSeshState;
    }

    public void updateSeshState(String seshStateIdentifier) {
        switch (seshStateIdentifier) {
            case "SeshStateNone":
                currentSeshState = SeshState.NONE;
                break;
            case "SeshStateInSesh":
                currentSeshState = SeshState.IN_SESH;
                break;
            default:
                currentSeshState = SeshState.NONE;
                break;
        }
        Log.d(TAG, "Sesh state updated to " + currentSeshState.name());
    }

    public void displayActivityForCurrentState() {
        final ApplicationLifecycleTracker applicationLifecycleTracker
                = ApplicationLifecycleTracker.sharedInstance(mContext);
        switch (currentSeshState) {
            case IN_SESH:
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
                break;
            case NONE:
                startMainContainerActivity();
                break;
        }
    }

    private void startInSeshActivity() {
        Intent intent = new Intent(mContext, InSeshActivity.class);
        ApplicationLifecycleTracker.sharedInstance(mContext).getActivityInForeground().startActivity(intent);
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

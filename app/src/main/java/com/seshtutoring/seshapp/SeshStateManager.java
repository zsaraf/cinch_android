package com.seshtutoring.seshapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.services.PeriodicFetchBroadcastReceiver;
import com.seshtutoring.seshapp.services.PeriodicFetchBroadcastReceiver.FetchUpdateListener;
import com.seshtutoring.seshapp.services.SeshInfoFetcher;
import com.seshtutoring.seshapp.services.SeshStateFetcher;
import com.seshtutoring.seshapp.view.InSeshActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity;

import org.joda.time.Period;

/**
 * Created by nadavhollander on 7/30/15.
 */
public class SeshStateManager implements FetchUpdateListener{
    private final static String TAG = SeshStateManager.class.getName();

    public interface ViewRefreshListener {
        void refreshView();
    }

    public enum SeshState {
        NONE("SeshStateNone"), STUDENT_REVIEW_SESH("SeshStateStudentReviewSesh"),
        IN_SESH("SeshStateInSesh"), TUTOR_REVIEW_SESH("SeshStateTutorReviewSesh"),
        TUTOR_CANCELLED_SESH("SeshStateTutorCancelledSesh"),
        STUDENT_CANCELLED_SESH("SeshStateStudentCancelledSesh");

        public String identifier;

        SeshState(String identifier) {
            this.identifier = identifier;
        }
    }

    private static SeshStateManager mInstance;
    private SeshState currentSeshState;
    private Context mContext;
    private ViewRefreshListener viewRefreshListener;

    public SeshStateManager(Context context) {
        this.mContext = context;
        this.currentSeshState = SeshState.NONE;

        PeriodicFetchBroadcastReceiver.setSeshInfoUpdateListener(this);
        PeriodicFetchBroadcastReceiver.setSeshStateUpdateListener(this);
    }

    public void setViewRefreshListener(ViewRefreshListener viewRefreshListener) {
        this.viewRefreshListener = viewRefreshListener;
    }


    public static SeshState getCurrentSeshState(Context context) {
        return sharedInstance(context).currentSeshState;
    }

    public void updateSeshState(String seshStateIdentifier) {
        switch (seshStateIdentifier) {
            case "SeshStateNone":
                currentSeshState = SeshState.NONE;
                break;
            case "SeshStateStudentReviewSesh":
                currentSeshState = SeshState.STUDENT_REVIEW_SESH;
                break;
            case "SeshStateInSesh":
                currentSeshState = SeshState.IN_SESH;
                break;
            case "SeshStateTutorReviewSesh":
                currentSeshState = SeshState.TUTOR_REVIEW_SESH;
                break;
            case "SeshStateTutorCancelledSesh":
                currentSeshState = SeshState.TUTOR_CANCELLED_SESH;
                break;
            case "SeshStateStudentCancelledSesh":
                currentSeshState = SeshState.STUDENT_CANCELLED_SESH;
                break;
            default:
                currentSeshState = SeshState.NONE;
                break;
        }
        Log.d(TAG, "Sesh state updated to " + currentSeshState.name());

        onSeshStateUpdate();
    }

    @Override
    public void onFetchUpdate(String type) {
        if (type.equals(SeshStateFetcher.FETCH_TYPE_STATE)) {
            onSeshStateUpdate();
            viewRefreshListener.refreshView();
        } else if (type.equals(SeshInfoFetcher.FETCH_TYPE_INFO)) {
            viewRefreshListener.refreshView();
        }
    }

    private void onSeshStateUpdate() {
        switch (currentSeshState) {
            case IN_SESH:
                if (Sesh.getCurrentSesh() == null) {
                    SeshInfoFetcher seshInfoFetcher = new SeshInfoFetcher(mContext);
                    seshInfoFetcher.fetch(new FetchUpdateListener() {
                        @Override
                        public void onFetchUpdate(String type) {
                            startInSeshActivity();
                        }
                    });
                } else {
                    startInSeshActivity();
                }
                break;
        }
    }

    private void startInSeshActivity() {
        Intent intent = new Intent(mContext, InSeshActivity.class);
        mContext.startActivity(intent);
    }

    public static SeshStateManager sharedInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SeshStateManager(context);
        }
        return mInstance;
    }

    public void validateActiveSeshState() {
        onSeshStateUpdate();
    }
 }

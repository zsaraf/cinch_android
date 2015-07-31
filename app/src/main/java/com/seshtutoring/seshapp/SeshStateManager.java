package com.seshtutoring.seshapp;

import android.util.Log;

/**
 * Created by nadavhollander on 7/30/15.
 */
public class SeshStateManager {
    private final static String TAG = SeshStateManager.class.getName();

    public enum SeshState {
        NONE, STUDENT_REVIEW_SESH, IN_SESH, TUTOR_REVIEW_SESH, TUTOR_CANCELLED_SESH,
        STUDENT_CANCELLED_SESH
    }

    private static SeshStateManager mInstance;
    private SeshState currentSeshState;

    public static SeshState getCurrentSeshState() {
        return sharedInstance().currentSeshState;
    }

    public static void updateSeshState(String seshStateIdentifier) {
        SeshStateManager stateManager = sharedInstance();
        switch (seshStateIdentifier) {
            case "SeshStateNone":
                stateManager.currentSeshState = SeshState.NONE;
                break;
            case "SeshStateStudentReviewSesh":
                stateManager.currentSeshState = SeshState.STUDENT_REVIEW_SESH;
                break;
            case "SeshStateInSesh":
                stateManager.currentSeshState = SeshState.IN_SESH;
                break;
            case "SeshStateTutorReviewSesh":
                stateManager.currentSeshState = SeshState.TUTOR_REVIEW_SESH;
                break;
            case "SeshStateTutorCancelledSesh":
                stateManager.currentSeshState = SeshState.TUTOR_CANCELLED_SESH;
                break;
            case "SeshStateStudentCancelledSesh":
                stateManager.currentSeshState = SeshState.STUDENT_CANCELLED_SESH;
                break;
            default:
                stateManager.currentSeshState = SeshState.NONE;
                break;
        }
        Log.d(TAG, "Sesh state updated to " + stateManager.currentSeshState.name());
    }

    private static SeshStateManager sharedInstance() {
        if (mInstance == null) {
            mInstance = new SeshStateManager();
        }
        return mInstance;
    }
 }

package com.seshtutoring.seshapp.view;

import android.support.v7.app.AppCompatActivity;

import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;

/**
 * Created by nadavhollander on 7/31/15.
 */
public class SeshActivity extends AppCompatActivity {
    @Override
    public void onResume() {
        super.onResume();
        ApplicationLifecycleTracker.activityResumed(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        ApplicationLifecycleTracker.activityPaused(this);
    }
}

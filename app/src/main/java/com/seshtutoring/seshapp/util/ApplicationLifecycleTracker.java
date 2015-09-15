package com.seshtutoring.seshapp.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.services.notifications.SeshNotificationManagerService;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.SeshActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nadavhollander on 7/30/15.
 */
public class ApplicationLifecycleTracker  {
    private static final String TAG = ApplicationLifecycleTracker.class.getName();
    private static ApplicationLifecycleTracker applicationLifecycleTracker;

    private boolean someActivityInForeground;
    private boolean activityTransitionInProgress;

    private Activity activityInForeground;

    private Context mContext;

    private static final int FIFTEEN_SECONDS = 1000 * 15;

    public static ApplicationLifecycleTracker sharedInstance(Context context) {
        if (applicationLifecycleTracker == null) {
            applicationLifecycleTracker = new ApplicationLifecycleTracker(context);
        }

        return applicationLifecycleTracker;
    }

    public ApplicationLifecycleTracker(Context context) {
        this.mContext = context;
        this.activityTransitionInProgress = false;
    }

    public void activityResumed(final Activity activity) {
        someActivityInForeground = true;
        activityInForeground = activity;

        if (!activityTransitionInProgress) {
            applicationDidEnterForeground();
        }

        activityTransitionInProgress = false;

        if (SeshAuthManager.sharedManager(mContext).isValidSession() && SeshApplication.IS_LIVE
                && !((SeshActivity)activityInForeground).isSplashScreen()) {
            Intent startNotificationQueueHandling
                    = new Intent(SeshNotificationManagerService.START_IN_APP_DISPLAY_QUEUE_HANDLING,
                    null, mContext, SeshNotificationManagerService.class);
            mContext.startService(startNotificationQueueHandling);
        }
    }

    public void activityPaused() {
        someActivityInForeground = false;
        activityInForeground = null;

        if (!activityTransitionInProgress) {
            applicationWillEnterBackground();
        }

        if (SeshAuthManager.sharedManager(mContext).isValidSession() && SeshApplication.IS_LIVE) {
            Intent pauseNotificationQueueHandling
                    = new Intent(SeshNotificationManagerService.PAUSE_IN_APP_DISPLAY_QUEUE_HANDLING,
                    null, mContext, SeshNotificationManagerService.class);
            mContext.startService(pauseNotificationQueueHandling);
        }
    }

    public boolean applicationInForeground() {
        if (someActivityInForeground) {
            return true;
        } else {
            return activityTransitionInProgress;
        }
    }

    public void applicationDidEnterForeground() {
        Toast.makeText(mContext, "Application did enter foreground", Toast.LENGTH_SHORT).show();
    }

    public void applicationWillEnterBackground() {
        Toast.makeText(mContext, "Application will enter background", Toast.LENGTH_SHORT).show();
    }

    public Activity getActivityInForeground() {
        return activityInForeground;
    }

    public void setActivityTransitionInProgress(boolean inProgress) {
        this.activityTransitionInProgress = inProgress;
    }
}

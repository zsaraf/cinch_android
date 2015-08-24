package com.seshtutoring.seshapp.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.SeshStateManager;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.services.GCMRegistrationIntentService;
import com.seshtutoring.seshapp.services.PeriodicFetchBroadcastReceiver;
import com.seshtutoring.seshapp.services.notifications.SeshNotificationManagerService;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nadavhollander on 7/30/15.
 */
public class ApplicationLifecycleTracker  {
    private static final String TAG = ApplicationLifecycleTracker.class.getName();
    private static ApplicationLifecycleTracker applicationLifecycleTracker;

    private boolean someActivityInForeground;
    private AlarmManager fetchSeshInfoAlarm;
    private PendingIntent fetchSeshInfoPendingIntent;
    private AlarmManager fetchNotificationsAlarm;
    private PendingIntent fetchNotificationsPendingIntent;
    private static ApplicationResumeListener applicationResumeListener;

    private Activity activityInForeground;

    private Context mContext;

    private static final int FIFTEEN_SECONDS = 1000 * 15;


    public interface ApplicationResumeListener {
        void onApplicationResume();
    }

    public static ApplicationLifecycleTracker sharedInstance(Context context) {
        if (applicationLifecycleTracker == null) {
            applicationLifecycleTracker = new ApplicationLifecycleTracker(context);
        }

        return applicationLifecycleTracker;
    }

    public ApplicationLifecycleTracker(Context context) {
        this.mContext = context;

        this.fetchSeshInfoAlarm = (AlarmManager) mContext.getSystemService(mContext.ALARM_SERVICE);
        Intent infoIntent = new Intent(PeriodicFetchBroadcastReceiver.PERIODIC_FETCH_ACTION);
        this.fetchSeshInfoPendingIntent =
                PendingIntent.getBroadcast(mContext, 1, infoIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        this.fetchNotificationsAlarm = (AlarmManager) mContext.getSystemService(mContext.ALARM_SERVICE);
        Intent notificationsIntent = new Intent(SeshNotificationManagerService.REFRESH_NOTIFICATIONS_ACTION,
                null, context, SeshNotificationManagerService.class);
        this.fetchNotificationsPendingIntent =
                PendingIntent.getService(mContext, 2, notificationsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void activityResumed(Activity activity) {
        someActivityInForeground = true;
        activityInForeground = activity;

        if (applicationResumeListener != null) {
            applicationResumeListener.onApplicationResume();
            applicationResumeListener = null;
        }

        if (SeshAuthManager.sharedManager(mContext).isValidSession() && SeshApplication.IS_LIVE) {
            fetchSeshInfoAlarm.
                    setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                            FIFTEEN_SECONDS, fetchSeshInfoPendingIntent);

            fetchNotificationsAlarm.
                    setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                            FIFTEEN_SECONDS, fetchNotificationsPendingIntent);
        }
    }

    public void activityPaused() {
        someActivityInForeground = false;
        activityInForeground = null;

        fetchSeshInfoAlarm.cancel(fetchSeshInfoPendingIntent);
        fetchNotificationsAlarm.cancel(fetchNotificationsPendingIntent);
    }

    public boolean applicationInForeground() {
        return someActivityInForeground;
    }

    public Activity getActivityInForeground() {
        return activityInForeground;
    }


    public static void setApplicationResumeListener(ApplicationResumeListener resumeListener) {
        applicationResumeListener = resumeListener;
    }
}

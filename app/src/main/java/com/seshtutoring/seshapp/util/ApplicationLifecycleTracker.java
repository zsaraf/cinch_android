package com.seshtutoring.seshapp.util;

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
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nadavhollander on 7/30/15.
 */
public class ApplicationLifecycleTracker  {
    private static final String TAG = ApplicationLifecycleTracker.class.getName();
    private boolean someActivityInForeground;
    private AlarmManager fetchSeshInfoAlarm;
    private PendingIntent fetchSeshInfoPendingIntent;
    private Context mContext;

    private static final int FIFTEEN_SECONDS = 1000 * 15;

    public ApplicationLifecycleTracker(Context context) {
        this.mContext = context;

        this.fetchSeshInfoAlarm = (AlarmManager) mContext.getSystemService(mContext.ALARM_SERVICE);
        Intent intent = new Intent(PeriodicFetchBroadcastReceiver.PERIODIC_FETCH_ACTION);
        this.fetchSeshInfoPendingIntent =
                PendingIntent.getBroadcast(mContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void activityResumed() {
        someActivityInForeground = true;

        if (SeshAuthManager.sharedManager(mContext).isValidSession() && SeshApplication.IS_LIVE) {
            fetchSeshInfoAlarm.
                    setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                            FIFTEEN_SECONDS, fetchSeshInfoPendingIntent);
        }
    }

    public void activityPaused() {
        someActivityInForeground = false;

        fetchSeshInfoAlarm.cancel(fetchSeshInfoPendingIntent);
    }

    public boolean applicationInForeground() {
        return someActivityInForeground;
    }
}

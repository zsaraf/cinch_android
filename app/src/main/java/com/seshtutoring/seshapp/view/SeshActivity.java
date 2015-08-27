package com.seshtutoring.seshapp.view;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.services.SeshGCMListenerService;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshDialog;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;

/**
 * Created by nadavhollander on 7/31/15.
 */
public abstract class SeshActivity extends AppCompatActivity implements SeshDialog.OnSelectionListener {
    private static final String TAG = SeshActivity.class.getName();

    public static final String APP_IS_LIVE_ACTION = "app_is_live";
    private static final String INTENT_HANDLED = "intent_handled";

    private static final boolean DEFAULT_SUPPORTS_SESH_DIALOG = true;
    private static final boolean DEFAULT_IS_FULLSCREEN = false;
    private static final boolean DEFAULT_IS_IN_SESH_ACTIVITY = false;
    private static final Bitmap DEFAULT_BLUR_BACKGROUND_OVERRIDE = null;

    //    Pre-reg app functionality -- to be deleted v1
    private boolean updateDialogShowing = false;

    private BroadcastReceiver notificationActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleNotificationIntent(intent);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        //    Pre-reg app functionality -- to be deleted v1
        SeshNetworking seshNetworking = new SeshNetworking(this);
        seshNetworking.getAndroidLaunchDate(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try  {
                    if (jsonObject.getString("status").equals("success")) {
                        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss").withZoneUTC();

                        String launchDateString = jsonObject.getString("launch_date");
                        ((SeshApplication)getApplication())
                                .setAndroidReleaseDate(formatter.parseDateTime(launchDateString));

                        if (appUpdateRequired() && !updateDialogShowing && supportsSeshDialog()) {
                            showAppIsLiveDialog();
                        }
                    } else {
                        Log.e(TAG,
                                "Failed to get Android launch date: " + jsonObject.getString("message"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to get Android launch date; response malformed: " + e);
                }
                Log.d(TAG, jsonObject.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "Failed to get android launch date; networkError ");
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(APP_IS_LIVE_ACTION);
        intentFilter.addAction(MainContainerActivity.UPDATE_CONTAINER_STATE_ACTION);
        intentFilter.addAction(MainContainerActivity.SESH_CANCELLED_ACTION);
        registerReceiver(notificationActionReceiver, intentFilter);

        Intent intent = getIntent();
        if (intent.hasExtra(SeshGCMListenerService.NOTIFICATION_ID_EXTRA)) {
            handleNotificationIntent(intent);
        }

//        if (appUpdateRequired() && !updateDialogShowing && supportsSeshDialog()) {
//            showAppIsLiveDialog();
//        }

        ApplicationLifecycleTracker.sharedInstance(this).activityResumed(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        ApplicationLifecycleTracker.sharedInstance(this).activityPaused();
        unregisterReceiver(notificationActionReceiver);
    }

    protected void handleNotificationIntent(Intent intent) {
        if (intent.hasExtra(INTENT_HANDLED) && intent.getBooleanExtra(INTENT_HANDLED, false)) {
            return;
        }

        if (intent.hasExtra(SeshGCMListenerService.NOTIFICATION_ID_EXTRA)) {
            int notificationId = intent.getIntExtra(SeshGCMListenerService.NOTIFICATION_ID_EXTRA, -1);
            ((NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE)).cancel(notificationId);
        }

//        //    Pre-reg app functionality -- to be deleted v1
//        if (intent.getAction() != null && intent.getAction().equals(APP_IS_LIVE_ACTION)) {
//            if (supportsSeshDialog() && !updateDialogShowing) {
//                showAppIsLiveDialog();
//            }
//        }

        intent.putExtra(INTENT_HANDLED, true);
    }

    //    Pre-reg app functionality -- to be deleted v1
    private void showAppIsLiveDialog() {
        final SeshDialog seshDialog = new SeshDialog();
        seshDialog.setContentLayout(getLayoutInflater().inflate(R.layout.sesh_dialog_content_update_app, null));
        seshDialog.setDialogType(SeshDialog.SeshDialogType.ONE_BUTTON);
        seshDialog.setFirstChoice("UPDATE NOW");
        seshDialog.setFirstButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=com.seshtutoring.seshapp"));
                startActivity(intent);
            }
        });

        seshDialog.setCancelable(false);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                seshDialog.show(getFragmentManager(), null);
            }
        }, 1500);

        updateDialogShowing = true;
    }

    //    Pre-reg app functionality -- to be deleted v1
    private boolean appUpdateRequired() {
        return ((SeshApplication)getApplication()).getAndroidReleaseDate().isBeforeNow();
    }

    protected Bitmap getBlurBackgroundOverrideBitmap() {
        return DEFAULT_BLUR_BACKGROUND_OVERRIDE;
    }

    public boolean supportsSeshDialog() {
        return DEFAULT_SUPPORTS_SESH_DIALOG;
    }

    @Override
    public void onDialogSelection(int i, String type) {
        // do nothing
    }

    @Override
    public void startActivity(Intent intent) {
        ApplicationLifecycleTracker.sharedInstance(this).setActivityTransitionInProgress(true);
        super.startActivity(intent);
    }

    public boolean isInSeshActivity() {
        return DEFAULT_IS_IN_SESH_ACTIVITY;
    }

    public boolean isFullscreen() {
        return DEFAULT_IS_FULLSCREEN;
    }
}

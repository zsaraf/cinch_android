package com.seshtutoring.seshapp.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LaunchPrerequisiteAsyncTask;
import com.seshtutoring.seshapp.util.LaunchPrerequisiteAsyncTask.LaunchPrerequisiteFlag;
import com.seshtutoring.seshapp.util.LaunchPrerequisiteAsyncTask.PrereqsFulfilledListener;
import com.seshtutoring.seshapp.util.SeshMixpanelAPI;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.services.UserInfoFetcher.UserInfoSavedListener;
import com.seshtutoring.seshapp.services.UserInfoFetcher.SaveUserInfoAsyncTask;
import com.seshtutoring.seshapp.view.components.SeshDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by nadavhollander on 7/17/15.
 */
public class ConfirmationCodeActivity extends SeshActivity {
    private static final String TAG = ConfirmationCodeActivity.class.getName();

    private String email;
    private String password;

    private SeshNetworking seshNetworking;
    private Timer attemptLoginTimer;

    private SeshMixpanelAPI seshMixpanelAPI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.confirmation_code_activity);

        this.seshMixpanelAPI = ((SeshApplication)getApplication()).getSeshMixpanelAPI();

        Bundle extras = getIntent().getExtras();
        this.email = extras.getString(AuthenticationActivity.SIGN_UP_EMAIL_KEY);
        this.password = extras.getString(AuthenticationActivity.SIGN_UP_PASSWORD_KEY);

        if (email == null || password == null) {
            Log.e(TAG, "Intent from AuthenticationActivity to ConfirmationCodeActivity did not include email/password");
        }

        seshNetworking = new SeshNetworking(this);

        SeshButton resendEmailButton = (SeshButton) findViewById(R.id.resend_email_button);
        resendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seshNetworking.resendVerificationEmail(email, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            if (jsonObject.get("status").equals("SUCCESS")) {
                                showDialog("Verification Email Sent!", "Check your inbox and confirm your email address in order to start Seshing!");
                            } else {
                                showDialog("Error!", jsonObject.getString("message"));
                                Log.e(TAG, jsonObject.getString("message"));
                            }
                        } catch (JSONException e) {
                            showDialog("Network Error", "We couldn't reach the server.  Check your connection and try again!");
                            Log.e(TAG, e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e(TAG, volleyError.toString());
                        showDialog("Network Error", "We couldn't reach the server.  Check your connection and try again!");
                    }
                });
            }
        });
    }

    private void showDialog(String title, String message) {
        SeshDialog seshDialog = new SeshDialog();
        seshDialog.setDialogType(SeshDialog.SeshDialogType.ONE_BUTTON);
        seshDialog.setTitle(title);
        seshDialog.setMessage(message);
        seshDialog.setFirstChoice("OKAY");
        seshDialog.setType("error_dialog");
        seshDialog.show(getFragmentManager(), null);
    }

    // user navigates to different app, presumably to check email
    @Override
    public void onStop() {
        super.onStop();
        attemptLoginTimer.cancel();
    }

    // called both when activity is initially started and if user navigates back to app from different
    // email.
    @Override
    protected void onStart() {
        super.onStart();

        attemptLoginTimer = new Timer();
        attemptLogin();
    }

    private void attemptLogin() {
        Log.i(TAG, "ATTEMPTING LOGIN");
        seshNetworking.loginWithEmail(email, password, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if (jsonObject.get("status").equals("SUCCESS")) {
                        (new SaveUserInfoAsyncTask()).execute(getApplicationContext(), jsonObject, new UserInfoSavedListener() {
                            @Override
                            public void onUserInfoSaved(User user) {
                                seshMixpanelAPI.track("User Verified Signup");

                                if (user.school.enabled) {
                                    Intent intent = new Intent(getApplicationContext(), MainContainerActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(getApplicationContext(), LaunchSchoolActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            }
                        });
                    } else {
                        restartTimer();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.toString());
                    restartTimer();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, volleyError.toString());
                restartTimer();
            }
        });
    }

    private void restartTimer() {
        Log.i(TAG, "RESTARTING TIMER");
        attemptLoginTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                attemptLogin();
            }
        }, 5000);
    }


    //    IF YOU WANT DEFAULT SESH FONTS, INCLUDE THIS METHOD IN YOUR ACTIVITIES
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void onDialogSelection(int selected, String type) {
        // do nothing
    }
}

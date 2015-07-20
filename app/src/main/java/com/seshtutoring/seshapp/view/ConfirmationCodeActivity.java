package com.seshtutoring.seshapp.view;

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
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by nadavhollander on 7/17/15.
 */
public class ConfirmationCodeActivity extends Activity {
    private static final String TAG = ConfirmationCodeActivity.class.getName();

    private String email;
    private String password;

    private SeshNetworking seshNetworking;
    private Timer attemptLoginTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.confirmation_code_activity);

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
                                Toast.makeText(getApplicationContext(), "Verification Email Resent", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_LONG).show();
                                Log.e(TAG, jsonObject.get("message").toString());
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_LONG).show();
                            Log.e(TAG, e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e(TAG, volleyError.toString());
                        Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    // user navigates to different app, presumably to check email
    @Override
    protected void onStop() {
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
                        User.createOrUpdateUserWithObject(jsonObject, getApplicationContext());
                        Intent intent = new Intent(getApplicationContext(), MainContainerActivity.class);
                        startActivity(intent);
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

}

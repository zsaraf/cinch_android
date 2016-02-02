package com.seshtutoring.seshapp.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.model.PastSesh;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.services.GCMRegistrationIntentService;
import com.seshtutoring.seshapp.services.SeshInstanceIDListenerService;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.LocationManager;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshAnimatedCheckmark;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.components.SeshInformationLabel;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by franzwarning on 9/15/15.
 */
public class LaunchSchoolActivity extends SeshActivity implements SeshDialog.OnSelectionListener {

    private static final String TAG = LaunchSchoolActivity.class.getName();

    private TextView schoolName;
    private TextView linePosition;
    private SeshButton becomeRepButton;
    private SeshButton becomeTutorButton;
    private SeshNetworking seshNetworking;
    private SeshActivityIndicator activityIndicator;
    private SeshAnimatedCheckmark animatedCheckmark;
    private RelativeLayout logoutButton;
    private RelativeLayout overlay;
    private BroadcastReceiver broadcastReceiver;

    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_school);

        // Assign the private variables
        this.schoolName = (TextView)findViewById(R.id.school_name);
        this.linePosition = (TextView)findViewById(R.id.line_position);
        this.becomeRepButton = (SeshButton)findViewById(R.id.become_rep_button);
        this.becomeTutorButton = (SeshButton)findViewById(R.id.become_tutor_button);
        this.activityIndicator = (SeshActivityIndicator)findViewById(R.id.launch_activity_indicator);
        this.animatedCheckmark = (SeshAnimatedCheckmark)findViewById(R.id.animated_check_mark);
        this.overlay = (RelativeLayout)findViewById(R.id.launch_school_overlay);
        this.logoutButton = (RelativeLayout) findViewById(R.id.logout_button);

        this.user = User.currentUser(this);

        this.seshNetworking = new SeshNetworking(this);

        // Set the fonts
        LayoutUtils layUtils = new LayoutUtils(this);
        this.linePosition.setTypeface(layUtils.getBookGothamTypeface());
        this.schoolName.setTypeface(layUtils.getBookGothamTypeface());
        ((TextView)findViewById(R.id.text_1)).setTypeface(layUtils.getBookGothamTypeface());
        ((TextView)findViewById(R.id.text_2)).setTypeface(layUtils.getBookGothamTypeface());


        this.becomeTutorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBecomeTutorClick();
            }
        });

        this.becomeRepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBecomeRepClick();
            }
        });

        this.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeshDialog.showDialog(getFragmentManager(), "Logout", "Are you sure you want to logout?", "YES", "CANCEL", "logout");
            }
        });

        setupLabels();

        ApplicationLifecycleTracker.sharedInstance(this).setApplicationLifecycleCallback(new ApplicationLifecycleTracker.ApplicationLifecycleCallback() {
            @Override
            public void applicationDidEnterForeground() {
                SeshNetworking seshNetworking = new SeshNetworking(getApplicationContext());
                seshNetworking.getFullUserInfo(new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            if (jsonObject.getString("status").equals("SUCCESS")) {
                                Context context = getApplicationContext();
                                user = User.createOrUpdateUserWithObject(jsonObject, context);
                                if (user.school.enabled) {
                                    Intent mainContainerIntent = new Intent(getApplicationContext(), MainContainerActivity.class);
                                    startActivity(mainContainerIntent);
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                }

                            }
                        } catch (JSONException e) {

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
            }

            @Override
            public void applicationWillEnterBackground() {

            }
        });
        broadcastReceiver = actionBroadcastReceiver;
    }

    private BroadcastReceiver actionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction() == MainContainerActivity.REFRESH_USER_INFO) {
                User currentUser = User.currentUser(getApplicationContext());
                if (currentUser.school.enabled) {
                    Intent mainContainerIntent = new Intent(getApplicationContext(), MainContainerActivity.class);
                    startActivity(mainContainerIntent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        this.unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainContainerActivity.REFRESH_USER_INFO);
        this.registerReceiver(broadcastReceiver, intentFilter);
    }

    public void onDialogSelection(int selection, String type) {
        if (type.equals("logout") && selection == 1) {
            seshNetworking.logout(
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject responseJson) {
                            onLogoutResponse(responseJson);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            SeshDialog.showDialog(getFragmentManager(), "Whoops!", "Something went wrong. Please try again later.", "OKAY", null, "logout");
                        }
                    });
        }
    }

    private void onLogoutResponse(JSONObject responseJson) {
        try {
            if (responseJson.get("status").equals("SUCCESS")) {
                User.logoutUserLocally(this);
                Intent intent = new Intent(this, AuthenticationActivity.class);
                startActivity(intent);
            } else if (responseJson.get("status").equals("FAILURE")) {
                SeshDialog.showDialog(getFragmentManager(), "Whoops!", responseJson.getString("message"), "OKAY", null, "logout");
            }
        } catch (JSONException e) {
            SeshDialog.showDialog(getFragmentManager(), "Whoops!", "Something went wrong. Please try again later.", "OKAY", null, "logout");

        }
    }

    private void setupLabels() {
        this.schoolName.setText(user.school.schoolName + " is");
        this.linePosition.setText("#" + user.school.linePosition);
    }

    private void onBecomeTutorClick() {
        becomeTutorButton.setEnabled(false);
        overlay.animate().alpha(1).setListener(null).setDuration(300).start();
        this.seshNetworking.sendBecomeATutorEmail(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {

                    if (jsonObject.getString("status").equals("SUCCESS")) {
                        hideAnimationWithSuccess(true, "Email Sent!", becomeTutorButton);
                    } else {
                        hideAnimationWithSuccess(false, jsonObject.getString("message"), becomeTutorButton);
                    }

                } catch (JSONException e) {
                    hideAnimationWithSuccess(false, "Something went wrong. Please try again later.", becomeTutorButton);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                hideAnimationWithSuccess(false, "Something went wrong. Please try again later.", becomeTutorButton);
            }
        });
    }

    private void onBecomeRepClick() {
        becomeRepButton.setEnabled(false);
        overlay.animate().alpha(1).setListener(null).setDuration(300).start();
        this.seshNetworking.becomeCampusRepAtSchool(user.school.schoolName, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {

                    if (jsonObject.getString("status").equals("SUCCESS")) {
                        hideAnimationWithSuccess(true, "Email Sent!", becomeRepButton);
                    } else {
                        hideAnimationWithSuccess(false, jsonObject.getString("message"), becomeRepButton);
                    }

                } catch (JSONException e) {
                    hideAnimationWithSuccess(false, "Something went wrong. Please try again later.", becomeRepButton);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                hideAnimationWithSuccess(false, "Something went wrong. Please try again later.", becomeRepButton);
            }
        });
    }

    private void hideAnimationWithSuccess(final boolean success, final String message, final SeshButton button) {
        if (!success) {
            overlay
                    .animate()
                    .setListener(null)
                    .alpha(0)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            SeshDialog.showDialog(getFragmentManager(), "Whoops!", message, "OKAY", null, "become_rep");
                            button.setEnabled(true);
                        }
                    });
        } else {
            activityIndicator
                    .animate()
                    .alpha(0)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            animatedCheckmark.setLabelText(message);
                            animatedCheckmark.setListener(new SeshAnimatedCheckmark.AnimationCompleteListener() {
                                @Override
                                public void onAnimationComplete() {
                                    button.setEnabled(false);
                                    overlay.animate().alpha(0).setDuration(300).setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            activityIndicator.setAlpha(1.0f);
                                            animatedCheckmark.setAlpha(0.0f);
                                        }
                                    });
                                }
                            });
                            animatedCheckmark.setAlpha(1.0f);
                            animatedCheckmark.startAnimation();
                        }
                    });
        }
    }

}

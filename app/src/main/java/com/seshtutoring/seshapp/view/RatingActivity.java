package com.seshtutoring.seshapp.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.model.PastSesh;
import com.seshtutoring.seshapp.services.notifications.InAppNotificationDisplayQueue;
import com.seshtutoring.seshapp.services.notifications.SeshNotificationManagerService;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.squareup.picasso.Callback;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.DecimalFormat;

import javax.xml.datatype.Duration;

/**
 * Created by nadavhollander on 8/25/15.
 */
public class RatingActivity extends SeshActivity {

    public static final String PAST_SESH_ID = "past_sesh";
    private static final String TAG = RatingActivity.class.getName();

    private TextView tutorName;
    private ImageView profilePicture;
    private TextView hours;
    private TextView hoursDescription;
    private TextView creditsUsed;
    private TextView creditsUsedDescription;
    private TextView cost;
    private TextView costDescription;
    private TextView helpful;
    private TextView knowledge;
    private TextView friendly;
    private SeshButton submitButton;
    private SeshButton reportProblemButton;
    private PastSesh pastSesh;
    private RatingBar helpfulBar;
    private RatingBar knowledgeBar;
    private RatingBar friendlyBar;
    private SeshNetworking seshNetworking;
    private RelativeLayout ratingView;
    private SeshActivityIndicator activityIndicator;
    private GestureDetectorCompat mDetector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rating_activity);

        // Setup the view items
        this.tutorName = (TextView)findViewById(R.id.tutor_name);
        this.profilePicture = (ImageView)findViewById(R.id.profile_picture);
        this.hours = (TextView)findViewById(R.id.hours);
        this.hoursDescription = (TextView)findViewById(R.id.hours_description);
        this.creditsUsed = (TextView)findViewById(R.id.credits_used);
        this.creditsUsedDescription = (TextView)findViewById(R.id.credits_description);
        this.cost = (TextView)findViewById(R.id.cost);
        this.costDescription = (TextView)findViewById(R.id.cost_description);
        this.helpful = (TextView)findViewById(R.id.helpful_text);
        this.knowledge = (TextView)findViewById(R.id.knowledge_text);
        this.friendly = (TextView)findViewById(R.id.friendly_text);
        this.submitButton = (SeshButton)findViewById(R.id.submit_button);
        this.reportProblemButton = (SeshButton)findViewById(R.id.report_problem_button);
        this.helpfulBar = (RatingBar)findViewById(R.id.rating_bar_helpful);
        this.knowledgeBar = (RatingBar)findViewById(R.id.rating_bar_knowledge);
        this.friendlyBar = (RatingBar)findViewById(R.id.rating_bar_friendly);
        this.seshNetworking = new SeshNetworking(this);
        this.ratingView = (RelativeLayout)findViewById(R.id.rating);
        this.activityIndicator = (SeshActivityIndicator)findViewById(R.id.rating_activity_indicator);
        this.mDetector =  new GestureDetectorCompat(this, new FavoritingGestureListener());

        // Set the appropriate fonts
        LayoutUtils layUtils = new LayoutUtils(this);
        this.tutorName.setTypeface(layUtils.getBookGothamTypeface());
        this.hours.setTypeface(layUtils.getLightGothamTypeface());
        this.hoursDescription.setTypeface(layUtils.getLightGothamTypeface());
        this.creditsUsed.setTypeface(layUtils.getLightGothamTypeface());
        this.creditsUsedDescription.setTypeface(layUtils.getLightGothamTypeface());
        this.cost.setTypeface(layUtils.getLightGothamTypeface());
        this.costDescription.setTypeface(layUtils.getLightGothamTypeface());
        this.helpful.setTypeface(layUtils.getBookGothamTypeface());
        this.knowledge.setTypeface(layUtils.getBookGothamTypeface());
        this.friendly.setTypeface(layUtils.getBookGothamTypeface());

        SeshButton seshButton = (SeshButton) findViewById(R.id.submit_button);
        this.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int helpfulRating = Math.round(helpfulBar.getRating());
                int knowledgeRating = Math.round(knowledgeBar.getRating());
                int friendlyRating = Math.round(friendlyBar.getRating());
                Log.d(TAG, "Helpful rating: " + helpfulRating);
                Log.d(TAG, "Knowledge rating: " + knowledgeRating);
                Log.d(TAG, "Friendly rating: " + friendlyRating);

                if (helpfulRating == 0 ||
                        knowledgeRating == 0 ||
                        friendlyRating == 0) {
                    SeshDialog.showDialog(getFragmentManager(),
                            "Whoops!",
                            "Please give the tutor a rating in every category.",
                            "Got It",
                            null,
                            "WHOOPS");
                } else {
                    setNetworkOperationInProgress(true);
                    seshNetworking.submitSeshRating(helpfulRating, knowledgeRating, friendlyRating, false, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            try {
                                if (jsonObject.getString("status").equals("SUCCESS")) {
                                    Notification currentNotification
                                            = InAppNotificationDisplayQueue
                                            .sharedInstance(getApplicationContext())
                                            .getCurrentNotification();
                                    currentNotification.handled(getApplicationContext(), true);
                                    finish();
                                    overridePendingTransition(0, R.anim.slide_down);
                                } else {
                                    setNetworkOperationInProgress(false);

                                    SeshDialog.showDialog(getFragmentManager(), "Whoops",
                                            jsonObject.getString("message"),
                                            "Okay", null, "REPORT_PROBLEM");
                                }
                            } catch (JSONException e) {
                                setNetworkOperationInProgress(false);

                                Log.e(TAG, "Failed to send report problem; JSON malformed: " + e);
                                SeshDialog.showDialog(getFragmentManager(), "Whoops",
                                        "Something went wrong.  Try again later.",
                                        "Okay", null, "error");
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            setNetworkOperationInProgress(false);
                            SeshDialog.showDialog(getFragmentManager(), "Whoops",
                                    "Something went wrong.  Try again later.",
                                    "Okay", null, "error");
                        }
                    });
                }



            }
        });

        Bundle b = getIntent().getExtras();
        final int pastSeshId = b.getInt(PAST_SESH_ID);
        this.pastSesh = PastSesh.find(PastSesh.class, "past_sesh_id = ?", Integer.toString(new Integer(pastSeshId))).get(0);

        this.reportProblemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RatingActivity.this, ReportProblemActivity.class);
                intent.putExtra(RatingActivity.PAST_SESH_ID, pastSeshId);
                RatingActivity.this.startActivity(intent);
             }
        });


        setupLabels();
        setupImageView();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class FavoritingGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        public boolean onDoubleTapEvent(MotionEvent event) {

            // Eventually add your favorite here...
            return true;
        }
    }

    private void setupLabels() {

        DecimalFormat df = new DecimalFormat("#.##");
        this.creditsUsed.setText(df.format(this.pastSesh.creditsUsed));
        this.cost.setText(df.format(this.pastSesh.cost));

        String[] splited = this.pastSesh.tutorFullName.split("\\s+");

        if (splited.length >= 2) {
            this.tutorName.setText("Rate " + splited[0] + " " + splited[1].substring(0, 1).toUpperCase() + ".");
        } else {
            this.tutorName.setText("Rate " + this.pastSesh.tutorFullName);
        }

        // setup the hours
        DateTime startTime = new DateTime(this.pastSesh.startTime);
        DateTime endTime = new DateTime(this.pastSesh.endTime);
        Period diff = new Period(startTime, endTime);

        int hours = diff.getHours();
        int minutes = diff.getMinutes();

        double totalHours = hours + (minutes / 60.0f);
        this.hours.setText(df.format(totalHours));


    }

    private void setupImageView() {
        this.seshNetworking.downloadProfilePictureAsync(this.pastSesh.tutorProfilePicture, this.profilePicture, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

            }
        });
    }

    private void setNetworkOperationInProgress(boolean inProgress) {

        submitButton.setEnabled(!inProgress);

        if (inProgress) {
            ratingView
                    .animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setStartDelay(0)
                    .start();
            activityIndicator
                    .animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setStartDelay(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                        }
                    })
                    .start();
        } else {
            ratingView
                    .animate()
                    .alpha(1f)
                    .setDuration(150)
                    .setStartDelay(0)
                    .start();
            activityIndicator
                    .animate()
                    .alpha(0f)
                    .setDuration(150)
                    .setStartDelay(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                        }
                    })
                    .start();
        }
    }


    @Override
    public void onBackPressed() {
        // do nothing
    }
}


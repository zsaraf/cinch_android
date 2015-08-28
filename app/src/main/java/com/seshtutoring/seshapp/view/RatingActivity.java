package com.seshtutoring.seshapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.model.PastSesh;
import com.seshtutoring.seshapp.services.notifications.InAppNotificationDisplayQueue;
import com.seshtutoring.seshapp.services.notifications.SeshNotificationManagerService;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.squareup.picasso.Callback;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.w3c.dom.Text;

import java.text.DecimalFormat;

import javax.xml.datatype.Duration;

/**
 * Created by nadavhollander on 8/25/15.
 */
public class RatingActivity extends SeshActivity {

    public static final String PAST_SESH_ID = "past_sesh";

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
    private int currentHelpfulValue = 0;
    private int currentKnowledgeValue = 0;
    private int currentFriendlyValue = 0;

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
                Notification currentNotification
                        = InAppNotificationDisplayQueue
                        .sharedInstance(getApplicationContext())
                        .getCurrentNotification();
                currentNotification.handled(getApplicationContext(), true);
                finish();
                overridePendingTransition(0, R.anim.slide_down);
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

    private void setupLabels() {

        DecimalFormat df = new DecimalFormat("#.##");
        this.creditsUsed.setText(df.format(this.pastSesh.creditsUsed));
        this.cost.setText(df.format(this.pastSesh.cost));

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
        this.seshNetworking.downloadProfilePicture(this.pastSesh.tutorProfilePicture, this.profilePicture, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

            }
        });
    }


    @Override
    public void onBackPressed() {
        // do nothing
    }
}


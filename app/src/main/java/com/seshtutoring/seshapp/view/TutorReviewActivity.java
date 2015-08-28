package com.seshtutoring.seshapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.model.PastSesh;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.services.notifications.InAppNotificationDisplayQueue;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshInformationLabel;
import com.squareup.picasso.Callback;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.text.DecimalFormat;

/**
 * Created by nadavhollander on 8/25/15.
 */
public class TutorReviewActivity extends SeshActivity {
    public static final String PAST_SESH_ID = "past_sesh";

    private ImageView profilePicture;
    private SeshInformationLabel cashEarned;
    private SeshInformationLabel className;
    private SeshInformationLabel duration;
    private TextView classTitle;
    private TextView fullName;
    private SeshButton reportProblemButton;
    private PastSesh pastSesh;
    private SeshNetworking seshNetworking;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutor_review_activity);

        // Assign the private variables
        this.profilePicture = (ImageView)findViewById(R.id.profile_picture);
        this.cashEarned = (SeshInformationLabel)findViewById(R.id.money_label);
        this.className = (SeshInformationLabel)findViewById(R.id.class_label);
        this.duration = (SeshInformationLabel)findViewById(R.id.duration_label);
        this.classTitle = (TextView)findViewById(R.id.class_name);
        this.fullName = (TextView)findViewById(R.id.full_name);
        this.reportProblemButton = (SeshButton)findViewById(R.id.report_problem_button);
        this.seshNetworking = new SeshNetworking(this);

        // Set the fonts
        LayoutUtils layUtils = new LayoutUtils(this);
        this.classTitle.setTypeface(layUtils.getBookGothamTypeface());
        this.fullName.setTypeface(layUtils.getBookGothamTypeface());

        SeshButton seshButton = (SeshButton) findViewById(R.id.okay_button);
        seshButton.setOnClickListener(new View.OnClickListener() {
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

        // Fill in the sesh information labels with appropriate content
        Bundle b = getIntent().getExtras();
        final int pastSeshId = b.getInt(PAST_SESH_ID);
        this.pastSesh = PastSesh.find(PastSesh.class, "past_sesh_id = ?", Integer.toString(new Integer(pastSeshId))).get(0);

        this.reportProblemButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TutorReviewActivity.this, ReportProblemActivity.class);
                intent.putExtra(ReportProblemActivity.PAST_SESH_ID, pastSeshId);

                TutorReviewActivity.this.startActivity(intent);
            }

        });

        setupLabels();
        setupImageView();
    }

    private void setupLabels() {
        double tutorEarnings = this.pastSesh.tutorEarnings;
        String className = this.pastSesh.className;
        DateTime startTime = new DateTime(this.pastSesh.startTime);
        DateTime endTime = new DateTime(this.pastSesh.endTime);

        DecimalFormat df = new DecimalFormat("#.##");
        this.cashEarned.setText(df.format(tutorEarnings));

        this.className.setText(className);
        this.fullName.setText(this.pastSesh.studentFullName);

        Period diff = new Period(startTime, endTime);
        PeriodFormatter pf = new PeriodFormatterBuilder()
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendHours()
                .appendSuffix(":")
                .appendMinutes()
                .appendSuffix(":")
                .appendSeconds()
                .toFormatter();
        this.duration.setText(diff.toString(pf));
    }

    private void setupImageView() {
        this.seshNetworking.downloadProfilePicture(this.pastSesh.studentProfilePicture, this.profilePicture, new Callback() {
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

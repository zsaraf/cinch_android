package com.seshtutoring.seshapp.view;

import android.app.ActionBar;
import android.content.Intent;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.services.notifications.InAppNotificationDisplayQueue;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshCircularImageView;
import com.seshtutoring.seshapp.view.components.SeshInformationLabel;

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

        this.reportProblemButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TutorReviewActivity.this, ReportProblemActivity.class);
                TutorReviewActivity.this.startActivity(intent);
            }

        });
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }
}

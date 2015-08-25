package com.seshtutoring.seshapp.view;

import android.os.Bundle;
import android.view.View;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.view.components.SeshButton;

/**
 * Created by nadavhollander on 8/25/15.
 */
public class TutorReviewActivity extends SeshActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutor_review_activity);

        SeshButton seshButton = (SeshButton) findViewById(R.id.review_button);
        seshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Notification.getTopPriorityNotification().handled(getApplicationContext(), true);
                finish();
                overridePendingTransition(0, R.anim.slide_down);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }
}

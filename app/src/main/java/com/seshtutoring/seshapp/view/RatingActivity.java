package com.seshtutoring.seshapp.view;

import android.os.Bundle;
import android.view.View;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.services.notifications.SeshNotificationManagerService;
import com.seshtutoring.seshapp.view.components.SeshButton;

/**
 * Created by nadavhollander on 8/25/15.
 */
public class RatingActivity extends SeshActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rating_activity);

        SeshButton seshButton = (SeshButton) findViewById(R.id.rate_button);
        seshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeshNotificationManagerService.currentNotification.handled(getApplicationContext(), true);
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


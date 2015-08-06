package com.seshtutoring.seshapp.view;


import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.components.SeshButton;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by nadavhollander on 8/4/15.
 */
public class InSeshActivity extends SeshActivity {
    private static final String TAG = InSeshActivity.class.getName();

    private Chronometer timer;
    private Sesh currentSesh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.in_sesh_activity);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.sesh_action_bar);
        getSupportActionBar().setElevation(0);

        this.currentSesh = Sesh.getCurrentSesh();

        ImageButton backButton = (ImageButton) findViewById(R.id.action_bar_back_button);
        ViewGroup layout = (ViewGroup) backButton.getParent();
        layout.removeView(backButton);

        ImageButton homeButton = (ImageButton) findViewById(R.id.action_bar_menu_button);
        layout.removeView(homeButton);

        Typeface light = Typeface.createFromAsset(this.getAssets(), "fonts/Gotham-Light.otf");

        TextView title = (TextView) findViewById(R.id.action_bar_title);
        title.setText("IN SESH");
        title.setTypeface(light);

        SeshButton endSeshButton = (SeshButton) findViewById(R.id.end_sesh_button);
        if (currentSesh.isStudent) {
            endSeshButton.setVisibility(View.INVISIBLE);
        }

        LayoutUtils layUtils = new LayoutUtils(this);
        getWindow().getDecorView().findViewById(android.R.id.content).setPadding(0, layUtils.getActionBarHeightPx(), 0, 0);

        this.timer = (Chronometer) findViewById(R.id.sesh_timer);
        timer.setTypeface(light);

        long elapsedRealtimeOffset = System.currentTimeMillis() - SystemClock.elapsedRealtime();

        timer.setBase(currentSesh.startTime.getTime() - elapsedRealtimeOffset);
        timer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                DateTime elapsedTime = new DateTime(SystemClock.elapsedRealtime() - chronometer.getBase());
                DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss").withZoneUTC();
                chronometer.setText(formatter.print(elapsedTime));
            }
        });

        timer.start();
    }

    @Override
    public void onBackPressed() {
    }
}

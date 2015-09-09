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

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.squareup.picasso.Callback;
import com.seshtutoring.seshapp.view.components.SeshDialog;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nadavhollander on 8/4/15.
 */
public class InSeshActivity extends SeshActivity {
    private static final String TAG = InSeshActivity.class.getName();
    public static final String DIALOG_TYPE_ERROR = "error_dialog";

    private Chronometer timer;
    private Sesh currentSesh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.in_sesh_activity);

        this.currentSesh = Sesh.getCurrentSesh();

        CircleImageView circleImageView = (CircleImageView)findViewById(R.id.profile_image);
        SeshNetworking seshNetworking = new SeshNetworking(getApplicationContext());
        seshNetworking.downloadProfilePictureAsync(this.currentSesh.userImageUrl, circleImageView, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

            }
        });

//        ImageButton backButton = (ImageButton) findViewById(R.id.action_bar_back_button);
//        ViewGroup layout = (ViewGroup) backButton.getParent();
//        layout.removeView(backButton);
//
//        ImageButton homeButton = (ImageButton) findViewById(R.id.action_bar_menu_button);
//        layout.removeView(homeButton);
//
        Typeface light = Typeface.createFromAsset(this.getAssets(), "fonts/Gotham-Light.otf");

        TextView titleTextView = (TextView) findViewById(R.id.title_text_view);
        titleTextView.setText(currentSesh.className.replace(" ", "") + " Sesh");
        titleTextView.setTypeface(light);
//
//        TextView title = (TextView) findViewById(R.id.action_bar_title);
//        title.setText("IN SESH");
//        title.setTypeface(light);

        SeshButton endSeshButton = (SeshButton) findViewById(R.id.end_sesh_button);
        if (currentSesh.isStudent) {
            endSeshButton.setVisibility(View.INVISIBLE);
        } else {
            endSeshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SeshNetworking seshNetworking = new SeshNetworking(getApplicationContext());
                    seshNetworking.endSesh(currentSesh.seshId, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            try {
                                if (jsonObject.getString("status").equals("SUCCESS")) {
                                    // do nothing
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Failed to end sesh; json response malformed: " + e);
                                showErrorDialog("Error!", "Something went wrong.  Try again later.");
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.e(TAG, "Failed to end sesh; network error: " + volleyError);
                            showErrorDialog("Network Error", "Whoops! We couldn't reach the server.  Check your network settings and try again.");
                        }
                    });
                }
            });
        }

        this.timer = (Chronometer) findViewById(R.id.sesh_timer);
        timer.setTypeface(light);

        long elapsedRealtimeOffset = System.currentTimeMillis() - SystemClock.elapsedRealtime();

        timer.setBase(currentSesh.startTime - elapsedRealtimeOffset);
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

    public void showErrorDialog(String title, String content) {
        SeshDialog errorDialog = new SeshDialog();
        errorDialog.setFirstChoice("OKAY");
        errorDialog.setDialogType(SeshDialog.SeshDialogType.ONE_BUTTON);
        errorDialog.setType(DIALOG_TYPE_ERROR);
        errorDialog.setTitle(title);
        errorDialog.setMessage(content);

        errorDialog.show(getFragmentManager(), null);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean isInSeshActivity() {
        return true;
    }
}

package com.seshtutoring.seshapp.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.util.SeshMixpanelAPI;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.components.TextureVideoView;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.json.JSONObject;

import me.brendanweinstein.views.InterceptEditText;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by nadavhollander on 8/6/15.
 */
public class UnreleasedLaunchActivity extends SeshActivity implements SeshDialog.OnSelectionListener {
    private TextView countUpText;
    private int daysUntilLaunch;
    private SeshButton motivateButton;
    private TextureVideoView videoView;
    private FrameLayout videoKeyFrame;
    private SeshMixpanelAPI seshMixpanelAPI;

    private static final String MOTIVATION_BUTTON_LAST_PRESSED = "button_last_pressed";

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        setContentView(R.layout.unreleased_launch_activity);

        this.seshMixpanelAPI = ((SeshApplication)getApplication()).getSeshMixpanelAPI();
        this.countUpText = (TextView) findViewById(R.id.countupText);

        DateTime launchDate =
                ((SeshApplication) getApplication()).getAndroidReleaseDate();
        this.daysUntilLaunch = Math.max(
                Days.daysBetween(new DateTime().toLocalDate(), launchDate.toLocalDate()).getDays(), 0);

        this.countUpText.setText(Integer.toString(daysUntilLaunch));

        final SharedPreferences defaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        this.motivateButton = (SeshButton) findViewById(R.id.motivateButton);
        motivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                motivateButton.setEnabled(false);
                defaultSharedPrefs.edit().putLong(MOTIVATION_BUTTON_LAST_PRESSED, (new DateTime()).getMillis()).apply();

                SeshNetworking seshNetworking = new SeshNetworking(getApplicationContext());
                seshNetworking.motivateTeam(new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        SeshDialog.showDialog(getFragmentManager(),
                                "Motivation Sent",
                                "The Sesh Team has been pinged!  We're working our hardest, we promise!  " + "Don't get tap-happy, though -- motivation can only be sent once a day.",
                                "Gotcha",
                                null, "motivate_the_team");

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        SeshDialog.showDialog(getFragmentManager(), "Network Error",
                                "We couldn't reach the server.  Try again later.", "Okay", null, "networkError");
                        motivateButton.setEnabled(true);
                    }
                });
            }
        });

        if (defaultSharedPrefs.contains(MOTIVATION_BUTTON_LAST_PRESSED)) {
            DateTime lastPressed = new DateTime(defaultSharedPrefs.getLong(MOTIVATION_BUTTON_LAST_PRESSED, 0));
            if (lastPressed.isBefore((new DateTime()).minusDays(1))) {
                motivateButton.setEnabled(true);
            } else {
                motivateButton.setEnabled(false);
            }
        }

        this.videoView = (TextureVideoView) findViewById(R.id.office_time_lapse);
        videoView.setDataSource(this, Uri.parse("android.resource://com.seshtutoring.seshapp/" + R.raw.office_timelapse_small));
        videoView.setLooping(true);
        videoView.play();

        this.videoKeyFrame = (FrameLayout) findViewById(R.id.videoKeyframe);

        seshMixpanelAPI.track("Entered Unreleased Launch Countdown Page");
    }

    //    IF YOU WANT DEFAULT SESH FONTS, INCLUDE THIS METHOD IN YOUR ACTIVITIES
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onDialogSelection(int position, String type) {
        // do nothing
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }

    @Override
    public void onResume() {
        super.onResume();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                videoKeyFrame.animate().alpha(0).setDuration(300).start();
            }
        }, 1000);
    }

    @Override
    public void onPause() {
        videoKeyFrame.setAlpha(1);
        super.onPause();
    }


    @Override
    public Bitmap getBlurBackgroundOverrideBitmap() {
            return BitmapFactory.decodeResource(getResources(),
                    R.drawable.keyframe);
    }

}

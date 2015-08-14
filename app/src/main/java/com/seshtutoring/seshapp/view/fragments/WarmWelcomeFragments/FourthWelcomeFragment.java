package com.seshtutoring.seshapp.view.fragments.WarmWelcomeFragments;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.Auth;
import com.seshtutoring.seshapp.R;

import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.AuthenticationActivity;
import com.seshtutoring.seshapp.view.AuthenticationActivity.EntranceType;
import com.seshtutoring.seshapp.view.WarmWelcomeActivity;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.components.TextureVideoView;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by nadavhollander on 8/10/15.
 */
public class FourthWelcomeFragment  extends Fragment
        implements ValueAnimator.AnimatorUpdateListener {
    private static final String TAG = FourthWelcomeFragment.class.getName();

    private boolean initAnimationShown = false;
    private SeshButton signupStudentButton;
    private SeshButton signupTutorButton;
    private SeshButton motivateButton;
    private TextView countUpText;
    private int daysUntilLaunch;

    private static final String MOTIVATION_BUTTON_LAST_PRESSED = "button_last_pressed";

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        View v = layoutInflater.inflate(R.layout.fourth_welcome_fragment, null);

        TextureVideoView videoView = (TextureVideoView) v.findViewById(R.id.office_time_lapse);
        videoView.setDataSource(getActivity(), Uri.parse("android.resource://com.seshtutoring.seshapp/" + R.raw.office_timelapse_small));
        videoView.setLooping(true);
        videoView.play();

        DateTime launchDate =
                ((SeshApplication) getActivity().getApplication()).getAndroidReleaseDate();
        this.daysUntilLaunch =
                Days.daysBetween(new DateTime().toLocalDate(), launchDate.toLocalDate()).getDays();
        this.countUpText = (TextView) v.findViewById(R.id.countupText);
        this.signupStudentButton = (SeshButton)v.findViewById(R.id.signupStudentButton);
        this.signupTutorButton = (SeshButton)v.findViewById(R.id.signupTutorButton);
        this.motivateButton = (SeshButton)v.findViewById(R.id.motivateButton);

        signupStudentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent authenticationIntent = new Intent(getActivity(), AuthenticationActivity.class);
                authenticationIntent.putExtra(AuthenticationActivity.ENTRANCE_TYPE_KEY, EntranceType.SIGNUP);
                startActivity(authenticationIntent);
            }
        });

        signupTutorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse("https://seshtutoring.com/index.html?action=tutor"));
                startActivity(browserIntent);
            }
        });

        final SharedPreferences defaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        motivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                motivateButton.setEnabled(false);
                defaultSharedPrefs.edit().putLong(MOTIVATION_BUTTON_LAST_PRESSED, (new DateTime()).getMillis()).apply();

                final Bitmap backgroundBitmap = BitmapFactory.decodeResource(getActivity().getResources(),
                        R.drawable.keyframe);

                SeshNetworking seshNetworking = new SeshNetworking(getActivity());
                seshNetworking.motivateTeam(new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        SeshDialog.showDialog(getFragmentManager(),
                                "Motivation Sent",
                                "The Sesh Team has been pinged!  We're working our hardest, we promise!  " + "Don't get tap-happy, though -- motivation can only be sent once a day.",
                                "Gotcha",
                                null, backgroundBitmap, "motivate_the_team");

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        SeshDialog.showDialog(getFragmentManager(), "Network Error",
                                "We couldn't reach the server.  Try again later.", "Okay", null, backgroundBitmap, "networkError");
                        motivateButton.setEnabled(true);
                    }
                });
            }
        });

        if (defaultSharedPrefs.contains(MOTIVATION_BUTTON_LAST_PRESSED)) {
            DateTime lastPressed = new DateTime(defaultSharedPrefs.getLong(MOTIVATION_BUTTON_LAST_PRESSED, 0));
            if (Days.daysBetween(new DateTime().toLocalDate(), lastPressed.toLocalDate()).getDays() < 1) {
                motivateButton.setEnabled(false);
            } else {
                motivateButton.setEnabled(true);
            }
        }

        if (initAnimationShown) {
            countUpText.setText(Integer.toString(daysUntilLaunch));
        }

        return v;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        Integer currentValue = (Integer) animation.getAnimatedValue();
        countUpText.setText(currentValue.toString());

        if (currentValue.intValue() == daysUntilLaunch) {
            initAnimationShown = true;
        }
    }

    public void onFragmentVisible() {
        if (!initAnimationShown) {
            int startVal = Math.max(0, daysUntilLaunch - 10);
            ValueAnimator animator = ValueAnimator.ofInt(startVal, daysUntilLaunch);
            animator.setInterpolator(new DecelerateInterpolator(0.7f));
            animator.setDuration(100 * (daysUntilLaunch - startVal));
            animator.addUpdateListener(this);
            animator.start();
        }
    }

}

package com.seshtutoring.seshapp.view;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Tutor;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TutorTermsActivity extends SeshActivity {

    private SeshButton acceptTermsButton;
    private TextView termsTextView;
    private SeshActivityIndicator seshActivityIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_terms);

        LayoutUtils layoutUtils = new LayoutUtils(this);
        layoutUtils.setupCustomActionBar(this, false);

        RelativeLayout menuButton = (RelativeLayout) findViewById(R.id.action_bar_menu_button);
        ViewGroup layout = (ViewGroup) menuButton.getParent();
        layout.removeView(menuButton);

        RelativeLayout backButton = (RelativeLayout) findViewById(R.id.action_bar_back_button);
        layout = (ViewGroup) backButton.getParent();
        layout.removeView(backButton);

        TextView title = (TextView) findViewById(R.id.action_bar_title);
        title.setText("Accept Terms");
        title.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Gotham-Book.otf"));

        seshActivityIndicator = (SeshActivityIndicator) findViewById(R.id.sesh_activity_indicator);

        termsTextView = (TextView) findViewById(R.id.text_view);
        termsTextView.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Gotham-Book.otf"));

        acceptTermsButton = (SeshButton) findViewById(R.id.accept_button);
        acceptTermsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNetworking(true);
                SeshNetworking seshNetworking = new SeshNetworking(TutorTermsActivity.this);
                seshNetworking.acceptTutorTerms(new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            if (jsonObject.getString("status").equals("SUCCESS")) {
                                Tutor tutor = User.currentUser(TutorTermsActivity.this).tutor;
                                tutor.didAcceptTerms = true;
                                tutor.save();
                                finish();
                            } else {
                                showNetworkingErrorWithTitle("Error!", jsonObject.getString("message"));
                            }
                        } catch (JSONException e) {
                            showNetworkingErrorWithTitle("Error!", null);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        showNetworkingErrorWithTitle("Error!", "Check your internet connection and try again!");
                    }
                });
            }
        });

        InputStream inputStream = getResources().openRawResource(R.raw.tutor_agreement);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        String newLine = "";
        try {
            line = reader.readLine();
            while (line != null) {
                newLine += line + "\n";
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        termsTextView.setText(newLine);
    }

    private void setNetworking(Boolean networking) {
        acceptTermsButton.setEnabled(!networking);

        termsTextView
                .animate()
                .alpha(networking ? 0f : 1f)
                .setDuration(300)
                .setStartDelay(0)
                .start();

        seshActivityIndicator
                .animate()
                .alpha(networking ? 1f : 0f)
                .setDuration(300)
                .setStartDelay(0)
                .start();
    }

    private void showNetworkingErrorWithTitle(String title, String message) {
        setNetworking(false);
        if (message == null) {
            message = "Something went wrong. Please try again later.";
        }
        SeshDialog.showDialog(this.getFragmentManager(), title,
                message,
                "OKAY", null, "error");
    }
}

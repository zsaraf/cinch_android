package com.seshtutoring.seshapp.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshDialog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by franzwarning on 8/26/15.
 */
public class ReportProblemActivity extends SeshActivity {

    private TextView uhoh;
    private TextView description;
    private EditText commentField;
    private static final String TAG = MainContainerActivity.class.getName();
    private SeshActivityIndicator activityIndicator;
    private SeshNetworking seshNetworking;
    private SeshButton submitButton;
    private SeshButton cancelButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_problem_activity);

        // Set the shite
        this.uhoh = (TextView)findViewById(R.id.textView);
        this.description = (TextView)findViewById(R.id.textView2);
        this.commentField = (EditText)findViewById(R.id.problem_text_field);
        this.activityIndicator = (SeshActivityIndicator)findViewById(R.id.report_activity_indicator);
        this.seshNetworking = new SeshNetworking(this);

        // Set the fonts
        LayoutUtils layUtils = new LayoutUtils(this);
        this.uhoh.setTypeface(layUtils.getBookGothamTypeface());
        this.description.setTypeface(layUtils.getBookGothamTypeface());
        this.commentField.setTypeface(layUtils.getBookGothamTypeface());

        this.commentField.setHintTextColor(getResources().getColor(R.color.seshlightgray));

        this.submitButton = (SeshButton) findViewById(R.id.submit_button);
        this.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if there is any text in edit text
                String problem = commentField.getText().toString();
                if (problem.equals("")) {
                    String message = "So you're telling me there was no problem? Enter some text and try again.";
                    SeshDialog.showDialog(getFragmentManager(),
                            "Whoops!",
                            message,
                            "Got It",
                            null,
                            "WHOOPS");
                } else {
                    // Report the problem
                    getWindow().getCurrentFocus().clearFocus();
                    setNetworkOperationInProgress(true);
                    Log.d(TAG, "Reporting problem: " + problem);
                    seshNetworking.reportProblem(problem, 173, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            try {
                                if (jsonObject.getString("status").equals("SUCCESS")) {
                                    finish();
                                } else {
                                    setNetworkOperationInProgress(false);

                                    SeshDialog.showDialog(getFragmentManager(), "Whoops",
                                            jsonObject.getString("message"),
                                            "Okay", null, "REPORT_PROBLEM");
                                }
                            } catch (JSONException e) {
                                setNetworkOperationInProgress(false);

                                Log.e(TAG, "Failed to send report problem; JSON malformed: " + e);
                                SeshDialog.showDialog(getFragmentManager(), "Whoops",
                                        "Something went wrong.  Try again later.",
                                        "Okay", null, "error");
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            setNetworkOperationInProgress(false);
                            SeshDialog.showDialog(getFragmentManager(), "Whoops",
                                    "Something went wrong.  Try again later.",
                                    "Okay", null, "error");
                        }
                    });
                }

            }
        });

        this.cancelButton = (SeshButton)findViewById(R.id.cancel_button);
        this.cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }

        });
    }

    private void setNetworkOperationInProgress(boolean inProgress) {

        submitButton.setEnabled(!inProgress);
        cancelButton.setEnabled(!inProgress);

        if (inProgress) {
            commentField
                    .animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setStartDelay(0)
                    .start();
            activityIndicator
                    .animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setStartDelay(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                        }
                    })
                    .start();
        } else {
            commentField
                    .animate()
                    .alpha(1f)
                    .setDuration(150)
                    .setStartDelay(0)
                    .start();
            activityIndicator
                    .animate()
                    .alpha(0f)
                    .setDuration(150)
                    .setStartDelay(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                        }
                    })
                    .start();
        }
    }


    @Override
    public void onBackPressed() {
        // do nothing
        //finish();
    }
}

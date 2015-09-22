package com.seshtutoring.seshapp.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshAnimatedCheckmark;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.components.SeshEditText;

import org.json.JSONException;
import org.json.JSONObject;

import me.brendanweinstein.util.ViewUtils;

public class ChangePasswordActivity extends SeshActivity {

    private static final String TAG = ChangePasswordActivity.class.getName();


    private SeshEditText oldPasswordField;
    private SeshEditText newPasswordField;
    private SeshEditText confirmPasswordField;
    private SeshButton changePasswordButton;
    private SeshNetworking seshNetworking;
    private RelativeLayout requestFlowOverlay;
    private SeshActivityIndicator activityIndicator;
    private SeshAnimatedCheckmark animatedCheckmark;
    private User user;
    private ChangePasswordActivity changePasswordActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        LayoutUtils layUtils = new LayoutUtils(this);
        layUtils.setupCustomActionBar(this, true);

        TextView title = (TextView) findViewById(R.id.action_bar_title);
        title.setText("Change Password");
        title.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Gotham-Book.otf"));

        RelativeLayout menuButton = (RelativeLayout) findViewById(R.id.action_bar_menu_button);
        ViewGroup layout = (ViewGroup) menuButton.getParent();
        layout.removeView(menuButton);

        this.requestFlowOverlay = (RelativeLayout) findViewById(R.id.request_flow_overlay);
        this.activityIndicator = (SeshActivityIndicator) findViewById(R.id.request_activity_indicator);
        this.animatedCheckmark = (SeshAnimatedCheckmark) findViewById(R.id.animated_check_mark);

        RelativeLayout backButton = (RelativeLayout) findViewById(R.id.action_bar_back_button);
        backButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    onBackPressed();
                }
                return true;
            }
        });


        this.oldPasswordField = (SeshEditText) findViewById(R.id.old_password);
        this.newPasswordField = (SeshEditText) findViewById(R.id.new_password);
        this.confirmPasswordField = (SeshEditText) findViewById(R.id.confirm_password);

        oldPasswordField.setKeyDownListener(new Runnable() {
            @Override
            public void run() {
                onBackPressed();
            }
        });
        newPasswordField.setKeyDownListener(new Runnable() {
            @Override
            public void run() {
                onBackPressed();
            }
        });
        confirmPasswordField.setKeyDownListener(new Runnable() {
            @Override
            public void run() {
                onBackPressed();
            }
        });
        this.changePasswordButton = (SeshButton) findViewById(R.id.change_password_button);
        this.user = User.currentUser(this);
        this.seshNetworking = new SeshNetworking(this);
        this.changePasswordActivity = this;

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ViewUtils.hideSoftKeyboard(ChangePasswordActivity.this);
                String oldPassword = oldPasswordField.getText().toString();
                String newPassword = newPasswordField.getText().toString();
                String confirmPassword = confirmPasswordField.getText().toString();

                if(newPassword.length() < 5) {
                    showErrorDialog("Whoops!", "Password must be greater than 5 characters.");
                    return;
                } else if (!newPassword.equals(confirmPassword)) {
                    showErrorDialog("Whoops!", "Password confirmation does not match.");
                    return;
                }

                requestFlowOverlay.animate().alpha(1).setListener(null).setDuration(300).start();

                seshNetworking.changePassword(oldPassword, newPassword,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject responseJson) {
                                onChangePasswordResponse(responseJson);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                onChangePasswordFailure(volleyError.getMessage());
                            }
                        });
            }
        });
    }

    private void onChangePasswordResponse(JSONObject responseJson) {
        try {
            if (responseJson.get("status").equals("SUCCESS")) {
                hideAnimationWithSuccess(true, "");
                //onBackPressed();
//                Intent intent = new Intent(this, MainContainerActivity.class);
//                startActivity(intent);
            } else if (responseJson.get("status").equals("FAILURE")) {
                String message = responseJson.get("message").toString();
                hideAnimationWithSuccess(false, responseJson.getString("message"));
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            hideAnimationWithSuccess(false, "Error changing password - please try again later.");
        }
    }

    private void onChangePasswordFailure(String errorMessage) {
        Log.e(TAG, "NETWORK ERROR: " + errorMessage);
        hideAnimationWithSuccess(false, "Error changing password - please try again later.");
    }

    private void hideAnimationWithSuccess(final boolean success, final String message) {
        if (!success) {
            requestFlowOverlay
                    .animate()
                    .setListener(null)
                    .alpha(0)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            showErrorDialog("Whoops!", message);
                        }
                    });
        } else {
            activityIndicator
                    .animate()
                    .alpha(0)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            animatedCheckmark.setListener(new SeshAnimatedCheckmark.AnimationCompleteListener() {
                                @Override
                                public void onAnimationComplete() {
                                    //setResult(PASSWORD_CHANGED_SUCCESSFULLY_RESPONSE_CODE, null);
                                    finish();
                                }
                            });
                            animatedCheckmark.startAnimation();

                        }
                    });
        }
    }

    private void showErrorDialog(String title, String message) {
        SeshDialog.showDialog(getFragmentManager(), title, message,
                "OKAY", null, "view_request_network_error");
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    @Override
    public void onBackPressed() {

        if (oldPasswordField.hasFocus()) {
            oldPasswordField.clearFocus();
            hideKeyboard();
        } else if (newPasswordField.hasFocus()) {
            newPasswordField.clearFocus();
            hideKeyboard();
        } else if (confirmPasswordField.hasFocus()) {
            confirmPasswordField.clearFocus();
            hideKeyboard();
        } else {
            finish();
        }
    }


}



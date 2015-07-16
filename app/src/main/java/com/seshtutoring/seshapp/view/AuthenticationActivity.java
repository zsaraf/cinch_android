package com.seshtutoring.seshapp.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshEditText;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AuthenticationActivity extends Activity {
    private static final String TAG = AuthenticationActivity.class.getName();
    public static enum EntranceType { LOGIN, SIGNUP }
    public static final String ENTRANCE_TYPE_KEY = "entrance_type";

    private SeshNetworking seshNetworking;
    private EntranceType entranceType;
    private SeshEditText fullnameEditText;
    private SeshEditText emailEditText;
    private SeshEditText passwordEditText;
    private SeshEditText reenterPasswordEditText;
    private LinearLayout dontHaveAccountText;
    private LinearLayout alreadyHaveAccountText;
    private SeshButton loginSignupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.entranceType = (EntranceType) getIntent().getSerializableExtra(ENTRANCE_TYPE_KEY);

        if (entranceType == null) {
            Log.e(TAG, "Intent does not specify EntranceType for AuthenticationActivity");
            this.entranceType = EntranceType.LOGIN;
        }

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.authentication_activity);

        loginSignupButton = (SeshButton) findViewById(R.id.loginSignupButton);

        this.fullnameEditText = (SeshEditText) findViewById(R.id.fullNameEditText);
        this.emailEditText = (SeshEditText) findViewById(R.id.emailEditText);
        this.passwordEditText = (SeshEditText) findViewById(R.id.passwordEditText);
        this.reenterPasswordEditText = (SeshEditText) findViewById(R.id.reenterPasswordEditText);

        this.dontHaveAccountText = (LinearLayout) findViewById(R.id.dont_have_account_text);
        this.alreadyHaveAccountText = (LinearLayout) findViewById(R.id.already_have_account_text);

        this.seshNetworking = new SeshNetworking(this);

        loginSignupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (loginDetailsValid(email, password)) {
                    seshNetworking.loginWithEmail(email, password,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject responseJson) {
                                    onLoginResponse(responseJson);
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    onLoginFailure(volleyError.getMessage());
                                }
                            });
                }
            }
        });

        TextView registerLink = (TextView) findViewById(R.id.register_link);
//        @TODO: implement forgot password link
//        TextView forgotPasswordLink = (TextView) findViewById(R.id.forgot_password_link);

        registerLink.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getBaseContext(), "yeaa", Toast.LENGTH_LONG).show();
                return true;
            }
        });

        setupEntranceType();
    }

    private void onLoginResponse(JSONObject responseJson) {
        try {
            if (responseJson.get("status").equals("SUCCESS")) {
                User.createOrUpdateUserWithObject(responseJson, this);
                Intent intent = new Intent(this, MainContainerActivity.class);
                startActivity(intent);
            } else if (responseJson.get("status").equals("UNVERIFIED")) {
                Toast.makeText(this, "unverified account", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Login Failed.", Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(this, "Login Failed.", Toast.LENGTH_LONG).show();
        }
    }

    private void onLoginFailure(String errorMessage) {
        Log.e(TAG, "NETWORK ERROR: " + errorMessage);
        Toast.makeText(this, "We couldn't reach the network, sorry!", Toast.LENGTH_LONG);
    }

    // @TODO: Implement some sort of verification that Login info formatted correctly
    private boolean loginDetailsValid(String email, String password) {
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void setupEntranceType() {
        if (entranceType == EntranceType.LOGIN) {
            fullnameEditText.setVisibility(View.INVISIBLE);
            reenterPasswordEditText.setVisibility(View.INVISIBLE);
            alreadyHaveAccountText.setVisibility(View.INVISIBLE);
            loginSignupButton.setText("Log in");

            ViewGroup.MarginLayoutParams fullNameLayoutParams =
                    (ViewGroup.MarginLayoutParams) fullnameEditText.getLayoutParams();
            ViewGroup.MarginLayoutParams reenterPasswordLayoutParams =
                    (ViewGroup.MarginLayoutParams) reenterPasswordEditText.getLayoutParams();

            fullNameLayoutParams.height = 0;
            reenterPasswordLayoutParams.height = 0;
            reenterPasswordLayoutParams.setMargins(0, 0, 0, 0);
        } else if (entranceType == EntranceType.SIGNUP) {
            dontHaveAccountText.setVisibility(View.INVISIBLE);
            loginSignupButton.setText("Sign up");
        } else {
            Log.e(TAG, "EntranceType malformed in intent to start AuthenticationActivity");
        }
    }

    public void toggleEntranceTypeWithAnimation() {
        if (entranceType == EntranceType.LOGIN) {

        } else if (entranceType == EntranceType.SIGNUP) {

        } else {

        }
    }
}

package com.seshtutoring.seshapp.view;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LoginSignupManager;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthenticationActivity extends Activity {
    private static final String TAG = AuthenticationActivity.class.getName();

    private SeshNetworking seshNetworking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.authentication_activity);

        Button loginButton = (Button) findViewById(R.id.loginButton);
        Button signupButton = (Button) findViewById(R.id.signupButton);

        final EditText emailEditText = (EditText) findViewById(R.id.emailEditText);
        final EditText passwordEditText = (EditText) findViewById(R.id.passwordEditText);

        this.seshNetworking = new SeshNetworking(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
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
                } else {
                    onLoginInformationMalformed("Login info is malformed.");
                }
            }
        });
    }

    private void onLoginResponse(JSONObject responseJson) {
        try {
            if (responseJson.get("status").equals("SUCCESS")) {
                User.createOrUpdateUserWithObject(responseJson);
                Toast.makeText(this, "yeaaa login success", Toast.LENGTH_LONG);
            } else if (responseJson.get("status").equals("UNVERIFIED")) {
                Toast.makeText(this, "unverified account", Toast.LENGTH_LONG);
            } else {
                Toast.makeText(this, "Login Failed.", Toast.LENGTH_LONG);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(this, "Login Failed.", Toast.LENGTH_LONG);
        }
    }

    private void onLoginFailure(String errorMessage) {
        Toast.makeText(this, "We couldn't reach the network, sorry!", Toast.LENGTH_LONG);
    }

    private void onLoginInformationMalformed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG);
    }

    private boolean loginDetailsValid(String email, String password) {
        return true;
    }
}

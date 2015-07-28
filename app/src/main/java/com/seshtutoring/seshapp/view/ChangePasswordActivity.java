package com.seshtutoring.seshapp.view;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshEditText;

import org.json.JSONException;
import org.json.JSONObject;

public class ChangePasswordActivity extends ActionBarActivity {

    private static final String TAG = ChangePasswordActivity.class.getName();

    private SeshEditText oldPasswordField;
    private SeshEditText newPasswordField;
    private SeshEditText confirmPasswordField;
    private SeshButton changePasswordButton;
    private SeshNetworking seshNetworking;
    private User user;
    private ChangePasswordActivity changePasswordActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        LayoutUtils layUtils = new LayoutUtils(this);
        getWindow().getDecorView().findViewById(android.R.id.content).setPadding(0, layUtils.getActionBarHeightPx(), 0, 0);

//        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.sesh_action_bar);
        getSupportActionBar().setElevation(0);
        TextView title = (TextView) findViewById(R.id.action_bar_title);
        title.setText("Change Password");
        title.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Gotham-Book.otf"));

        ImageButton menuButton = (ImageButton) findViewById(R.id.action_bar_menu_button);
        ViewGroup layout = (ViewGroup) menuButton.getParent();
        layout.removeView(menuButton);

        ImageButton backButton = (ImageButton) findViewById(R.id.action_bar_back_button);
        backButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    onBackPressed();
                    return true;
                }
                return false;
            }
        });


        this.oldPasswordField = (SeshEditText) findViewById(R.id.old_password);
        this.newPasswordField = (SeshEditText) findViewById(R.id.new_password);
        this.confirmPasswordField = (SeshEditText) findViewById(R.id.confirm_password);
        this.changePasswordButton = (SeshButton) findViewById(R.id.change_password_button);
        this.user = User.currentUser(this);
        this.seshNetworking = new SeshNetworking(this);
        this.changePasswordActivity = this;

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String oldPassword = oldPasswordField.getText().toString();
                String newPassword = newPasswordField.getText().toString();
                String confirmPassword = confirmPasswordField.getText().toString();
                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(changePasswordActivity, "Confirmation does not match new password.", Toast.LENGTH_LONG).show();
                }else {
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
            }
        });
    }

    private void onChangePasswordResponse(JSONObject responseJson) {
        try {
            if (responseJson.get("status").equals("SUCCESS")) {
                Toast.makeText(this, "Password updated!", Toast.LENGTH_LONG).show();
                onBackPressed();
//                Intent intent = new Intent(this, MainContainerActivity.class);
//                startActivity(intent);
            } else if (responseJson.get("status").equals("FAILURE")) {
                String message = responseJson.get("message").toString();
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(this, "Change Password Failed.", Toast.LENGTH_LONG).show();
        }
    }

    private void onChangePasswordFailure(String errorMessage) {
        Log.e(TAG, "NETWORK ERROR: " + errorMessage);
        Toast.makeText(this, "We couldn't reach the network, sorry!", Toast.LENGTH_LONG);
    }


}



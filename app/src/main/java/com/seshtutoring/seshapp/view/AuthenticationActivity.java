package com.seshtutoring.seshapp.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AuthenticationActivity extends Activity {
    private static final String TAG = AuthenticationActivity.class.getName();
    public static enum EntranceType { LOGIN, SIGNUP }
    public static final String ENTRANCE_TYPE_KEY = "entrance_type";
    public static final String SIGN_UP_EMAIL_KEY = "email";
    public static final String SIGN_UP_PASSWORD_KEY = "password";
    private static final int EDITTEXT_BOTTOM_MARGIN_DP = 10;
    private static final int TERMS_TEXT_OFFSET_DP = 30;

    private SeshNetworking seshNetworking;
    private EntranceType entranceType;
    private SeshEditText fullnameEditText;
    private SeshEditText emailEditText;
    private SeshEditText passwordEditText;
    private SeshEditText reenterPasswordEditText;
    private SeshEditText dummyEditText;
    private LinearLayout dontHaveAccountText;
    private LinearLayout alreadyHaveAccountText;
    private TextView forgotPasswordLink;
    private TextView termsAndPrivacyPolicyText;
    private SeshButton loginSignupButton;
    private ImageView seshLogo;

    private LinearLayout.MarginLayoutParams fullnameMargins;
    private LinearLayout.MarginLayoutParams emailMargins;
    private LinearLayout.MarginLayoutParams passwordMargins;
    private LinearLayout.MarginLayoutParams reenterPasswordMargins;
    private LinearLayout.MarginLayoutParams logoMargins;


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

        this.seshLogo = (ImageView) findViewById(R.id.seshLogo);

        loginSignupButton = (SeshButton) findViewById(R.id.loginSignupButton);

        this.fullnameEditText = (SeshEditText) findViewById(R.id.fullNameEditText);
        this.emailEditText = (SeshEditText) findViewById(R.id.emailEditText);
        this.passwordEditText = (SeshEditText) findViewById(R.id.passwordEditText);
        this.reenterPasswordEditText = (SeshEditText) findViewById(R.id.reenterPasswordEditText);
        this.dummyEditText = (SeshEditText) findViewById(R.id.dummyEditText);

        this.dontHaveAccountText = (LinearLayout) findViewById(R.id.dont_have_account_text);
        this.alreadyHaveAccountText = (LinearLayout) findViewById(R.id.already_have_account_text);
        this.forgotPasswordLink = (TextView) findViewById(R.id.forgot_password_link);
        this.termsAndPrivacyPolicyText = (TextView) findViewById(R.id.terms_and_privacy_text);

        this.seshNetworking = new SeshNetworking(this);

        loginSignupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loginSignupButton.setEnabled(false);
                if (entranceType == EntranceType.LOGIN) {
                    handleLogin();
                } else if (entranceType == EntranceType.SIGNUP) {
                    handleSignup();
                } else {
                    Log.e(TAG, "Entrance type is not set.");
                }
            }
        });

        TextView registerLink = (TextView) findViewById(R.id.register_link);
        TextView loginLink = (TextView) findViewById(R.id.login_link);
//        @TODO: implement forgot password link
//        TextView forgotPasswordLink = (TextView) findViewById(R.id.forgot_password_link);

        registerLink.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (entranceType == EntranceType.LOGIN) {
                    toggleEntranceTypeWithAnimation();
                }
                return true;
            }
        });

        loginLink.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (entranceType == EntranceType.SIGNUP) {
                    toggleEntranceTypeWithAnimation();
                }
                return true;
            }
        });
        Spannable spannable = new SpannableString(getResources().getString(R.string.terms_label));

        LayoutUtils.NoUnderlineClickableSpan termsLinkClickableSpan = new LayoutUtils.NoUnderlineClickableSpan() {
            @Override
            public void onClick(View widget) {
                // @TODO: implement terms link
            }
        };
        LayoutUtils.NoUnderlineClickableSpan privacyPolicyLink = new LayoutUtils.NoUnderlineClickableSpan() {
            @Override
            public void onClick(View widget) {
                // @TODO: implement privacy policy link
            }
        };

        ForegroundColorSpan termsLinksColor = new ForegroundColorSpan(getResources().getColor(R.color.seshorange));
        ForegroundColorSpan privacyLinkColor = new ForegroundColorSpan(getResources().getColor(R.color.seshorange));

        spannable.setSpan(termsLinkClickableSpan, 40, 52, Spanned.SPAN_POINT_MARK);
        spannable.setSpan(termsLinksColor, 40, 52, 0);
        spannable.setSpan(privacyPolicyLink, 96, 110, Spanned.SPAN_POINT_MARK);
        spannable.setSpan(privacyLinkColor, 96, 110, 0);

        termsAndPrivacyPolicyText.setText(spannable);

        setupEntranceType();
    }

    private void handleLogin() {

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (validateLoginDetails(email, password)) {
            seshNetworking.loginWithEmail(email, password,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject responseJson) {
                            onLoginResponse(responseJson);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            onNetworkError(volleyError);
                            loginSignupButton.setEnabled(true);
                        }
                    });
        }
    }

    private void handleSignup() {
        String fullName = fullnameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String reenterPassword = reenterPasswordEditText.getText().toString();

        if (validateSignupDetails(fullName, email, password, reenterPassword)) {
            seshNetworking.signUpWithName(fullName, email, password,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            onSignupResponse(jsonObject);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            onNetworkError(volleyError);
                        }
                    });
        }
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
        loginSignupButton.setEnabled(true);
    }

    private void onSignupResponse(JSONObject responseJson) {
        try {
            if (responseJson.get("status").equals("FAILURE")) {
                Toast.makeText(this, responseJson.getString("message"), Toast.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(this, ConfirmationCodeActivity.class);
                intent.putExtra(SIGN_UP_EMAIL_KEY, emailEditText.getText());
                intent.putExtra(SIGN_UP_PASSWORD_KEY, passwordEditText.getText());
                startActivity(intent);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(this, "Signup Failed.", Toast.LENGTH_LONG).show();
        }
        loginSignupButton.setEnabled(true);
    }

    private void onNetworkError(VolleyError volleyError) {
        Log.e(TAG, "NETWORK ERROR: " + volleyError.getMessage());
        Toast.makeText(getApplicationContext(), "We couldn't reach the network, sorry!", Toast.LENGTH_LONG);
        loginSignupButton.setEnabled(true);
    }


    // @TODO: Implement some sort of verification that Login info formatted correctly
    private boolean validateLoginDetails(String email, String password) {
        return true;
    }

    // @TODO: Implement some sort of verification that Login info formatted correctly
    private boolean validateSignupDetails(String fullName, String email,
                                          String password, String reenterPassword) {
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void setupEntranceType() {
        LayoutUtils utils = new LayoutUtils(this);

        if (entranceType == EntranceType.LOGIN) {
            fullnameEditText.setAlpha(0);
            reenterPasswordEditText.setAlpha(0);
            alreadyHaveAccountText.setAlpha(0);
            termsAndPrivacyPolicyText.setAlpha(0);

            loginSignupButton.setText("Log in");
        } else if (entranceType == EntranceType.SIGNUP) {
            int yDelta =
                    utils.dpToPixels(EDITTEXT_BOTTOM_MARGIN_DP + TERMS_TEXT_OFFSET_DP +
                            SeshEditText.SESH_EDIT_TEXT_HEIGHT_DP);

            loginSignupButton.animate().setDuration(0)
                    .translationYBy(-1 * utils.dpToPixels(TERMS_TEXT_OFFSET_DP));
            alreadyHaveAccountText.animate().setDuration(0)
                    .translationYBy(-1 * utils.dpToPixels(TERMS_TEXT_OFFSET_DP));
            emailEditText.animate().setDuration(0).translationYBy(-1 * yDelta);
            passwordEditText.animate().setDuration(0).translationYBy(-1 * yDelta);
            seshLogo.animate().setDuration(0).translationYBy(-1 * yDelta);

            dontHaveAccountText.setAlpha(0);
            forgotPasswordLink.setAlpha(0);
            loginSignupButton.setText("Sign up");
        } else {
            Log.e(TAG, "EntranceType malformed in intent to start AuthenticationActivity");
        }
    }

    public void toggleEntranceTypeWithAnimation() {
        LayoutUtils utils = new LayoutUtils(this);
        final int yDelta =
                utils.dpToPixels(EDITTEXT_BOTTOM_MARGIN_DP + TERMS_TEXT_OFFSET_DP +
                        SeshEditText.SESH_EDIT_TEXT_HEIGHT_DP);

        if (entranceType == EntranceType.LOGIN) {
            loginSignupButton.setText("Sign Up");

            utils.crossFade(dontHaveAccountText, alreadyHaveAccountText);
            utils.crossFade(forgotPasswordLink, termsAndPrivacyPolicyText);

            loginSignupButton.animate().setDuration(300)
                    .translationYBy(-1 * utils.dpToPixels(TERMS_TEXT_OFFSET_DP));
            alreadyHaveAccountText.animate().setDuration(300)
                    .translationYBy(-1 * utils.dpToPixels(TERMS_TEXT_OFFSET_DP));
            emailEditText.animate().setDuration(300).translationYBy(-1 * yDelta);
            passwordEditText.animate().setDuration(300).translationYBy(-1 * yDelta);
            seshLogo.animate().setDuration(300).translationYBy(-1 * yDelta);

            fullnameEditText.animate().setStartDelay(300).setDuration(300).alpha(1);
            reenterPasswordEditText.animate().setStartDelay(300).setDuration(300).alpha(1);

            entranceType = EntranceType.SIGNUP;
        } else if (entranceType == EntranceType.SIGNUP) {
            loginSignupButton.setText("Log In");

            utils.crossFade(alreadyHaveAccountText, dontHaveAccountText);
            utils.crossFade(termsAndPrivacyPolicyText, forgotPasswordLink);

            fullnameEditText.animate().setStartDelay(0).setDuration(100).alpha(0);
            reenterPasswordEditText.animate().setStartDelay(0).setDuration(100).alpha(0);

            loginSignupButton.animate().setDuration(300)
                    .translationYBy(utils.dpToPixels(TERMS_TEXT_OFFSET_DP));
            alreadyHaveAccountText.animate().setDuration(300)
                    .translationYBy(utils.dpToPixels(TERMS_TEXT_OFFSET_DP));
            emailEditText.animate().setDuration(300).translationYBy(yDelta);
            passwordEditText.animate().setDuration(300).translationYBy(yDelta);
            seshLogo.animate().setDuration(300).translationYBy(yDelta);


            entranceType = EntranceType.LOGIN;
        } else {
            Log.e(TAG, "No entrance type is specified.");
        }

    }
}

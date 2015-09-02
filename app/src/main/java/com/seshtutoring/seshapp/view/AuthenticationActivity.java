package com.seshtutoring.seshapp.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.services.GCMRegistrationIntentService;
import com.seshtutoring.seshapp.services.SeshInstanceIDListenerService;
import com.seshtutoring.seshapp.util.LaunchPrerequisiteAsyncTask;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.SeshMixpanelAPI;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.components.SeshEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AuthenticationActivity extends SeshActivity implements SeshDialog.OnSelectionListener {
    private static final String TAG = AuthenticationActivity.class.getName();
    public static enum EntranceType { LOGIN, SIGNUP }
    public static final String ENTRANCE_TYPE_KEY = "entrance_type";
    public static final String SIGN_UP_EMAIL_KEY = "email";
    public static final String SIGN_UP_PASSWORD_KEY = "password";
    private static final int EDITTEXT_BOTTOM_MARGIN_DP = 10;
    private static final int TERMS_TEXT_OFFSET_DP = 30;
    private static final int ACCOUNT_TEXT_OFFSET_DP = 102;
    private static final String DIALOG_TYPE_ERROR = "dialog_type_error";

    private SeshNetworking seshNetworking;
    private EntranceType entranceType;
    private SeshEditText fullnameEditText;
    private SeshEditText emailEditText;
    private SeshEditText passwordEditText;
    private SeshEditText reenterPasswordEditText;
    private SeshEditText dummyEditTextTop;
    private LinearLayout dontHaveAccountText;
    private LinearLayout alreadyHaveAccountText;
    private TextView forgotPasswordLink;
    private TextView termsAndPrivacyPolicyText;
    private SeshButton loginSignupButton;
    private ImageView seshLogo;
    private ImageView seshBlurredLogo;
    private SeshActivityIndicator seshActivityIndicator;
    private View blackOverlay;
    private boolean editDetailsMode;

    private int fullnameOriginalYPosition;
    private int emailOriginalYPosition;
    private int passwordOriginalYPosition;
    private int reenterPasswordOriginalYPosition;

    private DecelerateInterpolator decelerateInterpolator;
    private LinearInterpolator linearInterpolator;

    private SeshMixpanelAPI seshMixpanelAPI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.entranceType = (EntranceType) getIntent().getSerializableExtra(ENTRANCE_TYPE_KEY);

        if (entranceType == null) {
            Log.e(TAG, "Intent does not specify EntranceType for AuthenticationActivity");
            this.entranceType = EntranceType.LOGIN;
        }

        setContentView(R.layout.authentication_activity);

        this.seshMixpanelAPI = ((SeshApplication)getApplication()).getSeshMixpanelAPI();

        this.seshLogo = (ImageView) findViewById(R.id.seshLogo);
        this.seshBlurredLogo = (ImageView) findViewById(R.id.seshBlurredLogo);

        this.seshActivityIndicator = (SeshActivityIndicator) findViewById(R.id.login_signup_activity_indicator);

        loginSignupButton = (SeshButton) findViewById(R.id.loginSignupButton);

        this.fullnameEditText = (SeshEditText) findViewById(R.id.fullNameEditText);
        this.emailEditText = (SeshEditText) findViewById(R.id.emailEditText);
        this.passwordEditText = (SeshEditText) findViewById(R.id.passwordEditText);
        this.reenterPasswordEditText = (SeshEditText) findViewById(R.id.reenterPasswordEditText);
        this.dummyEditTextTop = (SeshEditText) findViewById(R.id.dummyEditTextTop);

        this.dontHaveAccountText = (LinearLayout) findViewById(R.id.dont_have_account_text);
        this.alreadyHaveAccountText = (LinearLayout) findViewById(R.id.already_have_account_text);
        this.forgotPasswordLink = (TextView) findViewById(R.id.forgot_password_link);
        this.termsAndPrivacyPolicyText = (TextView) findViewById(R.id.terms_and_privacy_text);

        this.blackOverlay = (View) findViewById(R.id.black_overlay);
        this.blackOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (editDetailsMode) {
                    animateEditTextsDown();
                }
                return false;
            }
        });

        this.seshNetworking = new SeshNetworking(this);

        this.editDetailsMode = false;

        loginSignupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setNetworkOperationInProgress(true, null);
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

        forgotPasswordLink.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && entranceType == EntranceType.LOGIN) {
                    forgotPassword();
                }
                return true;
            }
        });
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

        fullnameEditText.setOnTouchListener(onTouchAnimateUpListener(fullnameEditText));
        emailEditText.setOnTouchListener(onTouchAnimateUpListener(emailEditText));
        passwordEditText.setOnTouchListener(onTouchAnimateUpListener(passwordEditText));
        reenterPasswordEditText.setOnTouchListener(onTouchAnimateUpListener(reenterPasswordEditText));

        Runnable editTextKeyDownListener = new Runnable() {
            @Override
            public void run() {
                if (editDetailsMode) {
                    animateEditTextsDown();
                }
            }
        };

        fullnameEditText.setKeyDownListener(editTextKeyDownListener);
        emailEditText.setKeyDownListener(editTextKeyDownListener);
        passwordEditText.setKeyDownListener(editTextKeyDownListener);
        reenterPasswordEditText.setKeyDownListener(editTextKeyDownListener);

        Spannable spannable = new SpannableString(getResources().getString(R.string.terms_label));

        LayoutUtils.NoUnderlineClickableSpan termsLinkClickableSpan = new LayoutUtils.NoUnderlineClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(getApplicationContext(), TermsActivity.class);
                startActivity(intent);
            }
        };
        LayoutUtils.NoUnderlineClickableSpan privacyPolicyLink = new LayoutUtils.NoUnderlineClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(getApplicationContext(), PrivacyActivity.class);
                startActivity(intent);
            }
        };

        ForegroundColorSpan termsLinksColor = new ForegroundColorSpan(getResources().getColor(R.color.seshorange));
        ForegroundColorSpan privacyLinkColor = new ForegroundColorSpan(getResources().getColor(R.color.seshorange));

        spannable.setSpan(termsLinkClickableSpan, 40, 52, Spanned.SPAN_POINT_MARK);
        spannable.setSpan(termsLinksColor, 40, 52, 0);
        spannable.setSpan(privacyPolicyLink, 96, 110, Spanned.SPAN_POINT_MARK);
        spannable.setSpan(privacyLinkColor, 96, 110, 0);

        termsAndPrivacyPolicyText.setText(spannable);
        termsAndPrivacyPolicyText.setMovementMethod(LinkMovementMethod.getInstance());

        this.decelerateInterpolator = new DecelerateInterpolator(1.8f);

        setupEntranceType();
    }

    @Override
    public void onResume() {
        super.onResume();

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int code = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (code != ConnectionResult.SUCCESS) {
            googleApiAvailability.getErrorDialog(this, code, 0).show();
        }
    }

    private void animateEditTextsUp(final SeshEditText touchedEditText) {
        cacheEditTextsYPosition();

        final View rootView = findViewById(R.id.container);
        final LayoutUtils utils = new LayoutUtils(this);

        dummyEditTextTop.requestEditTextFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);

                int originalScreenHeight = rootView.getRootView().getHeight();
                int resizedScreenHeight = r.bottom - r.top;
                int heightDifference = originalScreenHeight - resizedScreenHeight;
                Log.d("Keyboard Size", "Size: " + heightDifference);

                boolean keyboardVisible = heightDifference > originalScreenHeight / 3;
                // IF height diff is more then 150, consider keyboard as visible.

                if (!keyboardVisible) return;

                int resizedScreenCenter = resizedScreenHeight / 2;

                blackOverlay.animate()
                        .alpha(0.8f)
                        .setStartDelay(0)
                        .setInterpolator(decelerateInterpolator)
                        .setDuration(500).start();

                float emailYRelativeToCenter = 0 - (EDITTEXT_BOTTOM_MARGIN_DP / 2) - SeshEditText.SESH_EDIT_TEXT_HEIGHT_DP;
                float fullNameYRelativeToCenter =
                        emailYRelativeToCenter - EDITTEXT_BOTTOM_MARGIN_DP - SeshEditText.SESH_EDIT_TEXT_HEIGHT_DP;
                float passwordYRelativeToCenter = EDITTEXT_BOTTOM_MARGIN_DP / 2;
                float reenterPasswordYRelativeToCenter =
                        passwordYRelativeToCenter + EDITTEXT_BOTTOM_MARGIN_DP + SeshEditText.SESH_EDIT_TEXT_HEIGHT_DP;

                int emailY = resizedScreenCenter + utils.dpToPixels(emailYRelativeToCenter);
                int fullNameY = resizedScreenCenter + utils.dpToPixels(fullNameYRelativeToCenter);
                int passwordY = resizedScreenCenter + utils.dpToPixels(passwordYRelativeToCenter);
                int reenterPasswordY = resizedScreenCenter + utils.dpToPixels(reenterPasswordYRelativeToCenter);

                fullnameEditText
                        .animate()
                        .setDuration(500)
                        .setStartDelay(0)
                        .y(fullNameY)
                        .setInterpolator(decelerateInterpolator)
                        .start();
                emailEditText
                        .animate()
                        .setDuration(500)
                        .setStartDelay(0)
                        .y(emailY)
                        .setInterpolator(decelerateInterpolator)
                        .start();
                passwordEditText
                        .animate()
                        .setDuration(500)
                        .setStartDelay(0)
                        .y(passwordY)
                        .setInterpolator(decelerateInterpolator)
                        .start();
                reenterPasswordEditText
                        .animate()
                        .setDuration(500)
                        .setStartDelay(0)
                        .y(reenterPasswordY)
                        .setInterpolator(decelerateInterpolator)
                        .start();
                seshLogo
                        .animate()
                        .setInterpolator(decelerateInterpolator)
                        .setDuration(500)
                        .setStartDelay(0)
                        .alpha(0f)
                        .start();
                seshBlurredLogo
                        .animate()
                        .setInterpolator(decelerateInterpolator)
                        .setStartDelay(0)
                        .setDuration(500)
                        .alpha(1f)
                        .start();

                final ViewTreeObserver.OnGlobalLayoutListener thisListener = this;

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        touchedEditText.requestEditTextFocus();

                        fullnameEditText.setOnTouchListener(defaultOnTouchListener());
                        emailEditText.setOnTouchListener(defaultOnTouchListener());
                        passwordEditText.setOnTouchListener(defaultOnTouchListener());
                        reenterPasswordEditText.setOnTouchListener(defaultOnTouchListener());

                        if (Build.VERSION.SDK_INT < 16) {
                            rootView.getViewTreeObserver().removeGlobalOnLayoutListener(thisListener);
                        } else {
                            rootView.getViewTreeObserver().removeOnGlobalLayoutListener(thisListener);
                        }

                        editDetailsMode = true;
                    }
                }, 300);
            }
        });
    }

    private void cacheEditTextsYPosition() {
        fullnameOriginalYPosition = (int) fullnameEditText.getY();
        emailOriginalYPosition = (int) emailEditText.getY();
        passwordOriginalYPosition = (int) passwordEditText.getY();
        reenterPasswordOriginalYPosition = (int) reenterPasswordEditText.getY();
    }

    private void animateEditTextsDown() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), 0);

        getWindow().getCurrentFocus().clearFocus();

        blackOverlay
                .animate()
                .alpha(0f)
                .setStartDelay(300)
                .setDuration(500)
                .start();
        fullnameEditText
                .animate()
                .y(fullnameOriginalYPosition)
                .setStartDelay(300)
                .setDuration(500)
                .setInterpolator(decelerateInterpolator)
                .start();
        emailEditText
                .animate()
                .y(emailOriginalYPosition)
                .setStartDelay(300)
                .setDuration(500)
                .setInterpolator(decelerateInterpolator)
                .start();
        passwordEditText
                .animate()
                .y(passwordOriginalYPosition)
                .setStartDelay(300)
                .setDuration(500)
                .setInterpolator(decelerateInterpolator)
                .start();
        reenterPasswordEditText
                .animate()
                .y(reenterPasswordOriginalYPosition)
                .setStartDelay(300)
                .setInterpolator(decelerateInterpolator)
                .setDuration(500)
                .start();
        seshLogo
                .animate()
                .setInterpolator(decelerateInterpolator)
                .setDuration(500)
                .setStartDelay(300)
                .alpha(1f)
                .start();
        seshBlurredLogo
                .animate()
                .setInterpolator(decelerateInterpolator)
                .setStartDelay(300)
                .setDuration(500)
                .alpha(0f)
                .start();

        fullnameEditText.setOnTouchListener(onTouchAnimateUpListener(fullnameEditText));
        emailEditText.setOnTouchListener(onTouchAnimateUpListener(emailEditText));
        passwordEditText.setOnTouchListener(onTouchAnimateUpListener(passwordEditText));
        reenterPasswordEditText.setOnTouchListener(onTouchAnimateUpListener(reenterPasswordEditText));

        editDetailsMode = false;
    }

    private View.OnTouchListener onTouchAnimateUpListener(final SeshEditText touchedEditText) {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                animateEditTextsUp(touchedEditText);
                return true;
            }
        };
    }

    private View.OnTouchListener defaultOnTouchListener() {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        };
    }

    @Override
    public void onBackPressed() {
        if (editDetailsMode) {
            animateEditTextsDown();
        } else {
            super.onBackPressed();
        }
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
                            onLoginSignupError("Network Error", "We couldn't reach the network, sorry!");
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
                            onLoginSignupError("Network Error", "We couldn't reach the network, sorry!");
                        }
                    });
        }
    }

    private void onLoginResponse(JSONObject responseJson) {
        try {
            if (responseJson.get("status").equals("SUCCESS")) {
                seshMixpanelAPI.track("User Logged In");

                (new User.CreateOrUpdateUserAsyncTask()).execute(this, responseJson, new Runnable() {
                    @Override
                    public void run() {
                        setNetworkOperationInProgress(false);

                        // Refresh device on server via GCM service
                        Intent gcmIntent = new Intent(getApplicationContext(), GCMRegistrationIntentService.class);
                        gcmIntent.putExtra(SeshInstanceIDListenerService.IS_TOKEN_STALE_KEY, true);
                        gcmIntent.putExtra(GCMRegistrationIntentService.ANONYMOUS_TOKEN_REFRESH, false);
                        startService(gcmIntent);

                        if (SeshApplication.IS_LIVE) {
                            (new LaunchPrerequisiteAsyncTask()).execute(getApplicationContext(), new Runnable() {
                                @Override
                                public void run() {
                                    Intent mainContainerIntent = new Intent(getApplicationContext(), MainContainerActivity.class);
                                    startActivity(mainContainerIntent);
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                }
                            });
                        } else {
                            Intent unreleasedLaunchIntent = new Intent(getApplicationContext(), UnreleasedLaunchActivity.class);
                            startActivity(unreleasedLaunchIntent);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                    }
                });
            } else if (responseJson.get("status").equals("UNVERIFIED")) {
                seshMixpanelAPI.track("User Login Failed - Unverified");

                final SeshDialog unverifiedDialog = new SeshDialog();
                unverifiedDialog.setTitle("Unverified Account");
                unverifiedDialog.setMessage("Your account still hasn't been verified. Check your email for the verification link.");
                unverifiedDialog.setFirstChoice("OKAY");
                unverifiedDialog.setSecondChoice("RESEND EMAIL");

                // @TODO: make more robust
                unverifiedDialog.setSecondButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        seshNetworking.resendVerificationEmail(emailEditText.getText(), new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                unverifiedDialog.dismiss();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                unverifiedDialog.dismiss();
                            }
                        });
                    }
                });

                setNetworkOperationInProgress(false);
                unverifiedDialog.show(getFragmentManager(), null);
            } else {
                Map<String, Object> properties = new HashMap<>();
                properties.put("error", responseJson.getString("message"));
                seshMixpanelAPI.trackMap("User Login Failed - Error", properties);

                onLoginSignupError("Login Error",
                                responseJson.getString("message"));
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            onLoginSignupError("Network Error",
                            "We couldn't reach the network, sorry!");
        }
    }

    private void onSignupResponse(JSONObject responseJson) {
        try {
            if (responseJson.get("status").equals("FAILURE")) {
                Map<String, Object> properties = new HashMap<>();
                properties.put("error", responseJson.getString("message"));
                seshMixpanelAPI.trackMap("User Signup Failed - Error", properties);

                onLoginSignupError("Sign Up Failed",
                        responseJson.getString("message"));
            } else {
                Intent intent = new Intent(this, ConfirmationCodeActivity.class);
                intent.putExtra(SIGN_UP_EMAIL_KEY, emailEditText.getText());
                intent.putExtra(SIGN_UP_PASSWORD_KEY, passwordEditText.getText());
                startActivity(intent);

                seshMixpanelAPI.track("User Signed Up Succesfully -- Needs Verification");
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            onLoginSignupError("Network Error", "We couldn't reach the network, sorry!");
        }
    }

    // @TODO: Implement some sort of verification that Login info formatted correctly
    private boolean validateLoginDetails(String email, String password) {
        return true;
    }

    private boolean validateSignupDetails(String fullName, String email,
                                          String password, String reenterPassword) {
        if (fullName.equals("") || email.equals("") || password.equals("") || reenterPassword.equals("")) {
            setNetworkOperationInProgress(false);
            onLoginSignupError("Error!", "You must fill in all fields to sign up for Sesh!");
            return false;
        }

        if (!password.equals(reenterPassword)) {
            setNetworkOperationInProgress(false);
            onLoginSignupError("Whoops", "The passwords don't match!");
            return false;
        }

        String[] splitFullname = fullName.split("\\s+");
        if (splitFullname.length < 2) {
            setNetworkOperationInProgress(false);
            onLoginSignupError("Error!", "You need to add your full name in order to sign up for Sesh!");
            return false;
        }

        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void setupEntranceType() {
        final View content = findViewById(android.R.id.content);
        final LayoutUtils utils = new LayoutUtils(this);

        content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                if (entranceType == EntranceType.LOGIN) {
                    fullnameEditText.setAlpha(0);
                    reenterPasswordEditText.setAlpha(0);
                    alreadyHaveAccountText.setAlpha(0);
                    termsAndPrivacyPolicyText.setAlpha(0);

                    fullnameEditText.setEditTextEnabled(false);
                    reenterPasswordEditText.setEditTextEnabled(false);

                    int seshLogoY = (int) (emailEditText.getY() / 2) - (seshLogo.getHeight() / 2);
                    seshLogo.setY(seshLogoY);
                    seshBlurredLogo.setY(seshLogoY);

                    loginSignupButton.setText("Log in");
                } else if (entranceType == EntranceType.SIGNUP) {
                    int alreadyHaveAccountY = content.getHeight() - utils.dpToPixels(ACCOUNT_TEXT_OFFSET_DP);
                    int loginSignupY = alreadyHaveAccountY - utils.dpToPixels(EDITTEXT_BOTTOM_MARGIN_DP + SeshEditText.SESH_EDIT_TEXT_HEIGHT_DP);
                    int passwordEditTextY = loginSignupY - (utils.dpToPixels(EDITTEXT_BOTTOM_MARGIN_DP + SeshEditText.SESH_EDIT_TEXT_HEIGHT_DP) * 2);
                    int emailEditTextY = passwordEditTextY - utils.dpToPixels(EDITTEXT_BOTTOM_MARGIN_DP + SeshEditText.SESH_EDIT_TEXT_HEIGHT_DP);
                    int seshLogoY = (fullnameEditText.getTop() / 2) - (seshLogo.getHeight() / 2);

                    loginSignupButton.setY(loginSignupY);
                    alreadyHaveAccountText.setY(alreadyHaveAccountY);
                    emailEditText.setY(emailEditTextY);
                    passwordEditText.setY(passwordEditTextY);
                    seshLogo.setY(seshLogoY);
                    seshBlurredLogo.setY(seshLogoY);

                    fullnameEditText.setEditTextEnabled(true);
                    reenterPasswordEditText.setEditTextEnabled(true);

                    dontHaveAccountText.setAlpha(0);
                    forgotPasswordLink.setAlpha(0);
                    loginSignupButton.setText("Sign up");
                } else {
                    Log.e(TAG, "EntranceType malformed in intent to start AuthenticationActivity");
                }

                setupFocusHandlingForEmailAndReenterPassword();
                setupFocusHandlingForPassword();

                if (Build.VERSION.SDK_INT < 16) {
                    content.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    content.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    public void setupFocusHandlingForEmailAndReenterPassword() {
        emailEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    emailEditText.clearEditTextFocus();
                    passwordEditText.requestEditTextFocus();
                    return true;
                }
                return false;
            }
        });

        reenterPasswordEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        reenterPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    reenterPasswordEditText.clearEditTextFocus();
                    animateEditTextsDown();
                    return true;
                }
                return false;
            }
        });
    }

    private void setupFocusHandlingForPassword() {
        if (entranceType == EntranceType.LOGIN) {
            passwordEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
            passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        passwordEditText.clearEditTextFocus();
                        animateEditTextsDown();
                        return true;
                    }
                    return false;
                }
            });
        } else if (entranceType == EntranceType.SIGNUP) {
            passwordEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_NEXT) {
                        passwordEditText.clearEditTextFocus();
                        reenterPasswordEditText.requestEditTextFocus();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    public void toggleEntranceTypeWithAnimation() {
        LayoutUtils utils = new LayoutUtils(this);
        final int yDeltaEmailAndPassword =
                utils.dpToPixels(EDITTEXT_BOTTOM_MARGIN_DP + TERMS_TEXT_OFFSET_DP +
                        SeshEditText.SESH_EDIT_TEXT_HEIGHT_DP);
        final int yDeltaTextAndButton = utils.dpToPixels(TERMS_TEXT_OFFSET_DP);

        if (entranceType == EntranceType.LOGIN) {
            loginSignupButton.setText("Sign Up");

            utils.crossFade(dontHaveAccountText, alreadyHaveAccountText);
            utils.crossFade(forgotPasswordLink, termsAndPrivacyPolicyText);

            int loginSignupY = (int) loginSignupButton.getY() - yDeltaTextAndButton;
            int alreadyHaveAccountY = (int) alreadyHaveAccountText.getY() - yDeltaTextAndButton;
            int emailEditTextY = (int) emailEditText.getY() - yDeltaEmailAndPassword;
            int passwordEditTextY = (int) passwordEditText.getY() - yDeltaEmailAndPassword;
            int seshLogoY = (fullnameEditText.getTop() / 2) - (seshLogo.getHeight() / 2);

            loginSignupButton.animate().setDuration(300).setStartDelay(0).y(loginSignupY);
            alreadyHaveAccountText.animate().setDuration(300).setStartDelay(0).y(alreadyHaveAccountY);

            emailEditText
                    .animate()
                    .setDuration(300)
                    .setStartDelay(0)
                    .y(emailEditTextY)
                    .setInterpolator(linearInterpolator);
            passwordEditText
                    .animate()
                    .setDuration(300)
                    .setStartDelay(0)
                    .y(passwordEditTextY)
                    .setInterpolator(linearInterpolator);

            seshLogo
                    .animate()
                    .setDuration(300)
                    .setStartDelay(0)
                    .y(seshLogoY)
                    .setInterpolator(linearInterpolator);
            seshBlurredLogo
                    .animate()
                    .setDuration(300)
                    .setStartDelay(0)
                    .y(seshLogoY)
                    .setInterpolator(linearInterpolator);

            fullnameEditText
                    .animate()
                    .setStartDelay(300)
                    .setDuration(300)
                    .alpha(1)
                    .setInterpolator(linearInterpolator);
            reenterPasswordEditText
                    .animate()
                    .setStartDelay(300)
                    .setDuration(300)
                    .alpha(1)
                    .setInterpolator(linearInterpolator);

            fullnameEditText.setEditTextEnabled(true);
            reenterPasswordEditText.setEditTextEnabled(true);

            entranceType = EntranceType.SIGNUP;
        } else if (entranceType == EntranceType.SIGNUP) {
            loginSignupButton.setText("Log In");

            utils.crossFade(alreadyHaveAccountText, dontHaveAccountText);
            utils.crossFade(termsAndPrivacyPolicyText, forgotPasswordLink);

            fullnameEditText.animate().setStartDelay(0).setDuration(100).alpha(0);
            reenterPasswordEditText.animate().setStartDelay(0).setDuration(100).alpha(0);

            int loginSignupY = (int)loginSignupButton.getY() + yDeltaTextAndButton;
            int alreadyHaveAccountY = (int)alreadyHaveAccountText.getY() + yDeltaTextAndButton;
            int emailEditTextY = (int)emailEditText.getY() + yDeltaEmailAndPassword;
            int passwordEditTextY = (int)passwordEditText.getY() + yDeltaEmailAndPassword;
            int seshLogoY = (emailEditTextY / 2) - (seshLogo.getHeight() / 2);

            loginSignupButton.animate().setStartDelay(0).setDuration(300).y(loginSignupY);
            alreadyHaveAccountText.animate().setStartDelay(0).setDuration(300).y(alreadyHaveAccountY);

            emailEditText
                    .animate()
                    .setStartDelay(0)
                    .setDuration(300)
                    .y(emailEditTextY)
                    .setInterpolator(linearInterpolator);
            passwordEditText
                    .animate()
                    .setStartDelay(0)
                    .setDuration(300)
                    .y(passwordEditTextY)
                    .setInterpolator(linearInterpolator);
            seshLogo
                    .animate()
                    .setDuration(300)
                    .setStartDelay(0)
                    .y(seshLogoY)
                    .setInterpolator(linearInterpolator);
            seshBlurredLogo
                    .animate()
                    .setDuration(300)
                    .setStartDelay(0)
                    .y(seshLogoY)
                    .setInterpolator(linearInterpolator);

            fullnameEditText.setEditTextEnabled(false);
            reenterPasswordEditText.setEditTextEnabled(false);

            entranceType = EntranceType.LOGIN;
        } else {
            Log.e(TAG, "No entrance type is specified.");
        }

        setupFocusHandlingForPassword();
    }

    private void setNetworkOperationInProgress(boolean inProgress) {
        setNetworkOperationInProgress(inProgress, null);
    }

    private void setNetworkOperationInProgress(boolean inProgress, final Runnable completionCallback) {
        if (inProgress) {
            loginSignupButton.setEnabled(false);

            fullnameEditText
                    .animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setStartDelay(0)
                    .start();
            emailEditText
                    .animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setStartDelay(0)
                    .start();
            passwordEditText
                    .animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setStartDelay(0)
                    .start();
            reenterPasswordEditText
                    .animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setStartDelay(0)
                    .start();

            float seshLogoBottomY = seshLogo.getY() + seshLogo.getHeight();
            float activityIndicatorY =
                    loginSignupButton.getY() - ((loginSignupButton.getY() - seshLogoBottomY) / 2) - (seshActivityIndicator.getHeight() / 2);

            seshActivityIndicator.setY(activityIndicatorY);
            seshActivityIndicator
                    .animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setStartDelay(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            if (completionCallback != null) {
                                completionCallback.run();
                            }
                        }
                    })
                    .start();
        } else {
            loginSignupButton.setEnabled(true);

            if (entranceType == EntranceType.SIGNUP) {
                fullnameEditText
                        .animate()
                        .alpha(1f)
                        .setDuration(150)
                        .setStartDelay(0)
                        .start();
                reenterPasswordEditText
                        .animate()
                        .alpha(1f)
                        .setDuration(150)
                        .setStartDelay(0)
                        .start();
            }

            emailEditText
                    .animate()
                    .alpha(1f)
                    .setDuration(150)
                    .setStartDelay(0)
                    .start();
            passwordEditText
                    .animate()
                    .alpha(1f)
                    .setDuration(150)
                    .setStartDelay(0)
                    .start();

            seshActivityIndicator
                    .animate()
                    .alpha(0f)
                    .setDuration(150)
                    .setStartDelay(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            if (completionCallback != null) {
                                completionCallback.run();
                            }
                        }
                    })
                    .start();
        }
    }

    private void onLoginSignupError(String dialogTitle, String dialogContent) {
        final SeshDialog errorDialog = new SeshDialog();
        errorDialog.setFirstChoice("OKAY");
        errorDialog.setDialogType(SeshDialog.SeshDialogType.ONE_BUTTON);
        errorDialog.setType(DIALOG_TYPE_ERROR);
        errorDialog.setTitle(dialogTitle);
        errorDialog.setMessage(dialogContent);

        setNetworkOperationInProgress(false, new Runnable() {
            @Override
            public void run() {
                errorDialog.show(getFragmentManager(), "error");
            }
        });
    }

    public void onDialogSelection(int selection, String type) {
    }

    private void forgotPassword() {
        String emailText = emailEditText.getText();
        if (emailText.equals("")) {
            SeshDialog.showDialog(getFragmentManager(), "Whoops!", "Type the email for which you would like to reset the password.",
                    "Okay", null, "forgot_password");
        } else {
            setNetworkOperationInProgress(true);

            seshNetworking.forgotPasswordWithEmail(emailText, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    setNetworkOperationInProgress(false);
                    try {
                        if (jsonObject.getString("status").equals("SUCCESS")) {
                            SeshDialog.showDialog(getFragmentManager(), "Success",
                                    jsonObject.getString("message"),
                                    "Okay", null, "forgot_password");
                        } else {
                            SeshDialog.showDialog(getFragmentManager(), "Whoops",
                                    jsonObject.getString("message"),
                                    "Okay", null, "forgot_password");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to send password email; JSON malformed: " + e);
                        SeshDialog.showDialog(getFragmentManager(), "Whoops",
                                "Something went wrong.  Try again later.",
                                "Okay", null, "error");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    onLoginSignupError("Network Error", "We couldn't reach the network, sorry!");
                }
            });

        }
    }

    @Override
    public boolean isFullscreen() {
        return true;
    }
}

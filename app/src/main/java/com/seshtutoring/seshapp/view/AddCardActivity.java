package com.seshtutoring.seshapp.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActionBar;
import android.app.Service;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
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
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.SoftKeyboard;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshAnimatedCheckmark;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.components.SeshEditText;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.SettingsFragment;
import com.stripe.android.TokenCallback;
import com.stripe.exception.AuthenticationException;
import com.stripe.android.Stripe;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import org.json.JSONException;
import org.json.JSONObject;

import me.brendanweinstein.util.ToastUtils;
import me.brendanweinstein.util.ViewUtils;
import me.brendanweinstein.views.FieldHolder;

public class AddCardActivity extends SeshActivity {
    private static final String TAG = SettingsFragment.class.getName();
    public static final String IS_RECIPIENT_INTENT_KEY = "is_cashout_card";
    public static final int CARD_ADDED_SUCCESSFULLY_RESPONSE_CODE = 19;
    public static final int ADD_CARD_REQUEST_CODE = 12;

    private SeshButton mSubmitBtn;
    private FieldHolder mFieldHolder;
    private SeshEditText mFullName;
    private SeshNetworking seshNetworking;
    private Stripe stripe;
    private boolean isRecipient;
    private RelativeLayout requestFlowOverlay;
    private SeshActivityIndicator activityIndicator;
    private SeshAnimatedCheckmark animatedCheckmark;
    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);
        seshNetworking = new SeshNetworking(this);
        try {
            stripe = new Stripe("pk_test_E48987GUrgLiosECqaIUdgXt");
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }

        isRecipient = (boolean) getIntent().getExtras().get(IS_RECIPIENT_INTENT_KEY);
        Typeface book = Typeface.createFromAsset(this.getAssets(), "fonts/Gotham-Book.otf");
        Typeface light = Typeface.createFromAsset(this.getAssets(), "fonts/Gotham-Light.otf");

        TextView header = (TextView) findViewById(R.id.add_card_header);
        header.setTypeface(book);
        TextView agreement = (TextView) findViewById(R.id.add_card_agreement);
        agreement.setTypeface(light);
        mFullName = (SeshEditText) findViewById(R.id.full_name_field);
        mFullName.setKeyDownListener(new Runnable() {
            @Override
            public void run() {
                onBackPressed();
            }
        });
        user = User.currentUser(getApplicationContext());
        if (!user.fullLegalName.equals("")) {
            mFullName.setText(user.fullLegalName);
        }

        LayoutUtils layUtils = new LayoutUtils(this);
        layUtils.setupCustomActionBar(this, true);

        TextView title = (TextView) findViewById(R.id.action_bar_title);
        if (isRecipient) {
            title.setText("Add a Debit Card");
        }else {
            title.setText("Add a Credit/Debit Card");
        }
        title.setTypeface(book);

        RelativeLayout menuButton = (RelativeLayout) findViewById(R.id.action_bar_menu_button);
        ViewGroup layout = (ViewGroup) menuButton.getParent();
        layout.removeView(menuButton);

        mFieldHolder = (FieldHolder) findViewById(R.id.field_holder);
        mSubmitBtn = (SeshButton) findViewById(R.id.add_card_button);
        mSubmitBtn.setOnClickListener(mSubmitBtnListener);

        this.requestFlowOverlay = (RelativeLayout) findViewById(R.id.request_flow_overlay);
        this.activityIndicator = (SeshActivityIndicator) findViewById(R.id.request_activity_indicator);
        this.animatedCheckmark = (SeshAnimatedCheckmark) findViewById(R.id.animated_check_mark);

        RelativeLayout backButton = (RelativeLayout) findViewById(R.id.action_bar_back_button);
        backButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    finish();
                }
                return true;
            }
        });
    }

    private void updateFullLegalNameWithCard(final String name, final Card card) {
        seshNetworking.updateFullLegalName(name, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if (jsonObject.getString("status").equals("SUCCESS")) {
                        user.fullLegalName = name;
                        user.save();
                        beginAddCard(card);
                    } else {
                        hideAnimationWithSuccess(false, jsonObject.getString("message"));
                    }
                } catch (JSONException e) {

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                hideAnimationWithSuccess(false, "Couldn't update full legal name.");
            }
        });
    }


    private void beginAddCard(final Card card) {

        stripe.createToken(
                card,
                new TokenCallback() {
                    public void onSuccess(Token token) {
                        // Have customer token, get Recipient Token
                        getSecondToken(card, token);

                    }

                    public void onError(Exception error) {
                        // Show localized error message
                        hideAnimationWithSuccess(false, error.getLocalizedMessage());
                    }
                }
        );
    }


    private View.OnClickListener mSubmitBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewUtils.hideSoftKeyboard(AddCardActivity.this);
            if (mFieldHolder.isFieldsValid()) {
                //valid entry, get stripe token
                String cardNumber = mFieldHolder.getCardNumHolder().getCardField().getText().toString();
                int month = Integer.parseInt(mFieldHolder.getExpirationEditText().getMonth());
                int year = Integer.parseInt(mFieldHolder.getExpirationEditText().getYear());
                String cvv = mFieldHolder.getCVVEditText().getText().toString();
                final Card card = new Card(cardNumber, month, year, cvv);

                if ( !card.validateCard() ) {
                    showErrorDialog("Whoops!", "Invalid card, please re-enter information");
                    return;
                }

                requestFlowOverlay.animate().alpha(1).setListener(null).setDuration(300).start();

                if (!user.fullLegalName.equals(mFullName.getText())) {
                    updateFullLegalNameWithCard(mFullName.getText(), card);
                } else {
                    beginAddCard(card);
                }

            } else {
                showErrorDialog("Whoops!", getResources().getString(R.string.pk_error_invalid_card_no));
            }
        }
    };

    private void getSecondToken(Card card, final Token customerToken) {

        stripe.createToken(
                card,
                new TokenCallback() {
                    public void onSuccess(Token recipientToken) {
                        // Have customer token, get Recipient Token

                        seshNetworking.addCard(customerToken.getId(), recipientToken.getId(), isRecipient,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject responseJson) {
                                        onAddCardResponse(responseJson);
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError volleyError) {
                                        onAddCardFailure(volleyError.getMessage());
                                    }
                                });

                    }

                    public void onError(Exception error) {
                        // Show localized error message
                        hideAnimationWithSuccess(false, error.getLocalizedMessage());
                    }
                }
        );

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
                                    setResult(CARD_ADDED_SUCCESSFULLY_RESPONSE_CODE, null);
                                    finish();
                                }
                            });
                            animatedCheckmark.startAnimation();

                        }
                    });
        }
    }

    private void onAddCardResponse(JSONObject responseJson) {

        try {
            if (responseJson.get("status").equals("SUCCESS")) {
                if (responseJson.has("customer")) {
                    JSONObject customer = responseJson.getJSONObject("customer");
                    User currentUser = User.currentUser(getApplicationContext());
                    if (customer.has("id")) {
                        String customerId = customer.getString("id");
                        currentUser.stripeCustomerId = customerId;
                        currentUser.save();
                    }
                    com.seshtutoring.seshapp.model.Card newCard = com.seshtutoring.seshapp.model.Card.createOrUpdateCardWithJSON(customer, currentUser);
                    newCard.save();
                    if (newCard.isDefault) {
                        com.seshtutoring.seshapp.model.Card.makeDefaultCard(newCard);
                    }
                }
                if (responseJson.has("recipient")) {
                    JSONObject recipient = responseJson.getJSONObject("recipient");
                    User currentUser = User.currentUser(getApplicationContext());
                    com.seshtutoring.seshapp.model.Card newCard = com.seshtutoring.seshapp.model.Card.createOrUpdateCardWithJSON(recipient, currentUser);
                    newCard.save();
                    if (newCard.isDefault) {
                        com.seshtutoring.seshapp.model.Card.makeDefaultCard(newCard);
                    }
                }
                hideAnimationWithSuccess(true, "");
            } else if (responseJson.get("status").equals("FAILURE")) {
                String message = responseJson.get("message").toString();
                hideAnimationWithSuccess(false, message);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            hideAnimationWithSuccess(false, "Error adding card -- please try again later.");
        }

    }

    private void onAddCardFailure(String message) {
        hideAnimationWithSuccess(false, message);
    }

    private void showErrorDialog(String title, String message) {
        SeshDialog.showDialog(getFragmentManager(), title, message,
                "OKAY", null, "view_request_network_error");
    }

    @Override
    public void onBackPressed() {
        if (mFullName.hasFocus()) {
            mFullName.clearEditTextFocus();
            mSubmitBtn.requestFocus();

            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

        } else {
            finish();
        }
    }


}


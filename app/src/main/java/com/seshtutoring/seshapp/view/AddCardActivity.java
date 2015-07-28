package com.seshtutoring.seshapp.view;

import android.app.ActionBar;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshButton;
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
import me.brendanweinstein.views.FieldHolder;

public class AddCardActivity extends ActionBarActivity {

    private static final String TAG = SettingsFragment.class.getName();

    private SeshButton mSubmitBtn;
    private FieldHolder mFieldHolder;
    private SeshEditText mFullName;
    private SeshNetworking seshNetworking;
    private Stripe stripe;
    private boolean isRecipient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);
        seshNetworking = new SeshNetworking(this);
        try {
            stripe = new Stripe("pk_test_E48987GUrgLiosECqaIUdgXt");
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }

        isRecipient = (boolean) getIntent().getExtras().get("is_cashout_card");
        Typeface book = Typeface.createFromAsset(this.getAssets(), "fonts/Gotham-Book.otf");
        Typeface light = Typeface.createFromAsset(this.getAssets(), "fonts/Gotham-Light.otf");

        TextView header = (TextView) findViewById(R.id.add_card_header);
        header.setTypeface(book);
        TextView agreement = (TextView) findViewById(R.id.add_card_agreement);
        agreement.setTypeface(light);
        mFullName = (SeshEditText) findViewById(R.id.full_name_field);

        LayoutUtils layUtils = new LayoutUtils(this);
        getWindow().getDecorView().findViewById(android.R.id.content).setPadding(0, layUtils.getActionBarHeightPx(), 0, 0);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.sesh_action_bar);
        getSupportActionBar().setElevation(0);
        TextView title = (TextView) findViewById(R.id.action_bar_title);
        if (isRecipient) {
            title.setText("Add a Debit Card");
        }else {
            title.setText("Add a Credit/Debit Card");
        }
        title.setTypeface(book);

        ImageButton menuButton = (ImageButton) findViewById(R.id.action_bar_menu_button);
        ViewGroup layout = (ViewGroup) menuButton.getParent();
        layout.removeView(menuButton);

        mFieldHolder = (FieldHolder) findViewById(R.id.field_holder);
        mSubmitBtn = (SeshButton) findViewById(R.id.add_card_button);
        mSubmitBtn.setOnClickListener(mSubmitBtnListener);

        ImageButton backButton = (ImageButton) findViewById(R.id.action_bar_back_button);
        backButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    setResult(RESULT_OK, null);
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    private View.OnClickListener mSubmitBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //ViewUtils.hideSoftKeyboard(AddCardActivity.this);
            if (mFieldHolder.isFieldsValid()) {
                //valid entry, get stripe token
                String cardNumber = mFieldHolder.getCardNumHolder().getCardField().getText().toString();
                int month = Integer.parseInt(mFieldHolder.getExpirationEditText().getMonth());
                int year = Integer.parseInt(mFieldHolder.getExpirationEditText().getYear());
                String cvv = mFieldHolder.getCVVEditText().getText().toString();
                final Card card = new Card(cardNumber, month, year, cvv);

                if ( !card.validateCard() ) {
                    ToastUtils.showToast(AddCardActivity.this, "Invalid card, please re-enter information");
                    return;
                }

                stripe.createToken(
                        card,
                        new TokenCallback() {
                            public void onSuccess(Token token) {
                                // Have customer token, get Recipient Token
                                getSecondToken(card, token);

                            }

                            public void onError(Exception error) {
                                // Show localized error message
                                Toast.makeText(AddCardActivity.this,
                                        error.getLocalizedMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );

            } else {
                ToastUtils.showToast(AddCardActivity.this, getResources().getString(R.string.pk_error_invalid_card_no));
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
                        Toast.makeText(AddCardActivity.this,
                                error.getLocalizedMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );

    }

    private void onAddCardResponse(JSONObject responseJson) {

        try {
            if (responseJson.get("status").equals("SUCCESS")) {

                setResult(RESULT_OK, null);
                finish();

            }else if (responseJson.get("status").equals("FAILURE")) {
                String message = responseJson.get("message").toString();
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(this, "Error adding card", Toast.LENGTH_LONG).show();
        }

    }

    private void onAddCardFailure(String message) {

        Log.e(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

    }

}


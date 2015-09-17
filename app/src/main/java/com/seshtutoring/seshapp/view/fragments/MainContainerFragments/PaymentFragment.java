package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

//import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Card;
import com.seshtutoring.seshapp.model.Message;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.AddCardActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity.FragmentOptionsReceiver;
import com.seshtutoring.seshapp.view.MessagingActivity;
import com.seshtutoring.seshapp.view.components.PaymentListItem;
import com.seshtutoring.seshapp.view.components.PaymentMenuAdapter;
import com.seshtutoring.seshapp.view.components.SeshDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by nadavhollander on 7/14/15.
 */

public class PaymentFragment extends ListFragment implements FragmentOptionsReceiver {
    private static final String TAG = SettingsFragment.class.getName();
    public static final String MAIN_WRAPPER_STATE_KEY = "main_wrapper_state";
    private MainContainerActivity mainContainerActivity;
    private TextView selectedTextView;
    private ListView menu;
    private User user;
    private PaymentMenuAdapter adapter;
    private RelativeLayout editButton;
    private TextView editText;
    private SeshNetworking seshNetworking;
    private SeshDialog confirmDialog;
    private BroadcastReceiver broadcastReceiver;

    private Map<String, Object> options;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        menu = (ListView) inflater.inflate(R.layout.payment_menu_list, null);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, Math.round(getResources().getDimension(R.dimen.action_bar_height)), 0, 0);
        menu.setLayoutParams(params);
        mainContainerActivity = (MainContainerActivity) getActivity();
        user = User.currentUser(mainContainerActivity.getApplicationContext());
        editButton = (RelativeLayout)mainContainerActivity.findViewById(R.id.action_bar_edit_button);
        editText = (TextView)mainContainerActivity.findViewById(R.id.edit_text);
        seshNetworking = new SeshNetworking(mainContainerActivity);
        confirmDialog = SeshDialog.createDialog("Delete Card", "", "Okay", "Cancel", "delete_card_confirm");

        return menu;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new PaymentMenuAdapter(mainContainerActivity, currentPaymentListItems(), this, false);
        setListAdapter(adapter);

        refreshCards();

        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                PaymentListItem item = (PaymentListItem) menu.getItemAtPosition(position);
                if (item.type == PaymentListItem.ADD_CARD_TYPE) {
                    if (item.text.equals("Add a New Card")) {
                        Intent intent = new Intent(mainContainerActivity.getApplicationContext(), AddCardActivity.class);
                        intent.putExtra(AddCardActivity.IS_RECIPIENT_INTENT_KEY, false);
                        startActivityForResult(intent, AddCardActivity.ADD_CARD_REQUEST_CODE);
                    } else if (item.text.equals("Add a New Debit Card")) {
                        Intent intent = new Intent(mainContainerActivity.getApplicationContext(), AddCardActivity.class);
                        intent.putExtra(AddCardActivity.IS_RECIPIENT_INTENT_KEY, true);
                                startActivityForResult(intent, AddCardActivity.ADD_CARD_REQUEST_CODE);
                    }
                } else if (item.type == PaymentListItem.CARD_TYPE) {
                    makeDefaultCard(item.card);
                }

            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (adapter.editMode) {
                    adapter.editMode = false;
                    editText.setText("Edit");
                } else {
                    adapter.editMode = true;
                    editText.setText("Done");
                }

                adapter.notifyDataSetChanged();

            }
        });

        broadcastReceiver = actionBroadcastReceiver;

        ((MainContainerActivity)getActivity()).onFragmentReplacedAndRendered();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AddCardActivity.ADD_CARD_REQUEST_CODE
                && resultCode == AddCardActivity.CARD_ADDED_SUCCESSFULLY_RESPONSE_CODE){
            refreshCards();
        }
    }

    private List<PaymentListItem> currentPaymentListItems() {
        boolean shouldShowEditButton = false;
        List<PaymentListItem> returnList = new ArrayList<>();

        returnList.add(new PaymentListItem(null, PaymentListItem.HEADER_TYPE, "Payment Cards"));
        returnList.add(new PaymentListItem(null, PaymentListItem.ADD_CARD_TYPE, "Add a New Card"));

        List<Card> paymentCards = Card.getPaymentCards();
        for (Card currentCard : paymentCards) {
            PaymentListItem cardItem = new PaymentListItem(currentCard, PaymentListItem.CARD_TYPE, null);
            returnList.add(cardItem);
            if (!currentCard.isDefault) shouldShowEditButton = true;
        }

        returnList.add(new PaymentListItem(null, PaymentListItem.HEADER_TYPE, "Cashout Cards (Debit)"));
        returnList.add(new PaymentListItem(null, PaymentListItem.ADD_CARD_TYPE, "Add a New Debit Card"));

        List<Card> cashoutCards = Card.getCashoutCards();
        for (Card currentCard : cashoutCards) {
            PaymentListItem cardItem = new PaymentListItem(currentCard, PaymentListItem.CARD_TYPE, null);
            returnList.add(cardItem);
            if (!currentCard.isDefault) shouldShowEditButton = true;
        }

        // Show or hide the edit button
        mainContainerActivity.setEditButtonHidden(!shouldShowEditButton);

        return returnList;
    }


    private void refreshCards() {
        adapter.paymentItems = currentPaymentListItems();
        adapter.notifyDataSetChanged();
    }

    private void onCardsFailure(String message) {
        showErrorDialog("Whoops!", "There was a network error, please check your internet connection and try again!");
    }

    public void deleteCard(final Card card) {
        String message = "Are you sure you want to delete your " + card.type + " ending in " + card.lastFour;
        confirmDialog.setMessage(message);
        confirmDialog.setFirstButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Officially delete the card
                confirmDialog.setNetworking(true);
                confirmDialog.setFirstButtonClickListener(null);
                seshNetworking.deleteCard(card.cardId, card.isRecipient, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {

                        onDeleteCardResponse(jsonObject);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e(TAG, "Error deleting card" + volleyError.getMessage());
                        confirmDialog.networkOperationFailed("Whoops!", volleyError.getMessage(), "Okay", null);
                    }
                });

            }
        });

        confirmDialog.showWithDelay(getActivity().getFragmentManager(), "delete_card_confirm", 0);
    }

    private void makeDefaultCard(Card card) {
        seshNetworking.makeDefaultCard(card.cardId, card.isRecipient, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {

                onDefaultCardResponse(jsonObject);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                //                        Log.e(TAG, volleyError.getMessage());
                showErrorDialog("Whoops!", volleyError.getMessage());

            }
        });
    }

    private void onDefaultCardResponse(JSONObject response) {

        try {
            if (response.get("status").equals("SUCCESS")) {

                // Delete the card locally and remove the overlay
                Card newCard = Card.createOrUpdateCardWithJSON(response, user);
                newCard.save();
                if (newCard.isDefault) {
                    Card.makeDefaultCard(newCard);
                }
                refreshCards();

            } else {
                showErrorDialog("Whoops!", response.getString("message"));
            }

        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            showErrorDialog("Whoops!", e.toString());
        }
    }


    private void onDeleteCardResponse(JSONObject response) {
        adapter.editMode = false;
        editText.setText("Edit");

        try {
            if (response.get("status").equals("SUCCESS")) {

                // Delete the card locally and remove the overlay
                Card.deleteCardWithId(response.getString("card_id"));
                refreshCards();

                confirmDialog.setNetworking(false);

            } else {
               confirmDialog.networkOperationFailed("Whoops!", response.getString("message"), "Okay", null);
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error deleting card" + e.toString());
            confirmDialog.networkOperationFailed("Whoops!", e.toString(), "Okay", null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Listen for new messages
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainContainerActivity.REFRESH_USER_INFO);
        this.getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.getActivity().unregisterReceiver(broadcastReceiver);
    }


    private BroadcastReceiver actionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshCards();
        }
    };

    private void showErrorDialog(String title, String message) {
        SeshDialog.showDialog(getActivity().getFragmentManager(), title, message,
                "OKAY", null, "view_request_network_error");
    }

    @Override
    public void updateFragmentOptions(Map<String, Object> options) {
        this.options = options;
    }

    @Override
    public void clearFragmentOptions() {
        this.options = null;
    }
}

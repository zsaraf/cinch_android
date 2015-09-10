package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

//import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.AddCardActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity.FragmentOptionsReceiver;

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
    private boolean editMode = false;
    private TextView editText;

    private Map<String, Object> options;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        menu = (ListView) inflater.inflate(R.layout.settings_menu_list, null);
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

        return menu;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new PaymentMenuAdapter(mainContainerActivity);

        refreshCards();

        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                PaymentListItem item = (PaymentListItem) menu.getItemAtPosition(position);
                if (item.type == PaymentListItem.ADD_CARD_TYPE) {
                    if (item.text.equals("Add a New Card")) {
                        Intent intent = new Intent(mainContainerActivity.getApplicationContext(), AddCardActivity.class);
                        intent.putExtra("is_cashout_card", false);
                        startActivityForResult(intent, 1);
                    } else if (item.text.equals("Add a New Debit Card")) {
                        Intent intent = new Intent(mainContainerActivity.getApplicationContext(), AddCardActivity.class);
                        intent.putExtra("is_cashout_card", true);
                        startActivityForResult(intent, 1);
                    }
                }

            }
        });

//        editButton.setVisibility(View.VISIBLE);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (editMode) {
                    editMode = false;
                    editText.setText("Edit");
                } else {
                    editMode = true;
                    editText.setText("Done");
                }

                adapter.notifyDataSetChanged();

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==getActivity().RESULT_OK){
            refreshCards();
        }
    }

    private void refreshCards() {

        //Build adapter
        adapter.add(new PaymentListItem(null, PaymentListItem.HEADER_TYPE, "Payment Cards"));
        adapter.add(new PaymentListItem(null, PaymentListItem.ADD_CARD_TYPE, "Add a New Card"));

        List<Card> paymentCards = Card.getPaymentCards();
        for (Card currentCard : paymentCards) {
            PaymentListItem cardItem = new PaymentListItem(currentCard, PaymentListItem.CARD_TYPE, null);
            adapter.add(cardItem);
        }

        adapter.add(new PaymentListItem(null, PaymentListItem.HEADER_TYPE, "Cashout Cards (Debit)"));
        adapter.add(new PaymentListItem(null, PaymentListItem.ADD_CARD_TYPE, "Add a New Debit Card"));

        List<Card> cashoutCards = Card.getCashoutCards();
        for (Card currentCard : cashoutCards) {
            PaymentListItem cardItem = new PaymentListItem(currentCard, PaymentListItem.CARD_TYPE, null);
            adapter.add(cardItem);
        }

        setListAdapter(adapter);
        ((MainContainerActivity)getActivity()).onFragmentReplacedAndRendered();
    }

    private void onCardsFailure(String message) {
        Log.e(TAG, message);
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private class PaymentListItem {

        public static final int HEADER_TYPE = 0;
        public static final int ADD_CARD_TYPE = 1;
        public static final int CARD_TYPE = 2;

        public Card card;
        public int type;
        public String text;

        public PaymentListItem(Card card, int type, String text) {
            this.card = card;
            this.type = type;
            this.text = text;
        }
    }

    private class ViewHolder {

        public TextView mainTextView;
        public TextView secondTextView;
        public View divider;
        public RelativeLayout deleteButton;

    }

    public class PaymentMenuAdapter extends ArrayAdapter<PaymentListItem> {

        public PaymentMenuAdapter(Context context) {
            super(context, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            PaymentListItem item = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.settings_menu_list_row,
                        null);

                viewHolder = new ViewHolder();

                int textID = R.id.settings_row_title;
                int rightTextID = R.id.settings_right_title;
                int resourceID = R.drawable.settings_row_item;
                Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Light.otf");

                if (item.type == PaymentListItem.HEADER_TYPE) {
                    textID = R.id.settings_header_title;
                    resourceID = R.drawable.settings_header_item;
                    typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Book.otf");
                }

                viewHolder.mainTextView = (TextView) convertView.findViewById(textID);
                viewHolder.mainTextView.setBackgroundResource(resourceID);
                viewHolder.mainTextView.setTypeface(typeFace);
                viewHolder.secondTextView = (TextView) convertView.findViewById(rightTextID);
                viewHolder.divider = (View)convertView.findViewById(R.id.divider);
                viewHolder.deleteButton = (RelativeLayout)convertView.findViewById(R.id.delete_button);

                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (item.card == null) {
                viewHolder.mainTextView.setText(item.text);
                viewHolder.secondTextView.setText("");
            } else {
                viewHolder.mainTextView.setText(item.card.type + " " + item.card.lastFour);
                viewHolder.secondTextView.setText(item.card.isDefault ? "default" : "");
            }

            if (editMode) {
                viewHolder.secondTextView.setVisibility(View.GONE);
                viewHolder.deleteButton.setVisibility(View.VISIBLE);
            } else {
                viewHolder.secondTextView.setVisibility(View.VISIBLE);
                viewHolder.deleteButton.setVisibility(View.GONE);
            }

            return convertView;
        }

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

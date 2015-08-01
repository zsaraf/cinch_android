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
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.AddCardActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity.FragmentFlagReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by nadavhollander on 7/14/15.
 */

public class PaymentFragment extends ListFragment implements FragmentFlagReceiver {
    //private SeshNetworking seshNetworking;
    private static final String TAG = SettingsFragment.class.getName();
    //private static final int RESULT_OK = 1;

    public static final String MAIN_WRAPPER_STATE_KEY = "main_wrapper_state";
    private MainContainerActivity mainContainerActivity;
    private TextView selectedTextView;
    private ListView menu;
    private User user;
    private PaymentMenuAdapter adapter;
    private String fragmentFlag;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        menu = (ListView) inflater.inflate(R.layout.settings_menu_list, null);
        LayoutUtils layUtils = new LayoutUtils(getActivity());
        menu.setPadding(0, layUtils.getActionBarHeightPx(), 0, 0);
        mainContainerActivity = (MainContainerActivity) getActivity();
        user = User.currentUser(mainContainerActivity.getApplicationContext());
        //updateCards();
        return menu;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateCards();

        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                PaymentListItem item = (PaymentListItem) menu.getItemAtPosition(position);

                if (item.tag.equals("Add a New Card")) {
                    Intent intent = new Intent(mainContainerActivity.getApplicationContext(), AddCardActivity.class);
                    intent.putExtra("is_cashout_card", false);
                    startActivityForResult(intent, 1);
                }else if (item.tag.equals("Add a New Debit Card")) {
                    Intent intent = new Intent(mainContainerActivity.getApplicationContext(), AddCardActivity.class);
                    intent.putExtra("is_cashout_card", true);
                    startActivityForResult(intent, 1);
                }

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==getActivity().RESULT_OK){
//            Intent refresh = new Intent(getActivity(), MainContainerActivity.class);
//            startActivity(refresh);
//            getActivity().finish();
            updateCards();
        }
    }


    private void updateCards() {

        SeshNetworking seshNetworking = new SeshNetworking(getActivity());
        seshNetworking.getCards(
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject responseJson) {
                        onCardsResponse(responseJson);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        onCardsFailure(volleyError.getMessage());
                    }
                });

    }

    private void onCardsResponse(JSONObject responseJson) {

        ArrayList<PaymentListItem> paymentCards = new ArrayList<PaymentListItem>();
        ArrayList<PaymentListItem> cashoutCards = new ArrayList<PaymentListItem>();

        try {
            if (responseJson.get("status").equals("SUCCESS")) {

                JSONArray cardsArr = (JSONArray) responseJson.get("cards");
                for (int i = 0; i < cardsArr.length(); i++) {
                    try {
                        JSONObject card = cardsArr.getJSONObject(i);

                        String tag = card.get("type") + " " + card.get("last_four");
                        String rightText = "";
                        if ((Boolean)card.get("is_default")) {
                            rightText = "default";
                        }
                        PaymentListItem newItem = new PaymentListItem(tag, 2, rightText);
                        if ((Boolean)card.get("is_recipient")) {
                            cashoutCards.add(newItem);
                        }else {
                            paymentCards.add(newItem);
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                    }
                }

            }else if (responseJson.get("status").equals("FAILURE")) {
                String message = responseJson.get("message").toString();
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(getActivity(), "Error loading cards", Toast.LENGTH_LONG).show();
        }

        //Build adapter
        PaymentMenuAdapter adapter = new PaymentMenuAdapter(getActivity());
        adapter.add(new PaymentListItem("Payment Cards", 1, ""));
        adapter.add(new PaymentListItem("Add a New Card", 2, ""));

        for (PaymentListItem item : paymentCards) {
            adapter.add(item);
        }

        adapter.add(new PaymentListItem("Cashout Cards (Debit)", 1, ""));
        adapter.add(new PaymentListItem("Add a New Debit Card", 2, ""));

        for (PaymentListItem item : cashoutCards) {
            adapter.add(item);
        }

        setListAdapter(adapter);

    }

    private void onCardsFailure(String message) {

        Log.e(TAG, message);
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

    }

    private class PaymentListItem {
        public String tag;
        public int type;
        public String rightText;
        public PaymentListItem(String tag, int type, String rightText) {
            this.tag = tag;
            this.type = type;
            this.rightText = rightText;
        }
    }

    private class ViewHolder {

        public TextView mainTextView;
        public TextView secondTextView;

    }

    public class PaymentMenuAdapter extends ArrayAdapter<PaymentListItem> {

        public PaymentMenuAdapter(Context context) {
            super(context, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.settings_menu_list_row,
                        null);

                viewHolder = new ViewHolder();

                int textID = R.id.settings_row_title;
                int rightTextID = R.id.settings_right_title;
                int resourceID = R.drawable.settings_row_item;
                Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Light.otf");

                if (getItem(position).type == 1) {
                    textID = R.id.settings_header_title;
                    resourceID = R.drawable.settings_header_item;
                    typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Book.otf");
                } else if (getItem(position).type == 3) {
                    textID = R.id.settings_explain_title;
                    resourceID = R.drawable.settings_explain_item;
                }

                viewHolder.mainTextView = (TextView) convertView.findViewById(textID);
                viewHolder.mainTextView.setBackgroundResource(resourceID);
                viewHolder.mainTextView.setTypeface(typeFace);
                viewHolder.secondTextView = (TextView) convertView.findViewById(rightTextID);

                convertView.setTag(viewHolder);

            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.mainTextView.setText(getItem(position).tag);
            viewHolder.secondTextView.setText(getItem(position).rightText);

            return convertView;
        }

    }

    @Override
    public void updateFragmentFlag(String flag) {
        this.fragmentFlag = flag;
    }

    @Override
    public void clearFragmentFlag() {
        this.fragmentFlag = null;
    }
}

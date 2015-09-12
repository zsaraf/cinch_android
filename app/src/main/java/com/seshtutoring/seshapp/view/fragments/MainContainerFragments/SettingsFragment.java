package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

//import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.seshtutoring.seshapp.view.ChangePasswordActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.view.AboutActivity;
import com.seshtutoring.seshapp.view.TermsActivity;
import com.seshtutoring.seshapp.view.PrivacyActivity;
import com.seshtutoring.seshapp.view.SupportActivity;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.components.SettingsMenuAdapter;
import com.seshtutoring.seshapp.view.components.SettingsMenuItem;
import com.seshtutoring.seshapp.view.fragments.CashoutDialogFragment;
import com.seshtutoring.seshapp.view.MainContainerActivity.FragmentOptionsReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by nadavhollander on 7/14/15.
 */

public class SettingsFragment extends ListFragment implements FragmentOptionsReceiver {

    private static final String TAG = SettingsFragment.class.getName();
    private Map<String, Object> options;

    public static final String MAIN_WRAPPER_STATE_KEY = "main_wrapper_state";
    private MainContainerActivity mainContainerActivity;
    private TextView selectedTextView;
    private ListView menu;
    private SettingsMenuAdapter adapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        menu = (ListView) inflater.inflate(R.layout.settings_menu_list, null);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, Math.round(getResources().getDimension(R.dimen.action_bar_height)), 0, 0);
        menu.setLayoutParams(params);
        mainContainerActivity = (MainContainerActivity) getActivity();
        return menu;
    }

    public void onResume() {
        super.onResume();
        mainContainerActivity.onFragmentReplacedAndRendered();
    }

    private void onNotificationsEnabledResponse(JSONObject response) {


        try {
            if (response.getString("status").equals("SUCCESS")) {
                User currentUser = User.currentUser(mainContainerActivity.getApplicationContext());
                currentUser.notificationsEnabled = (response.getInt("notifications_enabled") != 0);
                currentUser.save();
                adapter.menuItems = getMenuItemsForUser(currentUser);
                adapter.notifyDataSetChanged();
            } else {
                SeshDialog.showDialog(mainContainerActivity.getFragmentManager(),
                        "Whoops!",
                        response.getString("message"),
                        "OKAY", null, "whoops");
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }
    private void toggleNotificationsEnabled() {
        SeshNetworking seshNetworking = new SeshNetworking(mainContainerActivity);
        seshNetworking.toggleNotificationsEnabled(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                onNotificationsEnabledResponse(jsonObject);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                SeshDialog.showDialog(mainContainerActivity.getFragmentManager(),
                        "Whoops!",
                        "We can't reach the server right now. Please try again later.",
                        "OKAY", null, "whoops");
            }
        });
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //RowObject selectedItem = mainContainerActivity.getCurrentState();
       // RowObject selectedItem = mainContainerActivity.getCurrentState();
//        SeshButton logOut = (SeshButton) view.findViewById(R.id.log_out_button);

        //seshNetworking = new SeshNetworking(getActivity());
        User user = User.currentUser(mainContainerActivity.getApplicationContext());


        adapter = new SettingsMenuAdapter(getActivity(), getMenuItemsForUser(user), this);
        menu.setAdapter(adapter);

        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                switch (position) {
                    case 2:
                        Intent intent = new Intent(mainContainerActivity.getApplicationContext(), ChangePasswordActivity.class);
                        startActivityForResult(intent, 1);
                        break;
                    case 3:
                        SeshDialog.showDialog(mainContainerActivity.getFragmentManager(),
                                    "Cash Out?",
                                    "Would you like to cash out your tutor credits? The transfer will take 1-2 days to complete.",
                                    "YES", "NO", "cashout");
                        break;
                    case 4:
                        SeshDialog.showDialog(mainContainerActivity.getFragmentManager(),
                                    "Wait",
                                    "Are you sure you want to logout?",
                                    "YES", "NO", "logout");
                        break;
                    case 6:
                        toggleNotificationsEnabled();
                        break;
                    case 9:
                        Intent termsIntent = new Intent(mainContainerActivity.getApplicationContext(), TermsActivity.class);
                        startActivityForResult(termsIntent, 1);
                        break;
                    case 10:
                        Intent privacyIntent = new Intent(mainContainerActivity.getApplicationContext(), PrivacyActivity.class);
                        startActivityForResult(privacyIntent, 1);
                        break;
                    case 11:
                        Intent supportIntent = new Intent(mainContainerActivity.getApplicationContext(), SupportActivity.class);
                        startActivityForResult(supportIntent, 1);
                        break;
                    default:
                        break;


                }

//                RowObject obj = (RowObject) menu.getItemAtPosition(position);
//
//                if (obj.activity != null) {
//                    Intent intent = new Intent(mainContainerActivity.getApplicationContext(), obj.activity);
//                    startActivityForResult(intent, 1);
//                }else {
//                    switch (position) {
//                        case 3:
//                            //cashout
//                            SeshDialog.showDialog(mainContainerActivity.getFragmentManager(),
//                                    "Cash Out?",
//                                    "Would you like to cash out your tutor credits? The transfer will take 1-2 days to complete.",
//                                    "YES", "NO", "CASHOUT");
//                            break;
//                        case 4:
//                            //logout
//                            SeshDialog.showDialog(mainContainerActivity.getFragmentManager(),
//                                    "Wait",
//                                    "Are you sure you want to logout?",
//                                    "YES", "NO", "LOGOUT");
//                            break;
//                        case 6:
//                            //tutor offline ping
//                            //toggleOfflinePing();
//                            break;
//                    }
//                }
//
//            }
//        });
            }

        });
    }

    private List<SettingsMenuItem> getMenuItemsForUser(User user) {
        List<SettingsMenuItem> returnItems = new ArrayList<>();
        returnItems.add(new SettingsMenuItem("Account", SettingsMenuItem.HEADER_TYPE, ""));
        returnItems.add(new SettingsMenuItem("Email", SettingsMenuItem.ROW_TYPE, user.email));
        returnItems.add(new SettingsMenuItem("Change Password", SettingsMenuItem.ROW_TYPE, ""));
        returnItems.add(new SettingsMenuItem("Cashout", SettingsMenuItem.ROW_TYPE, ""));
        returnItems.add(new SettingsMenuItem("Logout", SettingsMenuItem.ROW_TYPE, ""));
        returnItems.add(new SettingsMenuItem("Notifications", SettingsMenuItem.HEADER_TYPE, ""));
        returnItems.add(new SettingsMenuItem("Notifications Enabled", SettingsMenuItem.ROW_TYPE, Boolean.toString(user.notificationsEnabled)));
        returnItems.add(new SettingsMenuItem("This will silence all notifications outside the app.", SettingsMenuItem.EXPLAIN_TYPE, ""));
        returnItems.add(new SettingsMenuItem("About", SettingsMenuItem.HEADER_TYPE, ""));
        returnItems.add(new SettingsMenuItem("Terms of Use", SettingsMenuItem.ROW_TYPE, ""));
        returnItems.add(new SettingsMenuItem("Privacy Policy", SettingsMenuItem.ROW_TYPE, ""));
        returnItems.add(new SettingsMenuItem("Support", SettingsMenuItem.ROW_TYPE, ""));
        return returnItems;
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

package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

//import android.app.DialogFragment;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.view.inputmethod.InputMethodManager;
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
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshAnimatedCheckmark;
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

public class SettingsFragment extends Fragment implements FragmentOptionsReceiver {

    private static final String TAG = SettingsFragment.class.getName();
    private Map<String, Object> options;

    public static final String MAIN_WRAPPER_STATE_KEY = "main_wrapper_state";
    private MainContainerActivity mainContainerActivity;
    private TextView selectedTextView;
    private ListView menu;
    private SettingsMenuAdapter adapter;
    private SeshNetworking seshNetworking;
    private RelativeLayout requestFlowOverlay;
    private SeshActivityIndicator activityIndicator;
    private SeshAnimatedCheckmark animatedCheckmark;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_menu_list, null);
        menu = (ListView) view.findViewById(R.id.settings_list);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        menu.setLayoutParams(params);
        mainContainerActivity = (MainContainerActivity) getActivity();
        seshNetworking = new SeshNetworking(mainContainerActivity);
        this.requestFlowOverlay = (RelativeLayout) view.findViewById(R.id.request_flow_overlay);
        this.activityIndicator = (SeshActivityIndicator) view.findViewById(R.id.request_activity_indicator);
        this.animatedCheckmark = (SeshAnimatedCheckmark) view.findViewById(R.id.animated_check_mark);
        return view;
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
                        final SeshDialog seshDialog = new SeshDialog();
                        seshDialog.setDialogType(SeshDialog.SeshDialogType.TWO_BUTTON);
                        seshDialog.setTitle("Cash Out?");
                        seshDialog.setMessage("Would you like to cash out your tutor credits? The transfer will take 1-2 days to complete.");
                        seshDialog.setDialogType(SeshDialog.SeshDialogType.ONE_BUTTON);
                        seshDialog.setFirstChoice("YES");
                        seshDialog.setSecondChoice("NO");
                        seshDialog.setFirstButtonClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                seshDialog.dismiss();
                                requestFlowOverlay.animate().alpha(1).setListener(null).setDuration(300).start();
                                seshNetworking.cashout(
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject responseJson) {
                                                onCashoutResponse(responseJson);
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError volleyError) {
                                                onCashoutFailure(volleyError.getMessage());
                                            }
                                        });
                            }
                        });
                        seshDialog.setSecondButtonClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                seshDialog.dismiss();
                            }
                        });
                        seshDialog.setType("CASHOUT");
                        seshDialog.show(mainContainerActivity.getFragmentManager(), "CASHOUT");
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

            }

        });
    }

    private List<SettingsMenuItem> getMenuItemsForUser(User user) {
        String notificationText = user.notificationsEnabled ? "off" : "on";
        List<SettingsMenuItem> returnItems = new ArrayList<>();
        returnItems.add(new SettingsMenuItem("Account", SettingsMenuItem.HEADER_TYPE, ""));
        returnItems.add(new SettingsMenuItem("Email", SettingsMenuItem.ROW_TYPE, user.email));
        returnItems.add(new SettingsMenuItem("Change Password", SettingsMenuItem.ROW_TYPE, ""));
        returnItems.add(new SettingsMenuItem("Cashout", SettingsMenuItem.ROW_TYPE, ""));
        returnItems.add(new SettingsMenuItem("Logout", SettingsMenuItem.ROW_TYPE, ""));
        returnItems.add(new SettingsMenuItem("Notifications", SettingsMenuItem.HEADER_TYPE, ""));
        returnItems.add(new SettingsMenuItem("Silent Mode", SettingsMenuItem.ROW_TYPE, notificationText));
        returnItems.add(new SettingsMenuItem("This will silence all notifications outside the app.", SettingsMenuItem.EXPLAIN_TYPE, ""));
        returnItems.add(new SettingsMenuItem("About", SettingsMenuItem.HEADER_TYPE, ""));
        returnItems.add(new SettingsMenuItem("Terms of Use", SettingsMenuItem.ROW_TYPE, ""));
        returnItems.add(new SettingsMenuItem("Privacy Policy", SettingsMenuItem.ROW_TYPE, ""));
        returnItems.add(new SettingsMenuItem("Support", SettingsMenuItem.ROW_TYPE, ""));
        return returnItems;
    }

    private void onCashoutResponse(JSONObject responseJson) {
        try {
            if (responseJson.get("status").equals("SUCCESS")) {
                User currentUser = User.currentUser(mainContainerActivity);
                currentUser.tutor.cashAvailable = 0.0f;
                currentUser.tutor.save();
                currentUser.save();
                mainContainerActivity.sendBroadcast(new Intent(mainContainerActivity.REFRESH_TUTOR_CREDITS));

                hideAnimationWithSuccess(true, "Successfully cashed out!");

            } else if (responseJson.get("status").equals("FAILURE")) {
                String message = responseJson.get("message").toString();
                hideAnimationWithSuccess(false, message);
            }
        } catch (JSONException e) {
            hideAnimationWithSuccess(false, "There was an error cashing out your account, please try again in a few days.");
            Log.e(TAG, e.toString());
        }
    }

    private void onCashoutFailure(String errorMessage) {
        hideAnimationWithSuccess(false, "There was an error cashing out your account, please try again in a few days.");
        Log.e(TAG, "NETWORK ERROR: " + errorMessage);
    }

    private void showErrorDialog(String title, String message) {
        SeshDialog.showDialog(mainContainerActivity.getFragmentManager(), title, message,
                "OKAY", null, "view_request_network_error");
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
                                    //setResult(PASSWORD_CHANGED_SUCCESSFULLY_RESPONSE_CODE, null);
                                    requestFlowOverlay.animate().alpha(0).setListener(null).setDuration(300).start();
                                }
                            });
                            animatedCheckmark.startAnimation();
                        }
                    });
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

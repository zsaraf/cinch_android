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
import com.seshtutoring.seshapp.view.fragments.CashoutDialogFragment;
import com.seshtutoring.seshapp.view.MainContainerActivity.FragmentOptionsReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by nadavhollander on 7/14/15.
 */

public class SettingsFragment extends ListFragment implements FragmentOptionsReceiver {
    //private SeshNetworking seshNetworking;
    private static final String TAG = SettingsFragment.class.getName();
    private Map<String, Object> options;

    public static enum RowObject {
        ACCOUNT("Account", 1, "", null),
        EMAIL("Email", 2, "", null),
        PASSWORD("Change Password", 2, "", ChangePasswordActivity.class),
        CASHOUT("Cashout", 2, "", null),
        LOGOUT("Logout", 2, "", null),
        NOTIFICATIONS("Notifications", 1, "", null),
        PING("Tutor Offline Ping", 2, "", null),
        EXPLANATION("Would you like to be notified when students who have favorited you need help, even when you're offline?", 3, "", null),
        ABOUT("About", 1, "", AboutActivity.class),
        TERMS("Terms of Use", 2, "", TermsActivity.class),
        PRIVACY("Privacy Policy", 2, "", PrivacyActivity.class),
        SUPPORT("Support", 2, "", SupportActivity.class);

        public String title;
        public int type;
        public String rightText;
        public Class activity;

        RowObject(String title, int type, String rightText, Class activity) {
            this.title = title;
            this.type = type;
            this.rightText = rightText;
            this.activity = activity;
        }
    }

    public static final String MAIN_WRAPPER_STATE_KEY = "main_wrapper_state";
    private MainContainerActivity mainContainerActivity;
    private TextView selectedTextView;
    private ListView menu;
    private User user;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        menu = (ListView) inflater.inflate(R.layout.settings_menu_list, null);
        LayoutUtils layUtils = new LayoutUtils(getActivity());
        menu.setPadding(0, layUtils.getActionBarHeightPx(), 0, 0);
        mainContainerActivity = (MainContainerActivity) getActivity();
        user = User.currentUser(mainContainerActivity.getApplicationContext());
        return menu;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //RowObject selectedItem = mainContainerActivity.getCurrentState();
       // RowObject selectedItem = mainContainerActivity.getCurrentState();
//        SeshButton logOut = (SeshButton) view.findViewById(R.id.log_out_button);
        String email = user.getEmail();

        //seshNetworking = new SeshNetworking(getActivity());

        SettingsMenuAdapter adapter = new SettingsMenuAdapter(getActivity());
        for (RowObject obj : RowObject.values()) {
            if (obj.title == "Email") {
                obj.rightText = email;
            }
            adapter.add(obj);
        }

        //settingsList.setAdapter(adapter);
        setListAdapter(adapter);

        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                RowObject obj = (RowObject) menu.getItemAtPosition(position);

                if (obj.activity != null) {
                    Intent intent = new Intent(mainContainerActivity.getApplicationContext(), obj.activity);
                    startActivityForResult(intent, 1);
                }else {
                    switch (position) {
                        case 3:
                            //cashout
                            SeshDialog.showDialog(mainContainerActivity.getFragmentManager(),
                                    "Cash Out?",
                                    "Would you like to cash out your tutor credits? The transfer will take 1-2 days to complete.",
                                    "YES", "NO", "CASHOUT");
                            break;
                        case 4:
                            //logout
                            SeshDialog.showDialog(mainContainerActivity.getFragmentManager(),
                                    "Wait",
                                    "Are you sure you want to logout?",
                                    "YES", "NO", "LOGOUT");
                            break;
                        case 6:
                            //tutor offline ping
                            //toggleOfflinePing();
                            break;
                    }
                }

            }
        });

    }

    private class SettingsMenuItem {
        public String tag;
        public int type;
        public String rightText;
        //public boolean isSelected;
        public SettingsMenuItem(String tag, int type, String rightText) {
            this.tag = tag;
            this.type = type;
            this.rightText = rightText;
           // this.isSelected = isSelected;
        }
    }

    private class ViewHolder {

        public TextView mainTextView;
        public TextView secondTextView;

    }

    public class SettingsMenuAdapter extends ArrayAdapter<RowObject> {

        public SettingsMenuAdapter(Context context) {
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

            viewHolder.mainTextView.setText(getItem(position).title);
            viewHolder.secondTextView.setText(getItem(position).rightText);

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

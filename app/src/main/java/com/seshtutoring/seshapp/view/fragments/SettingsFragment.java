package com.seshtutoring.seshapp.view.fragments;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.Auth;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.AuthenticationActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by nadavhollander on 7/14/15.
 */

// LISTFRAGMENT SHOULD BE USED INSTEAD OF FRAGMENT, ONLY TEMPORARILY EXTENDING FRAGMENT
public class SettingsFragment extends Fragment {
    private SeshNetworking seshNetworking;
    private static final String TAG = SettingsFragment.class.getName();

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.settings_fragment, null);

        // Add padding to account for action bar
        LayoutUtils utils = new LayoutUtils(getActivity());
        LinearLayout settingsLayout = (LinearLayout) view.findViewById(R.id.settings_layout);
        settingsLayout.setPadding(0, utils.getActionBarHeightPx(), 0, 0);

        Button logOut = (Button) view.findViewById(R.id.log_out_button);

        seshNetworking = new SeshNetworking(getActivity());

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seshNetworking.logout(new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject responseJson) {
                        try {
                            if (responseJson.getString("status").equals("SUCCESS")) {
                                User.logoutUserLocally(getActivity());

                                Intent intent = new Intent(getActivity(), AuthenticationActivity.class);
                                startActivity(intent);
                            } else {
                                Log.e(TAG, "Failed to logout on server side.");
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Logout json response malformed.");
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        MainContainerActivity mainContainerActivity = (MainContainerActivity) getActivity();
                        mainContainerActivity.onNetworkError();
                    }
                });
            }
        });

        return view;
    }
}

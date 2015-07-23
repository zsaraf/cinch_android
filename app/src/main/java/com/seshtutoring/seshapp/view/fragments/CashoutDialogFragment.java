package com.seshtutoring.seshapp.view.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lillioetting on 7/20/15.
 */
public class CashoutDialogFragment extends DialogFragment {

    private static final String TAG = MainContainerActivity.class.getName();
    private MainContainerActivity mainContainerActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final SeshNetworking seshNetworking = new SeshNetworking(getActivity());
        this.mainContainerActivity = (MainContainerActivity) getActivity();

        //create new layout
        //menu = () inflater.inflate(R.layout.settings_menu_list, null);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.cashout_dialog)
                .setTitle("Cash Out?")
                //.setView() - set view to new view created above
                .setPositiveButton(R.string.affirmative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //yes, proceed to cashout
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
                })
                .setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //no, cancel cashout
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private void onCashoutResponse(JSONObject responseJson) {
        try {
            if (responseJson.get("status").equals("SUCCESS")) {
                Toast.makeText(this.mainContainerActivity, "You have cashed out!", Toast.LENGTH_LONG).show();
            } else if (responseJson.get("status").equals("FAILURE")) {
                String message = responseJson.get("message").toString();
                Toast.makeText(this.mainContainerActivity, message, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(this.mainContainerActivity, "Cash out failed.", Toast.LENGTH_LONG).show();
        }
    }

    private void onCashoutFailure(String errorMessage) {
        Log.e(TAG, "NETWORK ERROR: " + errorMessage);
        Toast.makeText(getActivity(), "We couldn't reach the network, sorry!", Toast.LENGTH_LONG).show();
    }

}

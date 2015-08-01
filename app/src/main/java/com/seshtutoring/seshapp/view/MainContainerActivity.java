package com.seshtutoring.seshapp.view;

import android.app.ActionBar;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.services.GCMRegistrationIntentService;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.services.SeshGCMListenerService;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.fragments.SideMenuFragment;
import com.seshtutoring.seshapp.view.fragments.SideMenuFragment.MenuOption;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainContainerActivity extends SeshActivity implements SeshDialog.OnSelectionListener {
    private static final String TAG = MainContainerActivity.class.getName();
    public static final String MAIN_CONTAINER_STATE_KEY = "main_container_state";
    public static final String FRAGMENT_FLAG_KEY = "fragment_flags";
    public static final String UPDATE_CONTAINER_STATE_ACTION =
            "com.seshtutoring.seshapp.UPDATE_CONTAINER_STATE";

    private BroadcastReceiver notificationActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(SeshGCMListenerService.NOTIFICATION_ID_EXTRA)) {
                int notificationId = intent.getIntExtra(SeshGCMListenerService.NOTIFICATION_ID_EXTRA, -1);
                ((NotificationManager)
                        getSystemService(NOTIFICATION_SERVICE)).cancel(notificationId);
            }

            if (intent.getAction().equals(UPDATE_CONTAINER_STATE_ACTION)) {
                Bundle extras = intent.getExtras();
                MenuOption containerState = (MenuOption) extras.getSerializable(MAIN_CONTAINER_STATE_KEY);
                String flag = extras.getString(FRAGMENT_FLAG_KEY);

                setCurrentState(containerState, flag);
                sideMenuFragment.updateSelectedItem();
            }
        }
    };

    /**
     * All fragments inserted in the Main Container must implement this interface.  This allows
     * flags to be passed to Fragments contained within the container via Intents sent to
     * MainContainerActivity (eg. Intent wants to show available jobs to tutor, flag is passed to
     * TeachViewFragment to show the appropriate fragment)
     */
    public interface FragmentFlagReceiver {
        void updateFragmentFlag(String flag);
        void clearFragmentFlag();
    }

    private SlidingMenu slidingMenu;
    private SideMenuFragment sideMenuFragment;
    private MenuOption currentSelectedMenuOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_container_activity);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.sesh_action_bar);
        getSupportActionBar().setElevation(0);
        ImageButton backButton = (ImageButton) findViewById(R.id.action_bar_back_button);
        ViewGroup layout = (ViewGroup) backButton.getParent();
        layout.removeView(backButton);

        sideMenuFragment = new SideMenuFragment();

        slidingMenu = new SlidingMenu(this);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        slidingMenu.setBehindOffsetRes(R.dimen.sliding_menu_behind_offset);
        slidingMenu.setBehindScrollScale(0);
        slidingMenu.setFadeEnabled(false);
        slidingMenu.setMenu(R.layout.sliding_menu_frame);
        slidingMenu.setOnOpenedListener(sideMenuFragment);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.sliding_menu_frame, sideMenuFragment)
                .commit();

        ImageButton menuButton = (ImageButton) findViewById(R.id.action_bar_menu_button);
        menuButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i(TAG, "ACTION BAR HEIGHT: " + getSupportActionBar().getHeight());
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    slidingMenu.toggle(true);
                }
                return false;
            }
        });

        setCurrentState(MenuOption.HOME, null);
        registerReceiver(notificationActionReceiver, new IntentFilter(UPDATE_CONTAINER_STATE_ACTION));
    }

    @Override
    public void onResume() {
        super.onResume();

        User.fetchUserInfoFromServer(this);
        updateDeviceOnServer(this);

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int code = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (code != ConnectionResult.SUCCESS) {
            googleApiAvailability.getErrorDialog(this, code, 0).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onDialogSelection(int selection, String type) {
        SeshNetworking seshNetworking = new SeshNetworking(this);

        if (type.equals("CASHOUT") && selection == 1) {

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

        }else if (type.equals("LOGOUT") && selection == 1) {

            seshNetworking.logout(
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject responseJson) {
                            onLogoutResponse(responseJson);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            onLogoutFailure(volleyError.getMessage());
                        }
                    });

        }
    }

    private void onCashoutResponse(JSONObject responseJson) {
        try {
            if (responseJson.get("status").equals("SUCCESS")) {
                Toast.makeText(this, "You have cashed out!", Toast.LENGTH_LONG).show();
            } else if (responseJson.get("status").equals("FAILURE")) {
                String message = responseJson.get("message").toString();
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(this, "Cash out failed.", Toast.LENGTH_LONG).show();
        }
    }

    private void onCashoutFailure(String errorMessage) {
        Log.e(TAG, "NETWORK ERROR: " + errorMessage);
        Toast.makeText(this, "We couldn't reach the network, sorry!", Toast.LENGTH_LONG).show();
    }

    private void onLogoutResponse(JSONObject responseJson) {
        try {
            if (responseJson.get("status").equals("SUCCESS")) {
                User.logoutUserLocally(this);
                Intent intent = new Intent(this, AuthenticationActivity.class);
                startActivity(intent);
            } else if (responseJson.get("status").equals("FAILURE")) {
                String message = responseJson.get("message").toString();
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(this, "Logout failed.", Toast.LENGTH_LONG).show();
        }
    }

    private void onLogoutFailure(String errorMessage) {
        Log.e(TAG, "NETWORK ERROR: " + errorMessage);
        Toast.makeText(this, "We couldn't reach the network, sorry!", Toast.LENGTH_LONG).show();
    }

    public MenuOption getCurrentState() {
        return currentSelectedMenuOption;
    }

    /**
     * Convenience method for setting current state without flags
     * @param menuOption
     */
    public void setCurrentState(MenuOption menuOption) {
        setCurrentState(menuOption, null);
    }

    public void setCurrentState(MenuOption selectedMenuOption, String flag) {
        if (!(selectedMenuOption.fragment instanceof FragmentFlagReceiver)) {
            Log.e(TAG, "Invalid Fragment: All fragments within MainContainerActivity must implement FragmentFlagReceiver");
            return;
        }

        if (currentSelectedMenuOption != selectedMenuOption) {
            if (currentSelectedMenuOption != null) {
                FragmentFlagReceiver flagReceiver = (FragmentFlagReceiver) currentSelectedMenuOption.fragment;
                flagReceiver.clearFragmentFlag();
            }

            currentSelectedMenuOption = selectedMenuOption;

            TextView title = (TextView) findViewById(R.id.action_bar_title);

            title.setText(selectedMenuOption.title);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_container, currentSelectedMenuOption.fragment)
                    .commitAllowingStateLoss();
        }

        if (flag != null) {
            FragmentFlagReceiver flagReceiver = (FragmentFlagReceiver) currentSelectedMenuOption.fragment;
            flagReceiver.updateFragmentFlag(flag);
        }
    }

    public void closeDrawer() {
        slidingMenu.toggle(true);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        String currStr = "unknown";
//        MenuOption curr = getCurrentState();
//        if (curr == MenuOption.SETTINGS) {
//            currStr = "Settings";
//        }else if (curr == MenuOption.HOME) {
//            currStr = "Home";
//        }
//        outState.putString("current_state", currStr);
//    }

//    @Override
//    public void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        if( savedInstanceState != null ) {
//            //Then the application is being reloaded
//            String currentState = savedInstanceState.getString("current_state");
//
//        }
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RequestActivity.LEARN_REQUEST_CREATE_SUCCESS) {
            sideMenuFragment.setStatusFlag(SideMenuFragment.MENU_OPEN_DISPLAY_NEW_REQUEST);
            Handler handler = new Handler();
            Runnable openSideMenu = new Runnable() {
                @Override
                public void run() {
                    slidingMenu.toggle(true);
                }
            };

            // hacky, but delay menu open animation to account for activity transition
            handler.postDelayed(openSideMenu, 300);
        }
    }

    /**
     * Updates the Device row of the server database for user's phone.  Called everytime user
     * opens main container (onResume).  Will not send update request if we do not have a GCM registration
     * token locally saved -- device row data is only used to push information (via GCM), so any
     * update would be irrelevant if user device does not have a valid GCM token.
     */
    private static void updateDeviceOnServer(Context context) {
        String savedToken = GCMRegistrationIntentService.getSavedGCMToken(context);
        if (savedToken != null) {
            SeshNetworking seshNetworking = new SeshNetworking(context);
            seshNetworking.updateDeviceToken(savedToken, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    try {
                        if (jsonObject.getString("status").equals("SUCCESS")) {
                            Log.i(TAG, "Updated device on server");
                        } else {
                            Log.e(TAG, "Failed to update device on server: " + jsonObject.getString("message"));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to update device on server; response malformed: " + e);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Failed to update device on server; network error: " + error);
                }
            });
        }
    }

    public void onNetworkError() {
        Log.e(TAG, "Network Error");
    }
}



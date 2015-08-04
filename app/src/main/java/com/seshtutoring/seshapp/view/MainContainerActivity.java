package com.seshtutoring.seshapp.view;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Handler;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
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
import com.google.android.gms.maps.GoogleMap;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.services.FetchSeshInfoBroadcastReceiver;
import com.seshtutoring.seshapp.services.FetchSeshInfoBroadcastReceiver.SeshInfoUpdateListener;
import com.seshtutoring.seshapp.services.GCMRegistrationIntentService;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.services.SeshGCMListenerService;
import com.seshtutoring.seshapp.services.SeshInstanceIDListenerService;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.HomeFragment;
import com.seshtutoring.seshapp.view.fragments.SideMenuFragment;
import com.seshtutoring.seshapp.view.fragments.SideMenuFragment.MenuOption;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainContainerActivity extends SeshActivity implements SeshDialog.OnSelectionListener {
    private static final String TAG = MainContainerActivity.class.getName();
    public static final String MAIN_CONTAINER_STATE_KEY = "main_container_state";
    public static final String FRAGMENT_FLAG_KEY = "fragment_flags";
    private static final String DIALOG_TYPE_FOUND_TUTOR = "dialog_type_found_tutor";
    private static final String INTENT_HANDLED = "intent_handled";
    public static final String UPDATE_CONTAINER_STATE_ACTION =
            "com.seshtutoring.seshapp.UPDATE_CONTAINER_STATE";
    public static final String FOUND_TUTOR_ACTION =
            "com.seshtutoring.seshapp.FOUND_TUTOR";
    private static final int FIFTEEN_SECONDS = 1000 * 15;

    private BroadcastReceiver notificationActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleNotificationIntent(intent);
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
    private AlarmManager fetchSeshInfoAlarm;
    private PendingIntent fetchSeshInfoPendingIntent;

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

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UPDATE_CONTAINER_STATE_ACTION);
        intentFilter.addAction(FOUND_TUTOR_ACTION);
        registerReceiver(notificationActionReceiver, intentFilter);

        fetchSeshInfoAlarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(FetchSeshInfoBroadcastReceiver.FETCH_SESH_INFO_ACTION);
        fetchSeshInfoPendingIntent =
                PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        FetchSeshInfoBroadcastReceiver.setSeshInfoUpdateListener((SeshInfoUpdateListener)sideMenuFragment);
    }

    @Override
    public void onResume() {
        super.onResume();

        User.fetchUserInfoFromServer(this);

        // Refresh device token on server via GCM service
        Intent gcmIntent = new Intent(this, GCMRegistrationIntentService.class);
        gcmIntent.putExtra(SeshInstanceIDListenerService.IS_TOKEN_STALE_KEY, false);
        startService(gcmIntent);

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int code = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (code != ConnectionResult.SUCCESS) {
            googleApiAvailability.getErrorDialog(this, code, 0).show();
        }

        Intent intent = getIntent();
        if (intent.hasExtra(SeshGCMListenerService.NOTIFICATION_ID_EXTRA)) {
            handleNotificationIntent(intent);
        }

        fetchSeshInfoAlarm.
                setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                        FIFTEEN_SECONDS, fetchSeshInfoPendingIntent);
    }

    @Override
    public void onPause() {
        super.onPause();
        fetchSeshInfoAlarm.cancel(fetchSeshInfoPendingIntent);
    }

    private void handleNotificationIntent(Intent intent) {
        if (intent.hasExtra(INTENT_HANDLED) && intent.getBooleanExtra(INTENT_HANDLED, false)) {
            return;
        }

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

            if (ApplicationLifecycleTracker.applicationInForeground()) {
                sideMenuFragment.updateSelectedItem();
            }
        } else if (intent.getAction().equals(FOUND_TUTOR_ACTION)) {
            showNotificationDialog("Help is on the way",
                    "You've been matched with a tutor and your Sesh has been scheduled!",
                    "See details", DIALOG_TYPE_FOUND_TUTOR);
        }

        intent.putExtra(INTENT_HANDLED, true);
    }

    private void showNotificationDialog(final String title, final String content, final String confirmButtonText, final String type) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentSelectedMenuOption == MenuOption.HOME) {
                    HomeFragment homeFragment = (HomeFragment)currentSelectedMenuOption.fragment;
                    if (homeFragment.getCurrTabItem() == HomeFragment.TabItem.LEARN_TAB) {
                        homeFragment.getLearnViewMap().snapshot(new GoogleMap.SnapshotReadyCallback() {
                            @Override
                            public void onSnapshotReady(Bitmap bitmap) {
                                SeshDialog.showDialog(getFragmentManager(), title, content, confirmButtonText, null, bitmap,
                                        type);
                                Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                                v.vibrate(300);
                            }
                        });
                    }
                } else {
                    SeshDialog.showDialog(getFragmentManager(), title, content,
                            confirmButtonText, null, null,
                            type);
                    Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(300);
                }
            }
        }, 500);
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

        } else if (type.equals(DIALOG_TYPE_FOUND_TUTOR)) {
            Toast.makeText(this, "showing activity", Toast.LENGTH_LONG).show();
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
        Toast.makeText(this, "We couldn't reach the network, sorrys!", Toast.LENGTH_LONG).show();
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
        Log.d(TAG, "Activity Result: " + resultCode);
        if (resultCode == RequestActivity.LEARN_REQUEST_CREATE_SUCCESS) {
            Log.d(TAG, "INSIDE LEARN_REQUEST_CREATE_SUCCESS");
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

    public void onNetworkError() {
        Log.e(TAG, "Network Error");
    }
}



package com.seshtutoring.seshapp.view;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
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
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
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
import com.jeremyfeinstein.slidingmenu.lib.CustomViewAbove;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.SeshStateManager;
import com.seshtutoring.seshapp.services.PeriodicFetchBroadcastReceiver;
import com.seshtutoring.seshapp.services.GCMRegistrationIntentService;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.services.SeshGCMListenerService;
import com.seshtutoring.seshapp.services.SeshInstanceIDListenerService;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshBanner;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.HomeFragment;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.LoadingFragment;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.PaymentFragment;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.ProfileFragment;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.PromoteFragment;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.SettingsFragment;
import com.seshtutoring.seshapp.view.fragments.SideMenuFragment;
import com.seshtutoring.seshapp.view.fragments.ViewSeshFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainContainerActivity extends SeshActivity implements SeshDialog.OnSelectionListener,
        PeriodicFetchBroadcastReceiver.FetchUpdateListener {
    private static final String TAG = MainContainerActivity.class.getName();
    public static final String MAIN_CONTAINER_STATE_INDEX = "main_container_state_index";
    public static final String FRAGMENT_FLAG_KEY = "fragment_flags";
    private static final String DIALOG_TYPE_FOUND_TUTOR = "dialog_type_found_tutor";
    private static final String INTENT_HANDLED = "intent_handled";
    public static final String UPDATE_CONTAINER_STATE_ACTION = "update_main_container_state";
    public static final String DISPLAY_SIDE_MENU_UPDATE = "display_side_menu_update";
    public static final String VIEW_SESH_ACTION = "view_sesh";
    public static final String SESH_CANCELLED_ACTION = "sesh_cancelled";
    public static final String FOUND_TUTOR_ACTION = "com.seshtutoring.seshapp.FOUND_TUTOR";

    /**
     * All fragments inserted in the Main Container must implement this interface.  This allows
     * flags to be passed to Fragments contained within the container via Intents sent to
     * MainContainerActivity (eg. Intent wants to show available jobs to tutor, flag is passed to
     * TeachViewFragment to show the appropriate fragment)
     */
    public interface FragmentOptionsReceiver {
        void updateFragmentOptions(Map<String, Object> options);
        void clearFragmentOptions();
    }

    private SlidingMenu slidingMenu;
    private SideMenuFragment sideMenuFragment;
    private ContainerState currentContainerState;
    private SeshActivityIndicator fragmentLoadIndicator;

    public final ContainerState HOME = new ContainerState("Home", R.drawable.home, new HomeFragment());
    public final ContainerState PROFILE = new ContainerState("Profile", R.drawable.profile, new ProfileFragment());
    public final ContainerState PAYMENT = new ContainerState("Payment", R.drawable.payment, new PaymentFragment());
    public final ContainerState SETTINGS = new ContainerState("Settings", R.drawable.settings, new SettingsFragment());
    public final ContainerState PROMOTE = new ContainerState("Promote", R.drawable.share, new PromoteFragment());

    private final Fragment loadingFragment = new LoadingFragment();

    private final ViewPager.OnPageChangeListener pageChangeListener = new SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
        }
    };

    public ContainerState[] containerStates = new ContainerState[]{
            HOME, PROFILE, PAYMENT, SETTINGS, PROMOTE
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SeshApplication.IS_LIVE) {
            Intent intent = new Intent(this, SplashActivity.class);
            startActivity(intent);
            return;
        }

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
        slidingMenu.setOnOpenListener(sideMenuFragment);
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

        this.fragmentLoadIndicator =
                (SeshActivityIndicator) findViewById(R.id.fragment_loading_indicator);

        setCurrentState(HOME, null);

        PeriodicFetchBroadcastReceiver.setSeshInfoUpdateListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        User.fetchUserInfoFromServer(this);

        // Refresh device token on server via GCM service
        Intent gcmIntent = new Intent(this, GCMRegistrationIntentService.class);
        gcmIntent.putExtra(SeshInstanceIDListenerService.IS_TOKEN_STALE_KEY, false);
        gcmIntent.putExtra(GCMRegistrationIntentService.ANONYMOUS_TOKEN_REFRESH, false);
        startService(gcmIntent);

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int code = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (code != ConnectionResult.SUCCESS) {
            googleApiAvailability.getErrorDialog(this, code, 0).show();
        }
    }

    @Override
    protected void handleActionIntent(Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(UPDATE_CONTAINER_STATE_ACTION)) {
                int mainContainerStateIndex = intent.getIntExtra(MAIN_CONTAINER_STATE_INDEX, 0);
                String fragmentFlag = intent.getStringExtra(FRAGMENT_FLAG_KEY);

                HashMap<String, Object> options = new HashMap<>();
                options.put(fragmentFlag, true);

                setCurrentState(containerStates[mainContainerStateIndex], options);
            } else if (intent.getAction().equals(VIEW_SESH_ACTION)) {
                int seshId = intent.getIntExtra(ViewSeshFragment.SESH_KEY, -1);

                setCurrentState(new ContainerState("Sesh", 0, ViewSeshFragment.newInstance(seshId)));
            } else if (intent.getAction() == SESH_CANCELLED_ACTION) {
                // IF SESH HAS BEEN CANCELLED AND MAIN CONTAINER IS IN FOREGROUND, WE ENSURE VIEWSESHFRAGMENT IS NOT VISIBLE
                if (currentContainerState.fragment instanceof ViewSeshFragment) {
                    setCurrentState(HOME);
                }
            } else if (intent.getAction() == DISPLAY_SIDE_MENU_UPDATE) {
                sideMenuFragment.setStatusFlag(SideMenuFragment.MENU_OPEN_DISPLAY_NEW_REQUEST);
                Handler handler = new Handler();
                Runnable openSideMenu = new Runnable() {
                    @Override
                    public void run() {
                        slidingMenu.toggle(true);
                    }
                };

                if (!slidingMenu.isMenuShowing()) {
                    // hacky, but delay menu open animation to account for activity transition
                    handler.postDelayed(openSideMenu, 1000);
                }
            }
        }

        super.handleActionIntent(intent);
    }


    private void showNotificationDialog(final String title, final String content, final String confirmButtonText, final String type) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentContainerState == HOME) {
                    HomeFragment homeFragment = (HomeFragment) currentContainerState.fragment;
                    if (homeFragment.getCurrTabItem() == HomeFragment.TabItem.LEARN_TAB) {
                        SeshDialog.showDialog(getFragmentManager(), title, content, confirmButtonText, null,
                                type);
                        Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(300);
                    }
                } else {
                    SeshDialog.showDialog(getFragmentManager(), title, content,
                            confirmButtonText, null,
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

    public ContainerState getCurrentState() {
        return currentContainerState;
    }

    /**
     * Convenience method for setting current state without flags
     * @param state
     */
    public void setCurrentState(ContainerState state) {
        setCurrentState(state, null);
    }

    public void setCurrentState(ContainerState selectedMenuOption, Map<String, Object> options) {
        if (!(selectedMenuOption.fragment instanceof FragmentOptionsReceiver)) {
            Log.e(TAG, "Invalid Fragment: All fragments within MainContainerActivity must implement FragmentFlagReceiver");
            return;
        }

        if (currentContainerState != null) {
            FragmentOptionsReceiver optionsReceiver = (FragmentOptionsReceiver) currentContainerState.fragment;
            optionsReceiver.clearFragmentOptions();
        }

        TextView title = (TextView) findViewById(R.id.action_bar_title);
        title.setText(selectedMenuOption.title);

        if (slidingMenu.isMenuShowing()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(currentContainerState.fragment)
                    .commitAllowingStateLoss();
            getSupportFragmentManager()
                    .executePendingTransactions();
            currentContainerState = selectedMenuOption;
            fragmentLoadIndicator.setVisibility(View.VISIBLE);

            slidingMenu.toggle(true);
            slidingMenu.setOnClosedListener(new SlidingMenu.OnClosedListener() {
                @Override
                public void onClosed() {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.main_container, currentContainerState.fragment, currentContainerState.fragment.getClass().getName())
                            .addToBackStack(currentContainerState.fragment.getClass().getName())
                            .commitAllowingStateLoss();
                    fragmentLoadIndicator.setVisibility(View.GONE);

                    slidingMenu.setOnClosedListener(null);
                }
            });
        } else {
            currentContainerState = selectedMenuOption;

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_container, currentContainerState.fragment, currentContainerState.fragment.getTag())
                    .addToBackStack(currentContainerState.fragment.getTag())
                    .commitAllowingStateLoss();
        }

        if (options != null) {
            FragmentOptionsReceiver flagReceiver = (FragmentOptionsReceiver) currentContainerState.fragment;
            flagReceiver.updateFragmentOptions(options);
        }
    }

    public void onFragmentReplacedAndRendered() {
//        if (slidingMenu.isMenuShowing()) {
////            slidingMenu.stretchOut();
//            slidingMenu.toggle(true);
//        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onFetchUpdate() {
//        sideMenuFragment.updateRequestAndSeshList();
    }

    @Override
    public boolean isMainContainerActivity() {
        return true;
    }

    public void onNetworkError() {
        Log.e(TAG, "Network Error");
    }
}



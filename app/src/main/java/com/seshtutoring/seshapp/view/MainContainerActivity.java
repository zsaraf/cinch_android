package com.seshtutoring.seshapp.view;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.IntentSender;
import android.support.v4.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.SeshApplication;
import com.seshtutoring.seshapp.model.Constants;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.services.GCMRegistrationIntentService;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.services.SeshInstanceIDListenerService;
import com.seshtutoring.seshapp.services.notifications.SeshNotificationManagerService;
import com.seshtutoring.seshapp.services.notifications.handlers.SeshCancelledNotificationHandler;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.util.LaunchPrerequisiteAsyncTask;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.LocationManager;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.LoadingFragment;
import com.seshtutoring.seshapp.view.MainContainerStateManager.NavigationItemState;
import com.seshtutoring.seshapp.view.fragments.SideMenuFragment;
import com.seshtutoring.seshapp.view.fragments.ViewSeshFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainContainerActivity extends SeshActivity implements SeshDialog.OnSelectionListener {
    private static final String TAG = MainContainerActivity.class.getName();
    public static final String MAIN_CONTAINER_STATE_INDEX = "main_container_state_index";
    public static final String FRAGMENT_FLAG_KEY = "fragment_flags";
    private static final String DIALOG_TYPE_FOUND_TUTOR = "dialog_type_found_tutor";
    private static final String INTENT_HANDLED = "intent_handled";
    public static final String UPDATE_CONTAINER_STATE_ACTION = "update_main_container_state";
    public static final String DISPLAY_SIDE_MENU_UPDATE = "display_side_menu_update";
    public static final String LOCATION_MANAGER_CONNECTED = "location_manager_connected";
    public static final String LOCATION_MANAGER_FAILED = "location_manager_failed";
    public static final String VIEW_SESH_ACTION = "view_sesh";
    public static final String SESH_CANCELLED_ACTION = "sesh_cancelled";
    public static final String NEW_MESSAGE_ACTION = "new_message";
    public static final String REFRESH_TUTOR_CREDITS = "refresh_profile";
    public static final String REFRESH_JOBS = "refresh_jobs";
    public static final String REFRESH_USER_INFO = "refresh_user_info";
    public static final String REQUEST_SENT_ACTION = "request_sent";
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

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private SideMenuFragment sideMenuFragment;
    private RelativeLayout editButton;
    private MainContainerStateManager containerStateManager;
    private CallbackManager fbCallbackManager;
    private LocationManager locationManager;
    private BroadcastReceiver broadcastReceiver;
    private ApplicationLifecycleTracker tracker;

    private final Fragment loadingFragment = new LoadingFragment();

    private final ViewPager.OnPageChangeListener pageChangeListener = new SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
        }
    };

    public void setEditButtonHidden(boolean hidden) {
        if (hidden) {
            editButton.setVisibility(View.GONE);
        } else {
            editButton.setVisibility(View.VISIBLE);
        }
    }

    /* (non-Javadoc)
* @see android.app.Activity#onStart()
*/
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "On Restart .....");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "On Start .....");
    }

    /* (non-Javadoc)
    * @see android.app.Activity#onStop()
    */
    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "On Stop .....");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(this);

        fbCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                handleFacebookResponse();
            }

            @Override
            public void onCancel() {
                handleFacebookResponse();

            }

            @Override
            public void onError(FacebookException e) {
                handleFacebookResponse();

            }
        });

        if (!SeshApplication.IS_LIVE) {
            Intent intent = new Intent(this, SplashActivity.class);
            startActivity(intent);
            return;
        }

        setContentView(R.layout.main_container_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);

        LayoutUtils layoutUtils = new LayoutUtils(this);
        layoutUtils.setupCustomActionBar(this, false);

        RelativeLayout backButton = (RelativeLayout) findViewById(R.id.action_bar_back_button);
        ViewGroup layout = (ViewGroup) backButton.getParent();
        layout.removeView(backButton);

        editButton = (RelativeLayout)findViewById(R.id.action_bar_edit_button);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_main_container);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_layout_open,  /* "open drawer" description */
                R.string.drawer_layout_close  /* "close drawer" description */
        ) {

            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_SETTLING) {
                    if (!isDrawerOpen()) {
                        // starts opening
                        sideMenuFragment.onOpen();
                    }
                    invalidateOptionsMenu();
                }
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                sideMenuFragment.onOpened();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        sideMenuFragment = new SideMenuFragment();
        containerStateManager = new MainContainerStateManager(this, sideMenuFragment);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.left_drawer, sideMenuFragment)
                .commit();

        RelativeLayout menuButton = (RelativeLayout) findViewById(R.id.action_bar_menu_button);
        menuButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i(TAG, "ACTION BAR HEIGHT: " + getSupportActionBar().getHeight());
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                    } else {
                        mDrawerLayout.openDrawer(Gravity.LEFT);
                    }
                }
                return true;
            }
        });

        // If the constants have not been fetched, fetch 'em
        SharedPreferences prefs = this.getSharedPreferences(Constants.CONSTANTS_SHARED_PREFS, 0);
        if (prefs.getFloat(Constants.HOURLY_RATE_KEY, -1) == -1) {
            Constants.fetchConstantsFromServer(this);
        }

        this.locationManager = LocationManager.sharedInstance(this);
        broadcastReceiver = actionBroadcastReceiver;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private BroadcastReceiver actionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == LOCATION_MANAGER_FAILED) {
                Log.e(TAG, "Resolving location manager");
                try {
                    // Start an Activity that tries to resolve the error
                    ConnectionResult cr = intent.getParcelableExtra(LocationManager.CONNECTION_RESULT);
                    int resolutionRequest = intent.getIntExtra(LocationManager.RESOLUTION_REQUEST, 0);
                    cr.startResolutionForResult(MainContainerActivity.this, resolutionRequest);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            } else if (intent.getAction() == REFRESH_USER_INFO) {
                User currentUser = User.currentUser(getApplicationContext());
                if (!currentUser.school.enabled) {
                    Intent launchSchoolIntent = new Intent(getApplicationContext(), LaunchSchoolActivity.class);
                    startActivity(launchSchoolIntent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }

            }
        }
    };

    private void handleFacebookResponse() {
        //handle facebook login response here
    }

    public void facebookLogin() {
        //login to FB before trying to grab photos
        List<String> permissions = new ArrayList<String>();
        permissions.add("public_profile");
        LoginManager.getInstance().logInWithReadPermissions(this, permissions);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //should be checks here to make sure it's from FB in case other things actions trigger onActivityResult in the future
        super.onActivityResult(requestCode, resultCode, data);
        //add last line back in with checks when ready to use FB
        //fbCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestActivity.CREATE_LEARN_REQUEST_REQUEST_CODE) {
            if (resultCode == RequestActivity.LEARN_REQUEST_CREATE_SUCCESSFUL_RESPONSE_CODE) {
                Intent intent = new Intent(SeshNotificationManagerService.CREATE_REQUEST_SENT_NOTIFICATION_ACTION,
                        null, this, SeshNotificationManagerService.class);
                startService(intent);
            }
        } else if (requestCode == AddTutorClassesActivity.ADD_TUTOR_CLASSES_CREATE) {

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        this.locationManager.disconnectClient();
        this.unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh device token on server via GCM service
        Intent gcmIntent = new Intent(this, GCMRegistrationIntentService.class);
        gcmIntent.putExtra(SeshInstanceIDListenerService.IS_TOKEN_STALE_KEY, false);
        startService(gcmIntent);

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int code = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (code != ConnectionResult.SUCCESS) {
            googleApiAvailability.getErrorDialog(this, code, 0).show();
        }

        this.locationManager.connectClient();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainContainerActivity.LOCATION_MANAGER_FAILED);
        intentFilter.addAction(MainContainerActivity.REFRESH_USER_INFO);
        this.registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void handleActionIntent(Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(UPDATE_CONTAINER_STATE_ACTION)) {
                int mainContainerStateIndex = intent.getIntExtra(MAIN_CONTAINER_STATE_INDEX, 0);
                String fragmentFlag = intent.getStringExtra(FRAGMENT_FLAG_KEY);

                HashMap<String, Object> options = new HashMap<>();
                options.put(fragmentFlag, true);

                containerStateManager
                        .setContainerStateForNavigationIndex(mainContainerStateIndex, options);
            } else if (intent.getAction().equals(VIEW_SESH_ACTION)) {
                Sesh sesh = new Sesh(); // hacky, but allows us to pass seshId into containerStateManager in a compatible way
                sesh.seshId = intent.getIntExtra(ViewSeshFragment.SESH_KEY, -1);

                containerStateManager.setContainerStateForSesh(sesh);
            } else if (intent.getAction().equals(NEW_MESSAGE_ACTION)) {
                Sesh sesh = new Sesh(); // hacky, but allows us to pass seshId into containerStateManager in a compatible way
                sesh.seshId = intent.getIntExtra(ViewSeshFragment.SESH_KEY, -1);

                containerStateManager.setContainerStateForSeshWithMessaging(sesh);
            } else if (intent.getAction() == SESH_CANCELLED_ACTION) {
                // IF SESH HAS BEEN CANCELLED AND MAIN CONTAINER IS IN FOREGROUND, WE ENSURE VIEWSESHFRAGMENT IS NOT VISIBLE
                if (containerStateManager.getMainContainerState().fragment instanceof ViewSeshFragment) {
                    containerStateManager.setContainerStateForNavigation(NavigationItemState.HOME);
                }

                String title
                        = intent.getStringExtra(SeshCancelledNotificationHandler.SESH_CANCELLED_DIALOG_TITLE);
                String message
                        = intent.getStringExtra(SeshCancelledNotificationHandler.SESH_CANCELLED_DIALOG_MESSAGE);

                final SeshDialog seshDialog = new SeshDialog();
                seshDialog.setTitle(title);
                seshDialog.setMessage(message);
                seshDialog.setDialogType(SeshDialog.SeshDialogType.ONE_BUTTON);
                seshDialog.setFirstChoice("OKAY");
                seshDialog.setFirstButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        seshDialog.dismiss();
                        Notification.currentNotificationHandled(getApplicationContext(), true);
                    }
                });
                seshDialog.setType("sesh_cancelled");
                seshDialog.showWithDelay(getFragmentManager(), null, 2000);
            } else if (intent.getAction() == DISPLAY_SIDE_MENU_UPDATE) {
                Handler handler = new Handler();
                Runnable openSideMenu = new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.openDrawer(Gravity.LEFT);
                    }
                };

                if (!mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    // hacky, but delay menu open animation to account for activity transition
                    handler.postDelayed(openSideMenu, 2000);
                }
            } else if (intent.getAction() == REQUEST_SENT_ACTION) {
                final SeshDialog seshDialog = new SeshDialog();
                seshDialog.setDialogType(SeshDialog.SeshDialogType.ONE_BUTTON);
                seshDialog.setTitle("Request Sent!");
                seshDialog.setMessage("Hold Tight! We'll notify you as soon as a tutor accepts your request.");
                seshDialog.setFirstChoice("OKAY");
                seshDialog.setType("request_sent");
                seshDialog.setFirstButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        seshDialog.dismiss();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mDrawerLayout.openDrawer(Gravity.LEFT);
                            }
                        }, 1000);
                    }
                });
                seshDialog.show(getFragmentManager(), null);
            }
        }

        super.handleActionIntent(intent);
    }

    public void onDialogSelection(int selection, String type) {
        SeshNetworking seshNetworking = new SeshNetworking(this);

        if (type.equals("cashout") && selection == 1) {

            //moved to individual fragments

        } else if (type.equals("logout") && selection == 1) {

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
            //Toast.makeText(this, "showing activity", Toast.LENGTH_LONG).show();
        }
    }

    private void onLogoutResponse(JSONObject responseJson) {
        try {
            if (responseJson.get("status").equals("SUCCESS")) {
                User.logoutUserLocally(this);
                Intent intent = new Intent(this, AuthenticationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (responseJson.get("status").equals("FAILURE")) {
                String message = responseJson.get("message").toString();
                showErrorDialog("Whoops!", message);
            }
        } catch (JSONException e) {
            showErrorDialog("Whoops!", "There was a network error, please check your internet connection and try again!");

        }
    }

    private void onLogoutFailure(String errorMessage) {
        showErrorDialog("Whoops!", "There was a network error, please check your internet connection and try again!");

    }

    private void showErrorDialog(String title, String message) {
        SeshDialog.showDialog(getFragmentManager(), title, message,
                "OKAY", null, "view_request_network_error");
    }

    /**
     * Convenience method for setting current state without flags
     * @param
     */
    public void replaceCurrentFragment(ContainerState oldState, ContainerState newState) {
        replaceCurrentFragment(oldState, newState, null);
    }

    public void replaceCurrentFragment(ContainerState oldState, ContainerState newState, Map<String, Object> options) {
        if (!(newState.fragment instanceof FragmentOptionsReceiver)) {
            Log.e(TAG, "Invalid Fragment: All fragments within MainContainerActivity must implement FragmentFlagReceiver");
            return;
        }

        if (oldState != null) {
            FragmentOptionsReceiver optionsReceiver = (FragmentOptionsReceiver) oldState.fragment;
            optionsReceiver.clearFragmentOptions();
        }

        setActionBarTitle(newState.title);

        Log.d(TAG, "New container state tag: " + newState.tag);

        if (!newState.tag.equals("payment")) {
            setEditButtonHidden(true);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, newState.fragment, newState.fragment.getTag())
                .commitAllowingStateLoss();

        if (options != null) {
            FragmentOptionsReceiver flagReceiver = (FragmentOptionsReceiver) newState.fragment;
            flagReceiver.updateFragmentOptions(options);
        }
    }

    public void setActionBarTitle(String title) {
        TextView titleTextView = (TextView) findViewById(R.id.action_bar_title);
        titleTextView.setText(title);
        LayoutUtils layUtils = new LayoutUtils(this);
        titleTextView.setTypeface(layUtils.getBookGothamTypeface());
    }


    public void onFragmentReplacedAndRendered() {
        // Handle fragment replacement (if necessary)
    }

    public void closeDrawer(boolean animated) {
        Log.d(TAG, "CLOSING DRAWER");
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        }
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout.isDrawerOpen(Gravity.LEFT);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean isMainContainerActivity() {
        return true;
    }

    public void onNetworkError() {
        Log.e(TAG, "Network Error");
    }

    public MainContainerStateManager getContainerStateManager() {
        return containerStateManager;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}



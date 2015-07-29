package com.seshtutoring.seshapp.view;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.content.ComponentName;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshDialog;
import com.seshtutoring.seshapp.view.fragments.LearnViewFragment;
import com.seshtutoring.seshapp.view.fragments.SideMenuFragment;
import com.seshtutoring.seshapp.view.fragments.SideMenuFragment.MenuOption;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainContainerActivity extends AppCompatActivity implements SeshDialog.OnSelectionListener {
    private static final String TAG = MainContainerActivity.class.getName();

    private SlidingMenu slidingMenu;
    private SideMenuFragment sideMenuFragment;
    private MenuOption selectedMenuOption;

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

        setCurrentState(MenuOption.HOME);

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
    }

    @Override
    public void onResume() {
        super.onResume();
        User.fetchUserInfoFromServer(this);

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int code = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (code != ConnectionResult.SUCCESS) {
            googleApiAvailability.getErrorDialog(this, code, 0).show();
        }
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
        return selectedMenuOption;
    }

    public void setCurrentState(MenuOption selectedMenuOption) {
        this.selectedMenuOption = selectedMenuOption;
        TextView title = (TextView) findViewById(R.id.action_bar_title);

        title.setText(selectedMenuOption.title);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, selectedMenuOption.fragment)
                .commit();
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


    public void onNetworkError() {
        Log.e(TAG, "Network Error");
    }
}



package com.seshtutoring.seshapp.view;

import android.app.ActionBar;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.view.fragments.HomeFragment;
import com.seshtutoring.seshapp.view.fragments.SideMenuFragment;
import com.seshtutoring.seshapp.view.fragments.SideMenuFragment.MenuOption;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainContainerActivity extends ActionBarActivity {
    private static final String TAG = MainContainerActivity.class.getName();

    private SlidingMenu slidingMenu;
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

        setCurrentState(MenuOption.HOME);

        slidingMenu = new SlidingMenu(this);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        slidingMenu.setBehindOffsetRes(R.dimen.sliding_menu_behind_offset);
        slidingMenu.setBehindScrollScale(0);
        slidingMenu.setFadeEnabled(false);
        slidingMenu.setMenu(R.layout.sliding_menu_frame);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.sliding_menu_frame, new SideMenuFragment())
                .commit();

        ImageButton menuButton = (ImageButton) findViewById(R.id.action_bar_menu_button);
        menuButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    slidingMenu.toggle(true);
                }
                return false;
            }
        });
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

    public void onNetworkError() {
        Log.e(TAG, "Network Error");
    }
}

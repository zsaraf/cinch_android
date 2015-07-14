package com.seshtutoring.seshapp.view;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.seshtutoring.seshapp.R;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainContainerActivity extends ActionBarActivity {
    public static enum MenuOption {
        HOME, PROFILE, PAYMENT, SETTINGS, PROMOTE
    }

    private SlidingMenu slidingMenu;
    private MenuOption selectedMenuOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.sesh_action_bar);

        setCurrentState(MenuOption.HOME);

        slidingMenu = new SlidingMenu(this);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.setBehindOffsetRes(R.dimen.sliding_menu_behind_offset);
        slidingMenu.setBehindScrollScale(0);
        slidingMenu.setFadeEnabled(false);
        slidingMenu.setMenu(R.layout.sliding_menu_frame);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.sliding_menu_frame, new SideMenuFragment())
                .commit();

        ImageButton menuButton = (ImageButton) findViewById(R.id.action_bar_menu_button);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingMenu.toggle(true);
            }
        });
    }

    public MenuOption getCurrentState() {
        return selectedMenuOption;
    }

    public void setCurrentState(MenuOption selectedMenuOption) {
        this.selectedMenuOption = selectedMenuOption;
        TextView title = (TextView) findViewById(R.id.action_bar_title);

        if (selectedMenuOption == MenuOption.HOME) {
            title.setText(R.string.title_activity_home);
        } else if (selectedMenuOption == MenuOption.PROFILE) {
            title.setText(R.string.title_activity_profile);
        } else if (selectedMenuOption == MenuOption.PAYMENT) {
            title.setText(R.string.title_activity_payment);
        } else if (selectedMenuOption == MenuOption.SETTINGS) {
            title.setText(R.string.title_activity_settings);
        } else if (selectedMenuOption == MenuOption.PROMOTE) {
            title.setText(R.string.title_activity_promote);
        }
    }

    public void closeDrawer() {
        slidingMenu.toggle(true);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
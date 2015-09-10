package com.seshtutoring.seshapp.view;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.LayoutUtils;

public class PrivacyActivity extends SeshActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        LayoutUtils layUtils = new LayoutUtils(this);
        layUtils.setupCustomActionBar(this, true);

        TextView title = (TextView) findViewById(R.id.action_bar_title);
        title.setText("Privacy Policy");
        title.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Gotham-Book.otf"));

        RelativeLayout menuButton = (RelativeLayout) findViewById(R.id.action_bar_menu_button);
        ViewGroup layout = (ViewGroup) menuButton.getParent();
        layout.removeView(menuButton);

        RelativeLayout backButton = (RelativeLayout) findViewById(R.id.action_bar_back_button);
        backButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    onBackPressed();
                }
                return true;
            }
        });
    }


}

package com.seshtutoring.seshapp.view;

import android.app.ActionBar;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsoluteLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.LayoutUtils;

public class SupportActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        LayoutUtils layUtils = new LayoutUtils(this);
        getWindow().getDecorView().findViewById(android.R.id.content).setPadding(0, layUtils.getActionBarHeightPx(), 0, 0);

        WebView webView = (WebView) findViewById(R.id.support_webview);
        webView.loadUrl(getString(R.string.support_url));

        getWindow().setLayout(WebView.LayoutParams.MATCH_PARENT, WebView.LayoutParams.MATCH_PARENT);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.sesh_action_bar);
        getSupportActionBar().setElevation(0);
        TextView title = (TextView) findViewById(R.id.action_bar_title);
        title.setText("Support");
        title.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Gotham-Book.otf"));

        ImageButton menuButton = (ImageButton) findViewById(R.id.action_bar_menu_button);
        ViewGroup layout = (ViewGroup) menuButton.getParent();
        layout.removeView(menuButton);

        ImageButton backButton = (ImageButton) findViewById(R.id.action_bar_back_button);
        backButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    onBackPressed();
                    return true;
                }
                return false;
            }
        });
    }

}

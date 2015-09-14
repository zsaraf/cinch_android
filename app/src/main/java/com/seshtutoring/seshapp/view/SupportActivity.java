package com.seshtutoring.seshapp.view;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsoluteLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.util.LayoutUtils;

public class SupportActivity extends SeshActivity {

    private ProgressDialog progDailog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        LayoutUtils layUtils = new LayoutUtils(this);
        layUtils.setupCustomActionBar(this, true);

        WebView webView = (WebView) findViewById(R.id.support_webview);

        progDailog = ProgressDialog.show(this, "Loading","Please wait...", true);
        progDailog.setCancelable(false);


        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                progDailog.show();
                view.loadUrl(url);

                return true;
            }

            @Override
            public void onPageFinished(WebView view, final String url) {
                progDailog.dismiss();
            }
        });

        webView.loadUrl(getString(R.string.support_url));


        TextView title = (TextView) findViewById(R.id.action_bar_title);
        title.setText("Support");
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

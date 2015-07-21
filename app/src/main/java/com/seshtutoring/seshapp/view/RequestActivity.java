package com.seshtutoring.seshapp.view;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.view.fragments.LearnViewFragment;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by nadavhollander on 7/20/15.
 */
public class RequestActivity extends Activity {
    private static final String TAG = RequestActivity.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        setContentView(R.layout.request_transparent_layout);

        if (getIntent().hasExtra(LearnViewFragment.BLURRED_MAP_BITMAP_PATH_KEY)) {
            String path = getIntent().getStringExtra(LearnViewFragment.BLURRED_MAP_BITMAP_PATH_KEY);
            ImageView requestLayoutBackground =
                    (ImageView) findViewById(R.id.request_layout_background);
            requestLayoutBackground.setImageDrawable(Drawable.createFromPath(path));
            requestLayoutBackground.animate().alpha(1).setDuration(300);
        } else {
            Log.e(TAG, "Blurred background not included with intent to Request Layout");
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}

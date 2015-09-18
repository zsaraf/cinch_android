package com.seshtutoring.seshapp;

import android.content.Context;
import com.crashlytics.android.Crashlytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.orm.SugarApp;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.util.SeshImageCache;
import com.seshtutoring.seshapp.util.SeshMixpanelAPI;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import io.fabric.sdk.android.Fabric;
import org.joda.time.DateTime;

import java.io.IOException;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by nadavhollander on 7/7/15.
 */
public class SeshApplication extends SugarApp {
    private static final String TAG = SeshApplication.class.getName();

    private ApplicationLifecycleTracker applicationLifecycleTracker;

    public static final boolean IS_LIVE = true;
    public static final boolean IS_DEV = false;
    public static final boolean USE_PERSONAL = true;

    //    Pre-reg app functionality -- to be deleted v1
    private DateTime androidReleaseDate;
    private SeshMixpanelAPI seshMixpanelAPI;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        this.applicationLifecycleTracker = new ApplicationLifecycleTracker(this);

        // initialize default font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Gotham-Light.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        this.seshMixpanelAPI = SeshMixpanelAPI.getInstance(this);

    }

    public ApplicationLifecycleTracker getApplicationLifecycleTracker() {
        return applicationLifecycleTracker;
    }

    //    Pre-reg app functionality -- to be deleted v1
    public void setAndroidReleaseDate(DateTime releaseDate) {
        this.androidReleaseDate = releaseDate;
    }

    //    Pre-reg app functionality -- to be deleted v1
    public DateTime getAndroidReleaseDate() {
        if (androidReleaseDate != null) {
            return androidReleaseDate;
        } else {
            return new DateTime(2015, 8, 10, 9, 0);
        }
    }

    public SeshMixpanelAPI getSeshMixpanelAPI() {
        return seshMixpanelAPI;
    }

}

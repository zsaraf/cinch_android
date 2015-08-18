package com.seshtutoring.seshapp;

import android.content.Context;
import com.crashlytics.android.Crashlytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.orm.SugarApp;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import io.fabric.sdk.android.Fabric;
import org.joda.time.DateTime;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by nadavhollander on 7/7/15.
 */
public class SeshApplication extends SugarApp {
    private static final String TAG = SeshApplication.class.getName();
    public static final String MIXPANEL_PROJECT_TOKEN = "ee5da3fa7c3cdc47114fc51794ceb7b6";

    private ApplicationLifecycleTracker applicationLifecycleTracker;

    public static final boolean IS_LIVE = false;
    public static final boolean IS_DEV = false;

    //    Pre-reg app functionality -- to be deleted v1
    private DateTime androidReleaseDate;
    private MixpanelAPI mixpanelAPI;

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

        this.mixpanelAPI = MixpanelAPI.getInstance(this, MIXPANEL_PROJECT_TOKEN);
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

    public MixpanelAPI getMixpanelAPI() {
        return mixpanelAPI;
    }
}

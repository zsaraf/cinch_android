package com.seshtutoring.seshapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.orm.SugarApp;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.ApplicationLifecycleTracker;
import com.seshtutoring.seshapp.util.LaunchPrerequisiteAsyncTask;
import com.seshtutoring.seshapp.util.SeshImageCache;
import com.seshtutoring.seshapp.util.SeshMixpanelAPI;
import com.seshtutoring.seshapp.util.networking.SeshAuthManager;
import com.seshtutoring.seshapp.view.LaunchSchoolActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity;
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

        ApplicationLifecycleTracker.sharedInstance(this).setApplicationLifecycleCallback(new ApplicationLifecycleTracker.ApplicationLifecycleCallback() {
            @Override
            public void applicationDidEnterForeground() {
                beginAsyncTask();
            }

            @Override
            public void applicationWillEnterBackground() {

            }
        });
    }

    public void beginAsyncTask() {
        (new LaunchPrerequisiteAsyncTask(getApplicationContext(), new LaunchPrerequisiteAsyncTask.PrereqsFulfilledListener() {
            @Override
            public void onPrereqsFulfilled() {
                getApplicationContext().sendBroadcast(new Intent(MainContainerActivity.REFRESH_USER_INFO));
            }
        })).execute();
    }

    public ApplicationLifecycleTracker getApplicationLifecycleTracker() {
        return applicationLifecycleTracker;
    }

    public SeshMixpanelAPI getSeshMixpanelAPI() {
        return seshMixpanelAPI;
    }

}

package com.seshtutoring.seshapp.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.seshtutoring.seshapp.SeshApplication;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created by nadavhollander on 8/18/15.
 */
public class SeshMixpanelAPI {
    private MixpanelAPI mixpanelAPI;
    public static final String MIXPANEL_PROJECT_TOKEN = "ee5da3fa7c3cdc47114fc51794ceb7b6";

    public SeshMixpanelAPI(MixpanelAPI mixpanelAPI) {
        this.mixpanelAPI = mixpanelAPI;
    }

    public static SeshMixpanelAPI getInstance(Context context) {
        return new SeshMixpanelAPI(MixpanelAPI.getInstance(context, MIXPANEL_PROJECT_TOKEN));
    }

    public MixpanelAPI getAPI() {
        return mixpanelAPI;
    }

    public void track(String event) {
        if (!SeshApplication.IS_DEV) {
            mixpanelAPI.track(event);
        }
    }

    public void trackMap(String event, Map<String, Object> properties) {
        if (!SeshApplication.IS_DEV) {
            mixpanelAPI.trackMap(event, properties);
        }
    }
}

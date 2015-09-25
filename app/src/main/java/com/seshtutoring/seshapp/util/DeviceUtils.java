package com.seshtutoring.seshapp.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import org.joda.time.DateTimeZone;

import java.util.Map;

/**
 * Created by zacharysaraf on 9/24/15.
 */
public class DeviceUtils {

    private static final String TIMEZONE_NAME_PARAM = "timezone_name";
    private static final String DEVICE_MODEL_PARAM = "device_model";
    private static final String SYSTEM_VERSION_PARAM = "system_version";
    private static final String APP_VERSION_PARAM = "app_version";
    private static final String DEVICE_TYPE_PARAM = "type";


    /** Returns the consumer friendly device name */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        if (manufacturer.equalsIgnoreCase("HTC")) {
            // make sure "HTC" is fully capitalized.
            return "HTC " + model;
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
    }

    public static void paramsByAddingDeviceInformation(Map<String, String> params, Context mContext) {
        DateTimeZone timezone = DateTimeZone.getDefault();
        Integer appVersion = 0;
        try {
            PackageInfo pinfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            appVersion = pinfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        params.put(TIMEZONE_NAME_PARAM, timezone.getID());
        params.put(DEVICE_MODEL_PARAM, getDeviceName());
        params.put(APP_VERSION_PARAM, appVersion + "");
        params.put(SYSTEM_VERSION_PARAM, Build.VERSION.SDK_INT + "");
        params.put(DEVICE_TYPE_PARAM, "android");
    }
}

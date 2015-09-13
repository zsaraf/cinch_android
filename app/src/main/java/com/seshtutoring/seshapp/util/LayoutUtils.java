package com.seshtutoring.seshapp.util;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.SystemClock;
import android.support.v7.widget.Toolbar;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.enrique.stackblur.StackBlurManager;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.view.SeshActivity;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nadavhollander on 7/16/15.
 */
public class LayoutUtils {
    private static final String TAG = LayoutUtils.class.getName();
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    private Context mContext;

    public LayoutUtils(Context context) {
        this.mContext = context;
    }

    public void crossFade(final View fadeOutView, final View fadeInView) {
        fadeOutView.animate().alpha(0f).setDuration(100);

        fadeInView.animate().alpha(1f).setDuration(100);
    }

    public int dpToPixels(float dp) {
        Resources r = mContext.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public static abstract class NoUnderlineClickableSpan extends ClickableSpan {
        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(false);
        }
    }

    public static String abbreviatedNameForFullName(String fullName) {
        return fullName.substring(0, fullName.lastIndexOf(" ")+2) + ".";
    }

    public static Bitmap getBlurredView(View rootView) {
        rootView.setDrawingCacheEnabled(true);
        rootView.buildDrawingCache();
        Bitmap bm  = rootView.getDrawingCache();
        StackBlurManager stackBlurManager = new StackBlurManager(bm);
        stackBlurManager.process(3);
        return stackBlurManager.returnBlurredImage();
    }

    public int getActionBarHeightPx() {
        return dpToPixels(56f);
    }

    public Bitmap blurScreenshot(Bitmap screenshot) {
        int width = screenshot.getWidth();
        int height = screenshot.getHeight();

        int scaledWidth = width / 4;
        int scaledHeight = height / 4;

        Bitmap miniScreenshot = Bitmap.createScaledBitmap(screenshot, scaledWidth, scaledHeight, true);

        StackBlurManager stackBlurManager = new StackBlurManager(miniScreenshot);
        stackBlurManager.process(10);

        return stackBlurManager.returnBlurredImage();
    }

    public int getDimensionPx(int id) {
        return mContext.getResources().getDimensionPixelSize(id);
    }

    public int getScreenWidthPx(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    public int getScreenHeightPx(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public Typeface getLightGothamTypeface() {
        return Typeface.createFromAsset(mContext.getAssets(), "fonts/Gotham-Light.otf");
    }

    public Typeface getMediumGothamTypeface() {
        return Typeface.createFromAsset(mContext.getAssets(), "fonts/Gotham-Medium.otf");
    }

    public Typeface getBookGothamTypeface() {
        return Typeface.createFromAsset(mContext.getAssets(), "fonts/Gotham-Book.otf");
    }

    public void setupCustomActionBar(SeshActivity activity, boolean setOverlayPadding) {
        if (setOverlayPadding) {
            activity.getWindow().getDecorView().findViewById(android.R.id.content).setPadding(0, getActionBarHeightPx(), 0, 0);
        }
        activity.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        activity.getSupportActionBar().setCustomView(R.layout.sesh_action_bar);
        activity.getSupportActionBar().setElevation(0);
        Toolbar parent = (Toolbar) activity.getSupportActionBar().getCustomView().getParent();
        parent.setContentInsetsAbsolute(0, 0);
    }

    public static int generateViewId() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }
}

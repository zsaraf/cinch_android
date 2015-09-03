package com.seshtutoring.seshapp.util;

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
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.enrique.stackblur.StackBlurManager;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by nadavhollander on 7/16/15.
 */
public class LayoutUtils {
    private static final String TAG = LayoutUtils.class.getName();
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


    public int getScreenHeightPx(Activity baseActivity) {
        Display display = baseActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return size.y;
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
}

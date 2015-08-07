package com.seshtutoring.seshapp.util;

import android.app.Instrumentation;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.SystemClock;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.enrique.stackblur.StackBlurManager;

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
//        TypedValue tv = new TypedValue();
//        if (mContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
//            return TypedValue.complexToDimensionPixelSize(tv.data, mContext.getResources().getDisplayMetrics());
//        } else {
//            Log.e(TAG, "Couldn't programatically retrieve ActionBarHeight");
//            return dpToPixels(48f);
//        }
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

    public int getKeyboardHeight(View viewMain) {
        // viewMain <-- findViewById( android.R.id.content).getRootView();
        int iSoftkeyboardHeight = viewMain.getHeight();

        int y = iSoftkeyboardHeight - 2;
        int x = 10;
        int counter = 0;
        int height = y;
        int iSoftkeyboardHeightNow = 0;
        Instrumentation instrumentation = new Instrumentation();
        while (true) {
            final MotionEvent m = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x, y, 0);
            final MotionEvent m1 = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 0);
            boolean ePointerOnSoftkeyboard = false;

            try {
                instrumentation.sendPointerSync(m);
                instrumentation.sendPointerSync(m1);
            } catch (SecurityException e) {
                ePointerOnSoftkeyboard = true;
            }
            if (!ePointerOnSoftkeyboard) {
                if (y == height) {
                    if (counter++ < 100) {
                        Thread.yield();
                        continue;
                    }
                } else if (y > 0)
                    iSoftkeyboardHeightNow = iSoftkeyboardHeight - y;
                break;
            }
            y--;
            m.recycle();
            m1.recycle();
        }
        if (iSoftkeyboardHeightNow > 0) iSoftkeyboardHeight = iSoftkeyboardHeightNow;
        else iSoftkeyboardHeight = 0;
        return iSoftkeyboardHeight;
    }
}

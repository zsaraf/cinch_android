package com.seshtutoring.seshapp.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by nadavhollander on 7/16/15.
 */
public class LayoutUtils {
    private Context mContext;

    public LayoutUtils(Context context) {
        this.mContext = context;
    }

    public void crossFade(final View fadeOutView, final View fadeInView) {
        fadeOutView.animate().alpha(0f).setDuration(100);

        fadeInView.animate().alpha(1f).setDuration(100);
    }

    public int dpToPixels(int dp) {
        Resources r = mContext.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public static abstract class NoUnderlineClickableSpan extends ClickableSpan {
        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(false);
        }
    }
}

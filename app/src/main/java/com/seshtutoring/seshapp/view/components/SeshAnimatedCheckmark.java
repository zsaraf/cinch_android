package com.seshtutoring.seshapp.view.components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.ArcUtils;
import com.seshtutoring.seshapp.util.LayoutUtils;

/**
 * Created by nadavhollander on 9/8/15.
 */
public class SeshAnimatedCheckmark extends RelativeLayout implements ValueAnimator.AnimatorUpdateListener {
    private float mCircleRadius;
    private int mWidth;
    private int mHeight;
    private PointF center;
    private float progress;
    private ValueAnimator progressAnimator;
    private boolean springAnimationStarted;
    private Spring scaleSpring;
    private String labelText;
    private AnimationCompleteListener listener;

    public static abstract class AnimationCompleteListener {
        public abstract void onAnimationComplete();
    }

    public SeshAnimatedCheckmark(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater mInflater
                = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.sesh_animated_checkmark_layout, null);

        setWillNotDraw(false);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SeshAnimatedCheckmark,
                0, 0);

        try {
            labelText = a.getString(R.styleable.SeshAnimatedCheckmark_labelText);
        } finally {
            a.recycle();
        }

        this.progressAnimator = ValueAnimator.ofFloat(0f, 1f);
        progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        progressAnimator.setDuration(500);
        progressAnimator.addUpdateListener(this);

        this.scaleSpring = SpringSystem.create().createSpring();
        scaleSpring.setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(13.8d, 4.3d));
        scaleSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float currentValue = (float) spring.getCurrentValue();
                float endValue = (float) spring.getEndValue();
                if (currentValue < spring.getEndValue() / 2) {
                    setScaleX(1.0f + currentValue);
                    setScaleY(1.0f + currentValue);
                } else {
                    setScaleX(1.0f + (endValue - currentValue));
                    setScaleY(1.0f + (endValue - currentValue));
                }
            }

            @Override
            public void onSpringAtRest(Spring spring) {
                if (listener != null) {
                    listener.onAnimationComplete();
                }
            }
        });

        mCircleRadius = getResources().getDimensionPixelSize(R.dimen.sesh_animated_checkmark_circle_radius);
    }

    public void setListener(AnimationCompleteListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;

        center = new PointF();
        center.set(w / 2, h / 2);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        LayoutUtils utils = new LayoutUtils(getContext());

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(getResources().getColor(R.color.seshorange));
        paint.setStyle(Paint.Style.STROKE);

        float strokeWidth = utils.dpToPixels(4);

        paint.setStrokeWidth(strokeWidth);

        float radius = mCircleRadius - strokeWidth / 2;
        float sweep = progress * 360f;

        if (sweep != 0f) {
            ArcUtils.drawArc(canvas, center, radius, 0f, sweep, paint);
        }
    }

    private void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    public void setLabelText(String text) {
        this.labelText = text;
    }

    public void startAnimation() {
        springAnimationStarted = false;
        progressAnimator.start();
    }

    private void startSpringAnimation() {
        if (springAnimationStarted) return;

        LayoutUtils utils = new LayoutUtils(getContext());

        ImageView check = new ImageView(getContext());
        if (Build.VERSION.SDK_INT < 16) {
            check.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.check_green));
        } else if (Build.VERSION.SDK_INT < 21) {
            check.setBackground(getResources()
                    .getDrawable(R.drawable.check_green));
        } else  {
            check.setBackground(getResources()
                    .getDrawable(R.drawable.check_green));
        }

        addView(check);
        ViewGroup.LayoutParams params = check.getLayoutParams();
        params.width = getResources().getDimensionPixelSize(R.dimen.sesh_animated_checkmark_check_width);
        params.height = getResources().getDimensionPixelSize(R.dimen.sesh_animated_checkmark_check_height);
        check.setLayoutParams(params);
        check.setX(center.x - params.width / 2);
        check.setY(center.y - params.height / 2);

        TextView label = new TextView(getContext());
        label.setText(labelText);
        label.setTextColor(getResources().getColor(R.color.seshorange));
        label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        label.setTypeface(utils.getMediumGothamTypeface());
        label.setGravity(Gravity.CENTER_HORIZONTAL);

        addView(label);
        label.setWidth(mWidth);
        label.setX(0);
        label.setY(center.y + mCircleRadius + utils.dpToPixels(10));

        scaleSpring.setCurrentValue(0.0);
        scaleSpring.setEndValue(0.8);

        springAnimationStarted = true;
    }


    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        setProgress((float) animation.getAnimatedValue());
        if (Math.abs((float) animation.getAnimatedValue() - 1f) < 0.001) {
            startSpringAnimation();
        }
    }
}

package com.seshtutoring.seshapp.view.components;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.app.FragmentManager;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Notification;
import com.seshtutoring.seshapp.services.notifications.SeshNotificationManagerService;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.SeshActivity;

/**
 * Created by nadavhollander on 8/25/15.
 */
public class SeshBanner extends DialogFragment {
    public static final String TITLE_KEY = "title";
    public static final String SUBTITLE_KEY = "subtitle";
    public static final String DURATION_KEY = "duration";


    private Activity mActivity;
    private Handler timer;
    private Runnable dismissTimerCallback;
    private Notification correspondingNotification;

    private String titleText;
    private String subtitleText;
    private ImageView bannerImage;
    private Runnable singleTapCallback;
    private GestureDetector gestureDetector;
    private boolean isShowing = false;
    private long duration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SeshActivity currentActivity = (SeshActivity)mActivity;

        if (currentActivity.isFullscreen()) {
            setStyle(STYLE_NORMAL, R.style.SeshFullScreenDialog);
        } else {
            setStyle(STYLE_NORMAL, R.style.SeshNormalDialog);
        }

        Bundle args = getArguments();
        this.titleText = args.getString(TITLE_KEY);
        this.subtitleText = args.getString(SUBTITLE_KEY);
        this.duration = args.getLong(DURATION_KEY);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        View view = inflater.inflate(R.layout.sesh_banner_layout, container);
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView subtitle = (TextView) view.findViewById(R.id.subtitle);

        title.setText(titleText);
        subtitle.setText(subtitleText);

        LayoutUtils utils = new LayoutUtils(getActivity());
        title.setTypeface(utils.getMediumGothamTypeface());
        subtitle.setTypeface(utils.getLightGothamTypeface());

        RelativeLayout bannerImageContainer = (RelativeLayout) view.findViewById(R.id.banner_image);
        bannerImageContainer.addView(bannerImage);

        Window window = getDialog().getWindow();
        window.setGravity(Gravity.TOP | Gravity.LEFT);

        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        window.setAttributes(params);

        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);


        this.gestureDetector
                = new GestureDetector(getActivity(), new BannerGestureListener());
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        this.timer = new Handler();
        this.dismissTimerCallback = new Runnable() {
            @Override
            public void run() {
                if (isShowing) {
                    dismiss();
                }
            }
        };

        timer.postDelayed(dismissTimerCallback, duration);

        return view;
    }

    @Override
    public void onPause() {
        if (isShowing) {
            dismiss();
        }
        super.onPause();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().getAttributes().windowAnimations = R.style.SeshBannerWindowAnimations;
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();

        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    public static SeshBanner createBanner(long timeout, String title, String subtitle, ImageView bannerImage,
                                          Runnable singleTapCallback, Notification correspondingNotification) {
        SeshBanner seshBanner = new SeshBanner();
        Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);
        args.putString(SUBTITLE_KEY, subtitle);
        args.putLong(DURATION_KEY, timeout);

        seshBanner.setArguments(args);
        seshBanner.setSingleTapCallback(singleTapCallback);
        seshBanner.setBannerImage(bannerImage);
        seshBanner.setCorrespondingNotification(correspondingNotification);

        return seshBanner;
    }

    public void setSingleTapCallback(Runnable callback) {
        this.singleTapCallback = callback;
    }

    public void setBannerImage(ImageView bannerImage) {
        this.bannerImage = bannerImage;
    }

    public void setCorrespondingNotification(Notification notification) {
        this.correspondingNotification = notification;
    }

    private class BannerGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            if (velocityY < 0) {
                dismiss();
            }
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent me) {
            if (singleTapCallback != null) {
                dismiss();
                Handler handler = new Handler();
                handler.post(singleTapCallback);
            }
            return true;
        }
    }

    @Override
    public void show(FragmentManager fragmentManager, String tag) {
        isShowing = true;
        super.show(fragmentManager, tag);
    }

    @Override
    public void dismiss() {
        timer.removeCallbacks(dismissTimerCallback);
        isShowing = false;

        Notification.currentNotificationHandled(mActivity, true);

        super.dismiss();
    }
}

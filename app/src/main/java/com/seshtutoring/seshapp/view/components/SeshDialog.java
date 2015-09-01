package com.seshtutoring.seshapp.view.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.PastRequest;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.SeshActivity;

import android.os.Handler;
import android.support.v7.widget.CardView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by lillioetting on 7/20/15.
 */
public class SeshDialog extends DialogFragment {
    OnSelectionListener mCallback;
    public enum SeshDialogType { ONE_BUTTON, TWO_BUTTON, CUSTOM_BUTTONS };

    private static final String TAG = SeshDialog.class.getName();
    private Bitmap backgroundOverlayBitmap = null;

    private String firstChoice;
    private String secondChoice;
    private String title;
    private String message;
    private String type;

    private Button firstButton;
    private Button secondButton;

    private SeshDialogType dialogType = SeshDialogType.TWO_BUTTON;

    private View contentLayout;
    private View dialogView;
    private View dialogTransparentBackground;
    private CardView dialogCard;
    private Activity mActivity;
    private SpringSystem springSystem;
    private SeshActivityIndicator networkingIndicator;
    private LayoutUtils utils;

    private View.OnClickListener firstButtonClickListener;
    private View.OnClickListener secondButtonClickListener;

    private LayoutInflater inflater;

    private float screenHeight;

    // Container Activity must implement this interface
    public interface OnSelectionListener {
        void onDialogSelection(int selected, String type);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SeshActivity currentActivity = (SeshActivity)mActivity;

        if (currentActivity.isFullscreen()) {
            setStyle(STYLE_NORMAL, R.style.SeshFullScreenDialog);
        } else {
            setStyle(STYLE_NORMAL, R.style.SeshNormalDialog);
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnSelectionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }

        this.mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        final SeshNetworking seshNetworking = new SeshNetworking(getActivity());
        this.inflater = inflater;
        this.utils = new LayoutUtils(getActivity());

        Typeface medium = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Medium.otf");
        Typeface book = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Book.otf");
        Typeface light = utils.getLightGothamTypeface();

        dialogView = inflater.inflate(R.layout.sesh_dialog_layout, container);
        dialogTransparentBackground = dialogView.findViewById(R.id.dialog_transparent_background);

        dialogTransparentBackground.animate().alpha(1).setDuration(500).start();

        if (contentLayout == null) {
            contentLayout = inflater.inflate(R.layout.sesh_dialog_content_default, null);
        }

        replaceContentLayout(dialogView, contentLayout);

        networkingIndicator = (SeshActivityIndicator) dialogView.findViewById(R.id.network_indicator);

        TextView titleText = (TextView) dialogView.findViewById(R.id.dialog_title);
        if (titleText != null) {
            titleText.setText(title);
            titleText.setTypeface(book);
        }

        TextView text = (TextView) dialogView.findViewById(R.id.dialog_text);
        if (text != null) {
            text.setText(message);
            text.setTypeface(light);
        }

        this.firstButton = (Button) dialogView.findViewById(R.id.dialog_first_button);
        if (dialogType == SeshDialogType.CUSTOM_BUTTONS) {
            firstButton.setVisibility(View.GONE);
        } else {
            firstButton.setText(firstChoice);
            firstButton.setTypeface(medium);

            if (firstButtonClickListener != null) {
                firstButton.setOnClickListener(firstButtonClickListener);
            } else {
                firstButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Log.d(TAG,"DIALOG HAS BEEN CLICKED");
                        dismiss(1);
                    }
                });
            }
        }

        this.secondButton = (Button) dialogView.findViewById(R.id.dialog_second_button);

        if (dialogType == SeshDialogType.ONE_BUTTON || dialogType == SeshDialogType.CUSTOM_BUTTONS) {
            secondButton.setVisibility(View.GONE);
        } else {
            secondButton.setText(secondChoice);
            secondButton.setTypeface(medium);

            if (secondButtonClickListener != null) {
                secondButton.setOnClickListener(secondButtonClickListener);
            } else {
                secondButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dismiss(2);
                    }
                });
            }
        }

        this.dialogCard = (CardView) dialogView.findViewById(R.id.card_view);

        if (Build.VERSION.SDK_INT < 21) {
            dialogCard.setPreventCornerOverlap(false);
        }

        this.springSystem = SpringSystem.create();
        slideCardUp();

        return dialogView;
    }

    private void slideCardUp() {
        final Spring spring = springSystem.createSpring();

        spring.setSpringConfig(SpringConfig.fromBouncinessAndSpeed(9.0, 6.0));
        spring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float y = (float) spring.getCurrentValue();
                dialogCard.setY(y);
            }
        });

        dialogView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                LayoutUtils utils = new LayoutUtils(mActivity);
                screenHeight = utils.getScreenHeightPx(mActivity);
                double centeredDialogY = dialogCard.getY();

                dialogCard.setY((float) screenHeight);
                dialogCard.setAlpha(1);

                spring.setCurrentValue(screenHeight);
                spring.setEndValue(centeredDialogY);

                if (Build.VERSION.SDK_INT < 16) {
                    dialogView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    dialogView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    private void replaceContentLayout(View dialogView, View contentView) {
        ViewGroup contentContainer = (ViewGroup) dialogView.findViewById(R.id.dialog_content_area);
        contentContainer.removeAllViews();
        contentContainer.addView(contentView);
    }

    public void networkOperationFailed(String title, String message, String buttonTitle, final Runnable dismissCallback) {
        replaceContentLayout(dialogView, inflater.inflate(R.layout.sesh_dialog_content_default, null));

        firstButton.setVisibility(View.GONE);
        secondButton.setVisibility(View.VISIBLE);
        secondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(2);
                dismissCallback.run();
            }
        });

        TextView titleText = (TextView) dialogView.findViewById(R.id.dialog_title);
        TextView messageText = (TextView) dialogView.findViewById(R.id.dialog_text);
        titleText.setText(title);
        messageText.setText(message);

        secondButton.setText(buttonTitle);

        slideCardUp();
    }

    public static void showDialog(FragmentManager manager, String title, String message,
                                  String firstChoice, String secondChoice, String type) {
        SeshDialog dialog = createDialog(title, message, firstChoice, secondChoice, type);
        dialog.show(manager, type);
    }

    public static SeshDialog createDialog(String title, String message,
                             String firstChoice, String secondChoice, String type) {
        SeshDialog dialog = new SeshDialog();

        if (secondChoice != null) {
            dialog.dialogType = SeshDialog.SeshDialogType.TWO_BUTTON;
        } else  {
            dialog.dialogType = SeshDialog.SeshDialogType.ONE_BUTTON;
        }

        dialog.title = title;
        dialog.message = message;
        dialog.firstChoice = firstChoice;
        dialog.secondChoice = secondChoice;
        dialog.type = type;

        return dialog;
    }


    public void showWithDelay(final FragmentManager manager, final String type, long millis) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                show(manager, type);
            }
        }, millis);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();

        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    public void setNetworking(boolean networking) {
        if (networking) {
            dialogCard
                    .animate()
                    .y(screenHeight)
                    .setDuration(150)
                    .setInterpolator(new AccelerateInterpolator());
            networkingIndicator.animate().alpha(1).setDuration(300).start();
        } else {
            dialogTransparentBackground
                    .animate()
                    .alpha(0)
                    .setDuration(150)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            SeshDialog.super.dismiss();
                        }
                    }).start();
            networkingIndicator.animate().alpha(0).setDuration(150).start();
        }
    }

    public void dismiss(final int dialogSelection) {
        dialogCard
                .animate()
                .y(screenHeight)
                .setDuration(150)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        SeshDialog.super.dismiss();
                        mCallback.onDialogSelection(dialogSelection, type);
                    }
                }).start();
        dialogTransparentBackground.animate().alpha(0).setDuration(150).start();
    }

    @Override
    public void dismiss() {
        dismiss(1);
    }

    public void setContentLayout(View contentLayout) {
        this.contentLayout = contentLayout;
    }

    public void setFirstButtonClickListener(View.OnClickListener clickListener) {
        this.firstButtonClickListener = clickListener;
    }

    public void setSecondButtonClickListener(View.OnClickListener clickListener) {
        this.secondButtonClickListener = clickListener;
    }

    public void setFirstChoice(String firstChoice) {
        this.firstChoice = firstChoice;
    }

    public void setSecondChoice(String secondChoice) {
        this.secondChoice = secondChoice;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDialogType(SeshDialogType seshDialogType) {
        this.dialogType = seshDialogType;
    }

    public Button getFirstButton() {
        return firstButton;
    }

    public Button getSecondButton() {
        return secondButton;
    }
}


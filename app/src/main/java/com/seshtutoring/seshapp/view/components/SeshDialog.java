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
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;

import android.os.Handler;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;

import org.json.JSONException;
import org.json.JSONObject;

import fr.tvbarthel.lib.blurdialogfragment.BlurDialogFragment;

/**
 * Created by lillioetting on 7/20/15.
 */
public class SeshDialog extends BlurDialogFragment {
    OnSelectionListener mCallback;
    public enum SeshDialogType { ONE_BUTTON, TWO_BUTTON };

    private static final String TAG = SeshDialog.class.getName();
    private Bitmap backgroundOverlayBitmap = null;

    public String firstChoice;
    public String secondChoice;
    public String title;
    public String message;
    public String type;
    public SeshDialogType dialogType = SeshDialogType.TWO_BUTTON;

    private View contentLayout;
    private View dialogView;
    private CardView dialogCard;
    private Activity mActivity;
    private SpringSystem springSystem;

    private View.OnClickListener firstButtonClickListener;
    private View.OnClickListener secondButtonClickListener;

    private float screenHeight;

    // Container Activity must implement this interface
    public interface OnSelectionListener {
        void onDialogSelection(int selected, String type);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.SeshFullScreenDialog);
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
        Typeface bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Medium.otf");

        dialogView = inflater.inflate(R.layout.sesh_dialog_layout, null);

        if (contentLayout == null) {
            contentLayout = inflater.inflate(R.layout.sesh_dialog_content_default, null);
        }

        ViewGroup contentContainer = (ViewGroup) dialogView.findViewById(R.id.dialog_content_area);
        contentContainer.addView(contentLayout);

        TextView titleText = (TextView) dialogView.findViewById(R.id.dialog_title);
        if (titleText != null) {
            titleText.setText(title);
        }

        TextView text = (TextView) dialogView.findViewById(R.id.dialog_text);
        if (text != null) {
            text.setText(message);
        }

        Button firstButton = (Button) dialogView.findViewById(R.id.dialog_first_button);
        firstButton.setText(firstChoice);
        firstButton.setTypeface(bold);

        if (firstButtonClickListener != null) {
            firstButton.setOnClickListener(firstButtonClickListener);
        } else {
            firstButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mCallback.onDialogSelection(1, type);
                    dismiss();
                }
            });
        }

        Button secondButton = (Button) dialogView.findViewById(R.id.dialog_second_button);

        if (dialogType == SeshDialogType.ONE_BUTTON) {
            secondButton.setVisibility(View.GONE);
        } else {
            secondButton.setText(secondChoice);
            secondButton.setTypeface(bold);

            if (secondButtonClickListener != null) {
                secondButton.setOnClickListener(secondButtonClickListener);
            } else {
                secondButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        mCallback.onDialogSelection(2, type);
                        dismiss();
                    }
                });
            }
        }

        this.dialogCard = (CardView) dialogView.findViewById(R.id.card_view);

        if (Build.VERSION.SDK_INT < 21) {
            dialogCard.setPreventCornerOverlap(false);
        }

        this.springSystem = SpringSystem.create();
        final Spring spring = springSystem.createSpring();

        spring.setSpringConfig(SpringConfig.fromBouncinessAndSpeed(9.0, 6.0));
        spring.addListener(new SimpleSpringListener(){
            @Override
            public void onSpringUpdate(Spring spring) {
                float y = (float) spring.getCurrentValue();
                dialogCard.setY(y);
            }
        });

        final View content = mActivity.findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                LayoutUtils utils = new LayoutUtils(mActivity);
                screenHeight = utils.getScreenHeightPx(mActivity);
                double centeredDialogY = dialogCard.getY();

                dialogCard.setY((float)screenHeight);
                dialogCard.setAlpha(1);

                spring.setCurrentValue(screenHeight);
                spring.setEndValue(centeredDialogY);

                if (Build.VERSION.SDK_INT < 16) {
                    content.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    content.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

        return dialogView;
    }

    public static void showDialog(FragmentManager manager, String title, String message,
                                  String firstChoice, String secondChoice,
                                  Bitmap customBackground, String type) {
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
        dialog.setCustomBackgroundBitmap(customBackground);
        dialog.type = type;

        dialog.show(manager, type);
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

    @Override
    protected float getDownScaleFactor() {
        // Allow to customize the down scale factor.
        return 5;
    }

    @Override
    protected int getBlurRadius() {
        // Allow to customize the blur radius factor.
        return 7;
    }

    @Override
    protected boolean isActionBarBlurred() {
        // Enable or disable the blur effect on the action bar.
        // Disabled by default.
        return true;
    }

    @Override
    protected Bitmap getCustomBackgroundBitmap() {
        return backgroundOverlayBitmap;
    }

    @Override
    protected View getOverlayView() {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        return inflater.inflate(R.layout.opaque_white_overlay_view, null);
    }

    @Override
    public void dismiss() {
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
            }
        }).start();
    }

    /**
     * Overrides blur dialog's default behavior - instead of blurring the background
     * before showing dialog, Bitmap that is passed in will be blurred and placed in the background
     * @param overlay
     */
    public void setCustomBackgroundBitmap(Bitmap overlay) {
        backgroundOverlayBitmap = overlay;
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
}


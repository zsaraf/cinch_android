package com.seshtutoring.seshapp.view.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.AddTutorClassesActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.ViewAvailableJobsFragment;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.ViewClassesView;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import java.lang.reflect.Field;

import me.brendanweinstein.util.ToastUtils;

/**
 * Created by nadavhollander on 7/14/15.
 */
public class TeachViewFragment extends Fragment implements ViewClassesView.ViewClassesViewListener {

    private static View view;
    private MainContainerActivity activity;
    private boolean canSeeClasses;
    private TextView classesButtonText;
    private View tutorViewFrame;
    private View viewClassesButton;
    private View tintView;
    private ViewClassesView viewClassesView;
    private BroadcastReceiver broadcastReceiver;

    private static final float TINT_VIEW_ALPHA = .8f;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }

        try {
            view = layoutInflater.inflate(R.layout.teach_view_fragment, container, false);
        } catch (InflateException e) {
            return view;
        }

        LayoutUtils layUtils = new LayoutUtils(getActivity());
        view.setPadding(0, (int)getResources().getDimensionPixelSize(R.dimen.home_view_tab_buttons_height) - 1, 0, 0);

        viewClassesButton = view.findViewById(R.id.view_classes_button);
        classesButtonText = (TextView) view.findViewById(R.id.classes_button_text);
        tutorViewFrame = view.findViewById(R.id.tutor_view_frame);

        viewClassesView = (ViewClassesView) view.findViewById(R.id.view_classes_view);
        viewClassesView.listener = this;
        tintView = (View) view.findViewById(R.id.tint_view);

        activity = (MainContainerActivity) getActivity();

        viewClassesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleViewClasses();
            }
        });

        canSeeClasses = false;
        classesButtonText.setText(R.string.view_classes_off_text);
        setCurrentView();

        broadcastReceiver = actionBroadcastReceiver;

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Listen for new messages
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainContainerActivity.REFRESH_USER_INFO);
        this.getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.getActivity().unregisterReceiver(broadcastReceiver);
    }


    private BroadcastReceiver actionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshTutorClasses();
        }
    };

    public void refreshTutorClasses() {
        User currentUser = User.currentUser(getActivity());
        viewClassesView.refreshClassesViewWithUser(currentUser);
    }

    private void toggleViewClasses() {

        canSeeClasses = !canSeeClasses;

        if (canSeeClasses) {
            classesButtonText.setText(R.string.view_classes_on_text);
        } else {
            classesButtonText.setText(R.string.view_classes_off_text);
        }

            /* Animate frame */
        ValueAnimator frameAnimator = null;
        if (canSeeClasses) {
            frameAnimator = ValueAnimator.ofInt(viewClassesView.getMeasuredHeight(), getView().getMeasuredHeight() - viewClassesButton.getMeasuredHeight());
        } else {
            frameAnimator = ValueAnimator.ofInt(viewClassesView.getMeasuredHeight(), 0);
        }
        frameAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = viewClassesView.getLayoutParams();
                layoutParams.height = val;
                viewClassesView.setLayoutParams(layoutParams);
            }
        });
        frameAnimator.setDuration(400);
        frameAnimator.start();

            /* Animate alpha of tint view */
        tintView
                .animate()
                .alpha(canSeeClasses ? TINT_VIEW_ALPHA : 0)
                .setDuration(400)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (canSeeClasses) {
                            tintView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!canSeeClasses) {
                            tintView.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();

        if (canSeeClasses) {
            tintView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleViewClasses();
                }
            });
        } else {
            tintView.setOnClickListener(null);
        }
    }

    private void setCurrentView() {
            getActivity().getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.tutor_view_frame, new ViewAvailableJobsFragment())
                    .commit();

    }

    @Override
    public void viewClassesViewDidTapAddClasses() {
        Intent intent = new Intent(getActivity(), AddTutorClassesActivity.class);
        startActivityForResult(intent, AddTutorClassesActivity.ADD_TUTOR_CLASSES_CREATE);
        getActivity().overridePendingTransition(R.anim.fade_in, 0);
    }

}

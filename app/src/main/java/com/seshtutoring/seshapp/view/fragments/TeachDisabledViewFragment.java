package com.seshtutoring.seshapp.view.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshAnimatedCheckmark;
import com.seshtutoring.seshapp.view.components.SeshButton;
import com.seshtutoring.seshapp.view.components.SeshDialog;

import org.json.JSONException;
import org.json.JSONObject;

public class TeachDisabledViewFragment extends Fragment {

    private RelativeLayout requestFlowOverlay;
    private SeshActivityIndicator activityIndicator;
    private SeshAnimatedCheckmark animatedCheckmark;
    private static View view;
    private SeshButton becomeTutorButton;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TeachDisabledViewFragment.
     */
    public static TeachDisabledViewFragment newInstance() {
        TeachDisabledViewFragment fragment = new TeachDisabledViewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public TeachDisabledViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }

        try {
            view = inflater.inflate(R.layout.fragment_teach_disabled_view, container, false);
        } catch (InflateException e) {
            return view;
        }

        LayoutUtils layUtils = new LayoutUtils(getActivity());
        view.setPadding(0, layUtils.getActionBarHeightPx() + (int)getResources().getDimensionPixelSize(R.dimen.home_view_tab_buttons_height) - 1, 0, 0);

        this.requestFlowOverlay = (RelativeLayout) view.findViewById(R.id.request_flow_overlay);
        this.activityIndicator = (SeshActivityIndicator) view.findViewById(R.id.request_activity_indicator);
        this.animatedCheckmark = (SeshAnimatedCheckmark) view.findViewById(R.id.animated_check_mark);

        this.becomeTutorButton = (SeshButton) view.findViewById(R.id.become_tutor_button);
        this.becomeTutorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestFlowOverlay.animate().alpha(1).setListener(null).setDuration(300).start();

                SeshNetworking seshNetworking = new SeshNetworking(TeachDisabledViewFragment.this.getActivity());
                seshNetworking.sendBecomeATutorEmail(new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            if (jsonObject.get("status").equals("SUCCESS")) {
                                hideAnimationWithSuccess(true, "");
                            } else if (jsonObject.get("status").equals("FAILURE")) {
                                String message = jsonObject.get("message").toString();
                                hideAnimationWithSuccess(false, message);
                            }
                        } catch (JSONException e) {
                            hideAnimationWithSuccess(false, "Error sending email. Try again later!");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        hideAnimationWithSuccess(false, "Check your internet connection and try again!");
                    }
                });
            }
        });



        return view;
    }

    private void hideAnimationWithSuccess(final boolean success, final String message) {
        if (!success) {
            requestFlowOverlay
                    .animate()
                    .setListener(null)
                    .alpha(0)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            showErrorDialog("Whoops!", message);
                        }
                    });
        } else {
            activityIndicator
                    .animate()
                    .alpha(0)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            animatedCheckmark.setListener(new SeshAnimatedCheckmark.AnimationCompleteListener() {
                                @Override
                                public void onAnimationComplete() {

                                }
                            });
                            animatedCheckmark.startAnimation();

                        }
                    });
        }
    }

    private void showErrorDialog(String title, String message) {
        SeshDialog.showDialog(getActivity().getFragmentManager(), title, message,
                "OKAY", null, "view_request_network_error");
    }

}

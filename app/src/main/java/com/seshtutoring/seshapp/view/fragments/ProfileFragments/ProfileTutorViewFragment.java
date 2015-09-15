package com.seshtutoring.seshapp.view.fragments.ProfileFragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.components.SeshActivityIndicator;
import com.seshtutoring.seshapp.view.components.SeshAnimatedCheckmark;
import com.seshtutoring.seshapp.view.components.SeshDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

/**
 * Created by lillioetting on 8/28/15.
 */
public class ProfileTutorViewFragment extends Fragment implements MainContainerActivity.FragmentOptionsReceiver {
    private Map<String, Object> options;
    private User user;
    private SeshNetworking seshNetworking;
    private MainContainerActivity mainContainerActivity;
    private View homeView;
    private View listViewFrame;
    private TextView studentHistoryTab;
    private TextView classesTab;
    private int selectedTab;
    private TextView creditsView;
    private RelativeLayout requestFlowOverlay;
    private SeshActivityIndicator activityIndicator;
    private SeshAnimatedCheckmark animatedCheckmark;


    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        this.homeView = layoutInflater.inflate(R.layout.profile_tutor_fragment, container, false);

        mainContainerActivity = (MainContainerActivity) getActivity();
        this.user = User.currentUser(mainContainerActivity.getApplicationContext());
        this.seshNetworking = new SeshNetworking(mainContainerActivity);

        TextView hoursTutoredView = (TextView) this.homeView.findViewById(R.id.hours_taught_number);
        creditsView = (TextView) this.homeView.findViewById(R.id.tutor_credits_number);

        this.requestFlowOverlay = (RelativeLayout) this.homeView.findViewById(R.id.request_flow_overlay);
        this.activityIndicator = (SeshActivityIndicator) this.homeView.findViewById(R.id.request_activity_indicator);
        this.animatedCheckmark = (SeshAnimatedCheckmark) this.homeView.findViewById(R.id.animated_check_mark);

        creditsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SeshDialog seshDialog = new SeshDialog();
                seshDialog.setDialogType(SeshDialog.SeshDialogType.TWO_BUTTON);
                seshDialog.setTitle("Cash Out?");
                seshDialog.setMessage("Would you like to cash out your tutor credits? The transfer will take 1-2 days to complete.");
                seshDialog.setDialogType(SeshDialog.SeshDialogType.ONE_BUTTON);
                seshDialog.setFirstChoice("YES");
                seshDialog.setSecondChoice("NO");
                seshDialog.setFirstButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        seshDialog.dismiss();
                        requestFlowOverlay.animate().alpha(1).setListener(null).setDuration(300).start();
                        seshNetworking.cashout(
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject responseJson) {
                                        onCashoutResponse(responseJson);
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError volleyError) {
                                        onCashoutFailure(volleyError.getMessage());
                                    }
                                });
                    }
                });
                seshDialog.setSecondButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        seshDialog.dismiss();
                    }
                });
                seshDialog.setType("CASHOUT");
                seshDialog.show(mainContainerActivity.getFragmentManager(), "CASHOUT");
            }
        });

        DecimalFormat df = new DecimalFormat("0.00");
        hoursTutoredView.setText(df.format(this.user.tutor.hoursTutored));
        creditsView.setText("$" + df.format(this.user.tutor.cashAvailable));

        this.listViewFrame = this.homeView.findViewById(R.id.profile_tutor_view_frame);
        this.selectedTab = 0;

        this.studentHistoryTab = (TextView) this.homeView.findViewById(R.id.client_history_tab);
        this.classesTab = (TextView) this.homeView.findViewById(R.id.classes_tab);

        setCurrentListView();

        this.studentHistoryTab.setOnClickListener(firstTabPress);
        this.classesTab.setOnClickListener(secondTabPress);

        return this.homeView;

    }

    public void refreshTutorCredits() {
        this.user = User.currentUser(mainContainerActivity.getApplicationContext());
        DecimalFormat df = new DecimalFormat("0.00");
        creditsView.setText("$" + df.format(this.user.tutor.cashAvailable));
    }



    private View.OnClickListener firstTabPress = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (selectedTab != 0) {
                selectedTab = 0;
                setCurrentListView();
            }
        }
    };

    private View.OnClickListener secondTabPress = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (selectedTab != 1) {
                selectedTab = 1;
                setCurrentListView();
            }
        }
    };

    private void setCurrentListView() {

        if (selectedTab == 0) {
            this.studentHistoryTab.setTextColor(getResources().getColor(R.color.seshorange));
            this.classesTab.setTextColor(getResources().getColor(R.color.light_gray));
            getActivity().getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.profile_tutor_view_frame, new ClientHistoryListFragment(), "ClientHistoryListFragment")
                    .commit();

        }else {
            this.studentHistoryTab.setTextColor(getResources().getColor(R.color.light_gray));
            this.classesTab.setTextColor(getResources().getColor(R.color.seshorange));
            getActivity().getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.profile_tutor_view_frame, new ClassesListFragment(), "ClassesListFragment")
                    .commit();

        }

    }

    private void onCashoutResponse(JSONObject responseJson) {
        try {
            if (responseJson.get("status").equals("SUCCESS")) {
                User currentUser = User.currentUser(mainContainerActivity);
                currentUser.tutor.cashAvailable = 0.0f;
                currentUser.tutor.save();
                currentUser.save();
                mainContainerActivity.sendBroadcast(new Intent(mainContainerActivity.REFRESH_PROFILE));

                hideAnimationWithSuccess(true, "Successfully cashed out!");

            } else if (responseJson.get("status").equals("FAILURE")) {
                String message = responseJson.get("message").toString();
                hideAnimationWithSuccess(false, message);
            }
        } catch (JSONException e) {
            hideAnimationWithSuccess(false, "There was an error cashing out your account, please try again in a few days.");
        }
    }

    private void onCashoutFailure(String errorMessage) {
        hideAnimationWithSuccess(false, "There was an error cashing out your account, please try again in a few days.");
    }

    private void showErrorDialog(String title, String message) {
        SeshDialog.showDialog(mainContainerActivity.getFragmentManager(), title, message,
                "OKAY", null, "view_request_network_error");
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
                                    //setResult(PASSWORD_CHANGED_SUCCESSFULLY_RESPONSE_CODE, null);
                                    requestFlowOverlay.animate().alpha(0).setListener(null).setDuration(300).start();
                                }
                            });
                            animatedCheckmark.startAnimation();
                        }
                    });
        }
    }

    @Override
    public void updateFragmentOptions(Map<String, Object> options) {
        this.options = options;
    }

    @Override
    public void clearFragmentOptions() {
        this.options = null;
    }

}

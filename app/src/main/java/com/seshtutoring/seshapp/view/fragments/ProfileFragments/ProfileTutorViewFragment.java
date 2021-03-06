package com.seshtutoring.seshapp.view.fragments.ProfileFragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
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
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.ProfileFragment;

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
    private TextView hoursTutoredView;
    private ClassesListFragment classesListFragment;
    private ClientHistoryListFragment clientHistoryListFragment;
    private BroadcastReceiver broadcastReceiver;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        this.homeView = layoutInflater.inflate(R.layout.profile_tutor_fragment, container, false);

        mainContainerActivity = (MainContainerActivity) getActivity();
        this.user = User.currentUser(mainContainerActivity.getApplicationContext());
        this.seshNetworking = new SeshNetworking(mainContainerActivity);

        hoursTutoredView = (TextView) this.homeView.findViewById(R.id.hours_taught_number);
        creditsView = (TextView) this.homeView.findViewById(R.id.tutor_credits_number);

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
                        ((ProfileFragment) mainContainerActivity.getContainerStateManager().getMainContainerState().fragment).requestFlowOverlay.animate().alpha(1).setListener(null).setDuration(300).start();
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

        clientHistoryListFragment = new ClientHistoryListFragment();
        classesListFragment = new ClassesListFragment();

        setCurrentListView();

        this.studentHistoryTab.setOnClickListener(firstTabPress);
        this.classesTab.setOnClickListener(secondTabPress);

        broadcastReceiver = actionBroadcastReceiver;

        return this.homeView;

    }

    public void refreshTutorInfoWithUser(User user) {
        this.user = user;
        DecimalFormat df = new DecimalFormat("0.00");
        creditsView.setText("$" + df.format(this.user.tutor.cashAvailable));
        hoursTutoredView.setText(df.format(this.user.tutor.hoursTutored));
        classesListFragment.refreshListWithUser(user);
        clientHistoryListFragment.refreshListWithUser(user);
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

    @Override
    public void onResume() {
        super.onResume();

        // Listen for new messages
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainContainerActivity.REFRESH_USER_INFO);
        intentFilter.addAction(MainContainerActivity.REFRESH_TUTOR_CREDITS);
        this.mainContainerActivity.registerReceiver(broadcastReceiver, intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        this.mainContainerActivity.unregisterReceiver(broadcastReceiver);
    }


    private BroadcastReceiver actionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            User currentUser = User.currentUser(context);
            refreshTutorInfoWithUser(currentUser);
        }
    };

    private void setCurrentListView() {

        if (selectedTab == 0) {
            this.studentHistoryTab.setTextColor(getResources().getColor(R.color.seshorange));
            this.classesTab.setTextColor(getResources().getColor(R.color.light_gray));
            getActivity().getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.profile_tutor_view_frame, clientHistoryListFragment, "ClientHistoryListFragment")
                    .commit();

        }else {
            this.studentHistoryTab.setTextColor(getResources().getColor(R.color.light_gray));
            this.classesTab.setTextColor(getResources().getColor(R.color.seshorange));
            getActivity().getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.profile_tutor_view_frame, classesListFragment, "ClassesListFragment")
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
                refreshTutorInfoWithUser(currentUser);

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
            ((ProfileFragment)mainContainerActivity.getContainerStateManager().getMainContainerState().fragment).requestFlowOverlay
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
            ((ProfileFragment)mainContainerActivity.getContainerStateManager().getMainContainerState().fragment).activityIndicator
                    .animate()
                    .alpha(0)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            ((ProfileFragment)mainContainerActivity.getContainerStateManager().getMainContainerState().fragment).animatedCheckmark.setListener(new SeshAnimatedCheckmark.AnimationCompleteListener() {
                                @Override
                                public void onAnimationComplete() {
                                    //setResult(PASSWORD_CHANGED_SUCCESSFULLY_RESPONSE_CODE, null);
                                    ((ProfileFragment)mainContainerActivity.getContainerStateManager().getMainContainerState().fragment).requestFlowOverlay.animate().alpha(0).setListener(null).setDuration(300).start();
                                }
                            });
                            ((ProfileFragment)mainContainerActivity.getContainerStateManager().getMainContainerState().fragment).animatedCheckmark.startAnimation();
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

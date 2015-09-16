package com.seshtutoring.seshapp.view.fragments.ProfileFragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

/**
 * Created by lillioetting on 8/28/15.
 */
public class ProfileStudentViewFragment extends Fragment implements MainContainerActivity.FragmentOptionsReceiver {
    private Map<String, Object> options;
    private User user;
    private View homeView;
    private View listViewFrame;
    private SeshNetworking seshNetworking;
    private MainContainerActivity mainContainerActivity;
    private TextView hoursLearnedView;
    private TextView creditsView;
    private TutorHistoryListFragment tutorHistoryListFragment;
    private BroadcastReceiver broadcastReceiver;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        this.homeView = layoutInflater.inflate(R.layout.profile_student_fragment, container, false);

        mainContainerActivity = (MainContainerActivity) getActivity();
        this.user = User.currentUser(mainContainerActivity.getApplicationContext());
        this.seshNetworking = new SeshNetworking(mainContainerActivity);

        hoursLearnedView = (TextView) this.homeView.findViewById(R.id.hours_learned_number);
        creditsView = (TextView) this.homeView.findViewById(R.id.student_credits_number);

        DecimalFormat df = new DecimalFormat("0.00");
        hoursLearnedView.setText(df.format(this.user.student.hoursLearned));
        creditsView.setText("$" + df.format(this.user.student.credits));

        tutorHistoryListFragment = new TutorHistoryListFragment();

        getActivity().getFragmentManager()
                .beginTransaction()
                .replace(R.id.profile_student_view_frame, tutorHistoryListFragment, "TutorHistoryListFragment")
                .commit();

        broadcastReceiver = actionBroadcastReceiver;

        return this.homeView;

    }

    @Override
    public void onResume() {
        super.onResume();

        // Listen for new messages
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainContainerActivity.REFRESH_USER_INFO);
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
            refreshStudentInfoWithUser(currentUser);
        }
    };

    public void refreshStudentInfoWithUser(User user) {
        this.user = user;
        DecimalFormat df = new DecimalFormat("0.00");
        hoursLearnedView.setText(df.format(this.user.student.hoursLearned));
        creditsView.setText("$" + df.format(this.user.student.credits));
        tutorHistoryListFragment.refreshListWithUser(user);
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

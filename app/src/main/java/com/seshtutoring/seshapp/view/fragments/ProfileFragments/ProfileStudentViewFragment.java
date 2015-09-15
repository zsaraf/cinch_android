package com.seshtutoring.seshapp.view.fragments.ProfileFragments;

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

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        this.homeView = layoutInflater.inflate(R.layout.profile_student_fragment, container, false);

        mainContainerActivity = (MainContainerActivity) getActivity();
        this.user = User.currentUser(mainContainerActivity.getApplicationContext());
        this.seshNetworking = new SeshNetworking(mainContainerActivity);

        TextView hoursLearnedView = (TextView) this.homeView.findViewById(R.id.hours_learned_number);
        TextView creditsView = (TextView) this.homeView.findViewById(R.id.student_credits_number);

        DecimalFormat df = new DecimalFormat("0.00");
        hoursLearnedView.setText(df.format(this.user.student.hoursLearned));
        creditsView.setText("$" + df.format(this.user.student.credits));

        getActivity().getFragmentManager()
                .beginTransaction()
                .replace(R.id.profile_student_view_frame, new TutorHistoryListFragment(), "TutorHistoryListFragment")
                .commit();

        return this.homeView;

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

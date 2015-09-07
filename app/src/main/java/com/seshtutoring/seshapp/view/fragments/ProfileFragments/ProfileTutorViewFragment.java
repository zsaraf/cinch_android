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


    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        this.homeView = layoutInflater.inflate(R.layout.profile_tutor_fragment, container, false);

        mainContainerActivity = (MainContainerActivity) getActivity();
        this.user = User.currentUser(mainContainerActivity.getApplicationContext());
        this.seshNetworking = new SeshNetworking(mainContainerActivity);

        TextView hoursTutoredView = (TextView) this.homeView.findViewById(R.id.hours_taught_number);
        TextView creditsView = (TextView) this.homeView.findViewById(R.id.tutor_credits_number);

        DecimalFormat df = new DecimalFormat("0.00");
        hoursTutoredView.setText(df.format(this.user.tutor.hoursTutored));

        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        creditsView.setText(formatter.format(this.user.tutor.cashAvailable));

        this.listViewFrame = this.homeView.findViewById(R.id.profile_tutor_view_frame);
        this.selectedTab = 0;

        this.studentHistoryTab = (TextView) this.homeView.findViewById(R.id.client_history_tab);
        this.classesTab = (TextView) this.homeView.findViewById(R.id.classes_tab);

        setCurrentListView();

        this.studentHistoryTab.setOnClickListener(firstTabPress);
        this.classesTab.setOnClickListener(secondTabPress);

        return this.homeView;

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
//            getActivity().getFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.profile_tutor_view_frame, new ViewClassesFragment(), "ClassesListFragment")
//                    .commit();

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

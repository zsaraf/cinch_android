package com.seshtutoring.seshapp.view.fragments.ProfileFragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.ViewAvailableJobsFragment;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.ViewClassesFragment;

import java.util.Map;

/**
 * Created by lillioetting on 8/28/15.
 */
public class ProfileStudentViewFragment extends Fragment implements MainContainerActivity.FragmentOptionsReceiver {
    private Map<String, Object> options;
    private User user;
    private MainContainerActivity mainContainerActivity;
    private View homeView;
    private View listViewFrame;
    private Button tutorHistoryTab;
    private Button favoritesTab;
    private int selectedTab;


    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        this.homeView = layoutInflater.inflate(R.layout.profile_student_fragment, container, false);

        this.listViewFrame = this.homeView.findViewById(R.id.profile_student_view_frame);
        this.selectedTab = 0;

        this.tutorHistoryTab = (Button) this.homeView.findViewById(R.id.tutor_history_tab);
        this.favoritesTab = (Button) this.homeView.findViewById(R.id.favorites_tab);

        this.tutorHistoryTab.setOnClickListener(firstTabPress);
        this.favoritesTab.setOnClickListener(secondTabPress);

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
            getActivity().getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.profile_student_view_frame, new TutorHistoryListFragment(), "TutorHistoryListFragment")
                    .commit();

        }else {
            getActivity().getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.profile_student_view_frame, new ViewClassesFragment(), "FavoritesListFragment")
                    .commit();

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

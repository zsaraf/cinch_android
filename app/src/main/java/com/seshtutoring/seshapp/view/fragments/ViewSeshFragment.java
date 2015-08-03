package com.seshtutoring.seshapp.view.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.view.components.RequestFlowViewPager;
import com.seshtutoring.seshapp.view.fragments.LearnRequestFragments.LearnRequestAssignmentFragment;
import com.seshtutoring.seshapp.view.fragments.ViewSeshFragments.ViewSeshSeshDescriptionFragment;
import com.seshtutoring.seshapp.view.fragments.ViewSeshFragments.ViewSeshUserDescriptionFragment;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ViewSeshFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewSeshFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SESH_ID = "sesh";

    private int seshId;
    private Sesh sesh;

    private ViewPager viewPager;
    private FragmentActivity myContext;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param seshId Sesh to view.
     * @return A new instance of fragment ViewSeshFragment.
     */
    public static ViewSeshFragment newInstance(int seshId) {
        ViewSeshFragment fragment = new ViewSeshFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SESH_ID, seshId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            seshId = getArguments().getInt(ARG_SESH_ID);
//            List<Sesh> seshesFound = Sesh.find(Sesh.class, "sesh_id = ?", Integer.toString(new Integer(seshId)));
//            sesh = seshesFound.get(0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_view_sesh, container, false);

        viewPager = (ViewPager) v.findViewById(R.id.view_sesh_view_pager);

        viewPager.setAdapter(new ViewSeshPagerAdapter(myContext.getSupportFragmentManager(), true));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // do nothing
            }
        });

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }

    private class ViewSeshPagerAdapter extends FragmentStatePagerAdapter {
        private static final int NUM_FRAGMENTS = 2;
        private Boolean isStudent;
        private Fragment viewSeshFragments[];

        public ViewSeshPagerAdapter(FragmentManager fm, Boolean isStudent) {
            super(fm);
            this.isStudent = isStudent;
            viewSeshFragments = new Fragment[2];
            if (this.isStudent) {
                viewSeshFragments[0] = ViewSeshUserDescriptionFragment.newInstance(0);
                viewSeshFragments[1] = ViewSeshSeshDescriptionFragment.newInstance(0);
            } else {
                viewSeshFragments[0] = ViewSeshSeshDescriptionFragment.newInstance(0);
                viewSeshFragments[1] = ViewSeshUserDescriptionFragment.newInstance(0);
            }
        }

        @Override
        public Fragment getItem(int position) {
            return viewSeshFragments[position];
        }

        @Override
        public int getCount() {
            return NUM_FRAGMENTS;
        }
    }

}

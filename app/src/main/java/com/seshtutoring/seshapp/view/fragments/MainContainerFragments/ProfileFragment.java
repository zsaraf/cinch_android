package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.LayoutUtils;

/**
 * Created by nadavhollander on 7/14/15.
 */
public class ProfileFragment extends Fragment {
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View v = layoutInflater.inflate(R.layout.profile_fragment, null);

        // Add padding to account for action bar
        LayoutUtils utils = new LayoutUtils(getActivity());
        RelativeLayout profileLayout = (RelativeLayout) v.findViewById(R.id.profile_layout);
        profileLayout.setPadding(0, utils.getActionBarHeightPx(), 0, 0);

        return v;
    }
}

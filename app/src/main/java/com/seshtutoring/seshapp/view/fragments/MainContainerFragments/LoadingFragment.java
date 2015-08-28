package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.MainContainerActivity;

import java.util.Map;

/**
 * Created by nadavhollander on 8/27/15.
 */
public class LoadingFragment extends Fragment implements MainContainerActivity.FragmentOptionsReceiver{
    private Map<String, Object> options;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View v = layoutInflater.inflate(R.layout.loading_fragment, container);

        // Add padding to account for action bar
        LayoutUtils utils = new LayoutUtils(getActivity());
        RelativeLayout loadingContainer = (RelativeLayout) v.findViewById(R.id.loading_container);
        loadingContainer.setPadding(0, utils.getActionBarHeightPx(), 0, 0);

        return v;
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

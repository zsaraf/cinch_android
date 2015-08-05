package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.view.MainContainerActivity.FragmentOptionsReceiver;

import java.util.Map;

/**
 * Created by nadavhollander on 8/4/15.
 */
public class DummyRequestSeshFragment extends Fragment implements FragmentOptionsReceiver {
    private TextView dummyText;

    private Map<String, Object> options;

    public static final String SESH_DUMMY_KEY = "sesh_dummy";
    public static final String REQUEST_DUMMY_KEY = "sesh_dummy";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dummy_request_sesh_fragment, null);
        dummyText = (TextView) view.findViewById(R.id.dummy_text);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {

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



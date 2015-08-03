package com.seshtutoring.seshapp.view.fragments.ViewSeshFragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Sesh;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewSeshUserDescriptionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewSeshUserDescriptionFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SESH_ID = "seshId";

    private int seshId;
    private Sesh sesh;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ViewSeshUserDescriptionFragment.
     */
    public static ViewSeshUserDescriptionFragment newInstance(int seshId) {
        ViewSeshUserDescriptionFragment fragment = new ViewSeshUserDescriptionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SESH_ID, seshId);
        fragment.setArguments(args);
        return fragment;
    }

    public ViewSeshUserDescriptionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            seshId = getArguments().getInt(ARG_SESH_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_sesh_user_description, container, false);
    }

}

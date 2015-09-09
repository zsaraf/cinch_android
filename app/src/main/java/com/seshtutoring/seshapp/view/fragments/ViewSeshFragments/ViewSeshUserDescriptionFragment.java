package com.seshtutoring.seshapp.view.fragments.ViewSeshFragments;

import android.app.Activity;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.view.components.SeshInformationLabel;

import org.w3c.dom.Text;

import java.util.List;

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

    private SeshInformationLabel schoolLabel;
    private SeshInformationLabel majorLabel;
    private TextView bioTextView;

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
            List<Sesh> seshesFound = Sesh.find(Sesh.class, "sesh_id = ?", Integer.toString(new Integer(seshId)));
            sesh = seshesFound.get(0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_sesh_user_description, container, false);

        // Update user bio
        bioTextView = (TextView)view.findViewById(R.id.bio_text_view);
        Typeface medium = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Light.otf");
        bioTextView.setTypeface(medium);

        // Update user school
        schoolLabel = (SeshInformationLabel)view.findViewById(R.id.school_label);

        // Update user school
        majorLabel = (SeshInformationLabel)view.findViewById(R.id.major_label);

        refresh();

        return view;
    }

    public void refresh() {
        bioTextView.setText(sesh.userDescription);
        schoolLabel.setText(User.currentUser(getActivity()).school.schoolName);
        majorLabel.setText(sesh.userMajor);
    }

}

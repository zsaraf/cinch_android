package com.seshtutoring.seshapp.view.fragments.ViewSeshFragments;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.seshtutoring.seshapp.BuildConfig;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.view.components.SeshInformationLabel;

import java.text.DecimalFormat;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewSeshSeshDescriptionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewSeshSeshDescriptionFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SESH_ID = "seshId";

    private int seshId;
    private Sesh sesh;

    private TextView nameTextView;
    private SeshInformationLabel classLabel;
    private SeshInformationLabel subjectLabel;
    private SeshInformationLabel timeLabel;
    private SeshInformationLabel miscLabel;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ViewSeshSeshDescriptionFragment.
     */
    public static ViewSeshSeshDescriptionFragment newInstance(int seshId) {
        ViewSeshSeshDescriptionFragment fragment = new ViewSeshSeshDescriptionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SESH_ID, seshId);
        fragment.setArguments(args);
        return fragment;
    }

    public ViewSeshSeshDescriptionFragment() {
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_sesh_sesh_description, container, false);

        nameTextView = (TextView) view.findViewById(R.id.name_text_view);

        classLabel = (SeshInformationLabel)view.findViewById(R.id.class_label);

        subjectLabel = (SeshInformationLabel)view.findViewById(R.id.subject_label);

        timeLabel = (SeshInformationLabel)view.findViewById(R.id.time_label);

        // Handle display of either set time or location notes
        miscLabel = (SeshInformationLabel)view.findViewById(R.id.misc_label);

        refresh();

        return view;
    }

    private void updateLabelForStudent(SeshInformationLabel estimatedTimeLabel) {
        // Set estimated time text
        Drawable drawable = null;
        if (Build.VERSION.SDK_INT < 21) {
            drawable = getActivity().getResources().getDrawable(R.drawable.calendar_unfilled);
        } else {
            drawable = getActivity().getResources().getDrawable(R.drawable.calendar_unfilled, null);
        }
        estimatedTimeLabel.setIcon(drawable);
        if (sesh.isInstant) {
            estimatedTimeLabel.setText("NOW");
        } else if (sesh.seshEstTime != 0) {
            estimatedTimeLabel.setText(sesh.getTimeAbbrvString());
        } else {
            estimatedTimeLabel.setText("waiting for tutor to set time...");
        }
    }

    private void updateLabelForTutor(SeshInformationLabel locationNotesLabel) {
        // Set location notes text
        Drawable drawable = null;
        if (Build.VERSION.SDK_INT < 21) {
            drawable = getActivity().getResources().getDrawable(R.drawable.pointer);
        } else {
            drawable = getActivity().getResources().getDrawable(R.drawable.pointer, null);
        }
        locationNotesLabel.setIcon(drawable);

        if (sesh.locationNotes.length() > 0) {
            locationNotesLabel.setText(sesh.locationNotes);
        } else {
            locationNotesLabel.setText("waiting for location notes...");
        }
    }

    public void refresh() {
        // Set class text
        classLabel.setText(sesh.className);

        // Set subject text
        subjectLabel.setText(sesh.seshDescription);

        // Set time text
        /* Round to nearest half hour */
        Double numHours = (sesh.seshEstTime/30) / 2.0;
        DecimalFormat df = new DecimalFormat("0.#");
        String suffix = (numHours == 1.0) ? " Hour" : " Hours";
        timeLabel.setText(df.format(numHours) + suffix);

        if (sesh.isStudent) {
            updateLabelForStudent(miscLabel);
        } else {
            updateLabelForTutor(miscLabel);
        }
    }

}

package com.seshtutoring.seshapp.view.fragments.LearnRequestFragments;

import android.app.DownloadManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.view.RequestActivity;

/**
 * Created by nadavhollander on 7/21/15.
 */
public class LearnRequestNumberOfStudentsFragment extends Fragment implements RequestActivity.InputFragment {
    private int numStudents = -1;
    private RequestActivity parentActivity;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View v = layoutInflater.inflate(R.layout.learn_request_number_of_students_fragment, container, false);

        this.parentActivity = (RequestActivity) getActivity();

        Button oneStudentButton = (Button) v.findViewById(R.id.one_student_button);
        Button twoStudentsButton = (Button) v.findViewById(R.id.two_students_button);
        Button threeStudentsButton = (Button) v.findViewById(R.id.three_students_button);

        oneStudentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numStudents = 1;
                parentActivity.nextFragment();
            }
        });
        twoStudentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numStudents = 2;
                parentActivity.nextFragment();
            }
        });
        threeStudentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numStudents = 3;
                parentActivity.nextFragment();
            }
        });

        return v;
    }

    @Override
    public void saveValues() {
        parentActivity.setSelectedNumberOfStudents(numStudents);
    }

    @Override
    public boolean isCompleted() {
        return (numStudents != -1);
    }
}

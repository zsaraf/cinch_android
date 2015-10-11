package com.seshtutoring.seshapp.view.fragments.ProfileFragments;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.view.AddTutorClassesActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.ViewClassesView;

/**
 * Created by lillioetting on 9/9/15.
 */
public class ClassesListFragment extends Fragment implements ViewClassesView.ViewClassesViewListener {

    private MainContainerActivity mainContainerActivity;
    private User user;
    private ViewClassesView viewClassesView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        RelativeLayout view = (RelativeLayout) layoutInflater.inflate(R.layout.classes_list_fragment, null);
        viewClassesView = (ViewClassesView) view.findViewById(R.id.view_classes_view);
        viewClassesView.listener = this;

        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshListWithUser(this.user);
    }

    public void refreshListWithUser(User user) {
        if (viewClassesView != null) {
            viewClassesView.refreshClassesViewWithUser(user);
        }
    }

    public void viewClassesViewDidTapAddClasses() {
        Intent intent = new Intent(getActivity(), AddTutorClassesActivity.class);
        startActivityForResult(intent, AddTutorClassesActivity.ADD_TUTOR_CLASSES_CREATE);
        getActivity().overridePendingTransition(R.anim.fade_in, 0);
    }

}

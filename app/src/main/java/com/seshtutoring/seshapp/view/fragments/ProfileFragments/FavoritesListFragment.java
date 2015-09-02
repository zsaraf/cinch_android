package com.seshtutoring.seshapp.view.fragments.ProfileFragments;

import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.view.MainContainerActivity;

/**
 * Created by lillioetting on 9/1/15.
 */
public class FavoritesListFragment extends ListFragment{

    private User user;
    private MainContainerActivity mainContainerActivity;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        View v = layoutInflater.inflate(R.layout.favorites_list_fragment, null);

        this.mainContainerActivity = (MainContainerActivity) getActivity();
        this.user = User.currentUser(mainContainerActivity.getApplicationContext());

        return v;

    }
}

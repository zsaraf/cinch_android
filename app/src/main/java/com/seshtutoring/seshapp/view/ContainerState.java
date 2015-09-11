package com.seshtutoring.seshapp.view;

import android.support.v4.app.Fragment;

import com.seshtutoring.seshapp.model.Sesh;

/**
 * Created by nadavhollander on 8/4/15.
 */

public class ContainerState {
    public String title;
    public int iconRes;
    public Fragment fragment;
    public String tag;

    public ContainerState(String title, int iconRes, Fragment fragment, String tag,
                          boolean isNavigationItem) {
        this.title = title;
        this.iconRes = iconRes;
        this.fragment = fragment;
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ContainerState) {
            return ((ContainerState) o).title == title &&
                    ((ContainerState) o).iconRes == iconRes &&
                    ((ContainerState) o).fragment.getClass() == fragment.getClass() &&
                    ((ContainerState) o).tag == tag;
        } else return false;
    }
}
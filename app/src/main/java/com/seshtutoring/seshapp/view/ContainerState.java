package com.seshtutoring.seshapp.view;

import android.support.v4.app.Fragment;

/**
 * Created by nadavhollander on 8/4/15.
 */

public class ContainerState {
    public String title;
    public int iconRes;
    public Fragment fragment;

    public ContainerState(String title, int iconRes, Fragment fragment) {
        this.title = title;
        this.iconRes = iconRes;
        this.fragment = fragment;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ContainerState) {
            return ((ContainerState) o).title == title &&
                    ((ContainerState) o).iconRes == iconRes &&
                    ((ContainerState) o).fragment.getClass() == fragment.getClass();
        } else return false;
    }
}
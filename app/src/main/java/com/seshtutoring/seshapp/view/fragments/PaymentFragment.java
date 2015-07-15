package com.seshtutoring.seshapp.view.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seshtutoring.seshapp.R;

import java.util.List;

/**
 * Created by nadavhollander on 7/14/15.
 */


// LISTFRAGMENT SHOULD BE USED INSTEAD OF FRAGMENT, ONLY TEMPORARILY EXTENDING FRAGMENT
public class PaymentFragment extends Fragment {
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        return layoutInflater.inflate(R.layout.payment_fragment, null);
    }

}

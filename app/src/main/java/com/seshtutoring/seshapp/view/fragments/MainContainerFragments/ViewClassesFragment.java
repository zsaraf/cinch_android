package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.AboutActivity;
import com.seshtutoring.seshapp.view.ChangePasswordActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.PrivacyActivity;
import com.seshtutoring.seshapp.view.SupportActivity;
import com.seshtutoring.seshapp.view.TermsActivity;
import com.seshtutoring.seshapp.view.components.SeshDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lillioetting on 7/14/15.
 */
public class ViewClassesFragment extends ListFragment {

    private MainContainerActivity mainContainerActivity;
    private ListView menu;

    public static enum ClassItem {

        CS107("CS107", 1),
        CS108("CS108", 1),
        CS140("CS140", 1),
        MATH100("MATH100", 1),
        ENGLISH101("ENGLISH101", 1),
        CS229("CS229", 1),
        EDIT("Add or Remove Classes", 2);

        public String name;
        public int type;

        ClassItem(String name, int type) {
            this.name = name;
            this.type = type;
        }
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        menu = (ListView) layoutInflater.inflate(R.layout.view_classes_fragment, null);
        LayoutUtils layUtils = new LayoutUtils(getActivity());
        mainContainerActivity = (MainContainerActivity) getActivity();

        return menu;
    }

    @Override
    public void onStart() {
        super.onStart();
        //dummy data for now
        //this will need to move once using real data
        ViewClassesAdapter adapter = new ViewClassesAdapter(getActivity());
        for (ClassItem obj : ClassItem.values()) {
            adapter.add(obj);
        }

        //settingsList.setAdapter(adapter);
        menu.setAdapter(adapter);

    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //String email = user.getEmail();

        //seshNetworking = new SeshNetworking(getActivity());

        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                ClassItem obj = (ClassItem) menu.getItemAtPosition(position);

                if (obj.type == 2) {
                    //show web view
                    Intent viewIntent =
                            new Intent("android.intent.action.VIEW",
                                    Uri.parse("https://www.seshtutoring.com"));
                    startActivity(viewIntent);

                }

            }
        });

    }

    private class ViewHolder {

        public TextView mainTextView;

    }

    public class ViewClassesAdapter extends ArrayAdapter<ClassItem> {

        public ViewClassesAdapter(Context context) {
            super(context, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_classes_row,
                        null);

                viewHolder = new ViewHolder();

                int textID = R.id.course_title;
                Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Light.otf");

                if (getItem(position).type == 2) {
                    //do something special for button?
                }

                viewHolder.mainTextView = (TextView) convertView.findViewById(textID);
                convertView.setTag(viewHolder);

            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.mainTextView.setText(getItem(position).name);
            return convertView;
        }

    }

}

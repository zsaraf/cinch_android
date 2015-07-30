package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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
import com.seshtutoring.seshapp.view.components.SeshIconTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lillioetting on 7/14/15.
 */
public class ViewAvailableJobsFragment extends ListFragment {

    private MainContainerActivity mainContainerActivity;
    private ListView menu;

    public static enum JobItem {

        EXAMPLE_1("Lilli Oetting", "English100", "Shakespeare test", 25, 1.5, 0.5, 1),
        EXAMPLE_2("George Ma", "CS107", "Binary Bomb", 25, 2.0, 1.0, 1);

        public String name;
        public String course;
        public String assignment;
        public int rate;
        public double duration;
        public double distance;
        public int type;

        JobItem(String name, String course, String assignment, int rate, double duration, double distance, int type) {
            this.name = name;
            this.course = course;
            this.type = type;
            this.assignment = assignment;
            this.duration = duration;
            this.distance = distance;
        }
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        menu = (ListView) layoutInflater.inflate(R.layout.view_available_jobs_fragment, null);
        mainContainerActivity = (MainContainerActivity) getActivity();

        //dummy data for now
        //this may need to move once using real data
        ViewAvailableJobsAdapter adapter = new ViewAvailableJobsAdapter(getActivity());
        for (JobItem obj : JobItem.values()) {
            adapter.add(obj);
        }

        //settingsList.setAdapter(adapter);
        menu.setAdapter(adapter);

        return menu;
    }


//    @Override
//    public void onStart() {
//        super.onStart();
////
////        //dummy data for now
////        //this will need to move once using real data
////        ViewAvailableJobsAdapter adapter = new ViewAvailableJobsAdapter(getActivity());
////        for (JobItem obj : JobItem.values()) {
////            adapter.add(obj);
////        }
////
////        //settingsList.setAdapter(adapter);
////        menu.setAdapter(adapter);
//
//    }


    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //String email = user.getEmail();

        //seshNetworking = new SeshNetworking(getActivity());

//        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//
//                JobItem obj = (JobItem) menu.getItemAtPosition(position);
//
//                if (obj.type == 2) {
//                    //show web view
//
//                }
//
//            }
//        });

    }

    private class ViewHolder {

        public TextView nameTextView;
        public SeshIconTextView courseTextView;
        public SeshIconTextView assignmentTextView;
        public SeshIconTextView distanceTextView;
        public SeshIconTextView durationTextView;

    }

    public class ViewAvailableJobsAdapter extends ArrayAdapter<JobItem> {

        public ViewAvailableJobsAdapter(Context context) {
            super(context, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            JobItem item = getItem(position);
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_available_jobs_row, null);

                viewHolder = new ViewHolder();

                Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Light.otf");

                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.student_name);
                viewHolder.courseTextView = (SeshIconTextView) convertView.findViewById(R.id.course);
                viewHolder.assignmentTextView = (SeshIconTextView) convertView.findViewById(R.id.assignment);
                viewHolder.distanceTextView = (SeshIconTextView) convertView.findViewById(R.id.distance);
                viewHolder.durationTextView = (SeshIconTextView) convertView.findViewById(R.id.duration);
                convertView.setTag(viewHolder);

            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.nameTextView.setText(item.name);
            viewHolder.courseTextView.setText(item.course);
            viewHolder.courseTextView.setIconResourceId(R.drawable.book);
            viewHolder.assignmentTextView.setText(item.assignment);
            viewHolder.assignmentTextView.setIconResourceId(R.drawable.assignment_gray);
            viewHolder.distanceTextView.setText(item.distance + " miles");
            viewHolder.distanceTextView.setIconResourceId(R.drawable.current_location);
            viewHolder.durationTextView.setText(item.duration + " hours");
            viewHolder.durationTextView.setIconResourceId(R.drawable.clock_orange);
            return convertView;
        }

    }

}

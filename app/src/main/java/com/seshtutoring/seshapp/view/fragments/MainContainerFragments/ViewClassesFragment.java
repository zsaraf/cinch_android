package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.AvailableJob;
import com.seshtutoring.seshapp.model.Course;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.AboutActivity;
import com.seshtutoring.seshapp.view.ChangePasswordActivity;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.PrivacyActivity;
import com.seshtutoring.seshapp.view.SupportActivity;
import com.seshtutoring.seshapp.view.TermsActivity;
import com.seshtutoring.seshapp.view.components.SeshDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lillioetting on 7/14/15.
 */
public class ViewClassesFragment extends ListFragment {
    private static final String TAG =ViewClassesFragment.class.getName();

    private MainContainerActivity mainContainerActivity;
    private ListView menu;
    private ArrayList<CourseHolder> tutorCourses;
    private ViewClassesAdapter classesAdapter;
    private SeshNetworking seshNetworking;

    private class CourseHolder {

        public Course course;
        public int type;

        public CourseHolder(Course course, int type) {
            this.type = type;
            this.course = course;
        }

    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        menu = (ListView) layoutInflater.inflate(R.layout.view_classes_fragment, null);
        LayoutUtils layUtils = new LayoutUtils(getActivity());
        mainContainerActivity = (MainContainerActivity) getActivity();

        this.seshNetworking = new SeshNetworking(getActivity());

        this.tutorCourses = new ArrayList<CourseHolder>();
        //adds edit classes button
        tutorCourses.add(new CourseHolder(null, 2));
        this.classesAdapter = new ViewClassesAdapter(getActivity(), tutorCourses);
        menu.setAdapter(classesAdapter);

        //get courses from server
        seshNetworking.getTutorCourses(new Response.Listener<JSONObject>() {
            public void onResponse(JSONObject jsonResponse) {
                try {
                    if (jsonResponse.get("status").equals("SUCCESS")) {
                        tutorCourses.clear();
                        JSONArray tutorCoursesArrayJson = jsonResponse.getJSONArray("classes");
                        for (int i = 0; i < tutorCoursesArrayJson.length(); i++) {
                            CourseHolder courseHolder = new CourseHolder(Course.fromJson(tutorCoursesArrayJson.getJSONObject(i)), 1);
                            tutorCourses.add(courseHolder);
                        }
                        //adds edit classes button
                        tutorCourses.add(new CourseHolder(null, 2));
                        classesAdapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, jsonResponse.getString("message"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, volleyError.getMessage());
            }
        });

        return menu;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //String email = user.getEmail();

        //seshNetworking = new SeshNetworking(getActivity());

        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                CourseHolder obj = (CourseHolder) menu.getItemAtPosition(position);

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

    public class ViewClassesAdapter extends ArrayAdapter<CourseHolder> {

        private Context mContext;
        private LayoutInflater layoutInflater;

        public ViewClassesAdapter(Context context, ArrayList<CourseHolder> tutorCourses) {
            super(context, R.layout.view_classes_row, tutorCourses);
            this.mContext = context;
            layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            CourseHolder item = (CourseHolder) getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_classes_row,
                        null);

                viewHolder = new ViewHolder();

                int textID = R.id.course_title;
                Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Light.otf");

                if (item.type == 2) {
                    //do something special for button?
                }

                viewHolder.mainTextView = (TextView) convertView.findViewById(textID);
                convertView.setTag(viewHolder);

            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (item.type == 2) {
                viewHolder.mainTextView.setText("Add/Remove Class");
            }else {
                viewHolder.mainTextView.setText(item.course.shortFormatForTextView());
            }
            return convertView;
        }

    }

}

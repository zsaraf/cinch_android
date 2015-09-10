package com.seshtutoring.seshapp.view.fragments.ProfileFragments;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Course;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lillioetting on 9/9/15.
 */
public class ClassesListFragment extends ListFragment {


    private ListView list;
    private ClassListAdapter classesAdapter;
    private SeshNetworking seshNetworking;
    private MainContainerActivity mainContainerActivity;
    private User user;
    private ArrayList<Course> tutorCourses;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        this.list = (ListView) layoutInflater.inflate(R.layout.profile_list_view, null);

        mainContainerActivity = (MainContainerActivity) getActivity();
        this.user = User.currentUser(mainContainerActivity.getApplicationContext());
        this.seshNetworking = new SeshNetworking(mainContainerActivity);

        this.tutorCourses = new ArrayList<Course>();
        this.classesAdapter = new ClassListAdapter(getActivity(), tutorCourses);
        this.list.setAdapter(this.classesAdapter);

        //get courses from server
        seshNetworking.getTutorCourses(new Response.Listener<JSONObject>() {
            public void onResponse(JSONObject jsonResponse) {
                try {
                    if (jsonResponse.get("status").equals("SUCCESS")) {
                        tutorCourses.clear();
                        JSONArray tutorCoursesArrayJson = jsonResponse.getJSONArray("classes");
                        for (int i = 0; i < tutorCoursesArrayJson.length(); i++) {
                            tutorCourses.add(Course.fromJson(tutorCoursesArrayJson.getJSONObject(i)));
                        }
                        classesAdapter.notifyDataSetChanged();
                    } else {
//                        Log.e(TAG, jsonResponse.getString("message"));
                    }
                } catch (JSONException e) {
//                    Log.e(TAG, e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError volleyError) {
//                Log.e(TAG, volleyError.getMessage());
            }
        });

        return this.list;

    }

    public class ClassListAdapter extends ArrayAdapter<Course> {

        private Context mContext;
        private LayoutInflater layoutInflater;

        public ClassListAdapter(Context context, List<Course> courses) {
            super(context, R.layout.profile_class_row, courses);
            this.mContext = context;
            layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            Course course = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.profile_class_row,
                        null);
            }

            TextView classText = (TextView) convertView.findViewById(R.id.course_row_text);
            classText.setText(course.shortFormatForTextView());

            return convertView;
        }

    }

}
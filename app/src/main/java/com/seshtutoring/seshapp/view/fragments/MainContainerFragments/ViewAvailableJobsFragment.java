package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

import android.app.ListFragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.AvailableJob;
import com.seshtutoring.seshapp.model.Course;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.components.SeshIconTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by lillioetting on 7/14/15.
 */
public class ViewAvailableJobsFragment extends ListFragment {
    private static final String TAG =ViewAvailableJobsFragment.class.getName();

    private MainContainerActivity mainContainerActivity;
    private ListView menu;
    private Typeface boldTypeFace;
    private ArrayList<JobHolder> availableJobs;
    private ArrayList<Course> tutorCourses;
    private ViewAvailableJobsAdapter availableJobsAdapter;
    private SeshNetworking seshNetworking;

    private class JobHolder {
        public AvailableJob job;
        public int type;

        public JobHolder(AvailableJob job, int type) {
            this.type = type;
            this.job = job;
        }
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        menu = (ListView) layoutInflater.inflate(R.layout.view_available_jobs_fragment, null);
        mainContainerActivity = (MainContainerActivity) getActivity();
        boldTypeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Book.otf");
        this.seshNetworking = new SeshNetworking(getActivity());

        this.availableJobs = new ArrayList<JobHolder>();

        this.tutorCourses = new ArrayList<Course>();
        this.availableJobsAdapter = new ViewAvailableJobsAdapter(getActivity(), availableJobs);
        menu.setAdapter(availableJobsAdapter);

        //get courses from server
        seshNetworking.getTutorCourses(new Response.Listener<JSONObject>() {
            public void onResponse(JSONObject jsonResponse) {
                try {
                    if (jsonResponse.get("status").equals("SUCCESS")) {
                        tutorCourses.clear();
                        JSONArray tutorCoursesArrayJson = jsonResponse.getJSONArray("classes");
                        for (int i = 0; i < tutorCoursesArrayJson.length(); i++) {
                            tutorCourses.add(Course.fromJson((tutorCoursesArrayJson.getJSONObject(i))));
                        }

                        //get available jobs from server
                        seshNetworking.getAvailableJobs(tutorCourses, new Response.Listener<JSONObject>() {
                            public void onResponse(JSONObject jsonResponse) {
                                try {
                                    if (jsonResponse.get("status").equals("SUCCESS")) {
                                        availableJobs.clear();
                                        JSONArray availableJobsArrayJson = jsonResponse.getJSONArray("courses");
                                        for (int i = 0; i < availableJobsArrayJson.length(); i++) {
                                            JobHolder jobHolder = new JobHolder(AvailableJob.fromJson((availableJobsArrayJson.getJSONObject(i))), 1);
                                            availableJobs.add(jobHolder);
                                        }
                                        if (availableJobs.size() == 0) {
                                            //No available jobs
                                            availableJobs.add(new JobHolder(null, 2));
                                        }
                                        availableJobsAdapter.notifyDataSetChanged();
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

    private class ViewHolder {

        public TextView nameTextView;
        public TextView rateTextView;
        public TextView overlayTextView;
        public SeshIconTextView courseTextView;
        public SeshIconTextView assignmentTextView;
        //public SeshIconTextView distanceTextView;
        public SeshIconTextView durationTextView;

    }

    public class ViewAvailableJobsAdapter extends ArrayAdapter<JobHolder> {

        private Context mContext;
        private LayoutInflater layoutInflater;

        public ViewAvailableJobsAdapter(Context context, ArrayList<JobHolder> availableJobs) {
            super(context, R.layout.view_available_jobs_row, availableJobs);
            this.mContext = context;
            layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            JobHolder holder = getItem(position);
            AvailableJob item = holder.job;
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_available_jobs_row, null);

                viewHolder = new ViewHolder();

                viewHolder.overlayTextView = (TextView) convertView.findViewById(R.id.overlay_text);

                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.student_name);
                viewHolder.nameTextView.setTypeface(boldTypeFace);

                viewHolder.rateTextView = (TextView) convertView.findViewById(R.id.hourly_rate);

                viewHolder.courseTextView = (SeshIconTextView) convertView.findViewById(R.id.course);
                viewHolder.courseTextView.setIconResourceId(R.drawable.book);

                viewHolder.assignmentTextView = (SeshIconTextView) convertView.findViewById(R.id.assignment);
                viewHolder.assignmentTextView.setIconResourceId(R.drawable.assignment_gray);

                //viewHolder.distanceTextView = (SeshIconTextView) convertView.findViewById(R.id.distance);
                viewHolder.durationTextView = (SeshIconTextView) convertView.findViewById(R.id.duration);
                viewHolder.durationTextView.setIconResourceId(R.drawable.clock_orange);

                convertView.setTag(viewHolder);

            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (holder.type == 2) {
                viewHolder.overlayTextView.setText("no available jobs");
                viewHolder.nameTextView.setVisibility(View.GONE);
                viewHolder.assignmentTextView.setVisibility(View.GONE);
                viewHolder.durationTextView.setVisibility(View.GONE);
                viewHolder.rateTextView.setVisibility(View.GONE);
                viewHolder.courseTextView.setVisibility(View.GONE);
            }else {
                viewHolder.overlayTextView.setVisibility(View.GONE);

                viewHolder.nameTextView.setText(item.studentName);
                viewHolder.nameTextView.setVisibility(View.VISIBLE);

                //calcualte estimated price
                NumberFormat money = NumberFormat.getCurrencyInstance(Locale.US);
                viewHolder.rateTextView.setText(money.format(item.rate * item.maxTime));

                viewHolder.courseTextView.setText(item.course.shortFormatForTextView());
                viewHolder.courseTextView.setVisibility(View.VISIBLE);

                viewHolder.assignmentTextView.setText(item.description);
                viewHolder.assignmentTextView.setVisibility(View.VISIBLE);

                //calculate distance?
                //viewHolder.distanceTextView.setText(item + " miles");
                //viewHolder.distanceTextView.setIconResourceId(R.drawable.current_location);

                viewHolder.durationTextView.setText(item.maxTime + " hours");
                viewHolder.durationTextView.setVisibility(View.VISIBLE);
            }

            return convertView;
        }

    }

}

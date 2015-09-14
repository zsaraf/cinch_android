package com.seshtutoring.seshapp.view.fragments.LearnRequestFragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Course;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.RequestActivity;
import com.seshtutoring.seshapp.view.components.SeshViewPager;
import com.seshtutoring.seshapp.view.components.SeshEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by nadavhollander on 7/21/15.
 */
public class LearnRequestCourseFragment extends SeshViewPager.InputFragment {
    private static final String TAG = LearnRequestCourseFragment.class.getName();

    private RequestActivity parentActivity;
    private SeshEditText courseEditText;
    private SeshNetworking seshNetworking;
    private ListView courseResultsListView;
    private CourseResultsAdapter courseResultsAdapter;
    private ArrayList<Course> courseResults;
    private SeshViewPager seshViewPager;

    private Course selectedCourse = null;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View v = layoutInflater.inflate(R.layout.learn_request_course_fragment, container, false);

        this.parentActivity = (RequestActivity) getActivity();

        this.courseResults = new ArrayList<Course>();
        this.courseEditText = (SeshEditText) v.findViewById(R.id.course_edit_text);
        this.courseResultsListView = (ListView) v.findViewById(R.id.course_results_list);
        this.courseResultsAdapter = new CourseResultsAdapter(getActivity(), courseResults);

        this.courseResultsListView.setAdapter(courseResultsAdapter);
        this.courseResultsListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedCourse = courseResults.get(position);
                saveValues();

                courseEditText.setText(selectedCourse.formatForTextView());
                courseResults.clear();

                seshViewPager.flingNextFragment();
            }
        });
        this.seshNetworking = new SeshNetworking(getActivity());

        courseEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("") || selectedCourse != null) {
                    courseResults.clear();
                    return;
                }
                seshNetworking.searchForClassName(s.toString(), new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject jsonResponse) {
                        try {
                            if (jsonResponse.get("status").equals("SUCCESS")) {
                                courseResults.clear();
                                JSONArray classesArrayJson = jsonResponse.getJSONArray("classes");
                                for (int i = 0; i < classesArrayJson.length(); i++) {
                                    courseResults.add(Course.fromJson((classesArrayJson.getJSONObject(i))));
                                }
                                courseResultsAdapter.notifyDataSetChanged();
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
            }
        });

        return v;
    }


    private class CourseResultsAdapter extends ArrayAdapter<Course> {
        private Context mContext;
        private LayoutInflater layoutInflater;

        public CourseResultsAdapter(Context context, ArrayList<Course> classResults) {
            super(context, R.layout.course_results_row, classResults);
            this.mContext = context;
            layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return Math.min(courseResults.size(), maxRowsForScreenSize());
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Course courseRowData = getItem(position);
            View v = convertView;

            if (v == null) {
                 v = layoutInflater.inflate(R.layout.course_results_row, null);
            }

            TextView classRowText = (TextView) v.findViewById(R.id.course_row_text);
            classRowText.setText(courseRowData.formatForTextView());

            return v;
        }

        private int maxRowsForScreenSize() {
            // TODO change this
            return 3;
        }
    }

    @Override
    public void saveValues() {
        parentActivity.getCurrentLearnRequest().classId = String.format("%d", selectedCourse.classId);
        parentActivity.getCurrentLearnRequest().classString = String.format("%s %s",
                selectedCourse.deptAbbrev, selectedCourse.classNumber);
    }

    @Override
    public boolean isCompleted() {
        return (selectedCourse != null);
    }

    @Override
    public void attachSeshViewPager(SeshViewPager seshViewPager) {
        this.seshViewPager = seshViewPager;
    }

    @Override
    public void onFragmentInForeground() {
        courseEditText.setText("");
        courseEditText.requestEditTextFocus();
        selectedCourse = null;
        parentActivity.showKeyboard();
        parentActivity.showRequestFlowNextButton();
    }

    @Override
    public void beforeFragmentInForeground() {
        // do nothing
    }
}

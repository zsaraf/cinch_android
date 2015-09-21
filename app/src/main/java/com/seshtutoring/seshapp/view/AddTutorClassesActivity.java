package com.seshtutoring.seshapp.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Course;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.components.SeshEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class AddTutorClassesActivity extends AppCompatActivity {
    private static final String TAG = AddTutorClassesActivity.class.getName();

    private ArrayList<Course> classesToAdd;
    private ArrayList<Course> classesToDelete;
    private ArrayList<Course> courseResults;
    private SeshEditText courseEditText;
    private ListView courseResultsListView;
    private CourseResultsAdapter courseResultsAdapter;
    private List<Course> tutorClasses;
    SeshNetworking seshNetworking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tutor_classes);

        this.tutorClasses = User.currentUser(this).tutor.getCourses();
        this.classesToAdd = new ArrayList<Course>();
        this.classesToDelete = new ArrayList<Course>();

        this.courseResults = new ArrayList<Course>();
        this.courseEditText = (SeshEditText) findViewById(R.id.course_edit_text);
        this.courseResultsListView = (ListView) findViewById(R.id.course_results_list);
        this.courseResultsAdapter = new CourseResultsAdapter(this, courseResults);

        this.courseResultsListView.setAdapter(courseResultsAdapter);
        this.courseResultsListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Course selectedCourse = courseResults.get(position);

                if (tutorClasses.contains(selectedCourse)) {
                    if (classesToDelete.contains(selectedCourse)) {
                        classesToDelete.remove(selectedCourse);
                    } else {
                        classesToDelete.add(selectedCourse);
                    }
                } else {
                    if (classesToAdd.contains(selectedCourse)) {
                        classesToAdd.remove(selectedCourse);
                    } else {
                        classesToAdd.add(selectedCourse);
                    }
                }
                courseResultsAdapter.notifyDataSetChanged();
            }
        });
        this.seshNetworking = new SeshNetworking(this);

        courseEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (s.toString().equals("")) {
                            courseResults.clear();
                            return;
                        }
                    }
                });
                seshNetworking.searchForClassName(s.toString(), new Response.Listener<JSONObject>() {
                    public void onResponse(final JSONObject jsonResponse) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (jsonResponse.get("status").equals("SUCCESS")) {
                                        courseResults.clear();
                                        JSONArray classesArrayJson = jsonResponse.getJSONArray("classes");
                                        for (int i = 0; i < classesArrayJson.length(); i++) {
                                            courseResults.add(Course.createCourseFromSearchJSON((classesArrayJson.getJSONObject(i))));
                                        }
                                        courseResultsAdapter.notifyDataSetChanged();
                                    } else {
                                        Log.e(TAG, jsonResponse.getString("message"));
                                    }
                                } catch (JSONException e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            }
                        });
                    }


                }, new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e(TAG, volleyError.getMessage());
                    }
                });
            }
        });
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

            if (classesToAdd.contains(courseRowData)) {
                classRowText.setTextColor(getResources().getColor(R.color.seshgreen));
            } else {
                classRowText.setTextColor(getResources().getColor(R.color.seshorange));
            }

            return v;
        }

        private int maxRowsForScreenSize() {
            // TODO change this
            return 3;
        }
    }

}

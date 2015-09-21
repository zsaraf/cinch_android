package com.seshtutoring.seshapp.view.fragments.MainContainerFragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Course;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.ViewSeshSetTimeActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by lillioetting on 7/14/15.
 */
public class ViewClassesView extends RelativeLayout {
    private static final String TAG = ViewClassesView.class.getName();

    private MainContainerActivity mainContainerActivity;
    private ListView menu;
    private ArrayList<CourseHolder> tutorCourses;
    private ViewClassesAdapter classesAdapter;
    private SeshNetworking seshNetworking;
    private Context mContext;
    private User user;

    public interface ViewClassesViewListener {
        public void viewClassesViewDidTapAddClasses();
    }

    public ViewClassesViewListener listener;

    private class CourseHolder {

        public Course course;
        public int type;

        public CourseHolder(Course course, int type) {
            this.type = type;
            this.course = course;
        }

    }

    public ViewClassesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = mInflater.inflate(R.layout.view_classes_view, this, true);
        menu = (ListView)v.findViewById(R.id.list);

        init(attrs, 0);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        LayoutUtils layUtils = new LayoutUtils(mContext);

        this.seshNetworking = new SeshNetworking(mContext);

        this.tutorCourses = new ArrayList<CourseHolder>();

        this.classesAdapter = new ViewClassesAdapter(mContext, tutorCourses);
        menu.setAdapter(classesAdapter);

        this.user = User.currentUser(mContext);
        refreshClassesViewWithUser(user);

        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                CourseHolder obj = (CourseHolder) menu.getItemAtPosition(position);

                if (obj.type == 2 && listener != null) {
                    listener.viewClassesViewDidTapAddClasses();
                }

            }
        });
    }

    public void refreshClassesViewWithUser(User user) {
        this.tutorCourses.clear();
        this.tutorCourses.add(new CourseHolder(null, 2));
        List<Course> tutorCourses = user.tutor.getCourses();
        for (int i = 0; i < tutorCourses.size(); i++) {
            Course course = tutorCourses.get(i);
            this.tutorCourses.add(new CourseHolder(course, 1));
        }
        this.classesAdapter.notifyDataSetChanged();
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
                Typeface typeFace = Typeface.createFromAsset(mContext.getAssets(), "fonts/Gotham-Light.otf");

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

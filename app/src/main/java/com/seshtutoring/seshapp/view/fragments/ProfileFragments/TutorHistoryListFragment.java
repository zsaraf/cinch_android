package com.seshtutoring.seshapp.view.fragments.ProfileFragments;

import android.app.ListFragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Course;
import com.seshtutoring.seshapp.model.PastRequest;
import com.seshtutoring.seshapp.model.PastSesh;
import com.seshtutoring.seshapp.model.User;
import com.seshtutoring.seshapp.util.networking.SeshNetworking;
import com.seshtutoring.seshapp.view.MainContainerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lillioetting on 9/1/15.
 */
public class TutorHistoryListFragment extends ListFragment{

    private ListView list;
    private List<PastSesh> pastSeshes;
    private List<PastRequest> pastRequests;
    private TutorHistoryAdapter tutorHistoryAdapter;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        this.list = (ListView) layoutInflater.inflate(R.layout.tutor_history_list_fragment, null);

        this.pastSeshes = PastSesh.listAll(PastSesh.class);
        this.pastRequests = PastRequest.listAll(PastRequest.class);

        this.tutorHistoryAdapter = new TutorHistoryAdapter(getActivity(), pastSeshes);
        this.list.setAdapter(tutorHistoryAdapter);

        return this.list;

    }

    private class ViewHolder {

        public TextView mainTextView;
        public TextView subTextView;
        public TextView rightTextView;
        public de.hdodenhof.circleimageview.CircleImageView profileImageView;

    }

    public class TutorHistoryAdapter extends ArrayAdapter<PastSesh> {

        private Context mContext;
        private LayoutInflater layoutInflater;

        public TutorHistoryAdapter(Context context, List<PastSesh> tutorCourses) {
            super(context, R.layout.profile_list_view_row, tutorCourses);
            this.mContext = context;
            layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            PastSesh item = (PastSesh) getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_classes_row,
                        null);

                viewHolder = new ViewHolder();

                viewHolder.mainTextView = (TextView) convertView.findViewById(R.id.profile_list_main_text);
                viewHolder.subTextView = (TextView) convertView.findViewById(R.id.profile_list_sub_text);
                viewHolder.subTextView = (TextView) convertView.findViewById(R.id.profile_list_right_text);
                viewHolder.profileImageView = (de.hdodenhof.circleimageview.CircleImageView) convertView.findViewById(R.id.profile_list_profile_image);

                convertView.setTag(viewHolder);

            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            return convertView;
        }

    }

}

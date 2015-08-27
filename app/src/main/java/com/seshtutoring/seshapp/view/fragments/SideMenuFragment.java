package com.seshtutoring.seshapp.view.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.services.PeriodicFetchBroadcastReceiver;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.ContainerState;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.DummyRequestSeshFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SideMenuFragment extends Fragment implements SlidingMenu.OnOpenedListener {
    private static final String TAG = SideMenuFragment.class.getName();

    public static final String MAIN_WRAPPER_STATE_KEY = "main_wrapper_state";
    public static final String MENU_OPEN_DISPLAY_NEW_REQUEST = "display_new_request";

    private static final int NUM_STATIC_MENU_OPTIONS = 5;

    private MainContainerActivity mainContainerActivity;
    private SideMenuItem selectedItem;
    private ListView navigationMenu;
    private ListView openRequestsAndSeshesMenu;
    private RequestsAndSeshesAdapter openRequestsAndSeshesAdapter;
    private String menuOpenFlag;
    private SideMenuAdapter sideMenuAdapter;

    private TextView[] menuOptionTitles;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.side_menu_fragment, container, false);
        navigationMenu = (ListView) view.findViewById(R.id.side_menu_list);
        openRequestsAndSeshesMenu = (ListView) view.findViewById(R.id.open_requests_and_seshes_list);
        mainContainerActivity = (MainContainerActivity) getActivity();
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        menuOptionTitles = new TextView[5];

        sideMenuAdapter = new SideMenuAdapter(getActivity());

        navigationMenu.setAdapter(sideMenuAdapter);

        navigationMenu.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                ContainerState selectedMenuOption = mainContainerActivity.HOME;
                switch (position) {
                    case 0:
                        selectedMenuOption = mainContainerActivity.HOME;
                        break;
                    case 1:
                        selectedMenuOption = mainContainerActivity.PROFILE;
                        break;
                    case 2:
                        selectedMenuOption = mainContainerActivity.PAYMENT;
                        break;
                    case 3:
                        selectedMenuOption = mainContainerActivity.SETTINGS;
                        break;
                    case 4:
                        selectedMenuOption = mainContainerActivity.PROMOTE;
                        break;
                    default:
                        selectedMenuOption = mainContainerActivity.HOME;
                        break;
                }

                mainContainerActivity.setCurrentState(selectedMenuOption, null);

//                updateSelectedItem();
            }
        });

        updateSelectedItem();

        openRequestsAndSeshesAdapter = new RequestsAndSeshesAdapter(getActivity());
        openRequestsAndSeshesMenu.setAdapter(openRequestsAndSeshesAdapter);

        openRequestsAndSeshesMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RequestsAndSeshesListItem item = openRequestsAndSeshesAdapter.getItem(position);
                if (item.isDivider) return;

                Map<String, Object> options = new HashMap<String, Object>();

                if(item.isSesh) {
                    mainContainerActivity.setCurrentState(new ContainerState("Sesh!", 0, ViewSeshFragment.newInstance(item.sesh.seshId)));
                } else {
                    options.put(DummyRequestSeshFragment.REQUEST_DUMMY_KEY, "RequestId = " + item.learnRequest.learnRequestId);
                    mainContainerActivity.setCurrentState(new ContainerState("Request!", 0, new DummyRequestSeshFragment()));
                }
            }
        });

        updateLearnList();
    }

    public void updateSelectedItem() {
        ContainerState selectedMenuOption = mainContainerActivity.getCurrentState();
        sideMenuAdapter.clear();

        for (ContainerState state : mainContainerActivity.containerStates) {
            sideMenuAdapter.add(new SideMenuItem(state.title, state.iconRes,
                    (selectedMenuOption == state) ? true : false));
        }

        sideMenuAdapter.notifyDataSetChanged();
    }

    private class SideMenuItem {
        public String tag;
        public int icon;
        public boolean isSelected;
        public SideMenuItem(String tag, int icon, boolean isSelected) {
            this.tag = tag;
            this.icon = icon;
            this.isSelected = isSelected;
        }
    }

    public class SideMenuAdapter extends ArrayAdapter<SideMenuItem> {

        public SideMenuAdapter(Context context) {
            super(context, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.side_menu_list_row,
                        null);
            }
            ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
            icon.setImageResource(getItem(position).icon);
            TextView title = (TextView) convertView.findViewById(R.id.row_title);
            title.setText(getItem(position).tag);
            if (getItem(position).isSelected) {
                selectItem(title);
                selectedItem = getItem(position);
            } else {
                deselectItem(title);
            }
            return convertView;
        }

    }

    private class RequestsAndSeshesListItem {
        public boolean isSesh;
        public boolean isDivider;
        public LearnRequest learnRequest;
        public Sesh sesh;
        public String dividerText;
        public RequestsAndSeshesListItem(boolean isSesh, boolean isDivider, LearnRequest learnRequest, Sesh sesh,
                             String dividerText) {
            this.isSesh = isSesh;
            this.isDivider = isDivider;
            this.learnRequest = learnRequest;
            this.sesh = sesh;
            this.dividerText = dividerText;
        }
    }

    public class RequestsAndSeshesAdapter extends ArrayAdapter<RequestsAndSeshesListItem> {
        public RequestsAndSeshesAdapter(Context context) { super(context, 0); }

        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG, "refreshing position " + position);
            RequestsAndSeshesListItem item = getItem(position);
            if (item.isDivider) {
                // if convertView is not instantiated, or is of the wrong type, we re-instantiate
                if (convertView == null || isOpenRequestRow(convertView) || isOpenSeshRow(convertView)) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.open_request_divider,
                            null);
                }

                TextView dividerText = (TextView) convertView.findViewById(R.id.divider_text);
                dividerText.setText(item.dividerText);

                convertView.setEnabled(false);
                convertView.setOnClickListener(null);
            } else if (item.isSesh) {
                // if convertView is not instantiated, or is of the wrong type, we re-instantiate
                if (convertView == null || isOpenRequestRow(convertView)) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.open_sesh_list_row,
                            null);
                }

                TextView classAbbrvTextView = (TextView) convertView.findViewById(R.id.open_sesh_list_row_class);
                classAbbrvTextView.setText(item.sesh.className);

                Typeface  medium = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Medium.otf");
                classAbbrvTextView.setTypeface(medium);

                TextView timeAbbrvTextView = (TextView) convertView.findViewById(R.id.open_sesh_list_row_time);
                timeAbbrvTextView.setText(item.sesh.getTimeAbbrvString());

                if (!item.sesh.isStudent) {
                    ImageView icon = (ImageView)convertView.findViewById(R.id.open_sesh_list_row_status_icon);
                    int drawableId;

                    if (item.sesh.seshSetTime == null) {
                        drawableId = R.drawable.alert;
                    } else {
                        drawableId = R.drawable.check_green;
                    }

                    if (Build.VERSION.SDK_INT < 21) {
                        icon.setImageDrawable(getResources().getDrawable(drawableId));
                    } else  {
                        icon.setImageDrawable(getResources().getDrawable(drawableId, null));
                    }
                }
            } else {
                // if convertView is not instantiated, or is of the wrong type, we re-instantiate
                if (convertView == null || isOpenSeshRow(convertView))
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.open_request_list_row,
                            null);

                TextView classAbbrvTextView = (TextView) convertView.findViewById(R.id.open_request_list_row_class);
                classAbbrvTextView.setText(item.learnRequest.classString);
            }

            // if view represents the newest request and side menu was opened in context of a new
            // request being created, animate row in to emphasize it to user.
            if (menuOpenFlag == MENU_OPEN_DISPLAY_NEW_REQUEST && position == getCount() - 1) {
                Log.d("meh", "allegadly animating");
                Animation newItemAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_and_slide_in);
                convertView.startAnimation(newItemAnimation);
                Log.d(TAG, "bout to set this null:");
                setStatusFlag(null);
            }
            return convertView;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            Log.d(TAG, "notify data set changed");
        }
    }

    private boolean isOpenRequestRow(View view) {
        return (view.findViewById(R.id.open_request_list_row_class) != null);
    }

    private boolean isOpenSeshRow(View view) {
        return (view.findViewById(R.id.open_sesh_list_row_class) != null);
    }

    public void selectItem(TextView title) {
        Typeface bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Bold.otf");
        title.setTypeface(bold);
    }

    public void deselectItem(TextView title) {
        Typeface light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Light.otf");
        title.setTypeface(light);
    }

    public RequestsAndSeshesAdapter getRequestsAndSeshesAdapter() {
        return openRequestsAndSeshesAdapter;
    }

    public void setStatusFlag(String flag) {
        Log.d(TAG, "SETTING MENUOPTIONFLAG " + flag);
        this.menuOpenFlag = flag;
    }

    @Override
    public void onOpened() {
        Log.d(TAG, "onOpened called w/ menuOpenFlag: " + menuOpenFlag);
        if (menuOpenFlag == MENU_OPEN_DISPLAY_NEW_REQUEST) {
            updateLearnList();
        }
    }

    public synchronized void updateLearnList() {
        Log.d(TAG, "updateLearnList() begin " + new Date().toString() + " with instance " + this.getId());
        List<Sesh> studentSeshes = null;
        if (Sesh.listAll(Sesh.class).size() > 0) {
//            studentSeshes = Sesh.find(Sesh.class, "is_student = ?", Integer.toString(1));
            studentSeshes = Sesh.listAll(Sesh.class);
        } else {
            studentSeshes = new ArrayList<Sesh>();
        }
        Iterator<LearnRequest> learnRequests = LearnRequest.findAll(LearnRequest.class);

        int beforeCount = openRequestsAndSeshesAdapter.getCount();

        openRequestsAndSeshesAdapter.clear();

        for (Sesh sesh : studentSeshes) {
            RequestsAndSeshesListItem seshItem =
                    new RequestsAndSeshesListItem(true, false, null, sesh, null);
            openRequestsAndSeshesAdapter.add(seshItem);
        }

        while (learnRequests.hasNext()) {
            RequestsAndSeshesListItem requestItem = new RequestsAndSeshesListItem(false, false, learnRequests.next(), null, null);
            openRequestsAndSeshesAdapter.add(requestItem);
        }

        if (openRequestsAndSeshesAdapter.getCount() > 0) {
            RequestsAndSeshesListItem learnDivider = new RequestsAndSeshesListItem(false, true, null, null, "LEARN");

            openRequestsAndSeshesAdapter.insert(learnDivider, 0);
        }

        openRequestsAndSeshesAdapter.notifyDataSetChanged();
        Log.d(TAG, "updateLearnList() end " + new Date().toString());

    }
}
package com.seshtutoring.seshapp.view.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.google.android.gms.maps.model.Circle;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.LearnRequest;
import com.seshtutoring.seshapp.model.LearnRequest.LearnRequestTableListener;
import com.seshtutoring.seshapp.model.Message;
import com.seshtutoring.seshapp.model.Sesh;
import com.seshtutoring.seshapp.model.Sesh.SeshTableListener;
import com.seshtutoring.seshapp.services.PeriodicFetchBroadcastReceiver;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.ContainerState;
import com.seshtutoring.seshapp.view.MainContainerStateManager;
import com.seshtutoring.seshapp.view.MainContainerStateManager.NavigationItemState;
import com.seshtutoring.seshapp.view.MessagingActivity;
import com.seshtutoring.seshapp.view.animations.LearnRequestDisplayAnimation;
import com.seshtutoring.seshapp.view.animations.SeshDisplayAnimation;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.DummyRequestSeshFragment;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SideMenuFragment extends Fragment implements SeshTableListener, LearnRequestTableListener {
    private static final String TAG = SideMenuFragment.class.getName();

    private MainContainerActivity mainContainerActivity;
    private ListView navigationMenu;
    private ListView openRequestsAndSeshesMenu;
    private RequestsAndSeshesAdapter openRequestsAndSeshesAdapter;
    private String menuOpenFlag;
    private SideMenuAdapter sideMenuAdapter;
    private SideMenuOpenAnimation sideMenuOpenAnimation;
    private Spring selectedStateSpring;
    private SelectableItem currentSelectedItem;
    private MainContainerStateManager containerStateManager;
    private boolean itemSelectionInProgress;
    private BroadcastReceiver broadcastReceiver;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.side_menu_fragment, container, false);
        navigationMenu = (ListView) view.findViewById(R.id.side_menu_list);
        openRequestsAndSeshesMenu = (ListView) view.findViewById(R.id.open_requests_and_seshes_list);
        mainContainerActivity = (MainContainerActivity) getActivity();
        containerStateManager = mainContainerActivity.getContainerStateManager();
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sideMenuAdapter = new SideMenuAdapter(getActivity());

        NavigationItemState[] navigationMenuStates = NavigationItemState.values();
        for (int i = 0; i < navigationMenuStates.length; i++) {
            ContainerState state = containerStateManager.getContainerStateForNavigationItem(navigationMenuStates[i]);
            sideMenuAdapter.add(new SideMenuItem(state.title, state.iconRes, i, state.tag));
        }

        navigationMenu.setAdapter(sideMenuAdapter);

        openRequestsAndSeshesAdapter = new RequestsAndSeshesAdapter(getActivity());
        openRequestsAndSeshesAdapter.setNotifyOnChange(false);
        openRequestsAndSeshesMenu.setAdapter(openRequestsAndSeshesAdapter);

        selectedStateSpring = SpringSystem.create().createSpring();
        selectedStateSpring.setSpringConfig(SpringConfig.fromBouncinessAndSpeed(9, 6));

        Sesh.setTableListener(this);
        LearnRequest.setTableListener(this);

        (new UpdateRequestAndSeshListTask()).execute();

        containerStateManager.setContainerStateForNavigation(NavigationItemState.HOME);
    }

    public void updateSelectedItem(SelectableItem selectedItem) {
        if (currentSelectedItem != null) {
            currentSelectedItem.setSelected(false);
        }

        currentSelectedItem = selectedItem;
        currentSelectedItem.setSelected(true);

        itemSelectionInProgress = false;
    }

    public abstract class SelectableItem {
        private boolean selected = false;
        public String tag;
        public boolean selectionInProgress;

        SelectableItem(String tag) {
            this.tag = tag;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;

            if (getView() != null) {
                if (selected) {
                    selectLabel(getLabel());
                } else {
                    deselectLabel(getLabel());
                }
            }
        }

        public abstract View getView();
        public abstract TextView getLabel();
    }

    private class SideMenuItem extends SelectableItem {
        public String title;
        public int icon;
        public int position;
        public SideMenuItem(String title, int icon, int position, String tag) {
            super(tag);

            this.title = title;
            this.icon = icon;
            this.position = position;
        }

        public View getView() {
            return navigationMenu.getChildAt(position);
        }

        public TextView getLabel() {
            return (TextView) getView().findViewById(R.id.row_title);
        }
    }

    public void selectItemForContainerState(ContainerState containerState) {
        for (int i = 0; i < sideMenuAdapter.getCount(); i++) {
            SelectableItem item = sideMenuAdapter.getItem(i);
            if (item.tag.equals(containerState.tag)) {
                updateSelectedItem(item);
                return;
            }
        }

        for (int i = 0; i < openRequestsAndSeshesAdapter.getCount(); i++) {
            SelectableItem item = openRequestsAndSeshesAdapter.getItem(i);
            if (item.tag.equals(containerState.tag)) {
                updateSelectedItem(item);
                return;
            }
        }
    }

    public class SideMenuAdapter extends ArrayAdapter<SideMenuItem> {

        public SideMenuAdapter(Context context) {
            super(context, 0);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.side_menu_list_row,
                        null);
            }
            ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
            icon.setImageResource(getItem(position).icon);
            TextView title = (TextView) convertView.findViewById(R.id.row_title);
            title.setText(getItem(position).title);

            if (getItem(position).isSelected()) {
                selectLabel(title);
            } else {
                deselectLabel(title);
            }

            convertView.setOnTouchListener(selectedStateTouchListener(getItem(position), new Runnable() {
                @Override
                public void run() {
                    containerStateManager.setContainerStateForNavigationIndex(position);
                    containerStateManager.closeDrawerWithDelay(1000);
                }
            }));

            return convertView;
        }
    }

    private View.OnTouchListener selectedStateTouchListener(final SelectableItem item, Runnable touchEventUpBlock) {
        final Runnable completionBlock = touchEventUpBlock;

        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                synchronized (TAG) {
                    if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        selectedStateTouchDown(item);
                    } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP
                            || motionEvent.getActionMasked() == MotionEvent.ACTION_MOVE) {
                        selectedStateTouchUp(item, completionBlock);
                    }
                }
                return true;
            }
        };
    }

    private synchronized void selectedStateTouchDown(SelectableItem item) {
        if (itemSelectionInProgress) return;
        itemSelectionInProgress = true;
        item.selectionInProgress = true;

        final View view = item.getView();
        view.setPivotX(getResources().getDimensionPixelSize(R.dimen.side_menu_scale_pivot_x));

        selectedStateSpring.removeAllListeners();
        selectedStateSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                view.setScaleX((float) spring.getCurrentValue());
                view.setScaleY((float) spring.getCurrentValue());
            }
        });

        selectedStateSpring.setCurrentValue(1);
        selectedStateSpring.setEndValue(0.9);
    }

    private synchronized void selectedStateTouchUp(SelectableItem selectableItem, final Runnable completionBlock) {
        if (!itemSelectionInProgress || !selectableItem.selectionInProgress)  return;

        final View view = selectableItem.getView();

        selectedStateSpring.removeAllListeners();
        selectedStateSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                view.setScaleX((float) spring.getCurrentValue());
                view.setScaleY((float) spring.getCurrentValue());
            }

            @Override
            public void onSpringAtRest(Spring spring) {
                completionBlock.run();
            }
        });

        if (view.getScaleX() == 1) {
            completionBlock.run();
        } else {
            selectedStateSpring.setCurrentValue(view.getScaleX());
            selectedStateSpring.setEndValue(1);
        }

        if (currentSelectedItem != null) {
            currentSelectedItem.setSelected(false);
        }

        selectableItem.setSelected(true);
        selectableItem.selectionInProgress = false;
    }

    private class RequestsAndSeshesListItem extends SelectableItem {
        public boolean isSesh;
        public boolean isDivider;
        public LearnRequest learnRequest;
        public Sesh sesh;
        public String dividerText;
        public int position;

        public RequestsAndSeshesListItem(boolean isSesh, boolean isDivider, LearnRequest learnRequest, Sesh sesh,
                             String dividerText, int position, String tag) {
            super(tag);

            this.isSesh = isSesh;
            this.isDivider = isDivider;
            this.learnRequest = learnRequest;
            this.sesh = sesh;
            this.dividerText = dividerText;
            this.position = position;
        }

        public View getView() {
            return openRequestsAndSeshesMenu.getChildAt(position);
        }

        public TextView getLabel() {
            View view = getView();
            if (isSesh) {
                return (TextView) view.findViewById(R.id.open_sesh_list_row_class);
            } else if (isDivider) {
                return null;
            } else {
                return (TextView) view.findViewById(R.id.open_request_list_row_class);
            }
        }
    }

    public class RequestsAndSeshesAdapter extends ArrayAdapter<RequestsAndSeshesListItem> {
        public RequestsAndSeshesAdapter(Context context) { super(context, 0); }

        public View getView(final int position, View convertView, ViewGroup parent) {
            Log.d(TAG, "refreshing position " + position);
            final RequestsAndSeshesListItem item = getItem(position);
            LayoutUtils utils = new LayoutUtils(getContext());
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
                if (convertView == null || isOpenRequestRow(convertView) || isDividerRow(convertView)) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.open_sesh_list_row,
                            null);
                }

                TextView classAbbrvTextView = (TextView) convertView.findViewById(R.id.open_sesh_list_row_class);
                classAbbrvTextView.setText(item.sesh.className);

                TextView timeAbbrvTextView = (TextView) convertView.findViewById(R.id.open_sesh_list_row_time);
                timeAbbrvTextView.setText(item.sesh.getTimeAbbrvString());
                timeAbbrvTextView.setTypeface(utils.getLightGothamTypeface());

                CircleImageView profileImage = (CircleImageView) convertView.findViewById(R.id.profile_image);
                item.sesh.loadImageAsync(profileImage, getActivity());

                if (!item.sesh.isStudent) {
                    ImageView icon = (ImageView)convertView.findViewById(R.id.open_sesh_list_row_status_icon);
                    int drawableId;

                    if (item.sesh.seshSetTime == -1 && !item.sesh.isInstant) {
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

                RelativeLayout alertBadge = (RelativeLayout) convertView.findViewById(R.id.alert_badge);
                int unreadMessages = Message.getUnreadMessagesCountForSesh(item.sesh);

                if (unreadMessages > 0) {
                    alertBadge.setVisibility(View.VISIBLE);

                    TextView alertBadgeNumber = (TextView) convertView.findViewById(R.id.alert_badge_number);
                    if (unreadMessages < 10) {
                        alertBadgeNumber.setText(Integer.toString(unreadMessages));
                    } else {
                        alertBadgeNumber.setText("+");
                    }
                    alertBadgeNumber.setTypeface(utils.getBookGothamTypeface());
                } else {
                    if (alertBadge.getVisibility() == View.VISIBLE) {
                        alertBadge.setVisibility(View.GONE);
                    }
                }

                convertView.setOnTouchListener(
                        selectedStateTouchListener(item, new Runnable() {
                            @Override
                            public void run() {
                                containerStateManager.setContainerStateForSesh(item.sesh);
                                containerStateManager.closeDrawerWithDelay(1000);
                            }
                        }));

                if (item.sesh.requiresAnimatedDisplay) {
                    currentSelectedItem = null;
                    sideMenuOpenAnimation = new SeshDisplayAnimation(mainContainerActivity, item.sesh,
                            convertView);
                }
            } else {
                // if convertView is not instantiated, or is of the wrong type, we re-instantiate
                if (convertView == null || isOpenSeshRow(convertView) || isDividerRow(convertView)) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.open_request_list_row,
                            null);
                }

                TextView classAbbrvTextView = (TextView) convertView.findViewById(R.id.open_request_list_row_class);
                classAbbrvTextView.setText(item.learnRequest.classString);

                convertView.setOnTouchListener(
                        selectedStateTouchListener(item, new Runnable() {
                            @Override
                            public void run() {
                                containerStateManager.setContainerStateForLearnRequest(item.learnRequest);
                                containerStateManager.closeDrawerWithDelay(1000);
                            }
                        }));

                if (item.learnRequest.requiresAnimatedDisplay) {
                    sideMenuOpenAnimation = new LearnRequestDisplayAnimation(mainContainerActivity,
                            item.learnRequest, convertView);
                }
            }

            if (currentSelectedItem != null && currentSelectedItem.tag.equals(item.tag)) {
                currentSelectedItem = item;
                currentSelectedItem.setSelected(true);
            }

            return convertView;
        }
    }

    private boolean isDividerRow(View view) {
        return (view.findViewById(R.id.learn_divider) != null);
    }

    private boolean isOpenRequestRow(View view) {
        return (view.findViewById(R.id.open_request_list_row_class) != null);
    }

    private boolean isOpenSeshRow(View view) {
        return (view.findViewById(R.id.open_sesh_list_row_class) != null);
    }

    public void selectLabel(TextView label) {
        LayoutUtils utils = new LayoutUtils(getActivity());
        label.setTypeface(utils.getMediumGothamTypeface());
    }

    public void deselectLabel(TextView label) {
        LayoutUtils utils = new LayoutUtils(getActivity());
        label.setTypeface(utils.getLightGothamTypeface());
    }

    public void onOpen() {
        Log.d(TAG, "ON OPEN");
        if (sideMenuOpenAnimation != null) {
            sideMenuOpenAnimation.prepareAnimation();
        }
    }

    public void onOpened() {
        Log.d(TAG, "ON OPENED");
        if (sideMenuOpenAnimation != null) {
            sideMenuOpenAnimation.startAnimation();
        }
        sideMenuOpenAnimation = null;
    }


    public class UpdateRequestAndSeshListTask extends AsyncTask<Void, Void, Void> {
        private List<Sesh> studentSeshes;
        private List<Sesh> tutorSeshes;
        private List<LearnRequest> learnRequests;

        protected Void doInBackground(Void... voids){
            if (Sesh.listAll(Sesh.class).size() > 0) {
                studentSeshes = Sesh.find(Sesh.class, "is_student = ?", Integer.toString(1));
                tutorSeshes = Sesh.find(Sesh.class, "is_student = ?", Integer.toString(0));
            } else {
                studentSeshes = new ArrayList<Sesh>();
                tutorSeshes = new ArrayList<Sesh>();
            }

            learnRequests = LearnRequest.listAll(LearnRequest.class);
            return null;

        }

        protected void onPostExecute(Void result) {
            Log.d(TAG, "UpdateRequestAndSeshListTask on Main thread.");
            openRequestsAndSeshesAdapter.clear();

            int position = 0;

            if (tutorSeshes.size() > 0) {
                RequestsAndSeshesListItem teachDivider
                        = new RequestsAndSeshesListItem(false, true, null, null, "TEACH",
                        position++, "divider");
                openRequestsAndSeshesAdapter.add(teachDivider);
            }

            for (Sesh sesh : tutorSeshes) {
                RequestsAndSeshesListItem seshItem =
                        new RequestsAndSeshesListItem(true, false, null, sesh, null,
                                position++, sesh.getContainerStateTag());
                openRequestsAndSeshesAdapter.add(seshItem);
            }

            if (studentSeshes.size() + learnRequests.size() > 0) {
                RequestsAndSeshesListItem learnDivider
                        = new RequestsAndSeshesListItem(false, true, null, null, "LEARN",
                        position++, "divider");
                openRequestsAndSeshesAdapter.add(learnDivider);
            }

            for (Sesh sesh : studentSeshes) {
                RequestsAndSeshesListItem seshItem =
                        new RequestsAndSeshesListItem(true, false, null, sesh, null,
                                position++, sesh.getContainerStateTag());
                openRequestsAndSeshesAdapter.add(seshItem);
            }

            for (LearnRequest learnRequest : learnRequests) {
                RequestsAndSeshesListItem requestItem =
                        new RequestsAndSeshesListItem(false, false, learnRequest, null, null,
                                position++, learnRequest.getContainerStateTag());
                openRequestsAndSeshesAdapter.add(requestItem);
            }
            Log.d(TAG, "updateLearnList() end " + new Date().toString());

            openRequestsAndSeshesAdapter.notifyDataSetChanged();

            if (containerStateManager.getMainContainerState().fragment instanceof ViewSeshFragment) {
                ViewSeshFragment viewSeshFragment = (ViewSeshFragment) containerStateManager.getMainContainerState().fragment;
                viewSeshFragment.refresh();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        broadcastReceiver = actionBroadcastReceiver;
        // Listen for new messages
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MessagingActivity.REFRESH_MESSAGES);
        (new UpdateRequestAndSeshListTask()).execute();
        this.getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.getActivity().unregisterReceiver(broadcastReceiver);
    }


    private BroadcastReceiver actionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            (new UpdateRequestAndSeshListTask()).execute();
        }
    };

    public static abstract class SideMenuOpenAnimation {
        public abstract void prepareAnimation();
        public abstract void startAnimation();
        public abstract void onAnimationCompleted();
    }

    @Override
    public void tableUpdated() {
        Log.d(TAG, "TABLE UPDATED HAS BEEN CALLED");
        (new UpdateRequestAndSeshListTask()).execute();
    }
}
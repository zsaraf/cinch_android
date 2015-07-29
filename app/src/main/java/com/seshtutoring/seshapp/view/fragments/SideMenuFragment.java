package com.seshtutoring.seshapp.view.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.seshtutoring.seshapp.view.MainContainerActivity;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.HomeFragment;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.PaymentFragment;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.ProfileFragment;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.PromoteFragment;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.SettingsFragment;

import java.util.Iterator;

public class SideMenuFragment extends Fragment implements SlidingMenu.OnOpenedListener {
    public static enum MenuOption {
        HOME("Home", R.drawable.home, new HomeFragment()),
        PROFILE("Profile", R.drawable.profile, new ProfileFragment()),
        PAYMENT("Payment", R.drawable.payment, new PaymentFragment()),
        SETTINGS("Settings", R.drawable.settings, new SettingsFragment()),
        PROMOTE("Promote", R.drawable.share, new PromoteFragment());

        public String title;
        public int iconRes;
        public Fragment fragment;

        MenuOption(String title, int iconRes, Fragment fragment) {
            this.title = title;
            this.iconRes = iconRes;
            this.fragment = fragment;
        }
    }

    public static final String MAIN_WRAPPER_STATE_KEY = "main_wrapper_state";
    public static final String MENU_OPEN_DISPLAY_NEW_REQUEST = "display_new_request";

    private MainContainerActivity mainContainerActivity;
    private SideMenuItem selectedItem;
    private ListView menu;
    private ListView openRequestsList;
    private View dividerAboveOpenRequests;
    private OpenRequestsListAdapter openRequestsListAdapter;
    private String menuOpenFlag;

    private TextView[] menuOptionTitles;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.side_menu_fragment, container, false);
        menu = (ListView) view.findViewById(R.id.side_menu_list);
        openRequestsList = (ListView) view.findViewById(R.id.open_requests_list);
        dividerAboveOpenRequests = (View) view.findViewById(R.id.divider_above_requests);
        mainContainerActivity = (MainContainerActivity) getActivity();
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final MenuOption initialSelectedItem = mainContainerActivity.getCurrentState();

        menuOptionTitles = new TextView[MenuOption.values().length];

        final SideMenuAdapter sideMenuAdapter = new SideMenuAdapter(getActivity());
        for (MenuOption menuOption : MenuOption.values()) {
            sideMenuAdapter.add(new SideMenuItem(menuOption.title, menuOption.iconRes,
                    (initialSelectedItem == menuOption) ? true : false));
        }

        menu.setAdapter(sideMenuAdapter);

        menu.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                selectedItem.isSelected = false;
                selectedItem = sideMenuAdapter.getItem(position);
                selectedItem.isSelected = true;

                switch (position) {
                    case 0:
                        mainContainerActivity.setCurrentState(MenuOption.HOME);
                        break;
                    case 1:
                        mainContainerActivity.setCurrentState(MenuOption.PROFILE);
                        break;
                    case 2:
                        mainContainerActivity.setCurrentState(MenuOption.PAYMENT);
                        break;
                    case 3:
                        mainContainerActivity.setCurrentState(MenuOption.SETTINGS);
                        break;
                    case 4:
                        mainContainerActivity.setCurrentState(MenuOption.PROMOTE);
                        break;
                    default:
                        mainContainerActivity.setCurrentState(MenuOption.HOME);
                        break;
                }

                sideMenuAdapter.notifyDataSetChanged();
                mainContainerActivity.closeDrawer();
            }
        });

        openRequestsListAdapter = new OpenRequestsListAdapter(getActivity());
        openRequestsList.setAdapter(openRequestsListAdapter);

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

    public class OpenRequestsListAdapter extends ArrayAdapter<LearnRequest> {
        public OpenRequestsListAdapter(Context context) { super(context, 0); }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.open_request_list_row,
                        null);
            }

            TextView classAbbrvTextView = (TextView) convertView.findViewById(R.id.open_request_list_row_class);
            classAbbrvTextView.setText(getItem(position).classString);

            // if view represents the newest request and side menu was opened in context of a new
            // request being created, animate row in to emphasize it to user.
            if (menuOpenFlag == MENU_OPEN_DISPLAY_NEW_REQUEST && position == getCount() - 1) {
                Animation newItemAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_and_slide_in);
                convertView.startAnimation(newItemAnimation);
                menuOpenFlag = null;
            }
            return convertView;
        }
    }

    public void selectItem(TextView title) {
        Typeface bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Bold.otf");
        title.setTypeface(bold);
    }

    public void deselectItem(TextView title) {
        Typeface light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Light.otf");
        title.setTypeface(light);
    }

    public OpenRequestsListAdapter getOpenRequestsAdapter() {
        return openRequestsListAdapter;
    }

    public void setStatusFlag(String flag) {
        this.menuOpenFlag = flag;
    }


    @Override
    public void onOpened() {
        if (menuOpenFlag == MENU_OPEN_DISPLAY_NEW_REQUEST) {
            updateLearnRequestList();
        }
    }

    private void updateLearnRequestList() {
        Iterator<LearnRequest> learnRequests = LearnRequest.findAll(LearnRequest.class);

        openRequestsListAdapter.clear();
        while (learnRequests.hasNext()) {
            openRequestsListAdapter.add(learnRequests.next());
        }
        if (openRequestsListAdapter.getCount() > 0 && dividerAboveOpenRequests.getAlpha() == 0f) {
            dividerAboveOpenRequests.animate().alpha(1f).setDuration(300).setStartDelay(500);
        } else if (openRequestsListAdapter.getCount() == 0 && dividerAboveOpenRequests.getAlpha() == 1f) {
            dividerAboveOpenRequests.setAlpha(0f);
        }

        openRequestsListAdapter.notifyDataSetChanged();
    }
}
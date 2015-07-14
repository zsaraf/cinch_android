package com.seshtutoring.seshapp.view;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.view.MainContainerActivity.MenuOption;

public class SideMenuFragment extends ListFragment {
    public static final String MAIN_WRAPPER_STATE_KEY = "main_wrapper_state";
    private MainContainerActivity mainContainerActivity;
    private TextView selectedTextView;
    private ListView menu;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        menu = (ListView) inflater.inflate(R.layout.side_menu_list, null);
        mainContainerActivity = (MainContainerActivity) getActivity();
        return menu;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MenuOption selectedItem = mainContainerActivity.getCurrentState();

        SideMenuAdapter adapter = new SideMenuAdapter(getActivity());
        adapter.add(new SideMenuItem("Home", R.drawable.home,
                (selectedItem == MenuOption.HOME) ? true : false));
        adapter.add(new SideMenuItem("Profile", R.drawable.profile,
                (selectedItem == MenuOption.PROFILE) ? true : false));
        adapter.add(new SideMenuItem("Payment", R.drawable.payment,
                (selectedItem == MenuOption.PAYMENT) ? true : false));
        adapter.add(new SideMenuItem("Settings", R.drawable.settings,
                (selectedItem == MenuOption.SETTINGS) ? true : false));
        adapter.add(new SideMenuItem("Promote", R.drawable.share,
                (selectedItem == MenuOption.PROMOTE) ? true : false));
        setListAdapter(adapter);

        menu.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                TextView newSelectedTextView = (TextView) view.findViewById(R.id.row_title);
                selectItem(newSelectedTextView);
                deselectItem(selectedTextView);
                selectedTextView = newSelectedTextView;

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

                mainContainerActivity.closeDrawer();
            }
        });
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
                selectedTextView = title;
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
}
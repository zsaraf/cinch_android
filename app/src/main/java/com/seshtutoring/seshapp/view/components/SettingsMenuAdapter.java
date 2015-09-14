package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.SettingsFragment;

import java.util.List;

/**
 * Created by franzwarning on 9/11/15.
 */
public class SettingsMenuAdapter extends BaseAdapter {

    private static final String TAG = PaymentMenuAdapter.class.getName();

    public List<SettingsMenuItem> menuItems;
    Context context;
    private static LayoutInflater inflater = null;
    private Typeface tfLight;
    private Typeface tfBook;

    public SettingsFragment delegate;

    public SettingsMenuAdapter(Context context, List<SettingsMenuItem> menuItems, SettingsFragment delegate) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.menuItems = menuItems;
        this.delegate = delegate;

        LayoutUtils layUtils = new LayoutUtils(context);

        this.tfLight = layUtils.getLightGothamTypeface();
        this.tfBook = layUtils.getBookGothamTypeface();

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return menuItems.size();
    }

    @Override
    public SettingsMenuItem getItem(int position) {
        // TODO Auto-generated method stub
        return this.menuItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return Long.valueOf(position);
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        return this.menuItems.get(position).type;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        SettingsMenuItem item = getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.settings_menu_list_row, null);

            viewHolder = new ViewHolder();

            int rightTextID = R.id.settings_right_title;
            viewHolder.secondTextView = (TextView) convertView.findViewById(rightTextID);
            viewHolder.divider = (View)convertView.findViewById(R.id.divider);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (item.type == SettingsMenuItem.HEADER_TYPE) {
            viewHolder.mainTextView = (TextView) convertView.findViewById(R.id.settings_header_title);
            viewHolder.mainTextView.setBackgroundResource(R.drawable.settings_header_item);
            viewHolder.mainTextView.setTypeface(this.tfBook);
            viewHolder.divider.setVisibility(View.VISIBLE);
        } else if (item.type == SettingsMenuItem.EXPLAIN_TYPE) {
            viewHolder.mainTextView = (TextView) convertView.findViewById(R.id.settings_explain_title);
            viewHolder.mainTextView.setBackgroundResource(R.drawable.settings_explain_item);
            viewHolder.mainTextView.setTypeface(this.tfLight);
            viewHolder.divider.setVisibility(View.GONE);

        }  else {
            viewHolder.mainTextView = (TextView) convertView.findViewById(R.id.settings_row_title);
            viewHolder.mainTextView.setBackgroundResource(R.drawable.settings_row_item);
            viewHolder.mainTextView.setTypeface(this.tfLight);
            viewHolder.divider.setVisibility(View.VISIBLE);
        }


        viewHolder.mainTextView.setText(item.text);
        viewHolder.secondTextView.setText(item.rightText);

        return convertView;
    }

    private class ViewHolder {

        public TextView mainTextView;
        public TextView secondTextView;
        public View divider;

    }

}

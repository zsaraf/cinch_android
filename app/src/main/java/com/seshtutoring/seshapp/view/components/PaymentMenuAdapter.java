package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Card;
import com.seshtutoring.seshapp.model.Message;
import com.seshtutoring.seshapp.util.LayoutUtils;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.PaymentFragment;
import com.seshtutoring.seshapp.view.fragments.MainContainerFragments.SettingsFragment;

import java.util.List;

public class PaymentMenuAdapter extends BaseAdapter {

    private static final String TAG = PaymentMenuAdapter.class.getName();

    public List<PaymentListItem> paymentItems;
    Context context;
    private static LayoutInflater inflater = null;
    private Typeface tfLight;
    private Typeface tfBook;

    public boolean editMode;
    public PaymentFragment delegate;

    public PaymentMenuAdapter(Context context, List<PaymentListItem> paymentItems, PaymentFragment delegate, boolean editMode) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.paymentItems = paymentItems;
        this.delegate = delegate;

        LayoutUtils layUtils = new LayoutUtils(context);

        this.tfLight = layUtils.getLightGothamTypeface();
        this.tfBook = layUtils.getBookGothamTypeface();
        this.editMode = editMode;

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return paymentItems.size();
    }

    @Override
    public PaymentListItem getItem(int position) {
        // TODO Auto-generated method stub
        return this.paymentItems.get(position);
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
        return this.paymentItems.get(position).type;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        PaymentListItem item = getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.settings_menu_list_row, null);

            viewHolder = new ViewHolder();

            int rightTextID = R.id.settings_right_title;
            viewHolder.secondTextView = (TextView) convertView.findViewById(rightTextID);
            viewHolder.divider = (View)convertView.findViewById(R.id.divider);
            viewHolder.deleteButton = (RelativeLayout)convertView.findViewById(R.id.delete_button);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (item.type == PaymentListItem.HEADER_TYPE) {
            viewHolder.mainTextView = (TextView) convertView.findViewById(R.id.settings_header_title);
            viewHolder.mainTextView.setBackgroundResource(R.drawable.settings_header_item);
            viewHolder.mainTextView.setTypeface(this.tfBook);
        } else {
            viewHolder.mainTextView = (TextView) convertView.findViewById(R.id.settings_row_title);
            viewHolder.mainTextView.setBackgroundResource(R.drawable.settings_row_item);
            viewHolder.mainTextView.setTypeface(this.tfLight);
        }


        if (item.card == null) {
            viewHolder.mainTextView.setText(item.text);
            viewHolder.secondTextView.setText("");
        } else {
            viewHolder.mainTextView.setText(item.card.type + " " + item.card.lastFour);
            viewHolder.secondTextView.setText(item.card.isDefault ? "default" : "");
        }

        if (editMode) {
            if (item.type == PaymentListItem.CARD_TYPE && !item.card.isDefault) {
                viewHolder.secondTextView.setVisibility(View.GONE);
                viewHolder.deleteButton.setVisibility(View.VISIBLE);
                final Card deleteCard = item.card;
                viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        delegate.deleteCard(deleteCard);

                    }
                });
            } else {
                viewHolder.secondTextView.setVisibility(View.VISIBLE);
                viewHolder.deleteButton.setVisibility(View.GONE);
                viewHolder.deleteButton.setOnClickListener(null);
            }
        } else {
            viewHolder.secondTextView.setVisibility(View.VISIBLE);
            viewHolder.deleteButton.setVisibility(View.GONE);
        }

        return convertView;
    }

    private class ViewHolder {

        public TextView mainTextView;
        public TextView secondTextView;
        public View divider;
        public RelativeLayout deleteButton;

    }

}

package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.model.Message;
import com.seshtutoring.seshapp.util.LayoutUtils;

import java.util.List;

/**
 * Created by franzwarning on 9/2/15.
 */
public class MessageAdapter extends BaseAdapter {

    Context context;
    public List<Message> messages;
    private static LayoutInflater inflater = null;
    private Typeface tf;

    public MessageAdapter(Context context, List<Message> messages) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.messages = messages;
        LayoutUtils layUtils = new LayoutUtils(context);

        this.tf = layUtils.getLightGothamTypeface();

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return messages.size();
    }

    @Override
    public Message getItem(int position) {
        // TODO Auto-generated method stub
        return this.messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return this.getItem(position).messageId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.message_row, null);

        Message currentMessage = this.getItem(position);

        TextView leftText = (TextView) vi.findViewById(R.id.left_text);
        TextView rightText =  (TextView)vi.findViewById(R.id.right_text);

        leftText.setTypeface(this.tf);
        rightText.setTypeface(this.tf);

        if (currentMessage.fromYou) {
            leftText.setText("");
            rightText.setText(currentMessage.content.trim());
            if (rightText.getLineCount() > 1) {
                rightText.setGravity(Gravity.LEFT);
            } else {
                rightText.setGravity(Gravity.RIGHT);
            }
        } else {
            rightText.setText("");
            leftText.setText(currentMessage.content.trim());
        }

        return vi;
    }
}



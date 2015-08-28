package com.seshtutoring.seshapp.view.components;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.seshtutoring.seshapp.R;
import com.seshtutoring.seshapp.util.DateUtils;

import net.simonvt.numberpicker.NumberPicker;

import org.joda.time.DateTime;

public class SeshDatePicker extends RelativeLayout {
    public OnDateChangeListener onDateChangeListener;
    public DateTime currentDateTime;
    private Button nextButton;
    NumberPicker hoursPicker, minutesPicker, dayPicker, suffixPicker;

    private final String[] hourValues = new String[] {"12", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
    private final String[] minuteValues = new String[] {"0", "15", "30", "45"};
    private final String[] suffixValues = new String[] {"a", "p"};

    public interface OnDateChangeListener {
        public void onDateChanged(DateTime dateTime);
    }

    public SeshDatePicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = mInflater.inflate(R.layout.fragment_sesh_date_picker, this, true);

        this.nextButton = (Button) v.findViewById(R.id.next_button);

        this.dayPicker = (NumberPicker) v.findViewById(R.id.day_number_picker);
        this.hoursPicker = (NumberPicker) v.findViewById(R.id.hour_number_picker);
        this.minutesPicker = (NumberPicker) v.findViewById(R.id.minutes_number_picker);
        this.suffixPicker = (NumberPicker) v.findViewById(R.id.time_suffix_number_picker);

        String[] dayValues = new String[7];
        DateTime dateTime = DateTime.now();
        for (int i = 0; i < 7; i++) {
            dayValues[i] = DateUtils.getSeshFormattedDayString(dateTime.plusDays(i));
        }

        dayPicker.setMinValue(0);
        dayPicker.setMaxValue(dayValues.length - 1);
        dayPicker.setFocusable(true);
        dayPicker.setFocusableInTouchMode(true);
        dayPicker.setDisplayedValues(dayValues);
        dayPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateTextField();
            }
        });

        hoursPicker.setMinValue(0);
        hoursPicker.setMaxValue(hourValues.length - 1);
        hoursPicker.setFocusable(true);
        hoursPicker.setFocusableInTouchMode(true);
        hoursPicker.setDisplayedValues(hourValues);
        hoursPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateTextField();
            }
        });

        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(minuteValues.length - 1);
        minutesPicker.setFocusable(true);
        minutesPicker.setFocusableInTouchMode(true);
        minutesPicker.setDisplayedValues(minuteValues);
        minutesPicker.setValue(0);
        minutesPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateTextField();
            }
        });

        suffixPicker.setMinValue(0);
        suffixPicker.setMaxValue(suffixValues.length - 1);
        suffixPicker.setValue(1);
        suffixPicker.setFocusable(true);
        suffixPicker.setFocusableInTouchMode(true);
        suffixPicker.setDisplayedValues(suffixValues);
        suffixPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateTextField();
            }
        });

        updateTextField();
    }

    public void setOnDateChangedListener(OnDateChangeListener listener) {
        this.onDateChangeListener = listener;
    }

    public void setNextButtonOnClickListener(View.OnClickListener onClickListener) {
        nextButton.setOnClickListener(onClickListener);
    }

    public void updateTextField()
    {
        int day = dayPicker.getValue();
        int hour = hoursPicker.getValue();
        int minute = Integer.valueOf(minuteValues[minutesPicker.getValue()]);
        int suffix = suffixPicker.getValue();
        if (suffix == 1) {
            hour += 12;
        }
        currentDateTime = DateTime.now().plusDays(day).withTime(hour, minute, 0, 0);

        if (onDateChangeListener != null) {
            onDateChangeListener.onDateChanged(currentDateTime);
        }
    }

}

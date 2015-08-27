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
    private Button nextButton;
    NumberPicker hoursPicker, minutesPicker, dayPicker, suffixPicker;
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

//        this.hourValue = 0;
//        this.minuteValue = 30;
        String[] dayValues = new String[7];
        DateTime dateTime = DateTime.now();
        for (int i = 0; i < 7; i++) {
            dayValues[i] = DateUtils.getSeshFormattedDayString(dateTime.plusDays(i));
        }

        final String[] hourValues = new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        final String[] minuteValues = new String[] {"0", "15", "30", "45"};
        final String[] suffixValues = new String[] {"a", "p"};

        dayPicker.setMinValue(0);
        dayPicker.setMaxValue(dayValues.length - 1);
        dayPicker.setFocusable(true);
        dayPicker.setFocusableInTouchMode(true);
        dayPicker.setDisplayedValues(dayValues);
        dayPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
//                hourValue = Integer.valueOf(hoursPicker.getDisplayedValues()[newVal]);
//                minuteValue = Integer.valueOf(minutesPicker.getDisplayedValues()[minutesPicker.getValue()]);
//                onDurationChangedListener.onDurationChanged(hourValue, minuteValue);
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
//                hourValue = Integer.valueOf(hoursPicker.getDisplayedValues()[newVal]);
//                minuteValue = Integer.valueOf(minutesPicker.getDisplayedValues()[minutesPicker.getValue()]);
//                onDurationChangedListener.onDurationChanged(hourValue, minuteValue);
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
//                minuteValue = Integer.valueOf(minutesPicker.getDisplayedValues()[newVal]);
//                onDurationChangedListener.onDurationChanged(hourValue, minuteValue);
            }
        });

        suffixPicker.setMinValue(0);
        suffixPicker.setMaxValue(suffixValues.length - 1);
        suffixPicker.setFocusable(true);
        suffixPicker.setFocusableInTouchMode(true);
        suffixPicker.setDisplayedValues(suffixValues);
        suffixPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
//                hourValue = Integer.valueOf(hoursPicker.getDisplayedValues()[newVal]);
//                minuteValue = Integer.valueOf(minutesPicker.getDisplayedValues()[minutesPicker.getValue()]);
//                onDurationChangedListener.onDurationChanged(hourValue, minuteValue);
            }
        });
    }

    public void setOnDateChangedListener(OnDateChangeListener listener) {
        this.onDateChangeListener = listener;
    }

    public void setNextButtonOnClickListener(View.OnClickListener onClickListener) {
        nextButton.setOnClickListener(onClickListener);
    }

}

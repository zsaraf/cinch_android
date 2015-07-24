package com.seshtutoring.seshapp.view.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import net.simonvt.numberpicker.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.seshtutoring.seshapp.R;

/**
 * Created by nadavhollander on 7/23/15.
 */
public class SeshDurationPicker extends RelativeLayout {
    private OnDurationChangeListener onDurationChangedListener;
    private NumberPicker hoursPicker;
    private NumberPicker minutesPicker;
    private Button nextButton;
    private int hourValue;
    private int minuteValue;

    public interface OnDurationChangeListener {
        public void onDurationChanged(int hours, int minutes);
    }

    public SeshDurationPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = mInflater.inflate(R.layout.sesh_duration_picker, this, true);

        this.nextButton = (Button) v.findViewById(R.id.next_button);

        this.hoursPicker = (NumberPicker) v.findViewById(R.id.hour_number_picker);
        this.minutesPicker = (NumberPicker) v.findViewById(R.id.minutes_number_picker);

        this.hourValue = 0;
        this.minuteValue = 30;

        final String[] hourValues = new String[] {"0", "1", "2", "3"};
        final String[] minuteValues = new String[] {"0", "15", "30", "45"};
        final String[] minuteValuesForZeroHours = new String[] {"30", "45"};

        hoursPicker.setMinValue(0);
        hoursPicker.setMaxValue(hourValues.length - 1);
        hoursPicker.setFocusable(true);
        hoursPicker.setFocusableInTouchMode(true);
        hoursPicker.setDisplayedValues(hourValues);
        hoursPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (newVal == 0) {
                    minutesPicker.setValue(0);
                    minutesPicker.setDisplayedValues(minuteValuesForZeroHours);
                    minutesPicker.setMaxValue(minuteValuesForZeroHours.length - 1);
                } else if (oldVal == 0) {
                    minutesPicker.setDisplayedValues(minuteValues);
                    minutesPicker.setMaxValue(minuteValues.length - 1);
                }
                hourValue = Integer.valueOf(hoursPicker.getDisplayedValues()[newVal]);
                minuteValue = Integer.valueOf(minutesPicker.getDisplayedValues()[minutesPicker.getValue()]);
                onDurationChangedListener.onDurationChanged(hourValue, minuteValue);
            }
        });

        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(minuteValuesForZeroHours.length - 1);
        minutesPicker.setFocusable(true);
        minutesPicker.setFocusableInTouchMode(true);
        minutesPicker.setDisplayedValues(minuteValuesForZeroHours);
        minutesPicker.setValue(0);
        minutesPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                minuteValue = Integer.valueOf(minutesPicker.getDisplayedValues()[newVal]);
                onDurationChangedListener.onDurationChanged(hourValue, minuteValue);
            }
        });
    }


    public void setOnDurationChangedListener(OnDurationChangeListener listener) {
        this.onDurationChangedListener = listener;
    }

    public int getHourValue() {
        return hourValue;
    }

    public int getMinuteValue() {
        return minuteValue;
    }

    public void setNextButtonOnClickListener(View.OnClickListener onClickListener) {
        nextButton.setOnClickListener(onClickListener);
    }
}

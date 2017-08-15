package com.adherence.adherence;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * Created by sam on 8/14/17.
 */

public class SeekBarPreference extends DialogPreference implements OnSeekBarChangeListener {

    private static final String PREFERENCE_NS = "com.adherence.adherence.SeekBarPreference";
    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    private static final String ATTR_START_TIME = "startTime";
    private static final String ATTR_END_TIME = "endTime";
    private static final String ATTR_INTERVAL = "interval";


    private int startTimeHour;
    private int endTimeHour;
    private int minProgress;
    private int maxProgress;
    private int currentProgress;
    private int interval;

    private Boolean is24Hours = false;
//    private int progressTotal;
    private int progress_num_per_hour;

    private SeekBar seekBar;
    private TextView time;


    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);


//        startTimeHour = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_START_TIME, 0);
//        endTimeHour = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_END_TIME, 12);
//        interval = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_INTERVAL, 15);

        startTimeHour = typedArray.getInt(R.styleable.SeekBarPreference_startTime, 0);
        endTimeHour = typedArray.getInt(R.styleable.SeekBarPreference_endTime, 12);
        interval = typedArray.getInt(R.styleable.SeekBarPreference_interval, 15);

        is24Hours = DateFormat.is24HourFormat(getContext());
        progress_num_per_hour = 60 / interval;
        maxProgress = endTimeHour * progress_num_per_hour - 1;
        minProgress = startTimeHour * progress_num_per_hour;

        currentProgress = PreferenceManager.getDefaultSharedPreferences(context).getInt(getKey(), (maxProgress + minProgress) / 2);
        setSummary(timeTransfer(currentProgress, 0));

        setDialogTitle("Setting " + getDialogTitle());
        setPositiveButtonText("Done");
        setNegativeButtonText("Cancel");

        typedArray.recycle();
    }


    @Override
    protected View onCreateDialogView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rootView = inflater.inflate(R.layout.seekbar_preference, null);

        seekBar = (SeekBar) rootView.findViewById(R.id.pref_seekBar);
        time = (TextView) rootView.findViewById(R.id.pref_time);
        ((TextView) rootView.findViewById(R.id.pref_startTime)).setText(timeTransfer(minProgress, 0));
        ((TextView) rootView.findViewById(R.id.pref_endTime)).setText(timeTransfer(maxProgress, 0));
        currentProgress = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(getKey(), (maxProgress + minProgress) / 2);
        seekBar.setMax(maxProgress - minProgress);
        seekBar.setProgress(currentProgress - minProgress);
        time.setText(timeTransfer(currentProgress, 0));

        seekBar.setOnSeekBarChangeListener(this);

        return rootView;
    }



    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        int persisted = seekBar.getProgress() + minProgress;

        if (positiveResult){
            if (callChangeListener(persisted)) {
                persistInt(persisted);
                notifyChanged();
                setSummary(timeTransfer(persisted, 0));
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        time.setText(timeTransfer(i + minProgress, 0));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private String timeTransfer(int progress, int minOffsets){
        int hour = progress / progress_num_per_hour;
        int minute = (progress % progress_num_per_hour) * interval + minOffsets;
        if (minute > 59){
            hour += minute / 60;
            minute = minute % 60;
        }
        if (minute < 0){
            hour += minute / 60 - 1;
            minute = minute % 60 + 60;
        }
        String minString = Integer.toString(minute);
        if (minute == 0){
            minString = "00";
        }
        if (is24Hours) {
            return (Integer.toString(hour) + ":" + minString);
        }else {
            if (hour < 12){
                return (Integer.toString(hour) + ":" + minString + " AM");
            }else {
                return (Integer.toString(hour - 12) + ":" + minString + " PM");
            }
        }
    }
}

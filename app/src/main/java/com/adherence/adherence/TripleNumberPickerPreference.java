package com.adherence.adherence;

/**
 * Created by sam on 8/1/17.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

public class TripleNumberPickerPreference extends DialogPreference{

    private static final String PREFERENCE_NS = "com.adherence.adherence.TripleNumberPickerPreference";
    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";


    private NumberPicker hours = null;
    private NumberPicker mins = null;
    private NumberPicker ampm = null;
    private Boolean is24Hours = false;

    private static final String ATTR_DEFAULT_TIME = "defaultTime";
    private static final String ATTR_START_TIME = "startTime";
    private static final String ATTR_END_TIME = "endTime";

    private String defaultPersistedString;
    private String startTime;
    private String endTime;
    private String currentTime;
    private int maxHour;
    private int minHour;


    View rootView;


    public TripleNumberPickerPreference(Context ctxt) {
        this(ctxt, null);
    }

    public TripleNumberPickerPreference(Context ctxt, AttributeSet attrs) {
        this(ctxt, attrs, 0);
    }

    public TripleNumberPickerPreference(Context ctxt, AttributeSet attrs, int defStyle) {
        super(ctxt, attrs, defStyle);
        startTime = attrs.getAttributeValue(PREFERENCE_NS, ATTR_START_TIME);
        endTime = attrs.getAttributeValue(PREFERENCE_NS, ATTR_END_TIME);
        defaultPersistedString = attrs.getAttributeValue(PREFERENCE_NS, ATTR_DEFAULT_TIME);
        currentTime = PreferenceManager.getDefaultSharedPreferences(ctxt).getString(getKey(), defaultPersistedString);

        setSummaryTime(currentTime);

        minHour = Integer.parseInt(startTime.split(":")[0]);
        maxHour = Integer.parseInt(endTime.split(":")[0]);

        is24Hours = DateFormat.is24HourFormat(getContext());
        setDialogTitle("Setting " + getDialogTitle());
        setPositiveButtonText("Done");
        setNegativeButtonText("Cancel");
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView textView = (TextView) view.findViewById(android.R.id.title);
        textView.setTextSize(15);// TODO: 8/2/17 Defined in styles or integers later
    }

    @Override
    protected View onCreateDialogView() {

//        currentTime = getPersistedString(defaultPersistedString);
        Log.d("oncreate persistent", getPersistedString(defaultPersistedString));

        String[] minNums = new String[60];
        String[] hourNums = new String[maxHour - minHour];

        for(int i = 0; i < minNums.length; i++) {
            minNums[i] = Integer.toString(i);
        }
        for (int i = 0; i < hourNums.length; i++) {
            hourNums[i] = Integer.toString(i + minHour);
        }
//        Log.d("hourNums", hourNums[0]);

        LayoutInflater lI = LayoutInflater.from(getContext());
        rootView = lI.inflate(R.layout.triple_number_picker, null);
        hours = (NumberPicker) rootView.findViewById(R.id.hours);
        mins = (NumberPicker) rootView.findViewById(R.id.minutes);
        ampm = (NumberPicker) rootView.findViewById(R.id.ampm);
        ampm.setVisibility(View.GONE);
        rootView.findViewById(R.id.textView3).setVisibility(View.GONE);

//        if (is24Hours) {
//            hours.setMaxValue(24);
//
//
//
//        } else {
//            hours.setMaxValue(12);
//
//            ampm.setMaxValue(1);
//            ampm.setMinValue(0);
//            ampm.setDisplayedValues(new String[] { "AM", "PM"} );
//
//        }
        hours.setMaxValue(maxHour - 1);
        Log.d("maxHour", String.valueOf(maxHour));
        Log.d("minHour", String.valueOf(minHour));
        Log.d("length", String.valueOf(hourNums.length));
        hours.setMinValue(minHour);
        hours.setDisplayedValues(hourNums);

        mins.setMaxValue(59);
        mins.setMinValue(0);
        mins.setDisplayedValues(minNums);

        deconstructPersistedData();

        return (rootView);
    }

    private void deconstructPersistedData() {
//        Log.d("persisted String", getPersistedString(defaultPersistedString));

        hours.setValue(Integer.parseInt(getPersistedString(defaultPersistedString).split(":")[0]));
        mins.setValue(Integer.parseInt(getPersistedString(defaultPersistedString).split(":")[1]));
        ampm.setValue(Integer.parseInt(getPersistedString(defaultPersistedString).split(":")[2]));
    }

    private String buildPersistedData() {
        StringBuilder sB = new StringBuilder();
        String isAMPM = "0";
        if (hours.getValue() < 10){
            sB.append('0');
        }
        if (hours.getValue() >= 12){
            isAMPM = "1";
        }
        sB.append(hours.getValue());
        sB.append(":");

        if (mins.getValue() < 10) {
            sB.append('0');
        }
        sB.append(mins.getValue());


        sB.append(":");

//        sB.append(ampm.getValue());
        sB.append(isAMPM);

//        long timeTillNextSecureLock = buildTimeTillSecureLock();
//        Log.d("TIME TILL NEXT LOCK SET", ""+timeTillNextSecureLock);
//        sB.append(timeTillNextSecureLock);
        return sB.toString();
    }

//    private long buildTimeTillSecureLock() {
//        double hoursSimple = hours.getValue();
//        double minsSimple = mins.getValue();
//        double secsSimple = ampm.getValue();
//
//        double secsToMillis = 1000;
//        double minsToMillis = 60000;
//        double hoursToMillis = 3600000;
//
//        double secsAsMills = secsSimple * secsToMillis;
//        double minsAsMills = minsSimple * minsToMillis;
//        double hoursAsMills = hoursSimple * hoursToMillis;
//
//        return (long) (secsAsMills + minsAsMills + hoursAsMills);
//    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        String persisted = buildPersistedData();

        if (positiveResult) {
            if (callChangeListener(persisted)) {
                persistString(persisted);
                notifyChanged();
                setSummaryTime(persisted);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return defaultPersistedString;
    }



    public void setSummaryTime(String persisted){
        if (is24Hours) {
            setSummary(persisted.substring(0, 5));
        } else {
            if (Character.getNumericValue(persisted.charAt(6)) == 0){
                setSummary(persisted.substring(0, 5) + " AM");
            } else {
                setSummary(persisted.substring(0, 5) + " PM");
            }
        }
    }

//    public static long subAndCheck(long a, long b) {
//        long ret;
//        String msg = "overflow: subtract";
//        if (b == Long.MIN_VALUE) {
//            if (a < 0) {
//                ret = a - b;
//            } else {
//                throw new ArithmeticException(msg);
//            }
//        } else {
//            ret = addAndCheck(a, -b, msg);
//        }
//        return ret;
//    }
//
//    private static long addAndCheck(long a, long b, String msg) {
//        long ret;
//        if (a > b) {
//            ret = addAndCheck(b, a, msg);
//        } else {
//            if (a < 0) {
//                if (b < 0) {
//                    if (Long.MIN_VALUE - b <= a) {
//                        ret = a + b;
//                    } else {
//                        throw new ArithmeticException(msg);
//                    }
//                } else {
//                    ret = a + b;
//                }
//            } else {
//                if (a <= Long.MAX_VALUE - b) {
//                    ret = a + b;
//                } else {
//                    throw new ArithmeticException(msg);
//                }
//            }
//        }
//        return ret;
//    }
}
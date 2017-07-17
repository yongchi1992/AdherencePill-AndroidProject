package com.adherence.adherence;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by sam on 7/10/17.
 */

public class SettingFragment extends Fragment {

    private View view;
    private Resources res;

    private SeekBar morningSeekBar;
    private SeekBar afternoonSeekBar;
    private SeekBar eveningSeekBar;
    private SeekBar bedtimeSeekBar;

    private SeekBar remindSeekBar;
    private SeekBar intervalSeekBar;
    private SeekBar maxTimesSeekBar;

    private TextView save;

    private TextView morning_time;
    private TextView afternoon_time;
    private TextView evening_time;
    private TextView bedtime_time;

    private Switch notification;
    private Switch vibration;
    private Switch sound;

    private TextView remind_time;
    private TextView interval_time;
    private TextView max_remind_time;


    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    private static final String ARG_SESSION_TOKEN="session_token";

    public static SettingFragment newInstance(String sessionToken, int sectionNumber) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(ARG_SESSION_TOKEN,sessionToken);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.setting_fragment, container, false);
        res = getResources();

        morningSeekBar = (SeekBar) view.findViewById(R.id.morning_seek);
        afternoonSeekBar = (SeekBar) view.findViewById(R.id.afternoon_seek);
        eveningSeekBar = (SeekBar) view.findViewById(R.id.evening_seek);
        bedtimeSeekBar = (SeekBar) view.findViewById(R.id.bedtime_seek);

        remindSeekBar = (SeekBar) view.findViewById(R.id.remind_seek);
        intervalSeekBar = (SeekBar) view.findViewById(R.id.interval_seek);
        maxTimesSeekBar = (SeekBar) view.findViewById(R.id.max_seek);

        morning_time = (TextView) view.findViewById(R.id.morning_time);
        afternoon_time = (TextView) view.findViewById(R.id.afternoon_time);
        evening_time = (TextView) view.findViewById(R.id.evening_time);
        bedtime_time = (TextView) view.findViewById(R.id.bedtime_time);

        remind_time = (TextView) view.findViewById(R.id.remind_time);
        interval_time = (TextView) view.findViewById(R.id.interval_time);
        max_remind_time = (TextView) view.findViewById(R.id.maxRemind_time);

        notification = (Switch) view.findViewById(R.id.notification_switch);
        vibration = (Switch) view.findViewById(R.id.vibration_switch);
        sound = (Switch) view.findViewById(R.id.sound_switch);

        save = (TextView) view.findViewById(R.id.save);

        final int progress_num_per_hour = res.getInteger(R.integer.progress_num_per_hour);
        final int settings_period_minutes_interval = res.getInteger(R.integer.settings_period_minutes_interval);
        final int settings_morning_start_hour = res.getInteger(R.integer.settings_morning_start_hour);
        final int settings_afternoon_start_hour = res.getInteger(R.integer.settings_afternoon_start_hour);
        final int setting_evening_start_hour = res.getInteger(R.integer.setting_evening_start_hour);

        final int settings_notifi_progress_num_per_hour = res.getInteger(R.integer.settings_notifi_progress_num_per_hour);
        final int settings_interval_minutes_interval = res.getInteger(R.integer.settings_interval_minutes_interval);
        final int settings_notifi_minutes_interval = res.getInteger(R.integer.settings_notifi_minutes_interval);

        initSetting();
        checkNotification();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences settings = view.getContext().getSharedPreferences(MainActivity.UserPREFERENCES, view.getContext().MODE_PRIVATE);

                settings.edit().putBoolean("notification", notification.isChecked()).apply();
                settings.edit().putBoolean("vibration", vibration.isChecked()).apply();
                settings.edit().putBoolean("sound", sound.isChecked()).apply();

                settings.edit().putString("morning_time", (String) morning_time.getText()).apply();
                settings.edit().putString("afternoon_time", (String) afternoon_time.getText()).apply();
                settings.edit().putString("evening_time", (String) evening_time.getText()).apply();
                settings.edit().putString("bedtime_time", (String) bedtime_time.getText()).apply();

                settings.edit().putInt("morning_progress", morningSeekBar.getProgress()).apply();
                settings.edit().putInt("afternoon_progress", afternoonSeekBar.getProgress()).apply();
                settings.edit().putInt("evening_progress", eveningSeekBar.getProgress()).apply();
                settings.edit().putInt("bedtime_progress", bedtimeSeekBar.getProgress()).apply();

                settings.edit().putInt("remind_progress", remindSeekBar.getProgress()).apply();
                settings.edit().putInt("interval_progress", intervalSeekBar.getProgress()).apply();
                settings.edit().putInt("maximum_progress", maxTimesSeekBar.getProgress()).apply();

                settings.edit().putString("remind_time", (String) remind_time.getText()).apply();
                settings.edit().putString("interval_time", (String) interval_time.getText()).apply();
                settings.edit().putString("max_remind_time", (String) max_remind_time.getText()).apply();

                Toast.makeText(view.getContext(), "Save Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        morningSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int hour = i / progress_num_per_hour;
                int minute = (i - hour * progress_num_per_hour) * settings_period_minutes_interval;
                String minString = Integer.toString(minute);

                if (minute == 0){
                    minString = "00";
                }
                morning_time.setText(Integer.toString(hour + settings_morning_start_hour) + ":" + minString);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        afternoonSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int hour = i / progress_num_per_hour;
                int minute = (i - hour * progress_num_per_hour) * settings_period_minutes_interval;
                String minString = Integer.toString(minute);

                if (minute == 0){
                    minString = "00";
                }
                afternoon_time.setText(Integer.toString(hour + settings_afternoon_start_hour) + ":" + minString);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        eveningSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int hour = i / progress_num_per_hour;
                int minute = (i - hour * progress_num_per_hour) * settings_period_minutes_interval;
                String minString = Integer.toString(minute);

                if (minute == 0){
                    minString = "00";
                }
                evening_time.setText(Integer.toString(hour + setting_evening_start_hour) + ":" + minString);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        bedtimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int hour = i / progress_num_per_hour;
                int minute = (i - hour * progress_num_per_hour) * settings_period_minutes_interval;
                String minString = Integer.toString(minute);

                if (minute == 0){
                    minString = "00";
                }
                bedtime_time.setText(Integer.toString(hour) + ":" + minString);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

//        notification.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
        notification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkNotification();
            }
        });

        remindSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int hour = i / settings_notifi_progress_num_per_hour;
                int min = i * settings_notifi_minutes_interval - hour * 60;
                String minString = Integer.toString(min);
                if (min == 0){
                    minString = "00";
                }
                if (hour == 0){
                    remind_time.setText(minString + "min");
                }else{
                    remind_time.setText(Integer.toString(hour) + "hr" + minString + "min");
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        intervalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i < 1){
                    seekBar.setProgress(1);//At least 5 mins
                }
                interval_time.setText(Integer.toString(seekBar.getProgress() * settings_interval_minutes_interval) + "min");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        maxTimesSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i < 1){
                    seekBar.setProgress(1);//At least 1 time
                }
                max_remind_time.setText(Integer.toString(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((NextActivity) context).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    public void initSetting(){
        SharedPreferences settings = view.getContext().getSharedPreferences(MainActivity.UserPREFERENCES, view.getContext().MODE_PRIVATE);

        morning_time.setText(settings.getString("morning_time", getString(R.string.settings_morning_time_init)));
        afternoon_time.setText(settings.getString("afternoon_time", getString(R.string.settings_afternoon_time_init)));
        evening_time.setText(settings.getString("evening_time", getString(R.string.settings_evening_time_init)));
        bedtime_time.setText(settings.getString("bedtime_time", getString(R.string.settings_bedtime_time_init)));

        morningSeekBar.setMax(res.getInteger(R.integer.settings_morning_progress_max));
        afternoonSeekBar.setMax(res.getInteger(R.integer.settings_afternoon_progress_max));
        eveningSeekBar.setMax(res.getInteger(R.integer.settings_evening_progress_max));
        bedtimeSeekBar.setMax(res.getInteger(R.integer.settings_bedtime_progress_max));

        morningSeekBar.setProgress(settings.getInt("morning_progress", res.getInteger(R.integer.settings_morning_progress_init)));
        afternoonSeekBar.setProgress(settings.getInt("afternoon_progress", res.getInteger(R.integer.settings_afternoon_progress_init)));
        eveningSeekBar.setProgress(settings.getInt("evening_progress", res.getInteger(R.integer.settings_evening_progress_init)));
        bedtimeSeekBar.setProgress(settings.getInt("bedtime_progress", res.getInteger(R.integer.settings_bedtime_progress_init)));

        notification.setChecked(settings.getBoolean("notification", true));
        vibration.setChecked(settings.getBoolean("vibration", true));
        sound.setChecked(settings.getBoolean("sound", true));

        remindSeekBar.setMax(res.getInteger(R.integer.setting_remind_progress_max));
        intervalSeekBar.setMax(res.getInteger(R.integer.settings_interval_progress_max));
        maxTimesSeekBar.setMax(res.getInteger(R.integer.settings_max_remind_progress_max));

        remindSeekBar.setProgress(settings.getInt("remind_progress", res.getInteger(R.integer.settings_remind_progress_init)));
        intervalSeekBar.setProgress(settings.getInt("interval_progress", res.getInteger(R.integer.settings_interval_progress_init)));
        maxTimesSeekBar.setProgress(settings.getInt("maximum_progress", res.getInteger(R.integer.settings_max_remind_progress_init)));

        remind_time.setText(settings.getString("remind_time", getString(R.string.settings_remind_time_init)));
        interval_time.setText(settings.getString("interval_time", getString(R.string.settings_interval_time_init)));
        max_remind_time.setText(settings.getString("max_remind_time",  getString(R.string.settings_max_remind_times_init)));
    }


    public void checkNotification(){
        boolean status = notification.isChecked();
        vibration.setEnabled(status);
        sound.setEnabled(status);
        remindSeekBar.setEnabled(status);
        intervalSeekBar.setEnabled(status);
        maxTimesSeekBar.setEnabled(status);
    }
}

package com.adherence.adherence;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;


import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button button = new Button(this);
        button.setText("Log Out");
        setListFooter(button);
        setupActionBar();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    private void setupActionBar() {
        Toolbar toolbar;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ViewGroup root = (ViewGroup) findViewById(android.R.id.list).getParent().getParent().getParent();
            toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.tool_bar, root, false);
            root.addView(toolbar, 0);
        } else {
            ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
            ListView content = (ListView) root.getChildAt(0);
            root.removeAllViews();
            toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.tool_bar, root, false);
            int height;
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
                height = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            } else {
                height = toolbar.getHeight();
            }
            content.setPadding(0, height, 0, 0);
            root.addView(content);
            root.addView(toolbar);
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || NotificationFragment.class.getName().equals(fragmentName);
    }

    public static class NotificationFragment extends PreferenceFragment {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            SwitchPreference notify_pref = (SwitchPreference) findPreference(getResources().getString(R.string.pref_notification_category_notifi_switch_key));
            boolean notifi_switch = notify_pref.isChecked();
            if (!notifi_switch) {
                initCheck(notifi_switch);
            }
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if ((getResources().getString(R.string.pref_notification_category_notifi_switch_key)).equals(preference.getKey())){
                SwitchPreference notify_pref = (SwitchPreference) findPreference(getResources().getString(R.string.pref_notification_category_notifi_switch_key));
                boolean notifi_switch = notify_pref.isChecked();
                initCheck(notifi_switch);
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public void onOptionsMenuClosed(Menu menu) {

            ListPreference pref_notifi_times = (ListPreference) findPreference("notifi_times_key");
            pref_notifi_times.setSummary(pref_notifi_times.getValue());

            super.onOptionsMenuClosed(menu);
        }

        public void initCheck(boolean notifi_switch) {
            SwitchPreference notify_pref = (SwitchPreference) findPreference(getString(R.string.pref_notification_category_notifi_switch_key));
            SwitchPreference vibrate_pref = (SwitchPreference) findPreference(getString(R.string.pref_notification_category_vibrate_switch_key));
            SwitchPreference sound_pref = (SwitchPreference) findPreference(getString(R.string.pref_notification_category_sound_switch_key));

            ListPreference pref_notifi_after = (ListPreference) findPreference(getString(R.string.pref_notification_category_after_key));
            ListPreference pref_notifi_interval = (ListPreference) findPreference(getString(R.string.pref_notification_category_interval_key));
            ListPreference pref_notifi_times = (ListPreference) findPreference(getString(R.string.pref_notification_category_times_key));


//            TripleNumberPickerPreference morning_pref = (TripleNumberPickerPreference) findPreference(getString(R.string.pref_notification_category_morning_key));
//            TripleNumberPickerPreference afternoon_pref = (TripleNumberPickerPreference) findPreference(getString(R.string.pref_notification_category_afternoon_key));
//            TripleNumberPickerPreference night_pref = (TripleNumberPickerPreference) findPreference(getString(R.string.pref_notification_category_night_key));
//            TripleNumberPickerPreference bedtime_pref = (TripleNumberPickerPreference) findPreference(getString(R.string.pref_notification_category_bedtime_key));

            SeekBarPreference morning_pref = (SeekBarPreference) findPreference(getString(R.string.pref_morning_seekBar_key));
            SeekBarPreference afternoon_pref = (SeekBarPreference) findPreference(getString(R.string.pref_afternoon_seekBar_key));
            SeekBarPreference night_pref = (SeekBarPreference) findPreference(getString(R.string.pref_evening_seekBar_key));
            SeekBarPreference bedtime_pref = (SeekBarPreference) findPreference(getString(R.string.pref_bedtime_seekBar_key));




            vibrate_pref.setEnabled(notifi_switch);
            sound_pref.setEnabled(notifi_switch);
            pref_notifi_after.setEnabled(notifi_switch);
            pref_notifi_interval.setEnabled(notifi_switch);
            pref_notifi_times.setEnabled(notifi_switch);
            morning_pref.setEnabled(notifi_switch);
            afternoon_pref.setEnabled(notifi_switch);
            night_pref.setEnabled(notifi_switch);
            bedtime_pref.setEnabled(notifi_switch);

        }
    }

    public static class PersonalInfoFragment extends PreferenceFragment{
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            addPreferencesFromResource(R.xml.pref_personinfo);
            super.onCreate(savedInstanceState);
        }
    }

    public static class GeneralFragment extends PreferenceFragment{
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }
    }

    public static class BottleFragment extends PreferenceFragment{
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }
    }

    public static class PrivacyFragment extends PreferenceFragment{
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }
    }

    public static class FeedBackFragment extends PreferenceFragment{
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }
    }

    public static class AboutFragment extends PreferenceFragment{
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }
    }


}
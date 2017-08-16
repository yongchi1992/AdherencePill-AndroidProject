package com.adherence.adherence;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by sam on 8/3/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_fragment);
    }

//    @Override
//    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
//        addPreferencesFromResource(R.xml.pref_fragment);
//    }

    //    @Override
//    public void onCreatePreferences(Bundle bundle, String s) {
//
////        addPreferencesFromResource(R.xml.pref_fragment);
//        addPreferencesFromResource(R.xml.pref_fragment);
//    }

    public static SettingsFragment newInstance(String sessionToken) {

        Bundle args = new Bundle();

        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }
}

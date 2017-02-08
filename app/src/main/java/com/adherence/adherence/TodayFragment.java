package com.adherence.adherence;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.support.v4.app.FragmentTransaction;

import com.android.volley.RequestQueue;


public class TodayFragment extends Fragment{
    private Button btn;
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_SESSION_TOKEN="session_token";

    private String sessionToken;

    private Prescription[] prescriptions;
    private RequestQueue mRequestQueue;
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static TodayFragment newInstance(int sectionNumber, String sessionToken) {
        TodayFragment fragment = new TodayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SESSION_TOKEN,sessionToken);
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionToken=getArguments().getString(ARG_SESSION_TOKEN);
        Log.d("Today fragment session",sessionToken);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_today, container, false);
        ProgressBar progress = (ProgressBar) rootView.findViewById(R.id.progressWheel);
        progress.setSecondaryProgress(80);
        btn = (Button) rootView.findViewById(R.id.detailButton);
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                TodayFragment2 tf2 = new TodayFragment2();
                ft.replace(R.id.detailButton,tf2).commit();
            }
        });
        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((NextActivity) context).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }


}

package com.adherence.adherence;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TodayFragment4 extends Fragment  {
    static HashMap<String, HashMap<String, String>> patient = new HashMap<String, HashMap<String, String>>();
    private HashMap<String, HashMap<String, String>> display = new HashMap<String, HashMap<String, String>>();


    private RecyclerView mRcyclerView;
    private TodayListAdapter4 mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
//    private FloatingActionButton fab;
    private View view;
    private Paint p = new Paint();

    private Prescription[] prescriptions;
    private RequestQueue mRequestQueue;

    private ArrayList<JSONObject> todaySchedule;


    private ArrayList<JSONObject> prescription_morning;
    private ArrayList<JSONObject> prescription_afternoon;
    private ArrayList<JSONObject> prescription_evening;
    private ArrayList<JSONObject> prescription_bedtime;



    private TextView dayofweek;
    private ArrayAdapter<String> arrayAdapter;
    private TextView times_text;
    private String mDay;
    public Calendar c = Calendar.getInstance();
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


    private String sessionToken;

    public static TodayFragment4 newInstance(String sessionToken, int sectionNumber) {
        TodayFragment4 fragment = new TodayFragment4();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(ARG_SESSION_TOKEN,sessionToken);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.today4_fragment, container, false);


        mRcyclerView = (RecyclerView) view.findViewById(R.id.today_RV);
        mRcyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRcyclerView.setLayoutManager(mLayoutManager);
//        fab = (FloatingActionButton) view.findViewById(R.id.fab);
//        fab.setOnClickListener(this);





        sessionToken=getArguments().getString(ARG_SESSION_TOKEN);


        prescription_morning = new ArrayList<>();
        prescription_afternoon = new ArrayList<>();
        prescription_evening = new ArrayList<>();
        prescription_bedtime = new ArrayList<>();



        dayofweek= (TextView) view.findViewById(R.id.m_day);

        String int_day=String.valueOf(c.get(c.DAY_OF_WEEK));
        switch (int_day){
            case "1":mDay="Sunday";break;
            case "2":mDay="Monday";break;
            case "3":mDay="Tuesday";break;
            case "4":mDay="Wednesday";break;
            case "5":mDay="Thursday";break;
            case "6":mDay="Friday";break;
            case "7":mDay="Saturday";break;
            default:mDay="Sunday";break;
        }
        dayofweek.setText("This is "+mDay);
        mRequestQueue= Volley.newRequestQueue(getActivity());
        String url= getString(R.string.parseURL)  + "/patient/prescription";

        JsonArrayRequest request=new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
//                Log.d("response",response.toString());
                int i = response.length();

                prescriptions = new Prescription[i];
                for (int j = 0;j < i;j++){
                    prescriptions[j] = new Prescription();
                    try {
                        JSONObject prescript = response.getJSONObject(j);
                        prescriptions[j].setName(prescript.getString("name"));
                        if(prescript.has("note")) {
                            prescriptions[j].setNote(prescript.getString("note"));
                        }else {
                            prescriptions[j].setNote("null");
                        }
                        if(prescript.has("pill")) {
                            prescriptions[j].setPill(prescript.getString("pill"));
                        }else{
                            prescriptions[j].setPill("none");
                        }
                        prescriptions[j].setPrescriptionId(prescript.getString("objectId"));

                        JSONArray schedule = prescript.getJSONArray("schedule");


                        for(int k = 0; k < schedule.length(); k++){
                            JSONObject takeTime = schedule.getJSONObject(k);
                            String time = takeTime.getString("time").substring(11, 19);
                            JSONArray takeWeek = takeTime.getJSONArray("days");
                            Map<String, Integer> days = new HashMap<String, Integer>();
                            for(int l = 0; l < takeWeek.length(); l++){
                                JSONObject takeDays = takeWeek.getJSONObject(l);
                                if(takeDays.has("amount")){
                                    days.put(takeDays.getString("name"), takeDays.getInt("amount"));
                                }else {
                                    days.put(takeDays.getString("name"), 0);
                                }
                            }
                            prescriptions[j].setSchedule(time, days);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                for(int k = 0; k < prescriptions.length; k++){
                    final int finalK = k;
//                    Log.d("prescription_id : ", prescriptions[finalK].getPrescriptionId());

                    String prescriptReq = getString(R.string.parseURL)  + "/prescription?prescriptionId="+prescriptions[finalK].getPrescriptionId();

//                    Log.d("prescription", prescriptReq);
                    mRequestQueue.add(new JsonArrayRequest(prescriptReq, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                String date = new SimpleDateFormat("MM/dd/yy").format(new Date());
                                String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

                                DateFormat df = new SimpleDateFormat("HH:mm:ss");
                                Date currentTime = df.parse(time);
//                                Log.d("Date:", date);
//                                Log.d("Time", time);
//                                Log.d("Name", prescriptions[finalK].getName());

                                ArrayList<String> todayList = new ArrayList<String>();
                                if(!response.isNull(0)) {
                                    JSONArray updates = response.getJSONObject(0).getJSONArray("updates");
                                    int len = updates.length();
                                    for (int j = 0; j < len; j++) {
                                        if (date.equals(updates.getJSONObject(j).getString("timestamp").substring(10, 18))) {

                                            todayList.add(updates.getJSONObject(j).getString("timestamp").substring(0, 8));
                                        }
                                    }

                                }else{
                                }
                                Iterator<Map.Entry<String, Integer>> itr = prescriptions[finalK].getTimeAmount(mDay).entrySet().iterator();

                                while(itr.hasNext()){
                                    Map.Entry<String, Integer> entry = itr.next();
                                    int eaten = 1;
                                    int updatesLen = todayList.size();


                                    if (updatesLen == 0 && (currentTime.getTime() - df.parse(entry.getKey()).getTime() > 0)){
                                        eaten = 2;
                                    }else
                                    {
                                        for(int j = 0; j < updatesLen; j++){
                                            long l1 = df.parse(entry.getKey()).getTime();
                                            long l2 = df.parse(todayList.get(j)).getTime();
                                            if(Math.abs(df.parse(entry.getKey()).getTime() - df.parse(todayList.get(j)).getTime()) <= 7200000 * 2){
                                                eaten = 3;
                                                break;
                                            }else if (currentTime.getTime() - df.parse(entry.getKey()).getTime() > 0) {
                                                eaten = 2;
                                            }
                                        }
                                    }
//                                    }

                                    String tempPillName = prescriptions[finalK].getPill();
//                                    if (pillMap.containsKey(tempPillName)) eaten = Math.max(eaten, pillMap.get(tempPillName));

                                    int cur_hour = Integer.parseInt(entry.getKey().substring(0, 2));

                                    int amount=entry.getValue();
                                    String pill="pill";
                                    if(amount>1) pill=pill+"s";
                                    String temp_time_amount = entry.getKey()+": take "+entry.getValue()+" "+pill;

                                    JSONObject tempObj = new JSONObject();
                                    tempObj.put("name", tempPillName);
                                    tempObj.put("time", temp_time_amount);
                                    tempObj.put("timeStamp", entry.getKey());
                                    tempObj.put("flag", eaten);
                                    tempObj.put("note", prescriptions[finalK].getNote());
                                    tempObj.put("amount", amount);



                                    if (7 <= cur_hour && cur_hour < 12) {
//                                        applyAdapter(pillName1, time_amount1, flag1, tempPillName, temp_time_amount, eaten, mRecyclerView1);
                                        tempObj.put("period", "Morning");
                                        prescription_morning.add(tempObj);

                                    }

                                    if (12 <= cur_hour && cur_hour < 18) {
//                                        applyAdapter(pillName2, time_amount2, flag2, tempPillName, temp_time_amount, eaten, mRecyclerView2);
                                        tempObj.put("period", "Afternoon");
                                        prescription_afternoon.add(tempObj);
                                    }

                                    if (18 <= cur_hour && cur_hour <= 24) {
//                                        applyAdapter(pillName3, time_amount3, flag3, tempPillName, temp_time_amount, eaten, mRecyclerView3);
                                        tempObj.put("period", "Evening");
                                        prescription_evening.add(tempObj);
                                    }

                                    if (0 <= cur_hour && cur_hour < 7) {
//                                        applyAdapter(pillName4, time_amount4, flag4, tempPillName, temp_time_amount, eaten, mRecyclerView4);
                                        tempObj.put("period", "Bedtime");
                                        prescription_bedtime.add(tempObj);
                                    }
                                }

                                todaySchedule = new ArrayList<>();



                                todaySchedule.addAll(prescription_morning);
                                todaySchedule.addAll(prescription_afternoon);
                                todaySchedule.addAll(prescription_evening);
                                todaySchedule.addAll(prescription_bedtime);



                                mAdapter = new TodayListAdapter4(getContext(), todaySchedule, mRcyclerView, sessionToken);
                                mRcyclerView.setAdapter(mAdapter);

//                                mAdapter.notifyDataSetChanged();
//                                initSwipe();






                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (java.text.ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("error",error.toString());
                        }
                    }){
                        @Override
                        public Map<String,String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<String, String>();
                            headers.put("x-parse-session-token",sessionToken);
                            return headers;
                        }
                    });
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error",error.toString());
            }
        }){
            @Override
            public Map<String,String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("x-parse-session-token",sessionToken);
                return headers;
            }
        };
        mRequestQueue.add(request);




        return view;

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((NextActivity) context).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

}
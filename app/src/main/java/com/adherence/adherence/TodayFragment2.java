package com.adherence.adherence;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

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
import java.util.List;
import java.util.Map;

public class TodayFragment2 extends Fragment implements View.OnClickListener {
    static HashMap<String, HashMap<String, String>> patient = new HashMap<String, HashMap<String, String>>();
    private HashMap<String, HashMap<String, String>> display = new HashMap<String, HashMap<String, String>>();

    private RecyclerView mRecyclerView1;
    private RecyclerView mRecyclerView2;
    private RecyclerView mRecyclerView3;
    private RecyclerView mRecyclerView4;
    private TodayListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<String> pillName1;
    private ArrayList<String> time_amount1;
    private ArrayList<Integer> flag1;

    private ArrayList<String> pillName2;
    private ArrayList<String> time_amount2;
    private ArrayList<Integer> flag2;

    private ArrayList<String> pillName3;
    private ArrayList<String> time_amount3;
    private ArrayList<Integer> flag3;

    private ArrayList<String> pillName4;
    private ArrayList<String> time_amount4;
    private ArrayList<Integer> flag4;

    private Map<String, Integer> pillMap;

    private Prescription[] prescriptions;
    private RequestQueue mRequestQueue;

    private ArrayList<String> prescription_morning;
    private ArrayList<String> prescription_afternoon;
    private ArrayList<String> prescription_evening;
    private ArrayList<String> prescription_bedtime;



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

    public static TodayFragment2 newInstance(String sessionToken,int sectionNumber) {
        TodayFragment2 fragment = new TodayFragment2();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(ARG_SESSION_TOKEN,sessionToken);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.today2_fragment, container, false);
        mRecyclerView1= (RecyclerView) view.findViewById(R.id.today_schedule);
        mLayoutManager=new LinearLayoutManager(getActivity());
        mRecyclerView1.setLayoutManager(mLayoutManager);

        mRecyclerView2= (RecyclerView) view.findViewById(R.id.today_schedule2);
        mLayoutManager=new LinearLayoutManager(getActivity());
        mRecyclerView2.setLayoutManager(mLayoutManager);

        mRecyclerView3= (RecyclerView) view.findViewById(R.id.today_schedule3);
        mLayoutManager=new LinearLayoutManager(getActivity());
        mRecyclerView3.setLayoutManager(mLayoutManager);

        mRecyclerView4= (RecyclerView) view.findViewById(R.id.today_schedule4);
        mLayoutManager=new LinearLayoutManager(getActivity());
        mRecyclerView4.setLayoutManager(mLayoutManager);


        sessionToken=getArguments().getString(ARG_SESSION_TOKEN);
        pillMap = new HashMap<>();

        pillName1=new ArrayList<>();
        time_amount1=new ArrayList<>();
        flag1=new ArrayList<>();

        pillName2=new ArrayList<>();
        time_amount2=new ArrayList<>();
        flag2=new ArrayList<>();

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
                Log.d("response",response.toString());
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
                    Log.d("prescription_id : ", prescriptions[finalK].getPrescriptionId());

                    String prescriptReq = getString(R.string.parseURL)  + "/prescription?prescriptionId="+prescriptions[finalK].getPrescriptionId();

                    Log.d("prescription", prescriptReq);
                    mRequestQueue.add(new JsonArrayRequest(prescriptReq, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                String date = new SimpleDateFormat("MM/dd/yy").format(new Date());
                                String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

                                DateFormat df = new SimpleDateFormat("HH:mm:ss");
                                Date currentTime = df.parse(time);
                                Log.d("Date:", date);
                                Log.d("Time", time);
                                Log.d("Name", prescriptions[finalK].getName());

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
                                    if (pillMap.containsKey(tempPillName)) eaten = Math.max(eaten, pillMap.get(tempPillName));

                                    int cur_hour = Integer.parseInt(entry.getKey().substring(0, 2));

                                    int amount=entry.getValue();
                                    String pill="pill";
                                    if(amount>1) pill=pill+"s";
                                    String temp_time_amount = entry.getKey()+": take "+entry.getValue()+" "+pill;

                                    if (7 <= cur_hour && cur_hour < 12) {
                                        applyAdapter(pillName1, time_amount1, flag1, tempPillName, temp_time_amount, eaten, mRecyclerView1);
                                    }

                                    if (12 <= cur_hour && cur_hour < 18) {
                                        applyAdapter(pillName2, time_amount2, flag2, tempPillName, temp_time_amount, eaten, mRecyclerView2);
                                    }

                                    if (18 <= cur_hour && cur_hour <= 24) {
                                        applyAdapter(pillName3, time_amount3, flag3, tempPillName, temp_time_amount, eaten, mRecyclerView3);
                                    }

                                    if (0 <= cur_hour && cur_hour < 7) {
                                        applyAdapter(pillName4, time_amount4, flag4, tempPillName, temp_time_amount, eaten, mRecyclerView4);
                                    }


                                }
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


    private void applyAdapter(ArrayList<String> pillName, ArrayList<String> time_amount, ArrayList<Integer> flag, String tempPillName, String temp_time_amount, int eaten, RecyclerView mRecyclerView) {
        if (pillName == null || flag == null || pillName.size() != flag.size()) return;

        pillName.add(tempPillName);
        time_amount.add(temp_time_amount);
        flag.add(eaten);

        mAdapter = new TodayListAdapter(pillName, time_amount, flag);
        mRecyclerView.setAdapter(mAdapter);
    }

    //
    @Override
    public void onClick(View v) {
        //do what you want to do when button is clicked
        switch (v.getId()) {
            case R.id.button_send:

//                ParseQuery<ParseObject> query=ParseQuery.getQuery("GameScore");
//                query.getInBackground("YsHLfJ7tUB", new GetCallback<ParseObject>() {
//
//                    @Override
//                    public void done(ParseObject arg0, ParseException arg1) {
//                        // TODO Auto-generated method stub
//
//                        if (arg1==null)
//                        {
//                            int score = arg0.getInt("score");
//                            playerName = arg0.getString("playerName");
//                            boolean cheatMode = arg0.getBoolean("cheatMode");
//                            tv.setText("Updating "+playerName);
//
//                        }
//                        else
//                        {
//                            Log.d("score", "Error: " + arg1.getMessage());
//                        }
//                    }

                Toast.makeText(getActivity(), "Click button", Toast.LENGTH_LONG).show();
                break;
        }
    }




    private void getDisplay(final String day) throws ParseException {
        if (isNetworkConnected()) {
//            Toast.makeText(getActivity(), "connect to network", Toast.LENGTH_LONG).show();
            ParseUser currentUser = ParseUser.getCurrentUser();
//            Toast.makeText(getActivity(), "currentUser: " + currentUser, Toast.LENGTH_LONG).show();
            ParseObject.unpinAllInBackground("schedules");
            ParseRelation exportContactRelation = currentUser.getRelation( "Prescription" );
            //Toast.makeText(getActivity(), "exportContactRelation: " + exportContactRelation, Toast.LENGTH_LONG).show();
            Toast.makeText(getContext(),"",Toast.LENGTH_SHORT).show();
            try {
                List<ParseObject> prescriptions = exportContactRelation.getQuery().find();
//                Toast.makeText(getActivity(), "prescriptions: " + prescriptions, Toast.LENGTH_LONG).show();
                for (ParseObject prescription: prescriptions) {
                    ParseObject schedule = prescription.getParseObject("schedule");
                    schedule.fetchIfNeeded();
                    schedule.put("pillName", prescription.getString("pillName"));
                    schedule.pinInBackground("schedules");
                    JSONObject json = schedule.getJSONObject(day);
                    Iterator<String> iter = json.keys();
                    HashMap<String, String> time_pills = new HashMap<String, String>();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        try {
                            String value = json.get(key).toString();
                            if (key.length() < 8)
                                key = "0" + key;
                            key.replace(" ", "");
                            time_pills.put(key, value);

                        } catch (JSONException e) {
                            // Something went wrong!
                        }
                    }
                    display.put(prescription.getString("pillName"), time_pills);
//                    ParseObject ppp = schedule.get(day);
//                    Log.v(TAG, String.valueOf(ppp));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            try {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Schedule");
                query.fromLocalDatastore();
                List<ParseObject> schedules = query.find();
                if (schedules.size() != 0) {
                    for (ParseObject schedule : schedules) {
                        JSONObject json = schedule.getJSONObject(day);
                        Iterator<String> iter = json.keys();
                        HashMap<String, String> time_pills = new HashMap<String, String>();
                        while (iter.hasNext()) {
                            String key = iter.next();
                            try {
                                String value = json.get(key).toString();
                                if (key.length() < 8)
                                    key = "0" + key;
                                key.replace(" ", "");
                                time_pills.put(key, value);

                            } catch (JSONException e1) {
                                // Something went wrong!
                            }
                        }
                        display.put(schedule.getString("pillName"), time_pills);
                    }
                } else {
                    Context context = getContext();
                    CharSequence text = "Please check network connection.";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            } catch (Exception e) {
            }
        }
    }

    private void addTimeViews(View rootView, LayoutInflater timeViewInflater) {
        LinearLayout containerView = (LinearLayout) rootView.findViewById(R.id.today_container);
        int currenthour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) - 15;
        int currentminute = Calendar.getInstance().get(Calendar.MINUTE);

//        Toast.makeText(getActivity(), "display is: " + display, Toast.LENGTH_LONG).show();

        for(String key1:display.keySet()){
            System.out.println(key1);

            View timeView = timeViewInflater.inflate(R.layout.time_view, null);
            TextView timeStamp = (TextView) timeView.findViewById(R.id.time_stamp);
            timeStamp.setText(key1);
            LinearLayout pillListContainer = (LinearLayout) timeView.findViewById(R.id.time_pill_list);

            for(String key2:display.get(key1).keySet()){
                String timehour=key2.substring(0,2);
                String timeminute=key2.substring(3,5);
                String apm=key2.substring(key2.length()-2,key2.length());
                int hourtime=Integer.valueOf(timehour);
                int minutetime=Integer.valueOf(timeminute);
                if(apm.equals("am")&&(hourtime == 12)){hourtime -=12;}
                if(apm.equals("pm")&&(hourtime != 12)){hourtime+=12;}
                if(hourtime>currenthour || (hourtime == currenthour && minutetime>=currentminute)){
                    View  pillView = timeViewInflater.inflate(R.layout.pill_list_view, null);
                    TextView pillName = (TextView) pillView.findViewById(R.id.pill_name3);
                    pillName.setText(key2);
                    TextView pillCount = (TextView) pillView.findViewById(R.id.pill_count);
                    pillCount.setText(display.get(key1).get(key2));
                    pillListContainer.addView(pillView);
                }
            }
            containerView.addView(timeView);
        }
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm;
        cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((NextActivity) context).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
    private void showPopupWindow(View view,String times) {

        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(getContext()).inflate(
                R.layout.pop_up_window2, null);
        times_text= (TextView) contentView.findViewById(R.id.pop_times);
        times_text.setText(times);

        final PopupWindow popupWindow = new PopupWindow(contentView,
                RecyclerView.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setTouchable(true);

        popupWindow.setTouchInterceptor(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                Log.i("mengdd", "onTouch : ");

                return false;

            }
        });


        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape));

        View windowContentViewRoot = getView();
        int windowPos[] = calculatePopWindowPos(view, windowContentViewRoot);
        int xOff = 20;
        windowPos[0] -= xOff;
        popupWindow.showAtLocation(view, Gravity.TOP | Gravity.START, windowPos[0], windowPos[1]);

    }
    private static int[] calculatePopWindowPos(final View anchorView, final View contentView) {
        final int windowPos[] = new int[2];
        final int anchorLoc[] = new int[2];

        anchorView.getLocationOnScreen(anchorLoc);
        final int anchorHeight = anchorView.getHeight();

        final int screenHeight = ScreenUtils.getScreenHeight(anchorView.getContext());
        final int screenWidth = ScreenUtils.getScreenWidth(anchorView.getContext());
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        final int windowHeight = contentView.getMeasuredHeight();
        final int windowWidth = contentView.getMeasuredWidth();

        final boolean isNeedShowUp = (screenHeight - anchorLoc[1] - anchorHeight < windowHeight);
        if (isNeedShowUp) {
            windowPos[0] = screenWidth - windowWidth;
            windowPos[1] = anchorLoc[1] - windowHeight;
        } else {
            windowPos[0] = screenWidth - windowWidth;
            windowPos[1] = anchorLoc[1] + anchorHeight;
        }
        return windowPos;
    }

}
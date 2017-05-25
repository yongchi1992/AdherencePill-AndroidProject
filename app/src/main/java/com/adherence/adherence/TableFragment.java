
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

public class TableFragment extends Fragment implements View.OnClickListener {
    static HashMap<String, HashMap<String, String>> patient = new HashMap<String, HashMap<String, String>>();
    private HashMap<String, HashMap<String, String>> display = new HashMap<String, HashMap<String, String>>();

    private RecyclerView mRecyclerView;
    private TableListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<String> pillName;
    private ArrayList<String> time_amount;
    private ArrayList<Integer> flag;
    private ArrayList<String> morning;
    private ArrayList<String> afternoon;
    private ArrayList<String> night;
    private Map<String, Integer> pillMap;

    private Prescription[] prescriptions;
    private RequestQueue mRequestQueue;



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

    public static TableFragment newInstance(String sessionToken,int sectionNumber) {
        TableFragment fragment = new TableFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//        Log.d("today_frag_newinstance",sessionToken);
        args.putString(ARG_SESSION_TOKEN,sessionToken);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_table, container, false);
        mRecyclerView= (RecyclerView) view.findViewById(R.id.today_schedule);
        mLayoutManager=new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        sessionToken=getArguments().getString(ARG_SESSION_TOKEN);
        pillName=new ArrayList<>();
        time_amount=new ArrayList<>();
        morning = new ArrayList<>();
        afternoon = new ArrayList<>();
        night = new ArrayList<>();
        pillMap = new HashMap<>();
        flag=new ArrayList<>();
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
        String url="http://129.105.36.93:5000/patient/prescription";
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

                    String prescriptReq = "http://129.105.36.93:5000/prescription?prescriptionId="+prescriptions[finalK].getPrescriptionId();

                    Log.d("prescription", prescriptReq);
                    mRequestQueue.add(new JsonArrayRequest(prescriptReq, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                String date = new SimpleDateFormat("MM/dd/yy").format(new Date());
                                String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

                                DateFormat df = new SimpleDateFormat("HH:mm:ss");
//                                Date currentTime = df.parse(time + ", " + date);
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
//                                    todayList = new ArrayList<String>();
                                }
                                Iterator<Map.Entry<String, Integer>> itr = prescriptions[finalK].getTimeAmount(mDay).entrySet().iterator();

                                while(itr.hasNext()){
                                    Map.Entry<String, Integer> entry = itr.next();
                                    int eaten = 1;
                                    int updatesLen = todayList.size();

//                                    if ((currentTime.getTime() - df.parse(entry.getKey()).getTime()) > 0){

                                        if (updatesLen == 0 && (currentTime.getTime() - df.parse(entry.getKey()).getTime() > 0)){
                                            eaten = 2;
                                        }else
                                        {
                                            for(int j = 0; j < updatesLen; j++){
                                                long l1 = df.parse(entry.getKey()).getTime();
                                                long l2 = df.parse(todayList.get(j)).getTime();
                                                if(Math.abs(df.parse(entry.getKey()).getTime() - df.parse(todayList.get(j)).getTime()) <= 7200000 * 2){
//                                                if(Math.abs(currentTime.getTime() - df.parse(todayList.get(j)).getTime()) <= 7200000){
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

                                    Log.d("eaten", Integer.toString(eaten));
                                    flag.add(new Integer(eaten));

                                    pillName.add(tempPillName);
                                    int amount=entry.getValue();
                                    String pill="pill";
                                    if(amount>1) pill=pill+"s";
                                    time_amount.add(entry.getKey()+": take "+entry.getValue()+" "+pill);

                                    String prescriptionTime = entry.getKey().substring(0,2);
                                    int pTime = Integer.parseInt(prescriptionTime);
                                    String takenCondition = "Not taken";
                                    if(eaten == 3){
                                        takenCondition = "Taken";
                                    }
                                    if(pTime >= 6 && pTime < 12){
                                        //Morning
                                        morning.add(takenCondition);
                                        afternoon.add("Not necessary");
                                        night.add("Not necessary");
                                    }
                                    else if(pTime >= 12 && pTime < 18){
                                        //Afternoon
                                        morning.add("Not necessary");
                                        afternoon.add(takenCondition);
                                        night.add("Not necessary");
                                    }
                                    else{
                                        //Night
                                        morning.add("Not necessary");
                                        afternoon.add("Not necessary");
                                        night.add(takenCondition);
                                    }

                                    Log.d("flag.size:",flag.size()+"");
                                    Log.d("pillName.size:",pillName.size()+"");
                                    if(pillName.size()==flag.size()) {
                                        Log.d("flag.size:",flag.size()+"");
                                        mAdapter = new TableListAdapter(pillName, time_amount, flag, morning, afternoon, night);
                                        mRecyclerView.setAdapter(mAdapter);
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
                    TextView pillName = (TextView) pillView.findViewById(R.id.pill_name);
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
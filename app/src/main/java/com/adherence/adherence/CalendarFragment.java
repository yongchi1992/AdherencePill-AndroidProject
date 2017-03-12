package com.adherence.adherence;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static android.R.attr.max;
import static android.content.Context.MODE_PRIVATE;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.content.SharedPreferences;

public class CalendarFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_START_DATE="start_date";

    private Calendar calendar;
    private String startDate;
    private List<Float> percentages;

    //record the dates from earliest prescription created to yesterday
    private ArrayList<String> dates;
    private ArrayList<CalendarDay> calendars;
    private HashMap<CalendarDay,Integer> shouldTake;//Amount of pills should take every day
    private HashMap<CalendarDay,Integer> isTaken;//amount of pills taken every day

    private RequestQueue mRequestQueue;
    private String sessionToken;
    private Prescription[] prescriptions;
    private List<CalendarDay> calendars_too_few;//take too few pills, less than 50%, red
    private List<CalendarDay> calendars_most_taken;//take most pills, >=50%, blue
    private List<CalendarDay> calendars_all_taken;//take all pills, green
    private MaterialCalendarView materialCalendarView;

    private int[] colors;

    private static final String ARG_SESSION_TOKEN="session_token";
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static CalendarFragment newInstance(String sessionToken, String startDate, int sectionNumber) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SESSION_TOKEN,sessionToken);
        args.putString(ARG_START_DATE,startDate);
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }
    //TODO: Coloring days based solely on overall pills taken doesn't factor in the possibility of
    // multiple pills being taken or not. Technically, user could have taken 90% of total pills,
    // yet not taken any of their crucial pills, etc
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sessionToken=getArguments().getString(ARG_SESSION_TOKEN);
        startDate=getArguments().getString(ARG_START_DATE);
        //Log.d("Calendar fragment sesseionToken: ",sessionToken);
        View rootView = inflater.inflate(R.layout.calendarview, container, false);
        materialCalendarView= (MaterialCalendarView) rootView.findViewById(R.id.matCal);
        materialCalendarView.setShowOtherDates(MaterialCalendarView.SHOW_ALL);
        int currentWidth= materialCalendarView.getTileWidth(); // Set the proper width so that all 7 days of the week are showing
        materialCalendarView.setTileWidth(currentWidth-2);// Don't know why tilewidth is showing as -10. The arithmetic for TileWidth seems buggy, but it works

        sessionToken=getArguments().getString(ARG_SESSION_TOKEN);

        calendar=Calendar.getInstance();
        Date today=calendar.getTime();
        Log.d("Calendar today",today.toString());



        /*
        startDate is the earliest prescription created date
         */
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        String temp_start=sdf.format(new Date(Integer.parseInt(startDate.substring(0,4))-1900,
                Integer.parseInt(startDate.substring(5,7))-1, Integer.parseInt(startDate.substring(8))-1));
        startDate=temp_start;
        Log.d("Calendar_start_date",startDate);
        calendars=new ArrayList<>();
        calendars.add(new CalendarDay(Integer.parseInt(startDate.substring(0,4)),
                Integer.parseInt(startDate.substring(5,7))-1,Integer.parseInt(startDate.substring(8))));
        String nextDay=getSpecifiedDayAfter(startDate);
        Date dayAfter=new Date(Integer.parseInt(nextDay.substring(0,4))-1900,
                Integer.parseInt(nextDay.substring(5,7))-1,Integer.parseInt(nextDay.substring(8)));

        Log.d("Calendar day after",dayAfter.toString());
        dates=new ArrayList<>();
        dates.add(new String(startDate));


        calendars= new ArrayList<>();
        shouldTake=new HashMap<>();

        calendars_too_few= new ArrayList<>();
        calendars_most_taken= new ArrayList<>();
        calendars_all_taken= new ArrayList<>();

        while(isSameDay(new Date(Integer.parseInt(nextDay.substring(0,4))-1900,
                Integer.parseInt(nextDay.substring(5,7))-1,Integer.parseInt(nextDay.substring(8))),today)==false){
            //loop until yesterday
            calendars.add(new CalendarDay(Integer.parseInt(nextDay.substring(0,4)),
                    Integer.parseInt(nextDay.substring(5,7))-1,Integer.parseInt(nextDay.substring(8))));

//            dates.add(new String(nextDay));
            nextDay=getSpecifiedDayAfter(nextDay);
        }
        for(int i=0;i<calendars.size();i++){
            Log.d("calendars "+i+" : ",calendars.get(i).toString());
        }
        isTaken=new HashMap<>();
        for(CalendarDay calendarDay:calendars){
            isTaken.put(calendarDay,0);
        }
        //Log.d("isTaken_size:",isTaken.size()+"");
        final Calendar new_date= Calendar.getInstance();
        //final CalendarDay current_date= CalendarDay.from(new_date);
        percentages= new ArrayList<>();//This array functions as storage for the percentages of pills taken per day.

        materialCalendarView.setSelectedDate(new_date.getTime()); // Colors the current date green

        //Create array of size 3 that contains the 3 different colors to represent pill percentages
        colors=new int[3];
        colors[0]= -65536; //This is red; represents "less than 50% of pills taken that day"
        colors[1]= -16776961; //This is blue; represents "more than 50%, but less than 85% of pills taken that day"
        colors[2]= -16711936;// This is green; represents "all pills taken"

        //initialize as 0.0%
        for(int i=0;i<calendars.size();i++){
            percentages.add(new Float(0.0));
            Log.d("percentage "+i+" : ",percentages.get(i)+"");
        }
        //Query database to calclulate the real percentage every day
        mRequestQueue= Volley.newRequestQueue(getActivity());
        String url="http://129.105.36.93:5000/patient/prescription";
        JsonArrayRequest request=new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d("response",response.toString());
                final int i = response.length();

                prescriptions = new Prescription[i];
                for (int j = 0;j < i;j++){
                    prescriptions[j] = new Prescription();
                    try {
                        //here can get the createDate of prescription and find the startDate
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
                    for(CalendarDay calendarDay:calendars){
                        int year=calendarDay.getYear();
                        int month=calendarDay.getMonth();
                        int day=calendarDay.getDay();
                        Date calendar_date=new Date(year-1900,month,day);
                        //Log.d("traverse calendars: ", calendar_date.toString());
                        String getDay=calendar_date.toString().substring(0,3);
                        String mDay=new String("Monday");
                        if(getDay.equals("Mon")){
                            mDay="Monday";
                        }
                        else if(getDay.equals("Tue")){
                            mDay="Tuesday";
                        }
                        else if(getDay.equals("Wed")){
                            mDay="Wednesday";
                        }
                        else if (getDay.equals("Thu")){
                            mDay="Thursday";
                        }
                        else if(getDay.equals("Fri")){
                            mDay="Friday";
                        }
                        else if(getDay.equals("Sat")){
                            mDay="Saturday";
                        }
                        else {
                            mDay="Sunday";
                        }
                        //Log.d("mDay",mDay);
                        //Log.d(calendarDay.toString()+mDay,prescriptions[k].getTimeAmount(mDay).toString());
                        if(shouldTake.containsKey(calendarDay)){
                            int amount=shouldTake.get(calendarDay);
                            amount=amount+prescriptions[k].getTimeAmount(mDay).size();
                            shouldTake.put(calendarDay,amount);
                        }
                        else{
                            shouldTake.put(calendarDay,prescriptions[k].getTimeAmount(mDay).size());
                        }
                    }
//                    for(CalendarDay calendarDay:shouldTake.keySet()){
//                        Log.d("traverse shouldTake",calendarDay.toString()+" "+shouldTake.get(calendarDay));
//                    }
                    if(k==(prescriptions.length-1))
                        Log.d("shouldTake_size",shouldTake.size()+"");
                    String prescriptReq = "http://129.105.36.93:5000/prescription?prescriptionId="+prescriptions[finalK].getPrescriptionId();
                    // Log.d("prescription", prescriptReq);
                    mRequestQueue.add(new JsonArrayRequest(prescriptReq, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {

                                if(!response.isNull(0)){
                                    JSONArray bottleUpdates = response.getJSONObject(0).getJSONArray("updates");
                                    for(int i=0;i<bottleUpdates.length();i++){
                                        String timeStamp=bottleUpdates.getJSONObject(i).getString("timestamp");
                                        CalendarDay updateDay=new CalendarDay(2000+Integer.parseInt(timeStamp.substring(16,18)),
                                                Integer.parseInt(timeStamp.substring(11,12)), Integer.parseInt(timeStamp.substring(14,15)));
                                        for(CalendarDay calendarDay:isTaken.keySet()){
                                            if(calendarDay.getYear()==updateDay.getYear()&&(calendarDay.getMonth()+1)==updateDay.getMonth()
                                                    &&calendarDay.getDay()==updateDay.getDay()){
                                                Log.d("updateDay==calendarDay",updateDay.toString());
                                                int amount=isTaken.get(calendarDay);
                                                amount+=1;
                                                isTaken.put(calendarDay,amount);

                                            }
                                        }


                                    }

                                }
                                if(finalK==(prescriptions.length-1)) {
                                    Log.d("finalK_isTaken_size", isTaken.size() + "");
                                    for (CalendarDay calendarDay : isTaken.keySet()) {
                                        Log.d("isTaken", calendarDay + ":" + isTaken.get(calendarDay));
                                    }
                                }

                                for(CalendarDay calendarDay:shouldTake.keySet()){
                                    int year=calendarDay.getYear();
                                    int month=calendarDay.getMonth();
                                    int day=calendarDay.getDay();
                                    for(CalendarDay c:isTaken.keySet()){
                                        if(c.getYear()==year&&c.getMonth()==month&&c.getDay()==day){
                                            int taken=isTaken.get(c);
                                            int should_take=shouldTake.get(calendarDay);
                                            if(taken>=should_take){
                                                calendars_all_taken.add(calendarDay);
                                            }
                                            else if((taken*2)>=should_take){
                                                calendars_most_taken.add(calendarDay);
                                            }
                                            else {
                                                calendars_too_few.add(calendarDay);
                                            }
                                        }
                                    }
                                }
                                materialCalendarView.addDecorators(new EventDecorator(colors[0],calendars_too_few));
                                materialCalendarView.addDecorators(new EventDecorator(colors[1],calendars_most_taken));
                                materialCalendarView.addDecorators(new EventDecorator(colors[2],calendars_all_taken));

                                String date = new SimpleDateFormat("MM/dd/yy").format(new Date());
                                String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
                                DateFormat df = new SimpleDateFormat("HH:mm:ss");
                                Date currentTime = df.parse(time);

                                ArrayList<String> todayList = new ArrayList<String>();
                                if(!response.isNull(0)) {
                                    JSONArray updates = response.getJSONObject(0).getJSONArray("updates");
                                    int len = updates.length();
                                    for (int j = 0; j < len; j++) {
                                        if (date == updates.getJSONObject(j).getString("timestamp").substring(10, 18)) {
                                            todayList.add(updates.getJSONObject(j).getString("timestamp").substring(0, 8));
                                        }

                                    }

                                }else{
                                    todayList = new ArrayList<String>();
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


        return rootView;

    }



    public class EventDecorator implements DayViewDecorator {

        private final int color;
        private final HashSet<CalendarDay> dates;

        public EventDecorator(int color, Collection<CalendarDay> dates) {
            this.color = color;
            this.dates = new HashSet<>(dates);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(5, color));
        }
    }

    private Collection<CalendarDay> calendarDays = new Collection<CalendarDay>() {
        @Override
        public boolean add(CalendarDay object) {
            return false;
        }
        @Override
        public boolean addAll(Collection<? extends CalendarDay> collection) {
            return false;
        }
        @Override
        public void clear() {
        }
        @Override
        public boolean contains(Object object) {
            return false;
        }
        @Override
        public boolean containsAll(Collection<?> collection) {
            return false;
        }
        @Override
        public boolean isEmpty() {
            return false;
        }
        @NonNull
        @Override
        public Iterator<CalendarDay> iterator() {
            return null;
        }
        @Override
        public boolean remove(Object object) {
            return false;
        }
        @Override
        public boolean removeAll(Collection<?> collection) {
            return false;
        }
        @Override
        public boolean retainAll(Collection<?> collection) {
            return false;
        }
        @Override
        public int size() {
            return 0;
        }
        @NonNull
        @Override
        public Object[] toArray() {
            return new Object[0];
        }
        @NonNull
        @Override
        public <T> T[] toArray(T[] array) {
            return null;
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NextActivity){
            ((NextActivity) context).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
    /**
     * get the date after the specifiedDay
     *
     * @param specifiedDay
     * @return
     */
    public static String getSpecifiedDayAfter(String specifiedDay) {
        Calendar c = Calendar.getInstance();
        Date date = null;
        try {
            date = new SimpleDateFormat("yy-MM-dd").parse(specifiedDay);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.setTime(date);
        int day = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day + 1);

        String dayAfter = new SimpleDateFormat("yyyy-MM-dd")
                .format(c.getTime());
        return dayAfter;
    }

    //Determine if two dates are the same date
    private boolean isSameDay(Date temp, Date today) {
        return (temp.getYear()==today.getYear())&&
                (temp.getMonth()==today.getMonth())&&(temp.getDate()==today.getDate());
    }
}
package com.adherence.adherence;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static android.R.attr.max;
import static android.content.Context.MODE_PRIVATE;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class CalendarFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    private Calendar calendar;
    private String startDate;
    private List<Float> percentages;

    //record the dates from earliest prescription created to yesterday
    private ArrayList<String> dates;
    private ArrayList<CalendarDay> calendars;
    private ArrayList<Integer> shouldTake;//Amount of pills should take every day
    private ArrayList<Integer> isTaken;//amount of pills taken every day

    private RequestQueue mRequestQueue;
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static CalendarFragment newInstance(int sectionNumber) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
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
        View rootView = inflater.inflate(R.layout.calendarview, container, false);
        MaterialCalendarView materialCalendarView= (MaterialCalendarView) rootView.findViewById(R.id.matCal);
        materialCalendarView.setShowOtherDates(MaterialCalendarView.SHOW_ALL);
        int currentWidth= materialCalendarView.getTileWidth(); // Set the proper width so that all 7 days of the week are showing
        materialCalendarView.setTileWidth(currentWidth-2);// Don't know why tilewidth is showing as -10. The arithmetic for TileWidth seems buggy, but it works

        calendar=Calendar.getInstance();
        Date today=calendar.getTime();
        Log.d("Calendar today",today.toString());

        /* traverse prescriptions to get the start date
        For now just static code, set startDate=2017-3-1 */
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        String startDate=sdf.format(new Date(2017-1900, 2, 0));
        Log.d("Calendar start date",startDate);
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
        shouldTake=new ArrayList<>();
        isTaken=new ArrayList<>();

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

        final Calendar new_date= Calendar.getInstance();
        //final CalendarDay current_date= CalendarDay.from(new_date);
        percentages= new ArrayList<>();//This array functions as storage for the percentages of pills taken per day.

        materialCalendarView.setSelectedDate(new_date.getTime()); // Colors the current date green



        //Create array of size 3 that contains the 3 different colors to represent pill percentages
        int[] colors=new int[3];
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


        List<CalendarDay> calendars_too_few= new ArrayList<>();//take too few pills, red
        List<CalendarDay> calendars_most_taken= new ArrayList<>();//take most pills, blue
        List<CalendarDay> calendars_all_taken= new ArrayList<>();//take all pills, green


        for (int i=0; i<percentages.size(); i++){
            //Depending on what the % value is, set collection to specified date and add decorator
            if (percentages.get(i) == -1 ) // Simulates "too few" percentage; can be changed easily when actual data is passed
            {
                calendars_too_few.add(calendars.get(i));
            }
            else if (percentages.get(i) == 0) // Simulates taking most pills
            {
                calendars_most_taken.add(calendars.get(i));
            } else if (percentages.get(i) == 1) // Simulates taking all pills
            {
                calendars_all_taken.add(calendars.get(i));
            }
        }
        materialCalendarView.addDecorators(new EventDecorator(colors[0],calendars_too_few));
        materialCalendarView.addDecorators(new EventDecorator(colors[1],calendars_most_taken));
        materialCalendarView.addDecorators(new EventDecorator(colors[2],calendars_all_taken));

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
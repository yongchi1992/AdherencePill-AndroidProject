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

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static android.R.attr.max;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by suhon_000 on 10/29/2015.
 */

public class CalendarFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private float[] percentage;

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

        percentage=new float[7];

        //Get list of sample dates to fill in
        List<CalendarDay> calendars= new ArrayList<>();
        final Calendar new_date= Calendar.getInstance();
        //final CalendarDay current_date= CalendarDay.from(new_date);
        List<Byte> percentages= new ArrayList<>();//This array functions as storage for the percentages of pills taken per day.

        materialCalendarView.setSelectedDate(new_date.getTime()); // Colors the current date green

        //Create sample 10 day ArrayList of CalendarDay which can be parsed depending on pill percentage
        for (int i=0; i<10;i++){
            new_date.add(Calendar.DATE,-1);
            CalendarDay calendarDay= CalendarDay.from(new_date);
            calendars.add(calendarDay);
        }

        //Create array of size 3 that contains the 3 different colors to represent pill percentages
        int[] colors=new int[3];
        colors[0]= -65536; //This is red; represents "less than 50% of pills taken that day"
        colors[1]= -16776961; //This is blue; represents "more than 50%, but less than 85% of pills taken that day"
        colors[2]= -16711936;// This is green; represents "all pills taken"

        //Generate a randomized array of pill "percentages". When data is received from the backend team, this array can be replaced with actual pill data.
        byte switcher=1;
        int counter=0;
        for (int i=0; i<7; i++){
            switcher *= -1;
            counter++;
            if (counter % 3 ==0){
                percentages.add((byte) 0);
            }
            else{
                percentages.add(switcher);
            }
        }
//        for (int i=0; i<percentages.size(); i++){
//            Log.d("Percentages",""+percentages.get(i));
//        }
        Collections.shuffle(percentages); //Randomize the hardcoded array
//        for (int i=0; i<percentages.size(); i++){
//            Log.d("Shuffled Percentages",""+percentages.get(i));
//        }
        //Go through percentages array
        List<CalendarDay> calendars_too_few= new ArrayList<>();
        List<CalendarDay> calendars_most_taken= new ArrayList<>();
        List<CalendarDay> calendars_all_taken= new ArrayList<>();


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
}
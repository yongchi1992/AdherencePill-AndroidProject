package com.adherence.adherence;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

public class CalendarAdapter extends ArrayAdapter<Date> {

    // days with events
    private HashSet<Date> eventDays;

    // for view inflation
    private LayoutInflater inflater;

    public CalendarAdapter(Context context, ArrayList<Date> days, HashSet<Date> eventDays)
    {
        super(context, R.layout.calendar_day, days);
        this.eventDays = eventDays;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent)
    {
        // day in question
        Date date = getItem(position);
        int day = date.getDay();
        int month = date.getMonth();
        int year = date.getYear();
        Calendar cal = Calendar.getInstance();
        eventDays = new HashSet<Date>();
        eventDays.add(cal.getTime());


        // today
        Date today = new Date();

        // inflate item if it does not exist yet
        if (view == null)
            view = inflater.inflate(R.layout.calendar_day, parent, false);

        // if this day has an event, specify event image
        if (eventDays != null) {
            for (Date eventDate : eventDays) {
                if (eventDate.getDate() == day &&
                        eventDate.getMonth() == month &&
                        eventDate.getYear() == year) {
                    // mark this day for event
                    view.setBackgroundResource(R.mipmap.reminder);
                    break;
                }
            }
        }

        // clear styling
        ((TextView) view).setTypeface(null, Typeface.NORMAL);
        ((TextView) view).setTextColor(Color.BLACK);

        if (date.getMonth() != today.getMonth() ||
                date.getYear() != today.getYear())
        {
            // if this day is outside current month, grey it out
            ((TextView) view).setTextColor(getContext().getResources().getColor(android.R.color.darker_gray));
        }
        else if (date.getDate() == today.getDate())
        {
            // if it is today, set it to blue/bold
            ((TextView) view).setTypeface(null, Typeface.BOLD);
            ((TextView) view).setTextColor(getContext().getResources().getColor(R.color.material_blue_500));
        }

        // set text
        ((TextView) view).setText(String.valueOf(date.getDate()));

        return view;
    }
}

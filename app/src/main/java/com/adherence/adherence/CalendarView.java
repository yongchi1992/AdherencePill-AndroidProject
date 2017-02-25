package com.adherence.adherence;

        import android.content.Context;
        import android.util.AttributeSet;
        import android.view.LayoutInflater;
        import android.widget.GridView;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.TextView;
        import android.content.Intent;
        import android.widget.EditText;

        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.Calendar;
        import java.util.Date;
        import java.util.HashSet;
        import android.widget.Button;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.ImageButton;


public class CalendarView extends LinearLayout
{
    private static final int DAYS_COUNT = 35;

    // internal components
    private LinearLayout header;
    private TextView txtDate;
    private GridView grid;
    private Calendar abc = Calendar.getInstance();

    public CalendarView(Context context)
    {
        super(context);
    }

    public CalendarView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initControl(context, attrs);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initControl(context, attrs);
    }

    /**
     * Load component XML layout
     */
    private void initControl(Context context, AttributeSet attrs)
    {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.calendar, this);
        /*
        // layout is inflated, assign local variables to components
        header = (LinearLayout)findViewById(R.id.calendar_header);
        txtDate = (TextView)findViewById(R.id.calendar_date_display);
        grid = (GridView)findViewById(R.id.calendar_grid);

        ImageButton prebutton = (ImageButton)findViewById(R.id.calendar_prev_button);
        ImageButton nextbutton = (ImageButton)findViewById(R.id.calendar_next_button);
        OnClickListener ocl1 = new OnClickListener() {
            @Override
            public void onClick(View arg1) {
                Login1(arg1);
            }
        };
        prebutton.setOnClickListener(ocl1);
        nextbutton.setOnClickListener(ocl1);

        updateCalendar();
        */
    }
        /*
        public void Login1(View arg1){

        if(arg1.getId()==2131558507){
            System.out.println("11");
            SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy");

            abc.add(Calendar.MONTH,-1);
            txtDate.setText(sdf.format(abc.getTime()));

            updateCalendar2();
        }
        else{
            System.out.println("22");



            SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy");

            abc.add(Calendar.MONTH, 1);
            System.out.println(abc.get(Calendar.MONTH));
            txtDate.setText(sdf.format(abc.getTime()));
            updateCalendar1();

        }

    }
    */

    private void updateCalendar() {
        updateCalendar(null);
    }

    private void updateCalendar1() {
        updateCalendar1(null);
    }

    private void updateCalendar2() {
        updateCalendar2(null);
    }

    private void updateCalendar(HashSet<Date> events)
    {
        ArrayList<Date> cells = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        // determine the cell for current month's beginning
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int monthBeginningCell = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        // move calendar backwards to the beginning of the week
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);

        // fill cells (42 days calendar as per our business logic)
        while (cells.size() < DAYS_COUNT)
        {
            cells.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // update grid
        grid.setAdapter(new CalendarAdapter(getContext(), cells, events));

        // update title
        SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy");
        calendar = Calendar.getInstance();
        txtDate.setText(sdf.format(calendar.getTime()));
    }


    private void updateCalendar1(HashSet<Date> events){
        ArrayList<Date> cells = new ArrayList<>();
        int month = abc.get(Calendar.MONTH);
        System.out.println(abc.get(Calendar.MONTH));
        // determine the cell for current month's beginning
        abc.set(Calendar.DAY_OF_MONTH, 1);
        System.out.println(abc.get(Calendar.MONTH));
        int monthBeginningCell = abc.get(Calendar.DAY_OF_WEEK) - 1;
        System.out.println(abc.get(Calendar.MONTH));


        // move calendar backwards to the beginning of the week
        abc.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);

        System.out.println(abc.get(Calendar.MONTH));

        // fill cells (42 days calendar as per our business logic)
        while (cells.size() < DAYS_COUNT)
        {
            cells.add(abc.getTime());
            abc.add(Calendar.DAY_OF_MONTH, 1);
        }
        if (abc.get(Calendar.MONTH) == month) {
            while (cells.size() < 42)
            {
                cells.add(abc.getTime());
                abc.add(Calendar.DAY_OF_MONTH, 1);
            }

        }

        System.out.println(abc.get(Calendar.MONTH));
        // update grid
        grid.setAdapter(new CalendarAdapter(getContext(), cells, events));
        abc.add(Calendar.MONTH, -1);

    }

    private void updateCalendar2(HashSet<Date> events){
        ArrayList<Date> cells = new ArrayList<>();
        int month = abc.get(Calendar.MONTH);
        System.out.println(abc.get(Calendar.MONTH));
        // determine the cell for current month's beginning
        abc.set(Calendar.DAY_OF_MONTH, 1);
        System.out.println(abc.get(Calendar.MONTH));
        int monthBeginningCell = abc.get(Calendar.DAY_OF_WEEK) - 1;
        System.out.println(abc.get(Calendar.MONTH));


        // move calendar backwards to the beginning of the week
        abc.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);

        System.out.println(abc.get(Calendar.MONTH));

        // fill cells (42 days calendar as per our business logic)
        while (cells.size() < DAYS_COUNT)
        {
            cells.add(abc.getTime());
            abc.add(Calendar.DAY_OF_MONTH, 1);
        }
        if (abc.get(Calendar.MONTH) == month) {
            while (cells.size() < 42)
            {
                cells.add(abc.getTime());
                abc.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        System.out.println(abc.get(Calendar.MONTH));
        // update grid
        grid.setAdapter(new CalendarAdapter(getContext(), cells, events));
        abc.add(Calendar.MONTH, -1);
    }
}
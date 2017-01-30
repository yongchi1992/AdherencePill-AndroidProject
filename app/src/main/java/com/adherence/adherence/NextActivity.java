package com.adherence.adherence;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.ParseObject;

import java.util.Calendar;


public class NextActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private String sessionToken;
    /**
     * Used to store the last screen title. For use in      */
    private CharSequence mTitle;

    private Toolbar toolbar;
//
//    private ListView listView;
//    private ArrayAdapter<String>arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.next_main);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        mTitle = "Today";

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);

        toolbar.setTitle(mTitle);
        Intent intent=getIntent();
        sessionToken=intent.getStringExtra("sessionToken");
        Log.d("nextactivity session",sessionToken);


        createDelayedNotification();


    }

    private void createDelayedNotification() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 20);

        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, 0);

        long futureInMillis = cal.getTimeInMillis();
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC, futureInMillis, pendingIntent);
        Log.v("TAG" ,"set alarm manager");
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch(position) {
            case 0:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, TodayFragment2.newInstance(position + 1))
                        .commit();
                break;
            case 1:
                String[] medicineList = getResources().getStringArray(R.array.medicine_hardcode);
                String[] detailList = getResources().getStringArray(R.array. detail_hardcode);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, MedicationFragment.newInstance(medicineList,detailList,sessionToken,position + 1))
                        .commit();
                break;
            case 2:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, CalendarFragment.newInstance(position + 1))
                        .commit();
                break;
            default:
                break;
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
        if (toolbar != null) {
            toolbar.setTitle(mTitle);
        }

    }

//    public void restoreActionBar() {
//        ActionBar actionBar = getActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setTitle(mTitle);
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
//            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toolbar = getActionBarToolbar();
        Log.v("TAG", "Called");
    }

    protected Toolbar getActionBarToolbar() {
        if (toolbar == null) {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
            }
        }
        return toolbar;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.logout) {
            ParseObject.unpinAllInBackground("user");
            Intent intent = new Intent();
            intent.setClass(NextActivity.this, MainActivity.class);
            NextActivity.this.startActivity(intent);
        }

        if (id == R.id.update) {
            Intent intent = new Intent();
            intent.setClass(NextActivity.this, NextActivity.class);
            NextActivity.this.startActivity(intent);
        }

        if (id == R.id.calendar) {
            Intent intent = new Intent();
            intent.setClass(NextActivity.this, NextActivity.class);
            NextActivity.this.startActivity(intent);
        }

        if (id == R.id.bluetooth) {
            Intent intent = new Intent();
            intent.setClass(NextActivity.this, MainActivity2.class);
            NextActivity.this.startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

}
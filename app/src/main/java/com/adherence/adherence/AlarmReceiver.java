package com.adherence.adherence;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.zentri.zentri_ble_command.ZentriOSBLEManager;

import java.util.Calendar;
import java.util.Date;

public class AlarmReceiver extends BroadcastReceiver {

    private ZentriOSBLEManager mZentriOSBLEManager;
    public static final String AL1 = "AL1";
    public static final String TAG = "AlarmReceiver";
    public static final String EXTRA_DATA = "EXTRA_DATA";
    private String command1 = "Scan";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "starting service");

        String[] setTime = new String[4];
        SharedPreferences data_newdata=context.getSharedPreferences(MainActivity.UserPREFERENCES,context.MODE_PRIVATE);
        setTime[0] = data_newdata.getString("morning_time","null");
        setTime[1] = data_newdata.getString("afternoon_time","null");
        setTime[2] = data_newdata.getString("evening_time","null");
        setTime[3] = data_newdata.getString("bedtime_time","null");

        int remind_interval = data_newdata.getInt("interval_progress",0);   //5min : 5min : 60min
        int remind_times = data_newdata.getInt("maximum_progress", 0);    //  1: 1 : 6
        int remind_after = data_newdata.getInt("remind_progress", 0);     //  0 : 10min : 120min (2 hr)

        System.out.println("Log start");
        for(int i = 0; i < 4; i++){
            System.out.println(setTime[i]);
        }
        System.out.println(remind_interval);
        System.out.println(remind_times);
        System.out.println(remind_after);
        System.out.println("Log end");


        Calendar[] c = new Calendar[4];



        //响闹钟
        test_alarm(context);



//要用这个的时候要取消注释

        Intent ii = new Intent();
        ii.setAction(AL1);
        ii.putExtra(EXTRA_DATA, command1);
        context.sendBroadcast(ii);

        //再次开启LongRunningService这个服务，从而可以
        Intent i = new Intent(context, MyService.class);
        context.startService(i);


    }





    public void test_alarm(Context context){
        SharedPreferences data_newdata=context.getSharedPreferences(MainActivity.UserPREFERENCES,context.MODE_PRIVATE);
        boolean neednotify = data_newdata.getBoolean("notification",true);
        if(neednotify) {
            boolean vibration = data_newdata.getBoolean("vibration", true);
            boolean sound = data_newdata.getBoolean("sound", false);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification(R.drawable.ic_launcher, ""
                    , System.currentTimeMillis());
            notification.setLatestEventInfo(context, "Please take the pill on time.",
                    "", null);
            notification.defaults = Notification.DEFAULT_ALL;

            if(sound){
                notification.defaults |= Notification.DEFAULT_SOUND;
            }else{
                notification.defaults &= ~Notification.DEFAULT_SOUND;
            }
            if(vibration){
                notification.defaults |= Notification.DEFAULT_VIBRATE;
            }else{
                notification.defaults &= ~Notification.DEFAULT_VIBRATE;
            }
            manager.notify(1, notification);
        }
    }


}




//设置通知内容并在onReceive()这个函数执行时开启
//*********************************************************************************************//
        /*

        SharedPreferences data_newdata=context.getSharedPreferences(MainActivity.UserPREFERENCES,context.MODE_PRIVATE);
        boolean neednotify = data_newdata.getBoolean("notification",true);
        if(neednotify) {
            boolean vibration = data_newdata.getBoolean("vibration", true);
            boolean sound = data_newdata.getBoolean("sound", false);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification(R.drawable.ic_launcher, ""
                    , System.currentTimeMillis());
            notification.setLatestEventInfo(context, "Please take the pill on time.",
                    "", null);
            notification.defaults = Notification.DEFAULT_ALL;

            if(sound){
                notification.defaults |= Notification.DEFAULT_SOUND;
            }else{
                notification.defaults &= ~Notification.DEFAULT_SOUND;
            }
            if(vibration){
                notification.defaults |= Notification.DEFAULT_VIBRATE;
            }else{
                notification.defaults &= ~Notification.DEFAULT_VIBRATE;
            }
            manager.notify(1, notification);
        }

        */
//*********************************************************************************************//

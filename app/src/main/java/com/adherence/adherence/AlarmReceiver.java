package com.adherence.adherence;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zentri.zentri_ble_command.ZentriOSBLEManager;

import java.util.Calendar;
import java.util.Date;

public class AlarmReceiver extends BroadcastReceiver {

    private ZentriOSBLEManager mZentriOSBLEManager;
    public static final String AL1 = "AL1";
    public static final String TAG = "AlarmReceiver";
    public static final String EXTRA_DATA = "EXTRA_DATA";
    private String command1 = "Scan";
    public static boolean flag = false;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "starting service");


        SharedPreferences data_newdata2 = PreferenceManager.getDefaultSharedPreferences(context);


        Log.d("test get pref", "" + data_newdata2.getBoolean(context.getString(R.string.pref_notification_category_notifi_switch_key), false));
        Log.d("test get pref", "" + data_newdata2.getBoolean(context.getString(R.string.pref_notification_category_vibrate_switch_key), false));
        Log.d("test get pref", "" + data_newdata2.getBoolean(context.getString(R.string.pref_notification_category_sound_switch_key), false));
        Log.d("test get pref", data_newdata2.getString(context.getString(R.string.pref_notification_category_after_key), "null"));
        Log.d("test get pref", data_newdata2.getString(context.getString(R.string.pref_notification_category_interval_key), "null"));
        Log.d("test get pref", data_newdata2.getString(context.getString(R.string.pref_notification_category_times_key), "null"));
        Log.d("test get pref", "" + data_newdata2.getInt(context.getString(R.string.pref_morning_seekBar_key), 0));
        Log.d("test get pref", "" + data_newdata2.getInt(context.getString(R.string.pref_afternoon_seekBar_key), 0));
        Log.d("test get pref", "" + data_newdata2.getInt(context.getString(R.string.pref_evening_seekBar_key), 0));
        Log.d("test get pref", "" + data_newdata2.getInt(context.getString(R.string.pref_bedtime_seekBar_key), 0));



        boolean isNotify = data_newdata2.getBoolean(context.getString(R.string.pref_notification_category_notifi_switch_key), false);
        boolean isVibrate = data_newdata2.getBoolean(context.getString(R.string.pref_notification_category_vibrate_switch_key), false);
        boolean isSound = data_newdata2.getBoolean(context.getString(R.string.pref_notification_category_sound_switch_key), false);
        String afterTime = data_newdata2.getString(context.getString(R.string.pref_notification_category_after_key), "null");
        String intervalTime = data_newdata2.getString(context.getString(R.string.pref_notification_category_interval_key), "null");
        String notifyTimes = data_newdata2.getString(context.getString(R.string.pref_notification_category_times_key), "null");
        int morning = data_newdata2.getInt(context.getString(R.string.pref_morning_seekBar_key), 0);
        int afternoon = data_newdata2.getInt(context.getString(R.string.pref_afternoon_seekBar_key), 0);
        int evening = data_newdata2.getInt(context.getString(R.string.pref_evening_seekBar_key), 0);
        int bedtime = data_newdata2.getInt(context.getString(R.string.pref_bedtime_seekBar_key), 0);







        Calendar[] c = new Calendar[4];












        //响闹钟
        test_alarm(context);

//start that activity
//        Intent intentPlay = new Intent(context, PlayAlarmAty.class);
//        intentPlay.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(intentPlay);



        flag = true;

        Intent blestartintent = new Intent(context, BluetoothService.class);
        context.startService(blestartintent);


//要用这个的时候要取消注释

//        Intent ii = new Intent();
//        ii.setAction(AL1);
//        ii.putExtra(EXTRA_DATA, command1);
//        context.sendBroadcast(ii);


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

            Notification.Builder builder = new Notification.Builder(context);

            builder.setSmallIcon(R.drawable.ic_launcher);
            builder.setTicker("Please take the pills on time.");
            builder.setContentTitle("Please take the pills on time.");
            builder.setContentText("");
            builder.setWhen(System.currentTimeMillis());
            Notification notification = builder.build();


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

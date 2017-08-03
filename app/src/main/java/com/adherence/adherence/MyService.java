package com.adherence.adherence;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.zentri.zentri_ble_command.BLECallbacks;
import com.zentri.zentri_ble_command.Command;
import com.zentri.zentri_ble_command.ErrorCode;
import com.zentri.zentri_ble_command.Result;
import com.zentri.zentri_ble_command.ZentriOSBLEManager;

import java.io.Serializable;
import java.util.Date;


public class MyService extends Service {
    private LocalBroadcastManager mLocalBroadcastManager;
    public static PendingIntent[] pendingIntents;
    long[] triggerAtTimes;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("StartStart");
        if(AdherenceApplication.flag == true) {

            AdherenceApplication.flag = false;
            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            //Notification time interval

            int Minutes = 10 * 1000;


        /*

        * 第一个参数有4种值可选，分别是ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC, RTC_WAKEUP。区别如下：

          ELAPSED_REALTIME       让定时任务的触发时间从系统开机开始算起，但不唤醒CPU
          ELAPSED_REALTIME_WAKEUP      让定时任务的触发时间从系统开机开始算起，但会唤醒CPU

          RTC             让定时任务的触发时间从1970年1月1日0点开始算起，但不唤醒CPU
          RTC_WAKEUP          让定时任务的触发时间从1970年1月1日0点开始算起，但会唤醒CPU

          SystemClock.elapsedRealtime()方法可以获取到系统开机至今所经历时间的毫秒数，

          System.currentTimeMillis()方法可以获取到1970年1月1日0点至今所经历时间的毫秒数

        *
        * */

            //SystemClock.elapsedRealtime() shows time starting from when the phone starts
            triggerAtTimes = new long[2];
            triggerAtTimes[0] = SystemClock.elapsedRealtime() + Minutes;
            triggerAtTimes[1] = SystemClock.elapsedRealtime() + 3 * Minutes;



            //start AlarmReceiver Service
            Intent inte = new Intent(this, AlarmReceiver.class);
            pendingIntents = new PendingIntent[2];
            for(int i = 0; i < pendingIntents.length; i++){
                pendingIntents[i] = PendingIntent.getBroadcast(this,i,inte, 0);
            }

            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTimes[0], pendingIntents[0]);
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTimes[1], pendingIntents[1]);
            manager.cancel(pendingIntents[1]);
        }


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.cancel(pi);

    }
}
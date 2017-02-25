package com.adherence.adherence;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zentri.zentri_ble_command.ZentriOSBLEManager;

public class AlarmReceiver extends BroadcastReceiver {

    private ZentriOSBLEManager mZentriOSBLEManager;
    public static final String AL1 = "AL1";
    public static final String TAG = "AlarmReceiver";
    public static final String EXTRA_DATA = "EXTRA_DATA";
    private String command1 = "Scan";

    @Override
    public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "starting service");


            //设置通知内容并在onReceive()这个函数执行时开启
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification(R.drawable.ic_launcher, "用电脑时间过长了！白痴！"
                    , System.currentTimeMillis());
            notification.setLatestEventInfo(context, "快去休息！！！",
                    "一定保护眼睛,不然遗传给孩子，老婆跟别人跑啊。", null);
            notification.defaults = Notification.DEFAULT_ALL;
            manager.notify(1, notification);


            //Intent iii = new Intent(context, ZentriOSBLEService.class);
            //context.startService(iii);

            Intent ii = new Intent();
            ii.setAction(AL1);
            ii.putExtra(EXTRA_DATA, command1);
            context.sendBroadcast(ii);


            //再次开启LongRunningService这个服务，从而可以
            Intent i = new Intent(context, MyService.class);
            context.startService(i);


    }


}

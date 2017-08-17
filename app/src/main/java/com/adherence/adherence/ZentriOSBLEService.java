package com.adherence.adherence;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.parse.ParseObject;
import com.zentri.zentri_ble_command.BLECallbacks;
import com.zentri.zentri_ble_command.Command;
import com.zentri.zentri_ble_command.CommandMode;
import com.zentri.zentri_ble_command.ErrorCode;
import com.zentri.zentri_ble_command.Result;
import com.zentri.zentri_ble_command.ZentriOSBLEManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ZentriOSBLEService extends Service implements Serializable {
    public static final String ACTION_SCAN_RESULT = "ACTION_SCAN_RESULT";
    public static final String ACTION_CONNECTED = "ACTION_CONNECTED";
    public static final String ACTION_DISCONNECTED = "ACTION_DISCONNECTED";
    public static final String ACTION_MODE_WRITE = "ACTION_MODE_WRITE";
    public static final String ACTION_MODE_READ = "ACTION_MODE_READ";
    public static final String ACTION_STRING_DATA_WRITE = "ACTION_STRING_DATA_WRITE";
    public static final String ACTION_BINARY_DATA_WRITE = "ACTION_BINARY_DATA_WRITE";
    public static final String ACTION_STRING_DATA_READ = "ACTION_STRING_DATA_READ";
    public static final String ACTION_BINARY_DATA_READ = "ACTION_BINARY_DATA_READ";
    public static final String ACTION_COMMAND_SENT = "ACTION_COMMAND_SENT";
    public static final String ACTION_COMMAND_RESULT = "ACTION_COMMAND_RESULT";
    public static final String ACTION_VERSION_READ = "ACTION_VERSION_READ";
    public static final String ACTION_ERROR = "ACTION_ERROR";

    public static final String ACTION_OTA_INIT = "ACTION_OTA_INIT";
    public static final String ACTION_OTA_CHECK = "ACTION_OTA_CHECK";
    public static final String ACTION_OTA_ABORT = "ACTION_OTA_ABORT";
    public static final String ACTION_OTA_START = "ACTION_OTA_START";
    public static final String ACTION_OTA_DATA_SENT = "ACTION_OTA_DATA_SENT";
    public static final String ACTION_OTA_DONE = "ACTION_OTA_DONE";
    public static final String ACTION_OTA_ERROR = "ACTION_OTA_ERROR";

    public static final String EXTRA_MODE = "EXTRA_MODE";
    public static final String EXTRA_DATA = "EXTRA_DATA";
    public static final String EXTRA_ID = "EXTRA_ID";
    public static final String EXTRA_COMMAND = "EXTRA_COMMAND";
    public static final String EXTRA_RESPONSE_CODE = "EXTRA_RESPONSE_CODE";
    public static final String EXTRA_ERROR = "EXTRA_ERROR";

    public static final String EXTRA_VERSION = "EXTRA_VERSION";
    public static final String EXTRA_NAME = "EXTRA_NAME";
    public static final String EXTRA_IS_UP_TO_DATE = "EXTRA_IS_UP_TO_DATE";

    public static final int SERVICES_NONE = 0;
    public static final int SERVICES_TRUCONNECT_ONLY = 1;
    public static final int SERVICES_OTA_ONLY = 2;
    public static final int SERVICES_BOTH = 3;

    private static final boolean DISABLE_TX_NOTIFY = true;

    private final String TAG = "ZentriOSBLEService";

    private final boolean TX_NOTIFY_DISABLE = true;

    private final int mStartMode = START_NOT_STICKY;
    private final IBinder mBinder = new LocalBinder();
    boolean mAllowRebind = true;
    private ZentriOSBLEManager mZentriOSBLEManager;

    private BLECallbacks mCallbacks;
    private LocalBroadcastManager mBroadcastManager;
    private ZentriBroadcastReceiver mEevc = new ZentriBroadcastReceiver();
    private IntentFilter filter;
    public static final String AL1 = "AL1";
    private final boolean NO_TX_NOTIFY_DISABLE = false;
    private boolean header_done = false;
    private int count_bytes;
    private Boolean gi = false;

    private ArrayList<Date> openTime=new ArrayList<>();
    private ArrayList<Date> shouldTime=new ArrayList<>();
    private boolean beforeTime = false;

    private String mFileNameLog;
    private boolean mRecording = false;
    private int len_image;
    private byte[] imBytes;
    private int receiveimage;
    private String allinfo="";
    private String deviceinfo;
    private boolean isBack=false;
    private int index=0;
    private String sessionToken;
    private int scan_times = 0;
    private Date now;
    private int now_day;
    private boolean needremid = false;

    public Calendar mycalendar = Calendar.getInstance();
    public String mDay;
    public int mDayint = -1;
    public int mDayintcom = -1;
    public String mydate;
    public String mytime;
    public boolean aiflag=false;


    public String photostring;
    public String photostring2;
    public HashMap<String, HashMap<Date, Boolean>> hmp = new HashMap<>();


    private Prescription[] prescriptions;
    private String[] medicineListHardcode;
    private String[] detailListHardcode;

    private RequestQueue mRequestQueue;

    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private Context mContext = this;


    ArrayList<String> devices = new ArrayList<String>();

    public class LocalBinder extends Binder {
        ZentriOSBLEService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ZentriOSBLEService.this;
        }
    }

    @Override
    public void onCreate() {
        // The service is being created
        Log.d(TAG, "Creating service");

        mZentriOSBLEManager = new ZentriOSBLEManager();
        mBroadcastManager = LocalBroadcastManager.getInstance(this);

        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        initCallbacks();
        initTruconnectManager();

        filter = new IntentFilter();
        filter.addAction(AL1);

        registerReceiver(mEevc, filter);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        SQLiteDatabase testdb = openOrCreateDatabase("Adherence_app.db", Context.MODE_PRIVATE, null);
        testdb.execSQL("CREATE TABLE IF NOT EXISTS DeviceTable (name VARCHAR PRIMARY KEY)");
        //testdb.delete("DeviceTable"," name = ? ", new String[]{"dfsds"});
        Cursor c = testdb.rawQuery("SELECT * FROM DeviceTable", null);
        while (c.moveToNext()) {
            String d_name = c.getString(c.getColumnIndex("name"));
            devices.add(d_name);
        }
        c.close();
        testdb.close();
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        Log.d(TAG, "starting service");
        sessionToken = intent.getStringExtra("sessionToken");
        Log.d(TAG, sessionToken);

        return mStartMode;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return mAllowRebind;
    }

    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
        Log.d(TAG, "Destroying service");
        unregisterReceiver(mEevc);
        if (mZentriOSBLEManager != null) {
            mZentriOSBLEManager.stopScan();
            mZentriOSBLEManager.disconnect(DISABLE_TX_NOTIFY);//ensure all connections are terminated
            mZentriOSBLEManager.deinit();
        }
    }

    public ZentriOSBLEManager getManager() {
        return mZentriOSBLEManager;
    }

    public boolean initTruconnectManager() {
        return mZentriOSBLEManager.init(ZentriOSBLEService.this, mCallbacks);
    }

    private void initCallbacks() {
        mCallbacks = new BLECallbacks() {
            @Override
            public void onScanResult(String deviceName, String address) {   //This address is mac address
                scan_times++;
                if (scan_times == 30) {
                    mZentriOSBLEManager.stopScan();
                    scan_times = 0;
                }
                if(deviceName!=null) {
                    Log.d(TAG, "onScanResult   " + deviceName + "  " + address);
                    deviceinfo = deviceName + "  " + address;

                    Intent intent = new Intent(ACTION_SCAN_RESULT);
                    intent.putExtra(EXTRA_DATA, deviceinfo);
                    mBroadcastManager.sendBroadcast(intent);
                    sendBroadcast(intent);
                    if(isBack && !devices.isEmpty()) {
                        if (deviceinfo != null && deviceinfo.equals(devices.get(index))) {
                            mZentriOSBLEManager.stopScan();
                            scan_times = 0;
                            //delay_while();
                            mZentriOSBLEManager.connect(deviceName);
                        }
                    }
                }
            }

            @Override
            public void onFirmwareVersionRead(String deviceName, String version) {
                Log.d(TAG, "onFirmwareVersionRead: " + deviceName + " version: " + version);
            }

            @Override
            public void onConnected(String deviceName, int services) {
                Log.d(TAG, "onConnected " + deviceName);

                Intent intent = new Intent(ACTION_CONNECTED);

                intent.putExtra(EXTRA_NAME, deviceName);
                intent.putExtra(EXTRA_DATA, services);

                //mBroadcastManager.sendBroadcast(intent);
                if (services == ZentriOSBLEService.SERVICES_NONE || services == ZentriOSBLEService.SERVICES_OTA_ONLY)
                {
                    mZentriOSBLEManager.disconnect(NO_TX_NOTIFY_DISABLE);
                }
                startDeviceInfoActivity();
            }

            public void transmitdata() throws ParseException {

                String[] info = allinfo.split("\n");
                String info_time = null;
                String info_units = null;
                String info_battery = null;
                String info_voltage = null;

                for(int i=0;i<info.length;i++){
                    Log.d(TAG, info[i]);
                    Log.d(TAG, i+"");
                    if(info[i].contains(":")){
                        info_time = info[i];
                    } else if(info[i].contains("Units")){
                        info_units = info[i];
                    } else if(info[i].contains("mAh")){
                        info_battery = info[i];
                    } else if(info[i].contains("V")){
                        info_voltage = info[i];
                    }
                }



                Log.d(TAG, info_time);
                String[] timestamp = info_time.split(",");



                String info_clock = timestamp[0];
                DateFormat df = new SimpleDateFormat("HH:mm:ss");
                Date currentTime = df.parse(info_clock);
                openTime.add(currentTime);

                String info_Date = timestamp[1];

                Log.d(TAG, info_clock);



                //push it to server
                Log.d(TAG, "start upload");
                ParseObject testObject1 = new ParseObject("BottleUpdates");
                testObject1.put("Name", deviceinfo);
                testObject1.put("timeStamp", info_time);
                testObject1.put("Units", info_units);
                testObject1.put("Battery", info_battery);
                testObject1.put("Voltage", info_voltage);
                testObject1.put("Photo", photostring);
                testObject1.put("Photo2", photostring2);


                Log.d(TAG, " "+photostring);

                //Update data
                testObject1.saveEventually();


                allinfo="";

                ///////////////////////////////////////////////////////////////////
            }


            @Override
            public void onDisconnected()
            {
                needremid = false;


                Log.d(TAG, "onDisconnected");

                for(Date shoutime: shouldTime){
                    for(Date open: openTime){
                        if(open.getTime() - shoutime.getTime() <= 7200000 || open.getTime() - shoutime.getTime() <= -7200000){
                            hmp.get(devices.get(index)).put(shoutime,true);
                            break;
                        }
                    }
                }

                for(Date shoutime: shouldTime){
                    if(now.getTime() - shoutime.getTime() > 0 && hmp.get(devices.get(index)).get(shoutime) == false){
                        needremid = true;
                    }
                }


                if(needremid == true) {
                    Notification.Builder notification = new Notification.Builder(ZentriOSBLEService.this);
                    notification.setSmallIcon(R.drawable.ic_launcher);
                    notification.setTicker("Please take the pills on time.");
                    notification.setContentTitle("Please take the pills on time.");
                    notification.setContentText("Wait to implement……");
                    notification.setWhen(System.currentTimeMillis());
                    Notification notify = notification.build();
                    mNotificationManager.notify(1, notify);
//                    Notification notification = new Notification(R.drawable.ic_launcher, ""
//                            , System.currentTimeMillis());
//                    notification.setLatestEventInfo(mContext, "Please take the pills on time.",
//                            "", null);
//                    notification.defaults = Notification.DEFAULT_ALL;
//
//                    notification.defaults |= Notification.DEFAULT_SOUND;
//
//                    mNotificationManager.notify(1, notification);

                }

                isBack = false;
                index = (index + 1) % devices.size() ;

                openTime.clear();
                shouldTime.clear();

                turnOffBluetooth();

            }
            @Override
            public void onModeWritten(int mode) {
                Log.d(TAG, "onModeWritten");

                Intent intent = new Intent(ACTION_MODE_WRITE);
                intent.putExtra(EXTRA_MODE, mode);
                //mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onModeRead(int mode) {
                Log.d(TAG, "onModeRead");

                Intent intent = new Intent(ACTION_MODE_READ);
                intent.putExtra(EXTRA_MODE, mode);
                //mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onStringDataWritten(String data) {
                Log.d(TAG, "onStringDataWritten - " + data);
                Intent intent = new Intent(ACTION_STRING_DATA_WRITE);
                intent.putExtra(EXTRA_DATA, data);
                //mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onBinaryDataWritten(byte[] data) {
                Log.d(TAG, "onBinaryDataWritten - Wrote " + data.length + " bytes");

                Intent intent = new Intent(ACTION_BINARY_DATA_WRITE);
                intent.putExtra(EXTRA_DATA, data);
                //mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onStringDataRead(String data) {
                Log.d(TAG, "onDataRead - " + data );

                Intent intent = new Intent(ACTION_STRING_DATA_READ);
                intent.putExtra(EXTRA_DATA, data);

                //mBroadcastManager.sendBroadcast(intent);
                if (data.equals("I")) {
                    mZentriOSBLEManager.setReceiveMode(com.zentri.zentri_ble.BLECallbacks.ReceiveMode.BINARY);
                    count_bytes = 0;
                    header_done = false;
                } else if (data.contains("N") && data.contains("*gn")) {
                    mZentriOSBLEManager.disconnect(NO_TX_NOTIFY_DISABLE);
                    gi = false;
                    aiflag = false;
                } else if(data.contains("erasing") || data.contains("Flash")){
                    mZentriOSBLEManager.disconnect(NO_TX_NOTIFY_DISABLE);
                    gi = false;
                    aiflag = false;
                } else if(data.contains("Inval") || data.contains("Command")){
                    String dataToSend = "*gn#";
                    mZentriOSBLEManager.writeData(dataToSend);
                } else {
                    if(data.contains("*gn")){
                        gi = false;
                    }
                    if(gi==false){
                        String dataToSend = "*gi#";
                        mZentriOSBLEManager.writeData(dataToSend);
                        gi = true;
                    }
                    Log.d(TAG, "Bytes: " + count_bytes);

                    if(aiflag == true){
                        try {
                            transmitdata();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        String dataToSend = "*gn#";
                        mZentriOSBLEManager.writeData(dataToSend);
                        //mZentriOSBLEManager.disconnect(NO_TX_NOTIFY_DISABLE);
                        gi = false;
                        aiflag = false;
                    }


                    allinfo = allinfo + data;
                    Log.d(TAG, "allinfo - " + allinfo );



                }


            }

            @Override
            public void onBinaryDataRead(byte[] data) {
                Log.d(TAG, "onBinaryDataRead");

                Intent intent = new Intent(ACTION_BINARY_DATA_READ);
                intent.putExtra(EXTRA_DATA, data);
                //mBroadcastManager.sendBroadcast(intent);

                if (mRecording) {
                    writeLog(data.toString());
                }
                if (!header_done) {
                    if (data.length == 1 && count_bytes == 0) {
                        len_image = data[0];
                        count_bytes++;
                    } else if (data.length == 1 && count_bytes == 1) {
                        len_image += data[0] * 256;
                        count_bytes--;
                        header_done = true;
                        imBytes = new byte[len_image];
                    } else if (data.length > 1) {
                        len_image = (data[0] & 0x00FF) | ((data[1] << 8) & 0xFF00);
                        header_done = true;
                        imBytes = new byte[len_image];

                        if (data.length > 2) {
                            System.arraycopy(data, 2, imBytes, count_bytes, data.length - 2);
                            count_bytes += data.length - 2;
                        }
                    }
                } else {
                    if (count_bytes + data.length > len_image) {
                        System.arraycopy(data, 0, imBytes, count_bytes, len_image - count_bytes);
                        count_bytes += (len_image - count_bytes);
                    } else {
                        System.arraycopy(data, 0, imBytes, count_bytes, data.length);
                        count_bytes += data.length;
                    }
                }

                //if (count_bytes < len_image) mZentriOSBLEManager.writeData("0");
                //mZentriOSBLEManager.writeData("0");

                if (count_bytes > 2 && imBytes[count_bytes - 2] == -1 && imBytes[count_bytes - 1] == -39) {
                    //if (count_bytes>=(len_image) && header_done) {
                    saveImage(imBytes);

                    photostring2 = conver2HexStr(imBytes);

                    try {
                        photostring = new String(imBytes, "ISO-8859-1");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }


//                            mToggleIm.setChecked(false);
                    doStopRecording();
                    stopRecording();

                    //save photos

                    mZentriOSBLEManager.setReceiveMode(com.zentri.zentri_ble.BLECallbacks.ReceiveMode.STRING);


                    String dataToSend = "*ai#";
                    mZentriOSBLEManager.writeData(dataToSend);


                    aiflag = true;

                }
            }

            @Override
            public void onCommandSent(int ID, Command command) {
                Log.d(TAG, "onCommandSent");

                Intent intent = new Intent(ACTION_COMMAND_SENT);
                intent.putExtra(EXTRA_ID, ID);
                intent.putExtra(EXTRA_COMMAND, command);
                //mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onCommandResult(int ID, Command command, Result result) {
                Log.d(TAG, "onCommandResult");

                Intent intent = new Intent(ACTION_COMMAND_RESULT);
                intent.putExtra(EXTRA_COMMAND, command);

                if (result != null) {
                    intent.putExtra(EXTRA_ID, ID);
                    intent.putExtra(EXTRA_RESPONSE_CODE, result.getResponseCode());
                    intent.putExtra(EXTRA_DATA, result.getData());
                }

                //mBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onError(ErrorCode error) {
                Intent intent = new Intent(ACTION_ERROR);
                intent.putExtra(EXTRA_ERROR, error);
                //mBroadcastManager.sendBroadcast(intent);

                Log.d(TAG, "onError - " + error);

            }
        };
    }

    public static int getMode(Intent intent) {
        return intent.getIntExtra(EXTRA_MODE, 0);
    }

    public static String getData(Intent intent) {
        return intent.getStringExtra(EXTRA_DATA);
    }

    public static byte[] getBinaryData(Intent intent) {
        return intent.getByteArrayExtra(EXTRA_DATA);
    }

    public static int getID(Intent intent) {
        return intent.getIntExtra(EXTRA_ID, ZentriOSBLEManager.ID_INVALID);
    }

    public static Command getCommand(Intent intent) {
        return (Command) intent.getSerializableExtra(EXTRA_COMMAND);
    }

    public static int getResponseCode(Intent intent) {
        return intent.getIntExtra(EXTRA_RESPONSE_CODE, -1);
    }

    public static ErrorCode getErrorCode(Intent intent) {
        return (ErrorCode) intent.getSerializableExtra(EXTRA_ERROR);
    }

    public static String getVersion(Intent intent) {
        return intent.getStringExtra(EXTRA_VERSION);
    }

    public static String getDeviceName(Intent intent) {
        return intent.getStringExtra(EXTRA_NAME);
    }

    public static int getIntData(Intent intent) {
        return intent.getIntExtra(EXTRA_DATA, -1);
    }

    public static byte getByteData(Intent intent) {
        return intent.getByteExtra(EXTRA_DATA, (byte) 0);
    }

    public class ZentriBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Madeshoudaole");

            boolean open = false;
            open = turnOnBluetooth();

            while(!open){
                Log.d(TAG, "turnonbluetooth");
            }

            devices.clear();



            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            SQLiteDatabase testdb = openOrCreateDatabase("Adherence_app.db", Context.MODE_PRIVATE, null);
            testdb.execSQL("CREATE TABLE IF NOT EXISTS DeviceTable (name VARCHAR PRIMARY KEY)");
            //testdb.delete("DeviceTable"," name = ? ", new String[]{"dfsds"});
            Cursor c = testdb.rawQuery("SELECT * FROM DeviceTable", null);
            while (c.moveToNext()) {
                String d_name = c.getString(c.getColumnIndex("name"));
                devices.add(d_name);
            }
            c.close();
            testdb.close();
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//            if (mZentriOSBLEManager != null && mZentriOSBLEManager.isConnected())
//            {
//                mZentriOSBLEManager.disconnect(NO_TX_NOTIFY_DISABLE);
//            }
            isBack =  true;
            gi = true;

            try {
                getDBinfo();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            System.out.println("BeforeTime : " + beforeTime);

            if (mZentriOSBLEManager != null && mZentriOSBLEManager.isConnected()) {
                mZentriOSBLEManager.disconnect(NO_TX_NOTIFY_DISABLE);
            }else if(!devices.isEmpty() && !beforeTime) {
                for(String scanDevice : AdherenceApplication.scanRecord){
                    System.out.println(scanDevice);
                    if(scanDevice.contains(devices.get(index))) {
                        int tmp_idx = scanDevice.indexOf("+");
                        int beacon = Integer.parseInt(scanDevice.substring(tmp_idx + 1));
                        if(beacon != 0){
                            System.out.println("Beacon = " + beacon);
                            mZentriOSBLEManager.startScan();
                        } else {
                            System.out.println("Beacon = 0");
                        }
                    }
                }
            }
        }
    }

    private void startDeviceInfoActivity() {
        //GUISetCommandMode();
        delay_while();
        mZentriOSBLEManager.setMode(ZentriOSBLEManager.MODE_COMMAND_REMOTE);
//        mZentriOSBLEManager.setSystemCommandMode(CommandMode.MACHINE);//set mode
//        mZentriOSBLEManager.getVersion();

        delay_while();
        mZentriOSBLEManager.setMode(ZentriOSBLEManager.MODE_STREAM);//set mode
        delay_while();
        mZentriOSBLEManager.setMode(ZentriOSBLEManager.MODE_STREAM);
//        delay_while();
//        mZentriOSBLEManager.setMode(ZentriOSBLEManager.MODE_STREAM);

        delay_while();
        String dataToSend = "*gn#";
        mZentriOSBLEManager.writeData(dataToSend);
        delay_while();
        mZentriOSBLEManager.writeData(dataToSend);
    }

    private boolean writeLog(String buffer) {
        String state = Environment.getExternalStorageState();
        File logFile = new File(mFileNameLog);

        if (Environment.MEDIA_MOUNTED.equals(state)) {

            try {
                FileOutputStream f = new FileOutputStream(logFile, true);

                PrintWriter pw = new PrintWriter(f);
                pw.print(buffer);
                pw.flush();
                pw.close();

                f.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            this.stopRecording();
            return false;
        } else {
            this.stopRecording();
            return false;
        }

        return true;
    }

    public void stopRecording() {
        mRecording = false;
    }

    private void doStopRecording() {
        this.stopRecording();
    }

    private void saveImage(byte[] data) {
        File sdCard = Environment.getExternalStorageDirectory();
        String fileName = sdCard.getAbsolutePath() + "/image"+ deviceinfo +".jpg";
        File testimage = new File(fileName);

        if (testimage.exists()) {
            testimage.delete();
        }

        try {
            FileOutputStream fos = new FileOutputStream(testimage.getPath());
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getDBinfo() throws ParseException {

        mycalendar = Calendar.getInstance();
        String int_day=String.valueOf(mycalendar.get(mycalendar.DAY_OF_WEEK));
        mDayintcom = mycalendar.get(mycalendar.DAY_OF_WEEK);



        switch (int_day){
            case "1":mDay="Sunday";break;
            case "2":mDay="Monday";break;
            case "3":mDay="Tuesday";break;
            case "4":mDay="Wednesday";break;
            case "5":mDay="Thursday";break;
            case "6":mDay="Friday";break;
            case "7":mDay="Saturday";break;
            default:mDay="Sunday";break;
        }

        mytime = new SimpleDateFormat("HH:mm:ss").format(new Date());
        DateFormat dff = new SimpleDateFormat("HH:mm:ss");
        now = dff.parse(mytime);
        now_day = mycalendar.get(mycalendar.DAY_OF_WEEK);


        mRequestQueue= Volley.newRequestQueue(getApplicationContext());
        String url=getString(R.string.parseURL) + "/patient/prescriptions";
        final JsonArrayRequest prescriptionRequest=new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d("response",response.toString());

                //retrive data from JSONobject
                int i = response.length();
                medicineListHardcode=new String[i];
                detailListHardcode=new String[i];



                prescriptions = new Prescription[i];
                for (int j = 0;j < i;j++){
                    prescriptions[j] = new Prescription();
                    try {
                        JSONObject prescript = response.getJSONObject(j);
                        prescriptions[j].setName(prescript.getString("prescriptionName"));
                        if(prescript.has("bottle")){
                            if(prescript.getJSONObject("bottle").has("bottleName")){
                                prescriptions[j].setBottleName(prescript.getJSONObject("bottle").getString("bottleName"));
                            }
                            if(prescript.getJSONObject("bottle").has("pillNumber")){
                                prescriptions[j].setPillNumber(prescript.getJSONObject("bottle").getInt("pillNumber"));
                            }
                        }else{
                            prescriptions[j].setBottleName("null");
                            prescriptions[j].setPillNumber(0);
                        }
                        if(prescript.has("note")) {
                            prescriptions[j].setNote(prescript.getString("note"));
                        }else{
                            prescriptions[j].setNote("none");
                        }
                        if(prescript.has("pill")) {
                            prescriptions[j].setPill(prescript.getString("pill"));
                        }else{
                            prescriptions[j].setPill("none");
                        }
                        if(prescript.has("newAdded")){
                            prescriptions[j].setNewAdded(prescript.getBoolean("newAdded"));
                        }else {
                            prescriptions[j].setNewAdded(false);
                        }
                        //prescriptions[j].setPrescriptionId(prescript.getString("objectId"));

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
                // test if data stored in prescriptions

                for(int j = 0; j < i; j++) {

                    System.out.println(prescriptions[j].getName());
                    medicineListHardcode[j]=prescriptions[j].getName();
                    System.out.println(prescriptions[j].getNote());
                    detailListHardcode[j]=prescriptions[j].getNote();

                    System.out.println(prescriptions[j].getBottleName());
                    //////////////////////////////////////////////////////////////////////////////////////////////////////
                    if(prescriptions[j].getBottleName().contains(":")) {
                        SQLiteDatabase testdb = openOrCreateDatabase("Adherence_app.db", Context.MODE_PRIVATE, null);
                        testdb.execSQL("CREATE TABLE IF NOT EXISTS DeviceTable (name VARCHAR PRIMARY KEY)");
                        testdb.execSQL("REPLACE INTO DeviceTable VALUES (?)", new Object[]{prescriptions[j].getBottleName()});
                        testdb.close();
                    }
                    ////////////////////////////////////////////////////////////////////////////////////////////////
                    System.out.println(prescriptions[j].getNewAdded());
                    System.out.println(prescriptions[j].getPillNumber());




                    //Get information of Monday
        /* [{"prescriptionName":"Oxycontin 30mg","pill":"Oxycontin","schedule":[{"days":[{"amount":0,"name":"Sunday"},
        {"amount":0,"name":"Monday"},{"amount":1,"name":"Tuesday"},{"amount":1,"name":"Wednesday"},
        {"amount":0,"name":"Thursday"},{"amount":0,"name":"Friday"},{"amount":1,"name":"Saturday"}],
        "time":"1970-01-01T20:00:00.000Z"}],"bottle":{"bottleName":"SC36-05  4C:55:CC:10:7B:12",
        "pillNumber":5},"note":"Take it with hot water"},{"prescriptionName":"Vicodin 35mg",
        "pill":"Vicodin","schedule":[{"days":[{"amount":0,"name":"Sunday"},{"amount":1,"name":"Monday"},
        {"amount":1,"name":"Tuesday"},{"amount":1,"name":"Wednesday"},
        {"amount":1,"name":"Thursday"},{"amount":1,"name":"Friday"},
        {"amount":1,"name":"Saturday"}],"time":"1970-01-01T08:00:00.000Z"},
        {"days":[{"amount":1,"name":"Sunday"},{"amount":0,"name":"Monday"},
        {"amount":0,"name":"Tuesday"},{"amount":0,"name":"Wednesday"},
        {"amount":0,"name":"Thursday"},{"amount":0,"name":"Friday"},
        {"amount":0,"name":"Saturday"}],"time":"1970-01-01T12:00:00.000Z"}],
        "bottle":{"bottleName":"Adderal","pillNumber":5},"newAdded":true,"note":"Take 1 a day"},{"prescriptionName":"Antacid 30mg","pill":"Antacid","schedule":[{"days":[{"amount":1,"name":"Sunday"},{"amount":1,"name":"Monday"},{"amount":1,"name":"Tuesday"},{"amount":1,"name":"Wednesday"},{"amount":1,"name":"Thursday"},{"amount":1,"name":"Friday"},{"amount":1,"name":"Saturday"}],"time":"1970-01-01T13:00:00.000Z"}],"bottle":{"bottleName":"Adderal","pillNumber":5},"newAdded":true,"note":"Preferably take the pill with a snack or a meal"},{"prescriptionName":"Adderall 35 mg","pill":"Adderall","schedule":[{"days":[{"amount":1,"name":"Sunday"},{"amount":1,"name":"Monday"},{"amount":1,"name":"Tuesday"},{"amount":1,"name":"Wednesday"},{"amount":1,"name":"Thursday"},{"amount":1,"name":"Friday"},{"amount":1,"name":"Saturday"}],"time":"1970-01-01T21:30:00.000Z"}],"bottle":{"bottleName":"Adderal","pillNumber":5},"newAdded":true,"note":"Preferably take the pill with a snack or a meal"}]
        */

                    System.out.println("Query Today's Prescription");
                    if(prescriptions[j].getBottleName().equals(devices.get(index))){
                        System.out.println("Find the bottle");
                    }

                    if(mDayintcom != mDayint){
                        mDayint = mDayintcom;
                        hmp.put(prescriptions[j].getBottleName(),null);
                    }

                    Iterator<Map.Entry<String, Integer>> itr = prescriptions[j].getTimeAmount(mDay).entrySet().iterator();
                    HashMap<Date, Boolean> tempMap = new HashMap<>();
                    while(itr.hasNext()){
                        Map.Entry<String, Integer> entry = itr.next();

                        if(prescriptions[j].getBottleName().equals(devices.get(index))){
                            DateFormat df = new SimpleDateFormat("HH:mm:ss");
                            Date sTime = null;
                            try {
                                sTime = df.parse(entry.getKey());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            if(hmp.containsKey(prescriptions[j].getBottleName()) && hmp.get(prescriptions[j].getBottleName()) != null && hmp.get(prescriptions[j].getBottleName()).containsKey(sTime)) {
                                tempMap.put(sTime, hmp.get(prescriptions[j].getBottleName()).get(sTime));
                            }else{
                                tempMap.put(sTime, false);
                            }
                            shouldTime.add(sTime);
                        }

                        System.out.println("Key:  "+entry.getKey());
                        System.out.println("Value:  "+entry.getValue());

                    }

//                    if(shouldTime.size() == 0){
//                        beforeTime = true;
//                    }else{
//                        beforeTime = false;
//                        hmp.put(prescriptions[j].getBottleName(), tempMap);
//                    }

                    System.out.println("Print shtime");

                    for(Date shtime: shouldTime){
                        System.out.println(shtime);
                    }


                    System.out.println("Query end");


                    //traverse with Map.Entry
                    Iterator<Map.Entry<String, Map<String, Integer>>> it = prescriptions[j].getSchedule().entrySet().iterator();

                    while (it.hasNext()) {

                        // entry.getKey() return key
                        // entry.getValue() return value
                        Map.Entry<String, Map<String, Integer>> entry = (Map.Entry) it.next();
                        System.out.println(entry.getKey());

                        HashMap<String, Integer> tmp_in_hashmap = (HashMap) entry.getValue();

                        Iterator<Map.Entry<String, Integer>> in_iterator = tmp_in_hashmap
                                .entrySet().iterator();

                        while (in_iterator.hasNext()) {
                            Map.Entry in_entry = (Map.Entry) in_iterator.next();
                            System.out.println(in_entry.getKey() + ":" + in_entry.getValue());
                        }
                    }

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error",error.toString());
            }
        }) {
            @Override
            public Map<String,String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("x-parse-session-token",sessionToken);
                return headers;
            }
        };

        mRequestQueue.add(prescriptionRequest);
        ///////////////////////////////////////////////////////////////////
    }

    public String conver2HexStr(byte [] b)
    {
        StringBuffer result = new StringBuffer();
        for(int i = 0;i<b.length;i++)
        {
            result.append(b[i]+",");
        }
        return result.toString().substring(0, result.length()-1);
    }


    public void delay_while()
    {
        int i=0,j=0,k=0;
        while(i<500)
        {
            while(j<500)
            {
                while(k<500)
                {
                    k=k+1;
                }
                j=j+1;
                k=0;
            }
            i=i+1;
            j=0;
        }
    }


    /**
     * 强制开启当前 Android 设备的 Bluetooth
     *
     * @return true：强制打开 Bluetooth　成功　false：强制打开 Bluetooth 失败
     */
    public static boolean turnOnBluetooth()
    {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();

        if (bluetoothAdapter != null)
        {
            return bluetoothAdapter.enable();
        }

        return false;
    }


    /**
     * 强制关闭当前 Android 设备的 Bluetooth
     *
     * @return  true：强制关闭 Bluetooth　成功　false：强制关闭 Bluetooth 失败
     */
    public static boolean turnOffBluetooth()
    {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();

        if (bluetoothAdapter != null)
        {
            return bluetoothAdapter.disable();
        }

        return false;
    }

}



package com.adherence.adherence;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.content.IntentFilter;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
import java.util.ArrayList;
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


    private Prescription[] prescriptions;
    private String[] medicineListHardcode;
    private String[] detailListHardcode;

    private RequestQueue mRequestQueue;


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
            public void onScanResult(String deviceName, String address) {   //这个address就是mac address
                scan_times++;
                if (scan_times == 60) {
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
                    if(isBack) {

                        if (deviceinfo != null && deviceinfo.equals(devices.get(index))) {
                            mZentriOSBLEManager.stopScan();
                            scan_times = 0;
                            //delay_while();
                            mZentriOSBLEManager.connect(deviceName);

                        }

                    }




//                if (deviceName != null && deviceName.contains("SC")) {
//                    mZentriOSBLEManager.stopScan();
//                    delay_while();
//                    mZentriOSBLEManager.connect(deviceName);
//                }
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
                //delay_while();
                //delay_while();
                startDeviceInfoActivity();
            }

            @Override
            public void onDisconnected() {

                Log.d(TAG, "onDisconnected");

                Intent intent = new Intent(ACTION_DISCONNECTED);
                isBack = false;
                index = (index + 1) % devices.size() ;
                //mBroadcastManager.sendBroadcast(intent);
                //String int_day=String.valueOf(c.get(c.DAY_OF_WEEK));

                ///////////////////////////////////////////////////////////////////


                String[] info = allinfo.split("\n");

                for(int i=0;i<info.length;i++){
                    Log.d(TAG, info[i]);
                    Log.d(TAG, i+"");
                }


                String info_time = info[1];
                Log.d(TAG, info_time);
                String[] timestamp = info_time.split(",");



                String info_clock = timestamp[0];
                String info_Date = timestamp[1];

                Log.d(TAG, info_clock);

                String info_units = info[2];
                String info_battery = info[3];
                String info_voltage = info[4];


//这段的错误信息：02-08 09:05:38.611 11991-11991/com.adherence.adherence D/score: Error: java.lang.NullPointerException

//                ParseQuery<ParseObject> query = ParseQuery.getQuery("Bottle");
//                query.whereEqualTo("Name", deviceinfo);
//                query.findInBackground(new FindCallback<ParseObject>() {
//                    @Override
//                    public void done(List<ParseObject> objects, ParseException e) {
//                        if(e == null) {
//                            Log.d("score", objects.get(0).getString("Name"));
//                        } else {
//                            Log.d("score", "Error: " + e.getMessage());
//                        }
//                    }
//                });


                ParseObject testObject = new ParseObject("TestXZ");

                testObject.put("name", "Christina");
                testObject.saveEventually();

                //push it to server
                ParseObject testObject1 = new ParseObject("BottleUpdates");
                testObject1.put("Name", deviceinfo);
                testObject1.put("timeStamp", info_time);
                testObject1.put("Units", info_units);
                testObject1.put("Battery", info_battery);
                testObject1.put("Voltage", info_voltage);

                //要用回Parse
                testObject1.saveEventually();


                allinfo="";

                ///////////////////////////////////////////////////////////////////

                mRequestQueue= Volley.newRequestQueue(getApplicationContext());
                String url="http://129.105.36.93:5000/patient/prescriptions";
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
                            System.out.println(prescriptions[j].getNewAdded());
                            System.out.println(prescriptions[j].getPillNumber());



                            Iterator<Map.Entry<String, Integer>> itr = prescriptions[j].getTimeAmount("Monday").entrySet().iterator();
                            while(itr.hasNext()){
                                Map.Entry<String, Integer> entry = itr.next();
                                System.out.println(entry.getKey());
                                System.out.println(entry.getValue());
                            }


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
                }){
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
                } else if (data.equals("N")) {
                    ;
                } else if(data.contains("Inval") || data.contains("Command")){
                    String dataToSend = "*gn#";
                    mZentriOSBLEManager.writeData(dataToSend);
                } else {
//                    if (gi == true) {
//                        String dataToSend = "*ai#";
//                        mZentriOSBLEManager.writeData(dataToSend);
//                        gi = false;
//                    }
                    //else
                    if(data.contains("*gn")){
                        gi = false;
                    }
                    if(gi==false){
                        String dataToSend = "*gi#";
                        mZentriOSBLEManager.writeData(dataToSend);
                        //delay_while();
                        //delay_while();
                        //mZentriOSBLEManager.writeData(dataToSend);
                        gi = true;
                    }
                    Log.d(TAG, "Bytes: " + count_bytes);

                    if(receiveimage==1) {
                        allinfo = allinfo + data;
                        Log.d(TAG, "allinfo - " + allinfo );
                        if (allinfo.contains(" V")) {
                            mZentriOSBLEManager.disconnect(NO_TX_NOTIFY_DISABLE);
                            gi = false;

                            receiveimage=0;
                        }
                    }
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
//                            mToggleIm.setChecked(false);
                    doStopRecording();
                    stopRecording();
                    Log.d(TAG, "SDFADSFASDFASFASFASFASFA   photo show");
                    //显示图片

                    mZentriOSBLEManager.setReceiveMode(com.zentri.zentri_ble.BLECallbacks.ReceiveMode.STRING);
                    receiveimage=1;

                }
                Log.d(TAG, "Bytes: " + count_bytes + "Val: " + data[0]);

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
            if (mZentriOSBLEManager != null && mZentriOSBLEManager.isConnected())
            {
                mZentriOSBLEManager.disconnect(NO_TX_NOTIFY_DISABLE);
            }
            isBack =  true;
            gi = true;
            mZentriOSBLEManager.startScan();


        }
    }

    private void startDeviceInfoActivity() {
        //GUISetCommandMode();
        delay_while();
        mZentriOSBLEManager.setMode(ZentriOSBLEManager.MODE_COMMAND_REMOTE);
        mZentriOSBLEManager.setSystemCommandMode(CommandMode.MACHINE);//第一个set mode
        mZentriOSBLEManager.getVersion();
        //startActivity(new Intent(getApplicationContext(), DeviceInfoActivity.class));
        //delay_while();
        delay_while();
        mZentriOSBLEManager.setMode(ZentriOSBLEManager.MODE_STREAM);//第二个set mode
        //delay_while();
        delay_while();
        mZentriOSBLEManager.setMode(ZentriOSBLEManager.MODE_STREAM);
        //delay_while();
        delay_while();
        mZentriOSBLEManager.setMode(ZentriOSBLEManager.MODE_STREAM);


        //delay_while();
        //delay_while();
        //delay_while();
        //delay_while();
        //delay_while();
        delay_while();
        String dataToSend = "*gn#";
        mZentriOSBLEManager.writeData(dataToSend);
        //delay_while();
        //delay_while();
        //delay_while();
        delay_while();

        mZentriOSBLEManager.writeData(dataToSend);
        //gi = false;
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
}


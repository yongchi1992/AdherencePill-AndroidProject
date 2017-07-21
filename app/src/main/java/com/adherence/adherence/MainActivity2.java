package com.adherence.adherence;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;
import com.zentri.zentri_ble.BLECallbacks.ReceiveMode;
import com.zentri.zentri_ble_command.Command;
import com.zentri.zentri_ble_command.CommandMode;
import com.zentri.zentri_ble_command.ErrorCode;
import com.zentri.zentri_ble_command.Result;
import com.zentri.zentri_ble_command.ZentriOSBLEManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity2 extends Activity implements com.adherence.adherence.ServiceCallbacks
{
    private static final long SCAN_PERIOD = 5000;
    private static final long CONNECT_TIMEOUT_MS = 20000;
    private static final String TAG = "SmartCap";
    private final int BLE_ENABLE_REQ_CODE = 1;

    private final boolean NO_TX_NOTIFY_DISABLE = false;

    private ProgressDialog mConnectProgressDialog;
    private DeviceList mDeviceList;
    private Button mScanButton;
    private Button mFinishButton;
    private Button mDSButton;

    private Handler mHandler;
    private Runnable mStopScanTask;
    private Runnable mConnectTimeoutTask;

    private ZentriOSBLEManager mZentriOSBLEManager;
    private boolean mConnecting = false;
    private boolean mConnected = false;
    private boolean mErrorDialogShowing = false;

    private String mCurrentDeviceName;

    private ServiceConnection mConnection;
    private ZentriOSBLEService mService;
    private boolean mBound = false;

    //private MyService myService;
    private boolean bound = false;

    private LocalBroadcastManager mLocalBroadcastManager;
    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mReceiverIntentFilter;

    // Connected stuff
    //private ToggleButton mModeButton;
    private int mCurrentMode;
    //private Button mSendTextButton;
    //private Button AutoPhoto;
    //private EditText mTextToSendBox;
    private TextView mReceivedDataTextBox;
    private ScrollView mScrollView;
    private Button mClearTextButton;
    //private ToggleButton mToggleIm;
    private Button mShowIm;
    private ImageView imView;

    private ProgressDialog mDisconnectDialog;
    private boolean mDisconnecting = false;
    private Runnable mDisconnectTimeoutTask;

    private boolean x = false;

    private boolean mRecording = false;
    private String mFileNameLog;
    private byte[] imBytesSplit;
    private byte[] imBytes;
    private int count_bytes;
    private int len_image;
    private int val;
    private boolean header_done = false;

    private String temp = "";
    private Boolean gi = false;

    private String sessionToken;

    Calendar timeNow;

    private Set<String> ValidDevice = new HashSet<>();    //GL: use a set to store the valid device name
    private HashMap<String, Long> TimeTable = new HashMap<>();  //time of latest data requirement

    int RSSI;
    ArrayList<String> values = new ArrayList<String>();
    ArrayAdapter<String> newadapter;

    private Boolean delete_save=false;
    DBHelper mydb;

    public static final String AL1 = "AL1";
    public static final String EXTRA_DATA = "EXTRA_DATA";
    private String command1 = "Scan";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        SharedPreferences data=getSharedPreferences("data",MODE_PRIVATE);
        sessionToken=data.getString("sessionToken","null");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        SQLiteDatabase testdb = openOrCreateDatabase("Adherence_app.db", Context.MODE_PRIVATE, null);
        testdb.execSQL("CREATE TABLE IF NOT EXISTS DeviceTable (name VARCHAR PRIMARY KEY)");
        //testdb.delete("DeviceTable"," name = ? ", new String[]{"dfsds"});
        Cursor c = testdb.rawQuery("SELECT * FROM DeviceTable", null);
        while (c.moveToNext()) {
            String d_name = c.getString(c.getColumnIndex("name"));
            values.add(d_name);
        }
        c.close();
        testdb.close();
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        ValidDevice.add("SCv31b1");
        initScanButton();//设置Scan Button 的 onClickListener
        initDeviceList();//初始化，弄那个initialiseListviewListener，你一点就连接那个
        initBroadcastManager();//获取LocalBroadcastManager实例
        initServiceConnection();//service connection
        initBroadcastReceiver();// Receive the broadcast
        initReceiverIntentFilter();//设置过滤的信息


        Intent openservice = new Intent(this, ZentriOSBLEService.class);
        openservice.putExtra("sessionToken", sessionToken);
        startService(openservice);


        //startService(new Intent(this, MyService.class));

        mHandler = new Handler();

        mStopScanTask = new Runnable()
        {
            @Override
            public void run()
            {
                stopScan();
            }
        };

        mConnectTimeoutTask = new Runnable()
        {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showErrorDialog(R.string.error, R.string.con_timeout_message);
                        dismissConnectDialog();
                        if (mZentriOSBLEManager != null && mZentriOSBLEManager.isConnected()) {
                            disconnect(NO_TX_NOTIFY_DISABLE);
                        }
                    }
                });
            }
        };

        mReceivedDataTextBox = (TextView) findViewById(R.id.receivedDataBox);
        mScrollView = (ScrollView) findViewById(R.id.scroll_view);

        mDisconnectTimeoutTask = new Runnable()
        {
            @Override
            public void run()
            {
                dismissProgressDialog();
                showErrorDialog(R.string.error, R.string.discon_timeout_message);
            }
        };

//        original hard code data

        ListView currentView = (ListView) findViewById(R.id.currentList);
        newadapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);


        currentView.setAdapter(newadapter);

        currentView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(delete_save==true) {
                    String deleteDeviceName = newadapter.getItem(position);
                    SQLiteDatabase testdb = openOrCreateDatabase("Adherence_app.db", Context.MODE_PRIVATE, null);
                    testdb.delete("DeviceTable"," name = ? ", new String[]{deleteDeviceName});
                    testdb.close();
                    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    values.remove(deleteDeviceName);
                    newadapter.notifyDataSetChanged();
                }

            }
        });

//        delay_while();delay_while();delay_while();delay_while();delay_while();
        Intent ii = new Intent();
        ii.setAction(AL1);
        ii.putExtra(EXTRA_DATA, command1);
        sendBroadcast(ii);


    }
    /*       以下的部分是自动启动扫描     */
    private void auto_connect() {
        for (int i = 0; i < values.size(); i++) {
            mCurrentDeviceName = values.get(i);
            if(mDeviceList.findDeviceWithName(mCurrentDeviceName) != null) {

                showToast("work at au_co", Toast.LENGTH_SHORT);

                if (!mConnecting) {
                    mConnecting = true;

                    stopScan();
                    Log.d(TAG, "Connecta_c to BLE device " + mCurrentDeviceName);
                    mZentriOSBLEManager.connect(mCurrentDeviceName);
                    Log.d(TAG,"dfasfasdfasdfasdfasdfadsfdsafa" + mCurrentDeviceName);

                    mHandler.postDelayed(mConnectTimeoutTask, CONNECT_TIMEOUT_MS);
                }
            }
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

    public void auto_photo(){
        Log.d(TAG, "SDFADSFASDFASFASFASFASFA   auto_photo");
        String dataToSend = "*gn#";
        mZentriOSBLEManager.writeData(dataToSend);
        Log.d(TAG, "Sent: " + dataToSend);
        // try{ Thread.sleep(10000); }catch(InterruptedException e){ }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId())
        {
            case R.id.action_about:
                openAboutDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        mDeviceList.clear();
        mConnected = false;
        mConnecting = false;
        //Intent intent1 = new Intent(this, MyService.class);
        //bindService(intent1, serviceConnection, Context.BIND_AUTO_CREATE);

        Intent intent = new Intent(this, ZentriOSBLEService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mReceiverIntentFilter);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        mHandler.removeCallbacks(mStopScanTask);
        cancelConnectTimeout();
        dismissConnectDialog();
        if (bound) {
            //myService.setCallbacks(null); // unregister
            unbindService(serviceConnection);
            bound = false;
        }
        if (mBound)
        {
            mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
            unbindService(mConnection);
            mBound = false;
        }

        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        stopService(new Intent(this, ZentriOSBLEService.class));
        stopService(new Intent(this, MyService.class));
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            //MyService.LocalBinder binder = (MyService.LocalBinder) service;
            //myService = binder.getService();
            bound = true;
            //myService.setCallbacks(MainActivity2.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    /*
    LG:  Background service run this algorithm
     */
    @Override
    public void doSomething() {
        // try{ Thread.sleep(3000); }catch(InterruptedException e){ }
        Timer mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        ChooseAndConnect(); // do your work right here
                    }
                });
            }
        }, 20000, 20000);                           //scan time
//    }, 20, 20);                           //scan time
    }

    //LG: iterate the bluetooth device list and choose the valid device name
    private void ChooseAndConnect() {
        startScan();
        int Len = mDeviceList.count();
        for(int i = 0; i < Len; i++) {
            String name = mDeviceList.get(i);
            if (ValidDevice.contains(name)) {    //if it has been 2000 since last connected, connected.
                if(TimeTable.containsKey(name)) {
                    long time = System.currentTimeMillis();
                    long lasttime = TimeTable.get(name);
                    if (lasttime - time > 2000) {
                        TimeTable.put(name, time);
                        auto(name);
                    }
                }
                else {
                    TimeTable.put(name, System.currentTimeMillis());
                    auto(name);
                }
            }
        }
    }

    //LG: connect to the specific device (chose by name)
    private void auto(String name){
        // while (!mConnecting) {
        stopScan();
        if(mDeviceList.findDeviceWithName(name) != null) {
            Log.d(TAG, "Connecauto to BLE device " + name);
            mCurrentDeviceName = name;
            mZentriOSBLEManager.connect(mCurrentDeviceName);
            // showConnectingDialog(view.getContext());
            mHandler.postDelayed(mConnectTimeoutTask, CONNECT_TIMEOUT_MS);
            mConnecting = true;
            Log.d(TAG,"SDFADSFASDFASFASFASFASFA   111");
        }
        //    }

        Log.d(TAG,"SDFADSFASDFASFASFASFASFA   connected");
        while(mZentriOSBLEManager == null || !mZentriOSBLEManager.isConnected()) {

        }
        if (mZentriOSBLEManager != null && mZentriOSBLEManager.isConnected()) {
            Log.d(TAG, "SDFADSFASDFASFASFASFASFA   entered");
            mCurrentMode = ZentriOSBLEManager.MODE_STREAM;
            mZentriOSBLEManager.setMode(mCurrentMode);
            Log.d(TAG, "SDFADSFASDFASFASFASFASFA   mode");
            String dataToSend = "*gn#";

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    auto_photo();
                }
            }, 4500);
            //try{ Thread.sleep(3000); }catch(InterruptedException e){ }
            //auto_photo();

        }
        Log.d(TAG, "SDFADSFASDFASFASFASFASFA   out");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == BLE_ENABLE_REQ_CODE)
        {
            mService.initTruconnectManager();//try again
            if (mZentriOSBLEManager.isInitialised())
            {
                startScan();
            }
            else
            {
                showUnrecoverableErrorDialog(R.string.init_fail_title, R.string.init_fail_msg);
            }
        }
    }

    private void initScanButton()
    {
        mScanButton = (Button) findViewById(R.id.button_scan);
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDeviceList.clear();
                startScan();
            }
        });

        mFinishButton = (Button) findViewById(R.id.finishAdd);
        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDeviceList.clear();
                //pass the arraylist of device name to the alarm manager and then pass to the BLEService

                //try to jump to another activity
                /*
                Intent triggerNext = new Intent(MainActivity2.this,NextActivity.class);
                startActivity(triggerNext);*/
            }
        });
        mDSButton = (Button) findViewById(R.id.delete_save);
        mDSButton.setText("Delete");
        mDSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(delete_save==false){
                    mDSButton.setText("Save");
                    delete_save=true;

                }
                else{
                    mDSButton.setText("Delete");
                    delete_save=false;
                }
            }
        });

    }

    private void initDeviceList()
    {
        ListView deviceListView = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listitem, R.id.textView);

        initialiseListviewListener(deviceListView);
        mDeviceList = new DeviceList(adapter, deviceListView);
    }

    private void initServiceConnection()
    {
        mConnection = new ServiceConnection()
        {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service)
            {
                ZentriOSBLEService.LocalBinder binder = (ZentriOSBLEService.LocalBinder) service;
                mService = binder.getService();
                mBound = true;

                mZentriOSBLEManager = mService.getManager();
                if(!mZentriOSBLEManager.isInitialised())
                {
                    startBLEEnableIntent();
                }
                //else
                //{
                //    startScan();
                //}
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0)
            {
                mBound = false;
            }
        };
    }

    private void initBroadcastReceiver()
    {
        mBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                // Get extra data included in the Intent
                String action = intent.getAction();

                Log.d(TAG, "Received intent " + intent);

                switch (action)
                {
                    case ZentriOSBLEService.ACTION_SCAN_RESULT:
                        addDeviceToList(ZentriOSBLEService.getData(intent));

                        break;

                    case ZentriOSBLEService.ACTION_CONNECTED:
                        String deviceName = ZentriOSBLEService.getDeviceName(intent);
                        int services = ZentriOSBLEService.getIntData(intent);

                        cancelConnectTimeout();
                        dismissConnectDialog();

                        //RSSI = intent.getShortExtra(mBroadcastReceiver.EXTRA_RSSI,Short.MIN_VALUE);

                        //if no truconnect services
                        if (services == ZentriOSBLEService.SERVICES_NONE ||
                                services == ZentriOSBLEService.SERVICES_OTA_ONLY)
                        {
                            showErrorDialog(R.string.error, R.string.error_service_disc);
                            disconnect(NO_TX_NOTIFY_DISABLE);
                        }
                        else if (!mConnected)
                        {
                            mConnected = true;
                            showToast("Connected to " + deviceName, Toast.LENGTH_SHORT);
                            Log.d(TAG, "Connected to " + deviceName);

                            startDeviceInfoActivity();
                        }
                        break;

                    case ZentriOSBLEService.ACTION_DISCONNECTED:
                        setDisconnectedState();
                        dismissConnectDialog();
                        cancelConnectTimeout();
                        break;

                    case ZentriOSBLEService.ACTION_COMMAND_SENT:
                        String command = ZentriOSBLEService.getCommand(intent).toString();
                        Log.d(TAG, "Command " + command + " sent");
                        break;





                    case ZentriOSBLEService.ACTION_COMMAND_RESULT:
                        handleCommandResponse(intent);
                        break;

//                    case ZentriOSBLEService.ACTION_MODE_WRITE:
//                        int mode = ZentriOSBLEService.getMode(intent);
//                        if (mode == ZentriOSBLEManager.MODE_STREAM)
//                        {
//                            //disable buttons while in stream mode (must be in rem command to work)
//                            //GUISetStreamMode();
//                        }
//                        else
//                        {
//                            //GUISetCommandMode();
//                        }
//                        break;

                    case ZentriOSBLEService.ACTION_STRING_DATA_READ:
                        //if (mCurrentMode == ZentriOSBLEManager.MODE_STREAM)
                        //{
                        String text = ZentriOSBLEService.getData(intent);
                        updateReceivedTextBox(text);//就是这句往那个textbox写东西

                        if (text.equals("I")) {
                            mZentriOSBLEManager.setReceiveMode(ReceiveMode.BINARY);
                            count_bytes = 0;
                            header_done = false;
//                            mZentriOSBLEManager.writeData("0");
                            break;
                        }

                        if (text.equals("N")) {
                            break;
                        }



                        temp = text;

                        if (temp.contains(":")) {

                            ParseObject testObject = new ParseObject("TestXZ");
                            testObject.put("TIME", temp);
                            testObject.put("NAME", mCurrentDeviceName);
                            testObject.saveEventually();
                        }

                        Log.d(TAG, "text = : " + text);

//                        if(mRecording) {
//                            writeLog(text);
//                            if (count_bytes == 0) {
//                                val = Integer.parseInt(text);
//                                len_image = val;
//                                count_bytes++;
//                            }
//                            else if (count_bytes == 1) {
//                                val = Integer.parseInt(text);
//                                len_image += val*256;
//                                imBytesSplit = new byte[2*len_image];
//                                count_bytes++;
//                                header_done = true;
//                            }
//                            else {
//                                byte[] block = text.getBytes(Charset.forName("UTF-8"));
//                                //byte[] block = ZentriOSBLEService.getByteData(intent);
//                                System.arraycopy(block,0,imBytesSplit,count_bytes-2,block.length);
//                                    /*for (int ii=0;ii<block.length;ii++) {
//                                        imBytes[count_bytes-2+ii] = block[ii];
//                                    }*/
//                                count_bytes += block.length;
//                                //imBytes[count_bytes-2] = (byte) val;
//                            }
//
//                            if (count_bytes < len_image*2) mZentriOSBLEManager.writeData("0");
//
//                            //count_bytes++;
//
//                            if (count_bytes>=(2*len_image) && header_done) {
//                                imBytes = new byte[len_image];
//                                for (int ii=0;ii<len_image;ii++) {
//                                    imBytes[ii] = (byte) (imBytesSplit[2*ii] + (imBytesSplit[(2*ii)+1]*16));
//                                }
//                                saveImage(imBytes);
//                                mToggleIm.setChecked(false);
//                                doStopRecording();
//                                stopRecording();
//                            }
//                        }


//                                String dataToSend = "*gi#";
//                                mZentriOSBLEManager.writeData(dataToSend);

                        if (gi == true) {
                            String dataToSend = "*ai#";
                            mZentriOSBLEManager.writeData(dataToSend);
                            gi = false;
                        }
                        else {
                            String dataToSend = "*gi#";
                            mZentriOSBLEManager.writeData(dataToSend);
                            gi = true;
                        }

                        Log.d(TAG, "Bytes: " + count_bytes);
                        //}
                        break;

                    case ZentriOSBLEService.ACTION_BINARY_DATA_READ:
                        byte[] block = ZentriOSBLEService.getBinaryData(intent);
                        if(mRecording) {
                            writeLog(block.toString());
                        }
                        if (!header_done) {
                            if (block.length == 1 && count_bytes == 0) {
                                len_image = block[0];
                                count_bytes++;
                            }
                            else if (block.length == 1 && count_bytes == 1) {
                                len_image += block[0]*256;
                                count_bytes--;
                                header_done = true;
                                imBytes = new byte[len_image];
                            }
                            else if (block.length > 1) {
                                len_image = (block[0] & 0x00FF) | ((block[1] << 8) & 0xFF00);
                                header_done = true;
                                imBytes = new byte[len_image];

                                if (block.length > 2) {
                                    System.arraycopy(block, 2, imBytes, count_bytes, block.length - 2);
                                    count_bytes += block.length - 2;
                                }
                            }
                        }
                        else {
                            if (count_bytes + block.length > len_image) {
                                System.arraycopy(block, 0, imBytes, count_bytes, len_image-count_bytes);
                                count_bytes += (len_image-count_bytes);
                            }
                            else {
                                System.arraycopy(block, 0, imBytes, count_bytes, block.length);
                                count_bytes += block.length;
                            }
                        }

                        //if (count_bytes < len_image) mZentriOSBLEManager.writeData("0");
                        //mZentriOSBLEManager.writeData("0");

                        if (count_bytes>2 && imBytes[count_bytes-2]==-1 && imBytes[count_bytes-1]==-39) {
                            //if (count_bytes>=(len_image) && header_done) {
                            saveImage(imBytes);
//                            mToggleIm.setChecked(false);
                            doStopRecording();
                            stopRecording();
                            clearReceivedTextBox();
                            Log.d(TAG, "SDFADSFASDFASFASFASFASFA   photo show");
                            //显示图片
                            imView = (ImageView) findViewById(R.id.imageView);
                            Bitmap bmp = BitmapFactory.decodeByteArray(imBytes, 0, len_image);
                            imView.setImageBitmap(bmp);
                            mZentriOSBLEManager.setReceiveMode(ReceiveMode.STRING);
                            //mZentriOSBLEManager.writeData("*000#");
                            //mZentriOSBLEManager.writeData("*R#");
                            //mZentriOSBLEManager.writeData("*W#");
                        }
                        Log.d(TAG, "Bytes: " + count_bytes + "Val: " + block[0]);

                        break;

                    case ZentriOSBLEService.ACTION_ERROR:
                        ErrorCode errorCode = ZentriOSBLEService.getErrorCode(intent);
                        //handle errors
                        switch (errorCode)
                        {
                            case CONNECT_FAILED:
                                setDisconnectedState();
                                dismissConnectDialog();
                                cancelConnectTimeout();
                                showErrorDialog(R.string.error, R.string.con_err_message);
                                break;

                            case DEVICE_ERROR:
                                cancelConnectTimeout();
                                dismissConnectDialog();
                                if (mZentriOSBLEManager != null && mZentriOSBLEManager.isConnected())
                                {
                                    disconnect(NO_TX_NOTIFY_DISABLE);
                                }
                                break;

                            case SET_NOTIFY_FAILED:
                                cancelConnectTimeout();
                                dismissConnectDialog();
                                disconnect(NO_TX_NOTIFY_DISABLE);
                                showErrorDialog(R.string.error, R.string.error_configuration);
                                break;

                            case SERVICE_DISCOVERY_ERROR:
                                //need to disconnect
                                cancelConnectTimeout();
                                dismissConnectDialog();
                                disconnect(NO_TX_NOTIFY_DISABLE);
                                showErrorDialog(R.string.error, R.string.error_service_disc);
                                break;

                            case DISCONNECT_FAILED:
                                if (!isFinishing())
                                {
                                    showUnrecoverableErrorDialog(R.string.error, R.string.discon_err_message);
                                }
                                break;
                        }
                        break;
                }
            }
        };
    }

    public void initBroadcastManager()
    {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
    }

    public void initReceiverIntentFilter()
    {
        mReceiverIntentFilter = new IntentFilter();
        mReceiverIntentFilter.addAction(ZentriOSBLEService.ACTION_SCAN_RESULT);
        mReceiverIntentFilter.addAction(ZentriOSBLEService.ACTION_CONNECTED);
        mReceiverIntentFilter.addAction(ZentriOSBLEService.ACTION_DISCONNECTED);
        mReceiverIntentFilter.addAction(ZentriOSBLEService.ACTION_ERROR);
        mReceiverIntentFilter.addAction(ZentriOSBLEService.ACTION_COMMAND_RESULT);
        mReceiverIntentFilter.addAction(ZentriOSBLEService.ACTION_STRING_DATA_READ);
        mReceiverIntentFilter.addAction(ZentriOSBLEService.ACTION_BINARY_DATA_READ);
        mReceiverIntentFilter.addAction(ZentriOSBLEService.ACTION_MODE_WRITE);
    }

    private void startBLEEnableIntent()
    {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, BLE_ENABLE_REQ_CODE);
    }

    private void initialiseListviewListener(ListView listView)
    {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                mCurrentDeviceName = mDeviceList.get(position);
                if(values == null || !values.contains(mCurrentDeviceName)) {
                    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    SQLiteDatabase testdb = openOrCreateDatabase("Adherence_app.db", Context.MODE_PRIVATE, null);
                    testdb.execSQL("INSERT INTO DeviceTable VALUES (?)", new Object[]{mCurrentDeviceName});
                    testdb.close();
                    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                    values.add(mCurrentDeviceName);

                }
                newadapter.notifyDataSetChanged();
            }
        });
    }

    private void startScan()
    {

        if (mZentriOSBLEManager != null)
        {
            Toast.makeText(getApplicationContext(),"Manager is not null startscan",Toast.LENGTH_LONG).show();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mZentriOSBLEManager.startScan();
                }
            });
            disableScanButton();
            mHandler.postDelayed(mStopScanTask, SCAN_PERIOD);
        }
        else {Toast.makeText(getApplicationContext(),"Manager is null",Toast.LENGTH_LONG).show();}
    }

    private void stopScan()
    {
        if (mZentriOSBLEManager != null && mZentriOSBLEManager.stopScan())
        {
            //stopProgressBar();
            enableScanButton();
        }
    }

    private void startDeviceInfoActivity()
    {
        //GUISetCommandMode();
        mZentriOSBLEManager.setMode(ZentriOSBLEManager.MODE_COMMAND_REMOTE);
        mZentriOSBLEManager.setSystemCommandMode(CommandMode.MACHINE);//第一个set mode
        mZentriOSBLEManager.getVersion();
        //startActivity(new Intent(getApplicationContext(), DeviceInfoActivity.class));
        mDeviceList.clear();
        mDeviceList.add(mCurrentDeviceName);

        mCurrentMode = ZentriOSBLEManager.MODE_STREAM;
        x = mZentriOSBLEManager.setMode(mCurrentMode);//第二个set mode
//
//
//        Log.d(TAG, "Mode set to: " + mCurrentMode);
//
        Toast.makeText(getApplicationContext(), "here!", Toast.LENGTH_LONG).show();

//        String dataToSend = "*gn#";
//        mZentriOSBLEManager.writeData(dataToSend);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
//                auto(mCurrentDeviceName);
                auto_photo();
            }
        }, 2500);
    }

    private void enableScanButton()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mScanButton.setEnabled(true);
            }
        });
    }

    private void disableScanButton()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mScanButton.setEnabled(false);
            }
        });
    }

    private void showToast(final String msg, final int duration)
    {
        if (!isFinishing())
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(getApplicationContext(), msg, duration).show();
                }
            });
        }
    }

    private void showErrorDialog(final int titleID, final int msgID)
    {
        if (!mErrorDialogShowing && !isFinishing())
        {
            mErrorDialogShowing = true;

            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2.this);
                    builder.setTitle(titleID)
                            .setMessage(msgID)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.dismiss();
                                    mErrorDialogShowing = false;
                                }
                            })
                            .create()
                            .show();
                }
            });
        }
    }

    private void showUnrecoverableErrorDialog(final int titleID, final int msgID)
    {
        if (!isFinishing())
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2.this);

                    builder.setTitle(titleID)
                            .setMessage(msgID)
                            .setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .create()
                            .show();
                }
            });
        }
    }

    private void dismissConnectDialog()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (mConnectProgressDialog != null)
                {
                    mConnectProgressDialog.dismiss();
                    mConnectProgressDialog = null;
                }
            }
        });
    }

    //Only adds to the list if not already in it
    private void addDeviceToList(final String name)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeviceList.add(name);
            }
        });
    }

    private void showConnectingDialog(final Context context)
    {
        if (!isFinishing())
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    final ProgressDialog dialog = new ProgressDialog(MainActivity2.this);
                    String title = getString(R.string.progress_title);
                    String msg = getString(R.string.progress_message);
                    dialog.setIndeterminate(true);//Dont know how long connection could take.....
                    dialog.setCancelable(true);

                    mConnectProgressDialog = dialog.show(context, title, msg);
                    mConnectProgressDialog.setCancelable(true);
                    mConnectProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
                    {
                        @Override
                        public void onCancel(DialogInterface dialogInterface)
                        {
                            dialogInterface.dismiss();
                        }
                    });
                }
            });
        }
    }

    private void setDisconnectedState()
    {
        mConnected = false;
        mConnecting = false;
    }

    private void cancelConnectTimeout()
    {
        mHandler.removeCallbacks(mConnectTimeoutTask);
    }

    private void disconnect(boolean disableTxNotify)
    {
        setDisconnectedState();
        mZentriOSBLEManager.disconnect(disableTxNotify);
    }

    private void openAboutDialog()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialogs.makeAboutDialog(MainActivity2.this).show();
            }
        });
    }

    private void dismissProgressDialog()
    {
        if (mDisconnectDialog != null)
        {
            mDisconnectDialog.dismiss();
        }
    }

    private void handleCommandResponse(Intent intent)
    {
        Command command = ZentriOSBLEService.getCommand(intent);
        int ID = ZentriOSBLEService.getID(intent);
        int code = ZentriOSBLEService.getResponseCode(intent);
        String result = ZentriOSBLEService.getData(intent);
        String message = "";

        Log.d(TAG, "Command " + command + " result");

        if (code == Result.SUCCESS)
        {
            switch (command)
            {
                case ADC:
//                    /*if (ID == mCurrentADCCommandID)
//                    {
//                        message = String.fomat("ADC: %s", result);
//                        mADCTextView.setText(message);
//                    }
//                    else
//                    {
//                        Log.d(TAG, "Invalid ADC command ID!");
//                    }*/
                    break;

                case GPIO_GET:
//                    /*if (ID == mCurrentGPIOCommandID)
//                    {
//                        message = String.format("GPIO: %s", result);
//                        mGPIOTextView.setText(message);
//                    }
//                    else
//                    {
//                        Log.d(TAG, "Invalid GPIO command ID!");
//                    }*/
                    break;

                case GPIO_SET:
                    break;
            }
        }
        else
        {
            message = String.format("ERROR %d - %s", code, result);
            showToast(message, Toast.LENGTH_SHORT);
        }
    }




    //set up gui elements for command mode operation
//    private void GUISetCommandMode()
//    {
    //mSendTextButton.setEnabled(false);
    //mTextToSendBox.setVisibility(View.INVISIBLE);
//    }

    //set up gui elements for command mode operation
    //private void GUISetStreamMode()
    //{
    //mSendTextButton.setEnabled(true);
    //mTextToSendBox.setVisibility(View.VISIBLE);
    //}

    private void updateReceivedTextBox(String newData)
    {
        mReceivedDataTextBox.append(newData);
    }

    private void clearReceivedTextBox()
    {
        mReceivedDataTextBox.setText("");
    }

    private void doStartRecording() {
        File sdCard = Environment.getExternalStorageDirectory();

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateTimeString = format.format(new Date());
        String fileName = sdCard.getAbsolutePath() + "/ams001_" + currentDateTimeString + ".log";

        this.setFileNameLog(fileName);
        this.startRecording();

        showToast("Logging Started", Toast.LENGTH_SHORT);
    }

    private void doStopRecording() {
        this.stopRecording();
//        showToast("Logging Stopped", Toast.LENGTH_SHORT);
    }

    public void setFileNameLog( String fileNameLog ) {
        mFileNameLog = fileNameLog;
    }

    public void startRecording() {
        mRecording = true;
    }

    public void stopRecording() {
        mRecording = false;
    }

    private boolean writeLog(String buffer) {
        String state = Environment.getExternalStorageState();
        File logFile = new File ( mFileNameLog );

        if (Environment.MEDIA_MOUNTED.equals(state)) {

            try {
                FileOutputStream f = new FileOutputStream( logFile, true );

                PrintWriter pw = new PrintWriter(f);
                pw.print( buffer );
                pw.flush();
                pw.close();

                f.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            this.stopRecording();
            return false;
        } else {
            this.stopRecording();
            return false;
        }

        return true;
    }

    private void saveImage(byte[] data) {
        File sdCard = Environment.getExternalStorageDirectory();
        String fileName = sdCard.getAbsolutePath() + "/ams001_image.jpg";
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

    @Override
    public  void onBackPressed(){

        startActivity(new Intent(this, NextActivity.class));
        finish();
    }


}
package com.adherence.adherence;


import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.blelib.BleManager;
import com.example.blelib.conn.BleCharacterCallback;
import com.example.blelib.conn.BleGattCallback;
import com.example.blelib.conn.BleRssiCallback;
import com.example.blelib.data.ScanResult;
import com.example.blelib.exception.BleException;
import com.example.blelib.scan.ListScanCallback;
import com.example.blelib.utils.HexUtil;

public class BluetoothService extends Service implements View.OnClickListener {

    public BluetoothBinder mBinder = new BluetoothBinder();
    private BleManager bleManager;
    private Handler threadHandler = new Handler(Looper.getMainLooper());
    private Callback mCallback = null;
    private Callback2 mCallback2 = null;

    private String name;
    private String mac;
    private BluetoothGatt gatt;
    private BluetoothGattService service;
    private BluetoothGattCharacteristic characteristic;
    private int charaProp;

    public static final String EXTRA_DATA = "EXTRA_DATA";
    private String command1 = "Scan";
    public static final String AL1 = "AL1";



    AlertDialog mDialog = null;

    TextView dialogContent;
    Button dialogBuOK, dialogBuCancel;

    private MediaPlayer mp;


    //dasfwoenwefaona


    @Override
    public void onCreate() {
        System.out.println("BLE on create");
        bleManager = new BleManager(this);
        bleManager.enableBluetooth();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("test BLE Service");
        scanDevice();

        AdherenceApplication.bleserTimes++;
        if(AdherenceApplication.bleserTimes >= 50){
            AdherenceApplication.bleserTimes = 0;
            showDialog();
            test_alarm(this);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bleManager = null;
        mCallback = null;
        mCallback2 = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        bleManager.closeBluetoothGatt();
        return super.onUnbind(intent);
    }

    public class BluetoothBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    public void setScanCallback(Callback callback) {
        mCallback = callback;
    }

    public void setConnectCallback(Callback2 callback) {
        mCallback2 = callback;
    }

    public interface Callback {

        void onStartScan();

        void onScanning(ScanResult scanResult);

        void onScanComplete();

        void onConnecting();

        void onConnectFail();

        void onDisConnected();

        void onServicesDiscovered();
    }

    public interface Callback2 {

        void onDisConnected();
    }

    public void scanDevice() {
        resetInfo();

//        if (mCallback != null) {
//            mCallback.onStartScan();
//        }
        System.out.println(bleManager == null);
        boolean b = bleManager.scanDevice(new ListScanCallback(5000) {

            @Override
            public void onScanning(final ScanResult result) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onScanning(result);
                        }
                    }
                });
            }

            @Override
            public void onScanComplete(final ScanResult[] results) {
                System.out.println("Yongchi");

                AdherenceApplication.scanRecord.clear();


                if(MainActivity2.pressScan){
                    MainActivity2.BLE_Result.clear();
                }


                for(int i = 0; i < results.length; i++){
                    String temp_name = results[i].getDevice().getName();
                    String temp_address = results[i].getDevice().getAddress();
                    int rssi = results[i].getRssi();
                    if(temp_name == null){
                        continue;
                    }



                    if(MainActivity2.pressScan){
                        if(rssi > -60){
                            MainActivity2.BLE_Result.add(temp_name + "  " + temp_address);
                        }
                    }



                    System.out.println(temp_name);
                    System.out.println(rssi);
                    for(int j = 0; j < results[i].getScanRecord().length; j++){
                        int temp = 0xff & results[i].getScanRecord()[j];
                        int left = temp >> 4;
                        int right = temp & 0xf;
                        System.out.print(left + "," + right + ",");
                    }

                    int temp = 0xff & results[i].getScanRecord()[5];
                    AdherenceApplication.scanRecord.add(temp_name + "  " + temp_address + "+" + temp);

                    System.out.println();
                }


                MainActivity2.pressScan = false;




                if(AlarmReceiver.flag == true) {
                    AlarmReceiver.flag = false;
                    Intent ii = new Intent();
                    ii.setAction(AL1);
                    ii.putExtra(EXTRA_DATA, command1);
                    sendBroadcast(ii);
                }


            }
        });
    }

    public void cancelScan() {
        bleManager.cancelScan();
    }

    public void connectDevice(final ScanResult scanResult) {
        if (mCallback != null) {
            mCallback.onConnecting();
        }

        bleManager.connectDevice(scanResult, true, new BleGattCallback() {

            @Override
            public void onFoundDevice(ScanResult scanResult) {
                BluetoothService.this.name = scanResult.getDevice().getName();
                BluetoothService.this.mac = scanResult.getDevice().getAddress();
            }

            @Override
            public void onConnecting(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onConnectError(BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnectFail();
                        }
                    }
                });
            }

            @Override
            public void onConnectSuccess(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
                BluetoothService.this.gatt = gatt;
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onServicesDiscovered();
                        }
                    }
                });
            }

            @Override
            public void onDisConnected(BluetoothGatt gatt, int status, BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onDisConnected();
                        }
                        if (mCallback2 != null) {
                            mCallback2.onDisConnected();
                        }
                    }
                });
            }

        });
    }

    public void scanAndConnect1(String name) {
        resetInfo();

        if (mCallback != null) {
            mCallback.onStartScan();
        }

        bleManager.scanNameAndConnect(name, 5000, false, new BleGattCallback() {

            @Override
            public void onFoundDevice(ScanResult scanResult) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onScanComplete();
                        }
                    }
                });
                BluetoothService.this.name = scanResult.getDevice().getName();
                BluetoothService.this.mac = scanResult.getDevice().getAddress();
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnecting();
                        }
                    }
                });
            }

            @Override
            public void onConnecting(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onConnectError(BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnectFail();
                        }
                    }
                });
            }

            @Override
            public void onConnectSuccess(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                BluetoothService.this.gatt = gatt;
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onServicesDiscovered();
                        }
                    }
                });
            }

            @Override
            public void onDisConnected(BluetoothGatt gatt, int status, BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onDisConnected();
                        }
                        if (mCallback2 != null) {
                            mCallback2.onDisConnected();
                        }
                    }
                });
            }
        });
    }

    public void scanAndConnect2(String name) {
        resetInfo();

        if (mCallback != null) {
            mCallback.onStartScan();
        }

        bleManager.scanfuzzyNameAndConnect(name, 5000, false, new BleGattCallback() {

            @Override
            public void onFoundDevice(ScanResult scanResult) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onScanComplete();
                        }
                    }
                });
                BluetoothService.this.name = scanResult.getDevice().getName();
                BluetoothService.this.mac = scanResult.getDevice().getAddress();
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnecting();
                        }
                    }
                });
            }

            @Override
            public void onConnecting(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onConnectError(BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnectFail();
                        }
                    }
                });
            }

            @Override
            public void onConnectSuccess(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onDisConnected(BluetoothGatt gatt, int status, BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onDisConnected();
                        }
                        if (mCallback2 != null) {
                            mCallback2.onDisConnected();
                        }
                    }
                });
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                BluetoothService.this.gatt = gatt;
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onServicesDiscovered();
                        }
                    }
                });
            }
        });
    }

    public void scanAndConnect3(String[] names) {
        resetInfo();

        if (mCallback != null) {
            mCallback.onStartScan();
        }

        bleManager.scanNamesAndConnect(names, 5000, false, new BleGattCallback() {

            @Override
            public void onFoundDevice(ScanResult scanResult) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onScanComplete();
                        }
                    }
                });
                BluetoothService.this.name = scanResult.getDevice().getName();
                BluetoothService.this.mac = scanResult.getDevice().getAddress();
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnecting();
                        }
                    }
                });
            }

            @Override
            public void onConnecting(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onConnectError(BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnectFail();
                        }
                    }
                });
            }

            @Override
            public void onConnectSuccess(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onDisConnected(BluetoothGatt gatt, int status, BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onDisConnected();
                        }
                        if (mCallback2 != null) {
                            mCallback2.onDisConnected();
                        }
                    }
                });
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                BluetoothService.this.gatt = gatt;
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onServicesDiscovered();
                        }
                    }
                });
            }
        });

    }

    public void scanAndConnect4(String[] names) {
        resetInfo();

        if (mCallback != null) {
            mCallback.onStartScan();
        }

        bleManager.scanfuzzyNamesAndConnect(names, 5000, false, new BleGattCallback() {

            @Override
            public void onFoundDevice(ScanResult scanResult) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onScanComplete();
                        }
                    }
                });
                BluetoothService.this.name = scanResult.getDevice().getName();
                BluetoothService.this.mac = scanResult.getDevice().getAddress();
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnecting();
                        }
                    }
                });
            }

            @Override
            public void onConnecting(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onConnectError(BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnectFail();
                        }
                    }
                });
            }

            @Override
            public void onConnectSuccess(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onDisConnected(BluetoothGatt gatt, int status, BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onDisConnected();
                        }
                        if (mCallback2 != null) {
                            mCallback2.onDisConnected();
                        }
                    }
                });
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                BluetoothService.this.gatt = gatt;
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onServicesDiscovered();
                        }
                    }
                });
            }
        });
    }

    public void scanAndConnect5(String mac) {
        resetInfo();

        if (mCallback != null) {
            mCallback.onStartScan();
        }

        bleManager.scanMacAndConnect(mac, 5000, false, new BleGattCallback() {

            @Override
            public void onFoundDevice(ScanResult scanResult) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onScanComplete();
                        }
                    }
                });
                BluetoothService.this.name = scanResult.getDevice().getName();
                BluetoothService.this.mac = scanResult.getDevice().getAddress();
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnecting();
                        }
                    }
                });
            }

            @Override
            public void onConnecting(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onConnectError(BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onConnectFail();
                        }
                    }
                });
            }

            @Override
            public void onConnectSuccess(BluetoothGatt gatt, int status) {

            }

            @Override
            public void onDisConnected(BluetoothGatt gatt, int status, BleException exception) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onDisConnected();
                        }
                        if (mCallback2 != null) {
                            mCallback2.onDisConnected();
                        }
                    }
                });
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                BluetoothService.this.gatt = gatt;
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onServicesDiscovered();
                        }
                    }
                });
            }

        });
    }

    public boolean read(String uuid_service, String uuid_read, BleCharacterCallback callback) {
        return bleManager.readDevice(uuid_service, uuid_read, callback);
    }

    public boolean write(String uuid_service, String uuid_write, String hex, BleCharacterCallback callback) {
        return bleManager.writeDevice(uuid_service, uuid_write, HexUtil.hexStringToBytes(hex), callback);
    }

    public boolean notify(String uuid_service, String uuid_notify, BleCharacterCallback callback) {
        return bleManager.notify(uuid_service, uuid_notify, callback);
    }

    public boolean indicate(String uuid_service, String uuid_indicate, BleCharacterCallback callback) {
        return bleManager.indicate(uuid_service, uuid_indicate, callback);
    }

    public boolean stopNotify(String uuid_service, String uuid_notify) {
        return bleManager.stopNotify(uuid_service, uuid_notify);
    }

    public boolean stopIndicate(String uuid_service, String uuid_indicate) {
        return bleManager.stopIndicate(uuid_service, uuid_indicate);
    }

    public boolean readRssi(BleRssiCallback callback) {
        return bleManager.readRssi(callback);
    }

    public void closeConnect() {
        bleManager.closeBluetoothGatt();
    }


    private void resetInfo() {
        name = null;
        mac = null;
        gatt = null;
        service = null;
        characteristic = null;
        charaProp = 0;
    }

    public String getName() {
        return name;
    }

    public String getMac() {
        return mac;
    }

    public BluetoothGatt getGatt() {
        return gatt;
    }

    public void setService(BluetoothGattService service) {
        this.service = service;
    }

    public BluetoothGattService getService() {
        return service;
    }

    public void setCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    public void setCharaProp(int charaProp) {
        this.charaProp = charaProp;
    }

    public int getCharaProp() {
        return charaProp;
    }


    private void runOnMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            threadHandler.post(runnable);
        }
    }




    // 创建dialog 的地方
    private void createDialog() {
//        View view = View.inflate(BluetoothService.this, R.layout.dialog, null);
//        // 初始Dialog 里面的内容
//        dialogBuOK = (Button) view.findViewById(R.id.dialog_button_ok);
//        dialogBuOK.setOnClickListener(this);
//        dialogBuCancel = (Button) view.findViewById(R.id.dialog_button_cancel);
//        dialogBuCancel.setOnClickListener(this);
//        dialogContent = (TextView)view.findViewById(R.id.dialog_content);
//        AlertDialog.Builder builder = new AlertDialog.Builder(BluetoothService.this);
//        builder.setView(view);
//        mDialog = builder.create();
//        mDialog.getWindow().setType(
//                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);



        AlertDialog.Builder builder = new AlertDialog.Builder(BluetoothService.this);
        builder.setTitle("AdherencePill Notification")
                .setIcon(R.drawable.capsule)
                .setMessage("\nPlease take pills on time.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDialog.dismiss();
                        mp.stop();
                        mp.release();
                    }
                });
        mDialog = builder.create();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.getWindow().setType(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);


    }
    // 更新dialog 里面的内容
    private String updateDialogContent(){
        String content = null ;

        content = "Please take pills on time.";
        return content;
    }
    // 弹出dialog 的地方
    public void showDialog() {
        if (mDialog == null) {
            createDialog();
        }




        mp = MediaPlayer.create(this,R.raw.music);
        mp.start();
        mDialog.dismiss();
        mDialog.show();
    }




    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_button_ok:
                //TODO
                mDialog.dismiss();
                mp.stop();
                mp.release();
                break;
            case R.id.dialog_button_cancel:
                //TODO
                mDialog.dismiss();
                mp.stop();
                mp.release();
                break;

            default:
                break;
        }

    }

    public void test_alarm(Context context){
        SharedPreferences data_newdata=context.getSharedPreferences(MainActivity.UserPREFERENCES,context.MODE_PRIVATE);
        boolean neednotify = data_newdata.getBoolean("notification",true);
        if(neednotify) {
            boolean vibration = data_newdata.getBoolean("vibration", true);
            boolean sound = data_newdata.getBoolean("sound", false);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            Notification.Builder builder = new Notification.Builder(context);

            builder.setSmallIcon(R.drawable.capsule);
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

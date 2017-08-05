package com.adherence.adherence;


import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.example.blelib.BleManager;
import com.example.blelib.conn.BleCharacterCallback;
import com.example.blelib.conn.BleGattCallback;
import com.example.blelib.conn.BleRssiCallback;
import com.example.blelib.data.ScanResult;
import com.example.blelib.exception.BleException;
import com.example.blelib.scan.ListScanCallback;
import com.example.blelib.utils.HexUtil;

public class BluetoothService extends Service {

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

                for(int i = 0; i < results.length; i++){
                    for(int j = 0; j < results[i].getScanRecord().length; j++){
                        int temp = 0xff & results[i].getScanRecord()[j];
                        int left = temp >> 4;
                        int right = temp & 0xf;
                        System.out.print(left + "," + right + ",");
                    }
                    System.out.println();
                }
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onScanComplete();
                        }
                    }
                });
            }
        });
        System.out.println(b);
//        if (!b) {
//            if (mCallback != null) {
//                mCallback.onScanComplete();
//            }
//        }
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


}

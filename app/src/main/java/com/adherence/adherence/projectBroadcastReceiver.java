package com.adherence.adherence;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseObject;
import com.zentri.zentri_ble.BLECallbacks;
import com.zentri.zentri_ble_command.ErrorCode;
import com.zentri.zentri_ble_command.ZentriOSBLEManager;

public class projectBroadcastReceiver extends BroadcastReceiver {

    private ZentriOSBLEManager mZentriOSBLEManager;
    private final String TAG = "proBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        Log.d(TAG, "Received intent " + intent);

        switch (action)
        {
            case ZentriOSBLEService.ACTION_SCAN_RESULT:

                String temp = ZentriOSBLEService.getData(intent);
                Log.d(TAG, "Received intent scan_result : "+temp);

                break;

            case ZentriOSBLEService.ACTION_CONNECTED:
                //addDeviceToList(ZentriOSBLEService.getData(intent));
                Log.d(TAG, "Received intent connected");
                break;
/*            case ZentriOSBLEService.ACTION_CONNECTED:
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

            case ZentriOSBLEService.ACTION_STRING_DATA_READ:
                //if (mCurrentMode == ZentriOSBLEManager.MODE_STREAM)
                //{
                String text = ZentriOSBLEService.getData(intent);
                updateReceivedTextBox(text);//就是这句往那个textbox写东西

                if (text.equals("I")) {
                    mZentriOSBLEManager.setReceiveMode(BLECallbacks.ReceiveMode.BINARY);
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
                    mZentriOSBLEManager.setReceiveMode(BLECallbacks.ReceiveMode.STRING);
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
                break;*/
        }
    }


}

package cc.xiaojiang.lib.ble.test;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;


import java.util.UUID;

import cc.xiaojiang.lib.ble.callback.BleDataSetCallback;
import cc.xiaojiang.lib.ble.callback.ota.SendResultCallBack;
import cc.xiaojiang.lib.ble.data.BleMsg;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleConnector {

    private static final String UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mGattService;
    private BluetoothGattCharacteristic mCharacteristic;
    private BleBluetooth mBleBluetooth;
    private Handler mHandler;
    private SendResultCallBack sendResultCallBack;
    private BleDataSetCallback mBleDataSetCallback;

    BleConnector(BleBluetooth bleBluetooth) {
        this.mBleBluetooth = bleBluetooth;
        this.mBluetoothGatt = bleBluetooth.getBluetoothGatt();
        this.mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {

                    case BleMsg.MSG_CHA_NOTIFY_START: {
                    }

                    case BleMsg.MSG_CHA_NOTIFY_RESULT: {
                    }

                    case BleMsg.MSG_CHA_NOTIFY_DATA_CHANGE: {

                    }

                    case BleMsg.MSG_CHA_INDICATE_START: {

                    }

                    case BleMsg.MSG_CHA_INDICATE_RESULT: {
                    }

                    case BleMsg.MSG_CHA_INDICATE_DATA_CHANGE: {
                    }

                    case BleMsg.MSG_CHA_WRITE_START: {
                    }

                    case BleMsg.MSG_CHA_WRITE_RESULT: {
                    }
                }
            }
        };

    }

    private BleConnector withUUID(UUID serviceUUID, UUID characteristicUUID) {
        if (serviceUUID != null && mBluetoothGatt != null) {
            mGattService = mBluetoothGatt.getService(serviceUUID);
        }
        if (mGattService != null && characteristicUUID != null) {
            mCharacteristic = mGattService.getCharacteristic(characteristicUUID);
        }
        return this;
    }

    public BleConnector withUUIDString(String serviceUUID, String characteristicUUID) {
        return withUUID(formUUID(serviceUUID), formUUID(characteristicUUID));
    }

    private UUID formUUID(String uuid) {
        return uuid == null ? null : UUID.fromString(uuid);
    }


    /*------------------------------- main operation ----------------------------------- */

    /**
     * requestConnectionPriority
     *
     * @param connectionPriority Request a specific connection priority. Must be one of
     *                           {@link BluetoothGatt#CONNECTION_PRIORITY_BALANCED},
     *                           {@link BluetoothGatt#CONNECTION_PRIORITY_HIGH}
     *                           or {@link BluetoothGatt#CONNECTION_PRIORITY_LOW_POWER}.
     * @throws IllegalArgumentException If the parameters are outside of their
     *                                  specified range.
     */
    public boolean requestConnectionPriority(int connectionPriority) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return mBluetoothGatt.requestConnectionPriority(connectionPriority);
        }
        return false;
    }

//
//    /**************************************** Handle call back ******************************************/
//
//    /**
//     * notify
//     */
//    private void handleCharacteristicNotifyCallback(BleNotifyCallback bleNotifyCallback,
//                                                    String uuid_notify) {
//        if (bleNotifyCallback != null) {
//            notifyMsgInit();
//            bleNotifyCallback.setKey(uuid_notify);
//            bleNotifyCallback.setHandler(mHandler);
//            mBleBluetooth.addNotifyCallback(uuid_notify, bleNotifyCallback);
//            mHandler.sendMessageDelayed(
//                    mHandler.obtainMessage(BleMsg.MSG_CHA_NOTIFY_START, bleNotifyCallback),
//                    BleManager.getInstance().getOperateTimeout());
//        }
//    }
//
//    /**
//     * indicate
//     */
//    private void handleCharacteristicIndicateCallback(BleIndicateCallback bleIndicateCallback,
//                                                      String uuid_indicate) {
//        if (bleIndicateCallback != null) {
//            indicateMsgInit();
//            bleIndicateCallback.setKey(uuid_indicate);
//            bleIndicateCallback.setHandler(mHandler);
//            mBleBluetooth.addIndicateCallback(uuid_indicate, bleIndicateCallback);
//            mHandler.sendMessageDelayed(
//                    mHandler.obtainMessage(BleMsg.MSG_CHA_INDICATE_START, bleIndicateCallback),
//                    BleManager.getInstance().getOperateTimeout());
//        }
//    }
//
//    /**
//     * write
//     */
//    private void handleCharacteristicWriteCallback(BleWriteCallback bleWriteCallback,
//                                                   String uuid_write) {
//        if (bleWriteCallback != null) {
//            writeMsgInit();
//            bleWriteCallback.setKey(uuid_write);
//            bleWriteCallback.setHandler(mHandler);
//            mBleBluetooth.addWriteCallback(uuid_write, bleWriteCallback);
//            mHandler.sendMessageDelayed(
//                    mHandler.obtainMessage(BleMsg.MSG_CHA_WRITE_START, bleWriteCallback),
//                    BleManager.getInstance().getOperateTimeout());
//        }
//    }
//
//    /**
//     * read
//     */
//    private void handleCharacteristicReadCallback(BleReadCallback bleReadCallback,
//                                                  String uuid_read) {
//        if (bleReadCallback != null) {
//            readMsgInit();
//            bleReadCallback.setKey(uuid_read);
//            bleReadCallback.setHandler(mHandler);
//            mBleBluetooth.addReadCallback(uuid_read, bleReadCallback);
//            mHandler.sendMessageDelayed(
//                    mHandler.obtainMessage(BleMsg.MSG_CHA_READ_START, bleReadCallback),
//                    BleManager.getInstance().getOperateTimeout());
//        }
//    }
//
//    /**
//     * rssi
//     */
//    private void handleRSSIReadCallback(BleRssiCallback bleRssiCallback) {
//        if (bleRssiCallback != null) {
//            rssiMsgInit();
//            bleRssiCallback.setHandler(mHandler);
//            mBleBluetooth.addRssiCallback(bleRssiCallback);
//            mHandler.sendMessageDelayed(
//                    mHandler.obtainMessage(BleMsg.MSG_READ_RSSI_START, bleRssiCallback),
//                    BleManager.getInstance().getOperateTimeout());
//        }
//    }
//
//    /**
//     * set mtu
//     */
//    private void handleSetMtuCallback(BleMtuChangedCallback bleMtuChangedCallback) {
//        if (bleMtuChangedCallback != null) {
//            mtuChangedMsgInit();
//            bleMtuChangedCallback.setHandler(mHandler);
//            mBleBluetooth.addMtuChangedCallback(bleMtuChangedCallback);
//            mHandler.sendMessageDelayed(
//                    mHandler.obtainMessage(BleMsg.MSG_SET_MTU_START, bleMtuChangedCallback),
//                    BleManager.getInstance().getOperateTimeout());
//        }
//    }
//
//    public void notifyMsgInit() {
//        mHandler.removeMessages(BleMsg.MSG_CHA_NOTIFY_START);
//    }
//
//    public void indicateMsgInit() {
//        mHandler.removeMessages(BleMsg.MSG_CHA_INDICATE_START);
//    }
//
//    public void writeMsgInit() {
//        mHandler.removeMessages(BleMsg.MSG_CHA_WRITE_START);
//    }
//
//    public void readMsgInit() {
//        mHandler.removeMessages(BleMsg.MSG_CHA_READ_START);
//    }
//
//    public void rssiMsgInit() {
//        mHandler.removeMessages(BleMsg.MSG_READ_RSSI_START);
//    }
//
//    public void mtuChangedMsgInit() {
//        mHandler.removeMessages(BleMsg.MSG_SET_MTU_START);
//    }



}

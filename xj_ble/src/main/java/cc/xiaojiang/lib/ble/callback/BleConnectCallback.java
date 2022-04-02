package cc.xiaojiang.lib.ble.callback;

import android.bluetooth.BluetoothGatt;

import cc.xiaojiang.lib.ble.XJBleDevice;
import cc.xiaojiang.lib.ble.exception.BleException;

public interface BleConnectCallback {
    void onConnectSuccess(XJBleDevice xjBleDevice, BluetoothGatt gatt, int status);

    void onConnectFail(XJBleDevice xjBleDevice, int errorCode);

    void onDisConnected(XJBleDevice xjBleDevice, BluetoothGatt bluetoothGatt, int status, boolean isActiveDisConnected);
}

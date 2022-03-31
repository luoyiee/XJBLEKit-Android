package cc.xiaojiang.lib.ble.callback;

import android.bluetooth.BluetoothGatt;

import cc.xiaojiang.lib.ble.XJBleDevice;
import cc.xiaojiang.lib.ble.exception.BleException;

public interface BleConnectCallback {
    void onConnectSuccess(XJBleDevice XJBleDevice, BluetoothGatt gatt, int status);

    void onConnectFail(XJBleDevice XJBleDevice, BleException exception);

    void onDisConnected(XJBleDevice XJBleDevice,
                        BluetoothGatt bluetoothGatt, int status, boolean isActiveDisconnect);
}

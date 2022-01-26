package cc.xiaojiang.lib.ble.callback;

import android.bluetooth.BluetoothGatt;

import cc.xiaojiang.lib.ble.BleDevice;
import cc.xiaojiang.lib.ble.exception.AuthException;
import cc.xiaojiang.lib.ble.exception.BleException;

public interface BleConnectCallback {
    public abstract void onStartConnect();

    public abstract void onDeviceScanned(BleDevice bleDevice);

    public abstract void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status);

    public abstract void onConnectFail(BleDevice bleDevice, BleException exception);

    //    public abstract void onConnected(BleDevice bleDevice, BluetoothGatt gatt, int status);
    public abstract void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice,
                                        BluetoothGatt bluetoothGatt, int status);
}

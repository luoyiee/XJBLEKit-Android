package cc.xiaojiang.lib.ble.callback;

import android.bluetooth.BluetoothGatt;

import cc.xiaojiang.lib.ble.XJBleDevice;
import cc.xiaojiang.lib.ble.exception.BleException;

public interface BleConnectCallback {
    public abstract void onStartConnect();

    public abstract void onDeviceScanned(XJBleDevice XJBleDevice);

    public abstract void onConnectSuccess(XJBleDevice XJBleDevice, BluetoothGatt gatt, int status);

    public abstract void onConnectFail(XJBleDevice XJBleDevice, BleException exception);

    //    public abstract void onConnected(BleDevice bleDevice, BluetoothGatt gatt, int status);
    public abstract void onDisConnected(boolean isActiveDisConnected, XJBleDevice XJBleDevice,
                                        BluetoothGatt bluetoothGatt, int status);
}

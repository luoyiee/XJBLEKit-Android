package cc.xiaojiang.lib.ble.callback;

import android.bluetooth.BluetoothGatt;

import cc.xiaojiang.lib.ble.XJBleDevice;
import cc.xiaojiang.lib.ble.exception.BleException;

public interface BleConnectCallback {
    public abstract void onStartConnect();

    public abstract void onDeviceScanned(XJBleDevice bleDevice);

    public abstract void onConnectSuccess(XJBleDevice bleDevice, BluetoothGatt gatt, int status);

    public abstract void onConnectFail(XJBleDevice bleDevice, BleException exception);

    public abstract void onDisConnected(boolean isActiveDisConnected, XJBleDevice bleDevice,
                                        BluetoothGatt bluetoothGatt, int status);
}

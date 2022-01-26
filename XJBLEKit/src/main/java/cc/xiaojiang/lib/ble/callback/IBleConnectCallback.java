package cc.xiaojiang.lib.ble.callback;

import android.bluetooth.BluetoothGatt;

import cc.xiaojiang.lib.ble.XJBleDevice;
import cc.xiaojiang.lib.ble.exception.BleException;


/**
 * Created by facexxyz on 3/10/21.
 */
public interface IBleConnectCallback {
    public abstract void onConnectSuccess(XJBleDevice XJBleDevice, BluetoothGatt gatt, int status);

    public abstract void onConnectFail(XJBleDevice XJBleDevice, BleException exception);

}

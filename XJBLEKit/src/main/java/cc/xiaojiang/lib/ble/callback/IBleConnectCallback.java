package cc.xiaojiang.lib.ble.callback;

import android.bluetooth.BluetoothGatt;

import cc.xiaojiang.lib.ble.BleDevice;
import cc.xiaojiang.lib.ble.exception.BleException;


/**
 * Created by facexxyz on 3/10/21.
 */
public interface IBleConnectCallback {
    public abstract void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status);

    public abstract void onConnectFail(BleDevice bleDevice, BleException exception);

}

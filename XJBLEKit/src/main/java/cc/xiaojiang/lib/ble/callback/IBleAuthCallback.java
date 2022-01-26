package cc.xiaojiang.lib.ble.callback;

import android.bluetooth.BluetoothGatt;

import cc.xiaojiang.lib.ble.BleDevice;


/**
 * Created by facexxyz on 3/10/21.
 */
public interface IBleAuthCallback {

    public abstract void onAuthSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status);

    public abstract void onAuthFail(BleDevice bleDevice, BluetoothGatt gatt, int status);
}

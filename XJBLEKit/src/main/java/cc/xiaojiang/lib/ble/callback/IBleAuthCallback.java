package cc.xiaojiang.lib.ble.callback;

import android.bluetooth.BluetoothGatt;

import cc.xiaojiang.lib.ble.XJBleDevice;


/**
 * Created by facexxyz on 3/10/21.
 */
public interface IBleAuthCallback {

    public abstract void onAuthSuccess(XJBleDevice XJBleDevice, BluetoothGatt gatt, int status);

    public abstract void onAuthFail(XJBleDevice XJBleDevice, BluetoothGatt gatt, int status);
}

package cc.xiaojiang.lib.ble.callback;

import android.bluetooth.BluetoothGatt;

import cc.xiaojiang.lib.ble.BleDevice;

public interface IBleReadyCallback {

    public abstract void onReady();

    public abstract void onNotReady();
}

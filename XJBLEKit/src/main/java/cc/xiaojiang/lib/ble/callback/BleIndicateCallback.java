package cc.xiaojiang.lib.ble.callback;


import cc.xiaojiang.lib.ble.exception.BleException;

public abstract class BleIndicateCallback {

    public abstract void onIndicateSuccess();

    public abstract void onIndicateFailure(BleException exception);

    public abstract void onCharacteristicChanged(byte[] data);
}

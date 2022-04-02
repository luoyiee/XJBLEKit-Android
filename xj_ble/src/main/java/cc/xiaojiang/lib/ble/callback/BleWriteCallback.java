package cc.xiaojiang.lib.ble.callback;


import cc.xiaojiang.lib.ble.exception.BleException;

public abstract class BleWriteCallback{

    public abstract void onWriteSuccess(int current, int total, byte[] justWrite);

    public abstract void onWriteFailure(BleException exception);

}

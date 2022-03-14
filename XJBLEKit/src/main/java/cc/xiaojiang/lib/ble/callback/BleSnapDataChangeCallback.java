package cc.xiaojiang.lib.ble.callback;

public abstract class BleSnapDataChangeCallback {
    public abstract void onDataChanged(int errorCode,byte cmd, String hexString);
}

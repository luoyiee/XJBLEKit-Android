package cc.xiaojiang.lib.ble.callback;

public interface BleSnapshotGetCallback {
    void onResult(int errorCode, String hexString);
}

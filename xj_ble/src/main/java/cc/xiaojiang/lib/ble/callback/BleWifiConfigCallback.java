package cc.xiaojiang.lib.ble.callback;

/**
 * Created by facexxyz on 5/13/21.
 */
public interface BleWifiConfigCallback {
    void onBleWifiConfigSucceed();

    void onBleWifiConfigFailed(int code);
}

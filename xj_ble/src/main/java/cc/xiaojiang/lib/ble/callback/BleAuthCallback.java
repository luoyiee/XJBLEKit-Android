package cc.xiaojiang.lib.ble.callback;

import cc.xiaojiang.lib.ble.XJBleDevice;
import cc.xiaojiang.lib.ble.exception.AuthException;

public interface BleAuthCallback {
    void onAuthStep(XJBleDevice bleDevice,int step);

    void onAuthSuccess(XJBleDevice bleDevice);

    void onAuthFail(XJBleDevice bleDevice, int errorCode);

}

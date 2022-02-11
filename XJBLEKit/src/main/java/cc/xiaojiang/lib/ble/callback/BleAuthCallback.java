package cc.xiaojiang.lib.ble.callback;

import cc.xiaojiang.lib.ble.XJBleDevice;
import cc.xiaojiang.lib.ble.exception.AuthException;

public interface BleAuthCallback {

    void onAuthStep(int step);

    void onAuthSuccess(XJBleDevice var1);

    void onAuthFail(XJBleDevice var1, AuthException var2);


}

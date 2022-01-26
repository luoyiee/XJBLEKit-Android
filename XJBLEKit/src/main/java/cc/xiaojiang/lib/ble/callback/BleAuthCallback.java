package cc.xiaojiang.lib.ble.callback;

import cc.xiaojiang.lib.ble.BleDevice;
import cc.xiaojiang.lib.ble.exception.AuthException;

public interface BleAuthCallback {
    void onAuthSuccess(BleDevice var1);

    void onAuthFail(BleDevice var1, AuthException var2);
}

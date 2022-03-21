package cc.xiaojiang.lib.ble.callback;

import javax.security.auth.callback.Callback;

/**
 * Created by facexxyz on 5/12/21.
 */
public interface BleDataGetCallback {
    void onResult(int errorCode, String hexString);
}

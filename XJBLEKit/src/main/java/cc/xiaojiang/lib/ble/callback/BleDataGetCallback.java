package cc.xiaojiang.lib.ble.callback;

/**
 * Created by facexxyz on 5/12/21.
 */
public interface BleDataGetCallback {
    byte NO_ERR = 0x00;
    byte ERR_system = 0x01;
    byte ERR_needAuth = 0x02;
    byte ERR_decode = 0x03;
    byte ERR_invalidPara = 0x04;
    byte ERR_Auth = 0x05;

    void onResult(int errorCode, String hexString);
}

package cc.xiaojiang.lib.ble.callback.ota;

public interface OtaCheckCallback {

    byte NO_ERR = 0x00;
    byte ERR_system = 0x01;
    byte ERR_needAuth = 0x02;
    byte ERR_decode = 0x03;
    byte ERR_invalidPara = 0x04;
    byte ERR_Auth = 0x05;
    byte ERR_LOW_ENERGY = 0x06;
    byte ERR_VERSION = 0x07;

    void onOtaCheckSucceed();
    void onOtaCheckFailed(int code);

}

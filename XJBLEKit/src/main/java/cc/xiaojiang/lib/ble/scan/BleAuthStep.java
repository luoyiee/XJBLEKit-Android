package cc.xiaojiang.lib.ble.scan;

public enum  BleAuthStep {
    NONE(0),
    START(1),
    GETTING_TOKEN(2),
    DID_GET_TOKEN(3),
    GETTING_BLE_KEY(4),
    DID_GET_BLE_KEY(5),
    BINDING(6),
    READY(7);
    int code;

    BleAuthStep(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

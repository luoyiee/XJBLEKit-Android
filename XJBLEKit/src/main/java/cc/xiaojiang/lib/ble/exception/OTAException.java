package cc.xiaojiang.lib.ble.exception;


public class OTAException extends BleException {

    public OTAException() {
        super(ERROR_CODE_OTA, "OTA Exception Occurred!");
    }

}

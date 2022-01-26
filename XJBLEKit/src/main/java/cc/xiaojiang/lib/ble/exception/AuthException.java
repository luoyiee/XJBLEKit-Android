package cc.xiaojiang.lib.ble.exception;

import java.io.Serializable;


public class AuthException extends BleException implements Serializable {

    public AuthException() {
        super(ERROR_CODE_AUTH, "Auth Exception Occurred");
    }
}

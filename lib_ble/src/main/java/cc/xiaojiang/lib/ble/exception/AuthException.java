package cc.xiaojiang.lib.ble.exception;

import java.io.Serializable;

import lombok.Data;


@Data
public  class AuthException implements Serializable {
    public static final int NO_ERROR = 0;
    public static final int ERROR_DISCOVER_FAIL = 200;
    public static final int ERROR_GET_RANDOM_FAIL = 201;
    public static final int ERROR_GET_BLE_KEY = 202;

    private int code;
    private String description;

    public AuthException(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String toString() {
        return "AuthException { " +
                "code=" + code +
                ", description='" + description + '\'' +
                '}';
    }
}

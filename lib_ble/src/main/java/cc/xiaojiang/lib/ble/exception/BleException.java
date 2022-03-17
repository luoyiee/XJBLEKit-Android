package cc.xiaojiang.lib.ble.exception;

import java.io.Serializable;


public abstract class BleException implements Serializable {


    public static final int ERROR_CODE_TIMEOUT = 100;
    public static final int ERROR_CODE_GATT = 101;
    public static final int ERROR_CODE_OTHER = 102;
    public static final int ERROR_CODE_AUTH = 103;
    public static final int ERROR_CODE_OTA = 104;

    private int code;
    private String description;

    public BleException(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public BleException setCode(int code) {
        this.code = code;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public BleException setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return "BleException { " +
                "code=" + code +
                ", description='" + description + '\'' +
                '}';
    }
}

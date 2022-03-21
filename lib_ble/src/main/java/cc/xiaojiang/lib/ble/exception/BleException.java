package cc.xiaojiang.lib.ble.exception;

import java.io.Serializable;

import lombok.Data;


@Data
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

    @Override
    public String toString() {
        return "BleException { " +
                "code=" + code +
                ", description='" + description + '\'' +
                '}';
    }
}

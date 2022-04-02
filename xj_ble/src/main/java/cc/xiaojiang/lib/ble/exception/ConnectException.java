package cc.xiaojiang.lib.ble.exception;

import android.bluetooth.BluetoothGatt;

import java.io.Serializable;

import lombok.Data;

@Data
public class ConnectException implements Serializable {
    public static final int NO_ERROR = 0;
    public static final int ERROR_BLE_NOT_ENABLE = 1;
    public static final int ERROR_NO_DEVICE = 2;
    public static final int ERROR_GATT_CONNECT = 3;
    public static final int ERROR_RECONNECT = 4;
    public static final int ERROR_CONNECT_OVER_TIME = 5;



    private int code;
    private String description;

    public ConnectException(int code, String description) {
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

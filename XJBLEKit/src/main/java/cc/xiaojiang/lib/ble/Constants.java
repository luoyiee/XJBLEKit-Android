package cc.xiaojiang.lib.ble;

/**
 * Created by facexxyz on 3/9/21.
 */
public class Constants {

    public static final String UUID_AL_SERVICE = "0000feb3-0000-1000-8000-00805f9b34fb";
    public static final String UUID_XJ_SERVICE = "0000feb7-0000-1000-8000-00805f9b34fb";
    public static final String UUID_XJ_CHARACTERISTIC_INDICATE = "0000fea8-0000-1000-8000" +
            "-00805f9b34fb";
    public static final String UUID_XJ_CHARACTERISTIC_NOTIFY = "0000feaa-0000-1000-8000" +
            "-00805f9b34fb";
    public static final String UUID_XJ_CHARACTERISTIC_WRITE = "0000fea7-0000-1000-8000" +
            "-00805f9b34fb";

    public static final String UUID_XJ_CHARACTERISTIC_WRITE_WITH_NO_RESPONSE = "0000fea9-0000-1000-8000" +
            "-00805f9b34fb";

    public static final String UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR = "00002902-0000-1000" +
            "-8000-00805f9b34fb";

    public static final int XJ_MANUFACTURER_ID = 0xF018;
    public static final int AL_MANUFACTURER_ID = 0x01A8;
    /**
     * 常量
     */
    public static final boolean AUTO_CONNECT = false;
    public static final int DEFAULT_MAX_MULTIPLE_DEVICE = 7;
    public static final int DEFAULT_CONNECT_OVER_TIME = 10000;
    public static final int DEFAULT_CONNECT_RETRY_COUNT = 0;
}

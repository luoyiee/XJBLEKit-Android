package cc.xiaojiang.lib.ble.data;


public class BleMsg {


    // Scan
    public static final int MSG_SCAN_DEVICE = 0X00;

    // Connect
    public static final int MSG_CONNECT_FAIL = 0x01;
    public static final int MSG_DISCONNECTED = 0x02;
    public static final int MSG_RECONNECT = 0x03;
    public static final int MSG_DISCOVER_SERVICES = 0x04;
    public static final int MSG_DISCOVER_FAIL = 0x05;
    public static final int MSG_DISCOVER_SUCCESS = 0x06;
    public static final int MSG_CONNECT_OVER_TIME = 0x07;


    // Notify
    public static final int MSG_CHA_NOTIFY_START = 0x11;
    public static final int MSG_CHA_NOTIFY_RESULT = 0x12;
    public static final int MSG_CHA_NOTIFY_DATA_CHANGE = 0x13;
    public static final String KEY_NOTIFY_BUNDLE_STATUS = "notify_status";
    public static final String KEY_NOTIFY_BUNDLE_VALUE = "notify_value";

    // Indicate
    public static final int MSG_CHA_INDICATE_START = 0x21;
    public static final int MSG_CHA_INDICATE_RESULT = 0x22;
    public static final int MSG_CHA_INDICATE_DATA_CHANGE = 0x23;
    public static final String KEY_INDICATE_BUNDLE_STATUS = "indicate_status";
    public static final String KEY_INDICATE_BUNDLE_VALUE = "indicate_value";

    // Write
    public static final int MSG_CHA_WRITE_START = 0x31;
    public static final int MSG_CHA_WRITE_RESULT = 0x32;
    public static final int MSG_SPLIT_WRITE_NEXT = 0x33;
    public static final String KEY_WRITE_BUNDLE_VALUE = "write_value";

    // Read
    public static final int MSG_CHA_READ_START = 0x41;
    public static final int MSG_CHA_READ_RESULT = 0x42;
    public static final String KEY_READ_BUNDLE_STATUS = "read_status";
    public static final String KEY_READ_BUNDLE_VALUE = "read_value";

    // Rssi
    public static final int MSG_READ_RSSI_START = 0x51;
    public static final int MSG_READ_RSSI_RESULT = 0x52;
    public static final String KEY_READ_RSSI_BUNDLE_STATUS = "rssi_status";
    public static final String KEY_READ_RSSI_BUNDLE_VALUE = "rssi_value";

    // Mtu
    public static final int MSG_SET_MTU_START = 0x61;
    public static final int MSG_SET_MTU_RESULT = 0x62;
    public static final String KEY_SET_MTU_BUNDLE_STATUS = "mtu_status";
    public static final String KEY_SET_MTU_BUNDLE_VALUE = "mtu_value";

    // Auth
    public static final int MSG_AUTH_RANDOM = 0x71;
    public static final int MSG_AUTH_RANDOM_CIPHER = 0x72;
    public static final int MSG_AUTH_RESULT = 0x74;
    public static final int MSG_AUTH_SUCCEED = 0x75;
    public static final int MSG_AUTH_FAILED = 0x76;


    //cmd
    public static final int CMD_DOWN_VERIFY = 0x01;
    public static final int CMD_DOWN_INIT = 0x02;
    public static final int CMD_DOWN_REPORT = 0x03;
    public static final int CMD_DOWN_SET = 0x81;
    public static final int CMD_DOWN_GET = 0x82;
    public static final int CMD_DOWN_SNAPSHOT = 0x84;
    public static final int CMD_DOWN_WIFICONFIG = 0x85;
    public static final int CMD_DOWN_RANDOM = 0x86;
    public static final int CMD_DOWN_BIND = 0x87;

    // Ota
    public static final int MSG_OTA_VERSION_SUCCEED = 0xA0;
    public static final int MSG_OTA_VERSION_FAILED = 0xA1;
    public static final int MSG_OTA_DATA = 0xA2;

}

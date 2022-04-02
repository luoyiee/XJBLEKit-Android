package cc.xiaojiang.lib.ble.bleInterface;

public interface ConnectErrType {
    int NO_ERROR = 0; // 没有错误
    int ERR_NOT_BLE = 1; //不是蓝牙设备
    int ERR_NOT_AVAILABLE = 2; //蓝牙不可用
    int ERR_NO_PERMISSION = 3; //没有蓝牙权限
    int ERR_NOT_OPEN = 4; //手机没有打开蓝牙
    int ERR_CONNECTED = 5; //设备已连接（不需要再次连接了）
    int ERR_NO_DEVICE = 6; //没有搜索到设备
    int ERR_NOT_AUTH = 7; //设备认证失败
    int ERR_CONNECT_TIMEOUT = 8; //连接超时
    int ERR_CONNECT_CANCELED = 9; //连接被取消
    int ERR_CONNECT_FAILED = 10; //连接失败

}

package cc.xiaojiang.lib.ble.bleInterface;

public interface OtaErrType {
    int NO_ERROR = 0; // 成功
    int ERR_SYSTEM = 1; //通用错误
    int ERR_NEED_AUTH = 2; //设备未授权或者授权失败
    int ERR_DECODE = 3; //data域解码失败
    int ERR_INVALID_PARA = 4; //参数非法
    int ERR_AUTH = 5; //授权失败
    int ERR_LOW_BATTERY = 6; //设备电量不足
    int ERR_VER = 7; //版本号错误
    int ERR_BUSY = 8; //设备忙
    int ERR_VERIFY = 9; //文件校验失败
    int ERR_TIMEOUT = 99; //ota升级超时
}

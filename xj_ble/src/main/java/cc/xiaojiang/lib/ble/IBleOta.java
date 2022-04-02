package cc.xiaojiang.lib.ble;

public interface IBleOta {

    OtaInfo synchronizeOtaVersion(XJBleDevice bleDevice, String mcuVersion, String moduleVersion);

    String updateOtaProgress(XJBleDevice bleDevice, int firmwareType, String version, int status, int progress);
}

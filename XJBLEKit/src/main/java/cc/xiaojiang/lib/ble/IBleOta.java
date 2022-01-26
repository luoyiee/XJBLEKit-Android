package cc.xiaojiang.lib.ble;

public interface IBleOta {

    OtaInfo synchronizeOtaVersion(XJBleDevice XJBleDevice, String mcuVersion, String moduleVersion);

    String updateOtaProgress(XJBleDevice XJBleDevice, int firmwareType, String version, int status, int progress);
}

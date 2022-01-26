package cc.xiaojiang.lib.ble;

public interface IBleOta {

    OtaInfo synchronizeOtaVersion(BleDevice bleDevice, String mcuVersion, String moduleVersion);

    String updateOtaProgress(BleDevice bleDevice, int firmwareType, String version, int status, int progress);
}

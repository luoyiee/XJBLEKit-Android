package cc.xiaojiang.lib.ble;

public interface IBleAuth {

    String getBleKey(XJBleDevice XJBleDevice, String cipher);

    String getRandom(XJBleDevice XJBleDevice);

    String getToken(XJBleDevice XJBleDevice);

    OtaInfo synchronizeOtaVersion(String productKey, String deviceUuid, String mcuVersion, String moduleVersion);

    void updateOtaProgress(int status, int progress, long taskId);

    void upgradeDevice(long taskId);

    String updateOtaVersion(String deviceUuid, String mcuVersion, String moduleVersion);

    String reportOtaResult(String deviceUuid, int firmwareType, int taskId, String version);
}

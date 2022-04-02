package cc.xiaojiang.lib.ble;

public interface IBleAuth {

    String getBleKey(XJBleDevice bleDevice, String cipher);

    String getRandom(XJBleDevice bleDevice);

    String getToken(XJBleDevice bleDevice);

    OtaInfo synchronizeOtaVersion(String productKey, String deviceUuid, String mcuVersion, String moduleVersion);

    void updateOtaProgress(int status, int progress, long taskId);

    void upgradeDevice(long taskId);

    String updateOtaVersion(String deviceUuid, String mcuVersion, String moduleVersion);

    String reportOtaResult(String deviceUuid, int firmwareType, int taskId, String version);
}

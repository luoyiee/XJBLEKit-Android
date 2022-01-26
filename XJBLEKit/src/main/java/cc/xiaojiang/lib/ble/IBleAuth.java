package cc.xiaojiang.lib.ble;

public interface IBleAuth {

    String getBleKey(BleDevice bleDevice, String cipher);

    String getRandom(BleDevice bleDevice);

    String getToken(BleDevice bleDevice);

    OtaInfo synchronizeOtaVersion(String productKey, String deviceUuid, String mcuVersion, String moduleVersion);

    void updateOtaProgress(int status, int progress, long taskId);

    void upgradeDevice(long taskId);

    String updateOtaVersion(String deviceUuid, String mcuVersion, String moduleVersion);

    String reportOtaResult(String deviceUuid, int firmwareType, int taskId, String version);
}

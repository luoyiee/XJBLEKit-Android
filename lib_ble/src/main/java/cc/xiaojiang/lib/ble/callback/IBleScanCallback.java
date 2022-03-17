package cc.xiaojiang.lib.ble.callback;


import java.util.List;

import cc.xiaojiang.lib.ble.XJBleDevice;


/**
 * Created by facexxyz on 3/10/21.
 */
public interface IBleScanCallback {

    void onScanStarted(boolean success);

    void onLeDeviceScanned(XJBleDevice bleDevice);

    void onScanFinished(List<XJBleDevice> bleDeviceList);

    void onScanFailed(int errorCode);
}

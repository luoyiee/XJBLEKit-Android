package cc.xiaojiang.lib.ble.callback;


import java.util.List;

import cc.xiaojiang.lib.ble.XJBleDevice;


/**
 * Created by facexxyz on 3/10/21.
 */
public interface IBleScanCallback {

    public abstract void onScanStarted(boolean success);

    public abstract void onLeDeviceScanned(XJBleDevice XJBleDevice);

    public abstract void onScanFinished(List<XJBleDevice> XJBleDeviceList);

    public abstract void onScanFailed(int errorCode);
}

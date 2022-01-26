package cc.xiaojiang.lib.ble.callback;


import java.util.List;

import cc.xiaojiang.lib.ble.BleDevice;


/**
 * Created by facexxyz on 3/10/21.
 */
public interface IBleScanCallback {

    public abstract void onScanStarted(boolean success);

    public abstract void onLeDeviceScanned(BleDevice bleDevice);

    public abstract void onScanFinished(List<BleDevice> bleDeviceList);

    public abstract void onScanFailed(int errorCode);
}

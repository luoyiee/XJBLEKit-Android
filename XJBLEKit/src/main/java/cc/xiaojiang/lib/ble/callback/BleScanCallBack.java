package cc.xiaojiang.lib.ble.callback;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

import java.util.List;

import cc.xiaojiang.lib.ble.BleDevice;


public abstract class BleScanCallBack extends ScanCallback {

    public abstract void onScanStarted(boolean success);

    public abstract void onLeDeviceScanned(BleDevice bleDevice);

    public abstract void onScanFinished(List<BleDevice> bleDeviceList);

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);


    }
}

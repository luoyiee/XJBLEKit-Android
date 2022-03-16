package cc.xiaojiang.lib.ble.scan;

import static cc.xiaojiang.lib.ble.Constants.AL_MANUFACTURER_ID;
import static cc.xiaojiang.lib.ble.Constants.XJ_MANUFACTURER_ID;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Looper;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cc.xiaojiang.lib.ble.Constants;
import cc.xiaojiang.lib.ble.IBleAuth;
import cc.xiaojiang.lib.ble.XJBleDevice;
import cc.xiaojiang.lib.ble.XJBleManager;
import cc.xiaojiang.lib.ble.callback.BleConnectCallback;
import cc.xiaojiang.lib.ble.callback.IBleScanCallback;
import cc.xiaojiang.lib.ble.exception.OtherException;
import cc.xiaojiang.lib.ble.test.BleConnect;
import cc.xiaojiang.lib.ble.utils.BleLog;
import cc.xiaojiang.lib.ble.utils.ByteUtils;


/**
 * Created by facexxyz on 3/10/21.
 */
public class BleScanner {

    public static BleScanner getInstance() {
        return ourInstance;
    }

    private BleScanState mBleScanState = BleScanState.STATE_IDLE;

    private BluetoothLeScanner bluetoothLeScanner;
    private IBleScanCallback iBleScanCallback;
    private String autoConnectMac = "";
    public Map<Object, Object> productMap;
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            XJBleDevice bleDevice = scanResultToBleDevice(result);
            if (bleDevice == null) {
                return;
            }
            BleLog.d(bleDevice.toString());
            if (iBleScanCallback != null) {
                iBleScanCallback.onLeDeviceScanned(scanResultToBleDevice(result));
            }
            if (TextUtils.isEmpty(autoConnectMac)) {
                return;
            }
            //stop scan and connect
//            stopScan();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            List<XJBleDevice> bleDeviceList = new ArrayList<>();
            for (ScanResult result : results) {
                XJBleDevice bleDevice = scanResultToBleDevice(result);
                bleDeviceList.add(bleDevice);
            }
            if (iBleScanCallback != null) {
                iBleScanCallback.onScanFinished(bleDeviceList);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            if (iBleScanCallback != null) {
                iBleScanCallback.onScanFailed(errorCode);
            }
        }
    };

    private XJBleDevice scanResultToBleDevice(ScanResult result) {
        //获取厂商自定义格式广播
        if (result.getScanRecord() == null) {
            BleLog.w("getScanRecord null!");
            return null;
        }
        XJBleDevice bleDevice = new XJBleDevice();
        byte[] manufacturerSpecificData = null;
        String platform = "";
        if (result.getScanRecord().getManufacturerSpecificData(Constants.XJ_MANUFACTURER_ID) != null) {
            manufacturerSpecificData = result.getScanRecord().getManufacturerSpecificData(XJ_MANUFACTURER_ID);
            platform = XJBleDevice.PLATFORM_XJ;
            bleDevice.setPlatform(XJBleDevice.PLATFORM_XJ);
        } else if (result.getScanRecord().getManufacturerSpecificData(AL_MANUFACTURER_ID) != null) {
            manufacturerSpecificData = result.getScanRecord().getManufacturerSpecificData(Constants.AL_MANUFACTURER_ID);
            platform = XJBleDevice.PLATFORM_AL;
            bleDevice.setPlatform(XJBleDevice.PLATFORM_AL);
        }
        if (manufacturerSpecificData == null) {
            BleLog.w("get manufacturerSpecificData null!");
            return null;
        }
        if (manufacturerSpecificData.length != 12) {
            BleLog.w("manufacturerSpecificData length error= " + manufacturerSpecificData.length);
            return null;
        }
        BleLog.d("get Manufacturer Specific Data: " + ByteUtils.bytesToHexString
                (manufacturerSpecificData));
        ManufacturerData manufacturerData =
                new ManufacturerData(manufacturerSpecificData, platform);
        bleDevice.setDevice(result.getDevice());
        bleDevice.setRssi(result.getRssi());
        bleDevice.setManufacturerData(manufacturerData);
        return bleDevice;
    }

    private static final BleScanner ourInstance = new BleScanner();


    private BleScanner() {
    }

    public void scan(IBleScanCallback iBleScanCallback) {
        bluetoothLeScanner =
                XJBleManager.getInstance().getBluetoothAdapter().getBluetoothLeScanner();
        startLeScan(iBleScanCallback);
    }

    public BluetoothGatt scanAndConnect(final String mac, final IBleAuth iBleAuth,
                                        final BleConnectCallback bleConnectCallback) {
        if (TextUtils.isEmpty(mac) || iBleAuth == null || bleConnectCallback == null) {
            Log.d("H5", "scanAndConnect null");
            return null;
        }
        startLeScan(new IBleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {

            }

            @Override
            public void onLeDeviceScanned(final XJBleDevice bleDevice) {
                if (bleDevice == null) {
                    Log.d("H5", "onLeDeviceScanned null");
                    return;
                }
                if (mac.equals(bleDevice.getMac())) {
                    stopScan();
                    BleConnect.getInstance().connect(bleDevice, iBleAuth, bleConnectCallback);
                }
            }

            @Override
            public void onScanFinished(List<XJBleDevice> bleDeviceList) {

            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.d("H5", "onScanFailed");
            }
        });
        return null;
    }

    public void stopScan() {
        if (iBleScanCallback != null && mBleScanState == BleScanState.STATE_SCANNING) {
            mBleScanState = BleScanState.STATE_IDLE;
            Log.d("H5", "stopScan");
            iBleScanCallback = null;
            try {
                bluetoothLeScanner.stopScan(scanCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void startLeScan(IBleScanCallback iBleScanCallback) {
        if (iBleScanCallback == null) {
            throw new IllegalArgumentException("can not start le scan with callback null!");
        }
        this.iBleScanCallback = iBleScanCallback;
        if (mBleScanState != BleScanState.STATE_IDLE) {
            Log.d("H5", "scan action already exists, complete the previous scan action first");
            iBleScanCallback.onScanStarted(false);
            return;
        }
        //小匠服务uuid
        ScanFilter.Builder XJScanFilterBuilder = new ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString(Constants.UUID_XJ_SERVICE));
        //阿里服务uuid
        ScanFilter.Builder ALScanFilterBuilder = new ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString(Constants.UUID_AL_SERVICE));
        List<ScanFilter> scanFilters = new ArrayList<>();
        scanFilters.add(XJScanFilterBuilder.build());
        scanFilters.add(ALScanFilterBuilder.build());
        bluetoothLeScanner.startScan(scanFilters, new ScanSettings.Builder().build(), scanCallback);
//        mBleScanState = success ? BleScanState.STATE_SCANNING : BleScanState.STATE_IDLE;
        mBleScanState = BleScanState.STATE_SCANNING;
    }


    /**
     * 获取扫描状态
     *
     * @return
     */
    public BleScanState getScanState() {
        return mBleScanState;
    }


//    /**
//     * connect a known device
//     *
//     * @param bleDevice
//     * @param bleConnectCallback
//     * @return
//     */
//    public BluetoothGatt connect(BleDevice bleDevice, IBleAuth iBleAuth, BleConnectCallback bleConnectCallback) {
//        if (bleConnectCallback == null) {
//            throw new IllegalArgumentException("BleGattCallback can not be Null!");
//        }
//
//        if (!XJBleManager.getInstance().isBleEnable()) {
//            BleLog.e("Bluetooth not enable!");
//            bleConnectCallback.onConnectFail(bleDevice, new OtherException("Bluetooth not enable!"));
//            return null;
//        }
//
//        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) {
//            BleLog.w("Be careful: currentThread is not MainThread!");
//        }
//
//        if (bleDevice == null || bleDevice.getDevice() == null) {
//            bleConnectCallback.onConnectFail(bleDevice, new OtherException("Not Found Device Exception Occurred!"));
//        } else {
////            BleConnect bleConnect = XJBleManager.getInstance().getMultipleBluetoothController().buildConnectingBle(bleDevice);
//            return bleConnect.connect(bleDevice, iBleAuth, bleConnectCallback);
//        }
//
//        return null;
//    }


}

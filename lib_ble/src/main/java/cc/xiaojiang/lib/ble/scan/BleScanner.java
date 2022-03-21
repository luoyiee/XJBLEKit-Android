package cc.xiaojiang.lib.ble.scan;

import static cc.xiaojiang.lib.ble.Constants.AL_MANUFACTURER_ID;
import static cc.xiaojiang.lib.ble.Constants.XJ_MANUFACTURER_ID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.xiaojiang.lib.ble.Constants;
import cc.xiaojiang.lib.ble.XJBleDevice;
import cc.xiaojiang.lib.ble.XJBleManager;
import cc.xiaojiang.lib.ble.callback.BleConnectCallback;
import cc.xiaojiang.lib.ble.callback.IBleScanCallback;
import cc.xiaojiang.lib.ble.utils.BleLog;
import cc.xiaojiang.lib.ble.utils.ByteUtils;
import cc.xiaojiang.lib.ble.utils.ScanRecordUtil;


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
    private final String autoConnectMac = "";
    private String platform = "";
    public Map<Object, Object> productMap = new HashMap<>();
    private static final BleScanner ourInstance = new BleScanner();

    private BleScanner() {
    }

    private final ScanCallback scanCallback = new ScanCallback() {
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
        XJBleDevice xjBleDevice = new XJBleDevice();
        byte[] manufacturerSpecificData = null;
        if (result.getScanRecord().getManufacturerSpecificData(Constants.XJ_MANUFACTURER_ID) != null) {
            manufacturerSpecificData = result.getScanRecord().getManufacturerSpecificData(XJ_MANUFACTURER_ID);
            platform = XJBleDevice.PLATFORM_XJ;
            xjBleDevice.setPlatform(platform);
        } else if (result.getScanRecord().getManufacturerSpecificData(AL_MANUFACTURER_ID) != null) {
            manufacturerSpecificData = result.getScanRecord().getManufacturerSpecificData(AL_MANUFACTURER_ID);
            platform = XJBleDevice.PLATFORM_AL;
            xjBleDevice.setPlatform(platform);
        }
        if (manufacturerSpecificData == null) {
            BleLog.w("get manufacturerSpecificData null!");
            return null;
        }
        if (manufacturerSpecificData.length != 12) {
            BleLog.w("manufacturerSpecificData length error= " + manufacturerSpecificData.length);
            return null;
        }
        BleLog.d("get Manufacturer Specific Data: " + ByteUtils.bytesToHexString(manufacturerSpecificData));
        ManufacturerData manufacturerData = new ManufacturerData(manufacturerSpecificData, platform);
        xjBleDevice.setDevice(result.getDevice());
        xjBleDevice.setRssi(result.getRssi());
        xjBleDevice.setManufacturerData(manufacturerData);
        return xjBleDevice;
    }

    public XJBleDevice rnToBleDevice(HashMap<String, Object> propsMap, byte[] scanRecordBytes) {
        XJBleDevice xjBleDevice = new XJBleDevice();
        //获取厂商自定义格式广播
        if (scanRecordBytes == null) {
            return xjBleDevice;
        }
        String id = String.valueOf(propsMap.get("id"));
        int rssi = Double.valueOf(String.valueOf(propsMap.get("rssi"))).intValue();
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(id);
        xjBleDevice.setDevice(device);
        xjBleDevice.setRssi(rssi);
        xjBleDevice.setId(id);
        byte[] manufacturerSpecificData = null;
        ScanRecordUtil scanRecordUtil = ScanRecordUtil.parseFromBytes(scanRecordBytes);
        if (scanRecordUtil.getManufacturerSpecificData() == null) {
            return xjBleDevice;
        }
        if (scanRecordUtil.getManufacturerSpecificData(XJ_MANUFACTURER_ID) != null) {
            manufacturerSpecificData = scanRecordUtil.getManufacturerSpecificData(XJ_MANUFACTURER_ID);
            platform = XJBleDevice.PLATFORM_XJ;
            xjBleDevice.setPlatform(platform);
            xjBleDevice.setCid(XJ_MANUFACTURER_ID);
        } else if (scanRecordUtil.getManufacturerSpecificData() != null) {
            manufacturerSpecificData = scanRecordUtil.getManufacturerSpecificData(AL_MANUFACTURER_ID);
            platform = XJBleDevice.PLATFORM_AL;
            xjBleDevice.setPlatform(platform);
            xjBleDevice.setCid(AL_MANUFACTURER_ID);
        }
        if (manufacturerSpecificData == null) {
            BleLog.w("get manufacturerSpecificData null!");
            return xjBleDevice;
        }
        if (manufacturerSpecificData.length != 12) {
            BleLog.w("manufacturerSpecificData length error= " + manufacturerSpecificData.length);
            return xjBleDevice;
        }
        BleLog.d("get Manufacturer Specific Data: " + ByteUtils.bytesToHexString(manufacturerSpecificData));
        ManufacturerData manufacturerData = new ManufacturerData(manufacturerSpecificData, platform);
        xjBleDevice.setDevice(device);
        xjBleDevice.setRssi(rssi);
        xjBleDevice.setManufacturerData(manufacturerData);
        return xjBleDevice;
    }

    public void scan(IBleScanCallback iBleScanCallback) {
        startLeScan(iBleScanCallback);
    }

    public BluetoothGatt scanAndConnect(final String mac,
                                        final BleConnectCallback bleConnectCallback) {
        if (TextUtils.isEmpty(mac) || bleConnectCallback == null) {
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
                    XJBleManager.getInstance().connect(bleDevice, bleConnectCallback);
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

    public synchronized void stopScan() {
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
        bluetoothLeScanner =
                XJBleManager.getInstance().getBluetoothAdapter().getBluetoothLeScanner();
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

}

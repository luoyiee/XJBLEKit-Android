package cc.xiaojiang.lib.ble.scan;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Looper;
import android.os.ParcelUuid;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cc.xiaojiang.lib.ble.XJBleDevice;
import cc.xiaojiang.lib.ble.Constants;
import cc.xiaojiang.lib.ble.IBleAuth;
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
    private BleScanState mBleScanState = BleScanState.STATE_IDLE;
    private BluetoothLeScanner bluetoothLeScanner;
    private IBleScanCallback iBleScanCallback;
    private String autoConnectMac = "";
    private String random = "";
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            XJBleDevice XJBleDevice = scanResultToBleDevice(result);
            if (XJBleDevice == null) {
                return;
            }
            BleLog.d(XJBleDevice.toString());
            iBleScanCallback.onLeDeviceScanned(scanResultToBleDevice(result));
            if (TextUtils.isEmpty(autoConnectMac)) {
                return;
            }
            //stop scan and connect
            stopScan();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            List<XJBleDevice> XJBleDeviceList = new ArrayList<>();
            for (ScanResult result : results) {
                XJBleDevice XJBleDevice = scanResultToBleDevice(result);
                XJBleDeviceList.add(XJBleDevice);
            }
            iBleScanCallback.onScanFinished(XJBleDeviceList);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            iBleScanCallback.onScanFailed(errorCode);
        }
    };

    private XJBleDevice scanResultToBleDevice(ScanResult result) {
        //获取厂商自定义格式广播
        if (result.getScanRecord() == null) {
            BleLog.w("getScanRecord null!");
            return null;
        }
        XJBleDevice XJBleDevice = new XJBleDevice();
        byte[] manufacturerSpecificData = null;
        if (result.getScanRecord().getServiceUuids().contains(ParcelUuid.fromString(Constants.UUID_XJ_SERVICE))) {
            manufacturerSpecificData =
                    result.getScanRecord().getManufacturerSpecificData(Constants.XJ_MANUFACTURER_ID);
            XJBleDevice.setPlatform(XJBleDevice.PLATFORM_XJ);
        } else if (result.getScanRecord().getServiceUuids().contains(ParcelUuid.fromString(Constants.UUID_AL_SERVICE))) {
            manufacturerSpecificData =
                    result.getScanRecord().getManufacturerSpecificData(Constants.AL_MANUFACTURER_ID);
            XJBleDevice.setPlatform(XJBleDevice.PLATFORM_AL);
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
                new ManufacturerData(manufacturerSpecificData, XJBleDevice.getPlatform());
        XJBleDevice.setDevice(result.getDevice());
        XJBleDevice.setRssi(result.getRssi());
        XJBleDevice.setManufacturerData(manufacturerData);
        return XJBleDevice;
    }


    public XJBleDevice rnToBleDevice(BluetoothDevice device, HashMap<Object, Object> advertising, byte[] data) {
        //获取厂商自定义格式广播
        if (advertising == null) {
            return null;
        }
        ArrayList<Object> serviceUUIDs = (ArrayList) advertising.get("serviceUUIDs");
        if (serviceUUIDs == null) {
            return null;
        }
        XJBleDevice xjBleDevice = new XJBleDevice();
        byte[] manufacturerSpecificData;
        manufacturerSpecificData = data;
        if (manufacturerSpecificData == null) {
            BleLog.w("get manufacturerSpecificData null!");
            return null;
        }
        if (manufacturerSpecificData.length != 12) {
            BleLog.w("manufacturerSpecificData length error= " + manufacturerSpecificData.length);
            return null;
        }
        if (serviceUUIDs.contains(Constants.XJ_MANUFACTURER_ID)) {
            xjBleDevice.setPlatform(XJBleDevice.PLATFORM_XJ);
        } else if (serviceUUIDs.contains(Constants.AL_MANUFACTURER_ID)) {
            xjBleDevice.setPlatform(XJBleDevice.PLATFORM_AL);
        }
        BleLog.d("get Manufacturer Specific Data: " + ByteUtils.bytesToHexString
                (manufacturerSpecificData));
        ManufacturerData manufacturerData =
                new ManufacturerData(manufacturerSpecificData, xjBleDevice.getPlatform());
        xjBleDevice.setDevice(device);
        xjBleDevice.setRssi(Double.valueOf(String.valueOf(advertising.get("rssi"))).intValue());
        xjBleDevice.setManufacturerData(manufacturerData);
        return xjBleDevice;
    }


    private static final BleScanner ourInstance = new BleScanner();

    public static BleScanner getInstance() {
        return ourInstance;
    }

    private BleScanner() {
        bluetoothLeScanner =
                XJBleManager.getInstance().getBluetoothAdapter().getBluetoothLeScanner();
    }

    public void scan(IBleScanCallback iBleScanCallback) {
        startLeScan(iBleScanCallback);
    }

    public BluetoothGatt scanAndConnect(final String mac, final IBleAuth iBleAuth,
                                        final BleConnectCallback bleConnectCallback) {
        if (TextUtils.isEmpty(mac) || iBleAuth == null || bleConnectCallback == null) {
            BleLog.e("scanAndConnect with empty params!");
            return null;
        }
        startLeScan(new IBleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {

            }

            @Override
            public void onLeDeviceScanned(final XJBleDevice XJBleDevice) {
                if (XJBleDevice == null) {
                    return;
                }
                if (mac.equals(XJBleDevice.getMac())) {
                    stopScan();
                    BleConnect.getInstance().connect(XJBleDevice, iBleAuth, bleConnectCallback);
                }
            }

            @Override
            public void onScanFinished(List<XJBleDevice> XJBleDeviceList) {

            }

            @Override
            public void onScanFailed(int errorCode) {

            }
        });
        return null;
    }

    public void stopScan() {
        bluetoothLeScanner.stopScan(scanCallback);
        mBleScanState = BleScanState.STATE_IDLE;
    }

    public void startLeScan(IBleScanCallback iBleScanCallback) {
        if (iBleScanCallback == null) {
            throw new IllegalArgumentException("can not start le scan with callback null!");
        }
        this.iBleScanCallback = iBleScanCallback;
        if (mBleScanState != BleScanState.STATE_IDLE) {
            BleLog.w("scan action already exists, complete the previous scan action first");
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
        mBleScanState = BleScanState.STATE_SCANNING;
        BleLog.d("start le scan……");
    }


    /**
     * 获取扫描状态
     *
     * @return
     */
    public BleScanState getScanState() {
        return mBleScanState;
    }


    /**
     * connect a known device
     *
     * @param XJBleDevice
     * @param bleConnectCallback
     * @return
     */
    public BluetoothGatt connect(XJBleDevice XJBleDevice, IBleAuth iBleAuth, BleConnectCallback
            bleConnectCallback) {
        if (bleConnectCallback == null) {
            throw new IllegalArgumentException("BleGattCallback can not be Null!");
        }

        if (!XJBleManager.getInstance().isBleEnable()) {
            BleLog.e("Bluetooth not enable!");
            bleConnectCallback.onConnectFail(XJBleDevice, new OtherException("Bluetooth not enable!"));
            return null;
        }

        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) {
            BleLog.w("Be careful: currentThread is not MainThread!");
        }

        if (XJBleDevice == null || XJBleDevice.getDevice() == null) {
            bleConnectCallback.onConnectFail(XJBleDevice, new OtherException("Not Found Device Exception Occurred!"));
        } else {
            BleConnect bleConnect = XJBleManager.getInstance().getMultipleBluetoothController().buildConnectingBle(XJBleDevice);
            return bleConnect.connect(XJBleDevice, iBleAuth, bleConnectCallback);
        }

        return null;
    }


}

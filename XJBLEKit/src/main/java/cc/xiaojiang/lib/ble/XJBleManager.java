package cc.xiaojiang.lib.ble;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;

import java.nio.ByteBuffer;
import java.util.List;

import cc.xiaojiang.lib.ble.callback.BleAuthCallback;
import cc.xiaojiang.lib.ble.callback.BleConnectCallback;
import cc.xiaojiang.lib.ble.callback.BleDataChangeCallback;
import cc.xiaojiang.lib.ble.callback.BleDataSetCallback;
import cc.xiaojiang.lib.ble.callback.BleWifiConfigCallback;
import cc.xiaojiang.lib.ble.callback.IBleScanCallback;
import cc.xiaojiang.lib.ble.callback.ota.OtaProgressCallBack;
import cc.xiaojiang.lib.ble.callback.ota.OtaResultCallback;
import cc.xiaojiang.lib.ble.callback.ota.OtaVersionCallback;
import cc.xiaojiang.lib.ble.callback.ota.SendResultCallBack;
import cc.xiaojiang.lib.ble.data.BleStatusCallback;
import cc.xiaojiang.lib.ble.data.BluetoothChangedObserver;
import cc.xiaojiang.lib.ble.data.GpsChangedObserver;
import cc.xiaojiang.lib.ble.data.GpsStatusCallback;
import cc.xiaojiang.lib.ble.scan.BleScanState;
import cc.xiaojiang.lib.ble.scan.BleScanner;
import cc.xiaojiang.lib.ble.test.BleConnect;
import cc.xiaojiang.lib.ble.test.MultipleBluetoothController;
import cc.xiaojiang.lib.ble.utils.BleLog;
import cc.xiaojiang.lib.ble.utils.ByteUtils;


public class XJBleManager {
    public static final int REQUEST_ENABLE_BT = 1;
    private Application context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    public static BluetoothChangedObserver bleObserver;
    public static GpsChangedObserver gpsObserver;
    private static final XJBleManager ourInstance = new XJBleManager();

    public static XJBleManager getInstance() {
        return ourInstance;
    }

    private XJBleManager() {
    }

    public void init(Application app) {
        if (context == null && app != null) {
            context = app;
            if (isSupportBle()) {
                bluetoothManager =
                        (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                bluetoothAdapter = bluetoothManager.getAdapter();
                multipleBluetoothController = new MultipleBluetoothController();
//                BleConnect.getInstance().initController();
            } else {
                BleLog.e("device not support ble");
            }
        }
    }


    /**
     * Get the Context
     *
     * @return
     */
    public Context getContext() {
        return context;
    }

    public void setData(byte[] payload, BleDataSetCallback callback) {
        BleConnect.getInstance().setData(payload, callback);
    }

    public void getData(byte[] payload) {
        BleConnect.getInstance().getData(payload);
    }


    public void sendApInfoWithSSID(String ssid, String pwd,
                                   String token, String url, BleWifiConfigCallback callback) {
        // TODO: 4/7/21 参数判断
        //payload
        byte[] ssidBytes = PayLoadUtils.getStringPayload(PayLoadUtils.ATTR_ID_SSID, ssid);
        byte[] pwdBytes = PayLoadUtils.getStringPayload(PayLoadUtils.ATTR_ID_PASSWORD, pwd);
        byte[] tokenBytes = PayLoadUtils.getArrayPayload(PayLoadUtils.ATTR_ID_TOKEN,
                ByteUtils.hexStrToBytes(token));

        byte[] areaTypeBytes = PayLoadUtils.getUInt8Payload(PayLoadUtils.ATTR_TYPE_AREA,
                (byte) 0x01);//传url

        byte[] areaIdBytes = PayLoadUtils.getStringPayload(PayLoadUtils.ATTR_URL_AREA, url);

        int payloadLength = ssidBytes.length + pwdBytes.length + tokenBytes.length + areaIdBytes.length + areaTypeBytes.length;
        ByteBuffer payloadBuffer = ByteBuffer.allocate(payloadLength);
        payloadBuffer.put(ssidBytes);
        payloadBuffer.put(pwdBytes);
        payloadBuffer.put(tokenBytes);
        payloadBuffer.put(areaTypeBytes);
        payloadBuffer.put(areaIdBytes);
        BleConnect.getInstance().startBleWifiConfig(payloadBuffer.array(), token, callback);
    }


    public void queryVersion(IBleAuth iBleAuth, OtaVersionCallback callback) {//查询设备版本信息
        // 固件类型：0x01-表示蓝牙设备固件
        // 0x02-表示MCU设备固件
        // 0x00-表示同时查询以上两个固件版本
        byte[] firmwareTypeArray = ByteUtils.byteToBytes((byte) 0x00);
        BleConnect.getInstance().queryVersion(firmwareTypeArray, iBleAuth, callback);
    }

    //升级请求--0xA1
    public void otaRequestOTA(OtaInfo.ContentBean.ModuleBean bean, OtaResultCallback resultCallback, OtaProgressCallBack progressCallBack) {//升级请求--0xA1
        //0x01-表示蓝牙设备固件
        //0x02-表示MCU设备固件

        byte[] firmwareTypeBytes = ByteUtils.byteToBytes((byte) bean.getFirmwareType());
        byte[] fileVersionBytes = ByteUtils.stringToBytes(bean.getVersion());
        byte[] verifyTypeBytes = ByteUtils.byteToBytes((byte) bean.getVerifyType());
        byte[] verifyValueBytes = ByteUtils.hexStrToBytes(bean.getFileHash());
        byte[] fileSizeBytes = ByteUtils.intToBytes(bean.getDownLoadBytes().length);
        int length = firmwareTypeBytes.length + fileVersionBytes.length
                + verifyTypeBytes.length + verifyValueBytes.length + fileSizeBytes.length;
        ByteBuffer payloadBuffer = ByteBuffer.allocate(length);
        payloadBuffer.put(firmwareTypeBytes);
        payloadBuffer.put(fileVersionBytes);
        payloadBuffer.put(verifyTypeBytes);
        payloadBuffer.put(verifyValueBytes);
        payloadBuffer.put(fileSizeBytes);
        BleConnect.getInstance().otaRequestOTA(payloadBuffer.array(), bean, resultCallback, progressCallBack);
    }

    public void removeGpsObserver() {
        if (gpsObserver != null) {
            gpsObserver.unregisterReceiver();
            gpsObserver = null;
        }
    }

    public void removeBleObserver() {
        if (bleObserver != null) {
            bleObserver.unregisterReceiver();
            bleObserver = null;
        }
    }

    /**
     * 设置全局蓝牙开启、关闭监听
     *
     * @param callback
     */
    public void setBleStatusCallback(BleStatusCallback callback) {
        if (bleObserver == null) {
            bleObserver = new BluetoothChangedObserver(context);
            bleObserver.registerReceiver();
        }
        bleObserver.setBleScanCallbackInner(callback);
    }

    /**
     * 设置全局GPS开启、关闭监听
     *
     * @param callback
     */

    public void setGpsStatusCallback(GpsStatusCallback callback) {
        if (gpsObserver == null) {
            gpsObserver = new GpsChangedObserver(context);
            gpsObserver.registerReceiver();
        }
        gpsObserver.setGpsCallbackInner(callback);
    }

    public void startLeScan(IBleScanCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("BleScanCallback can not be Null!");
        }
        if (!isBleEnable()) {
            BleLog.e("Bluetooth not enable!");
            callback.onScanStarted(false);
            return;
        }
        BleScanner.getInstance().scan(callback);
    }

    public boolean isConnected(XJBleDevice XJBleDevice) {
        return getConnectState(XJBleDevice) == BluetoothProfile.STATE_CONNECTED;
    }

    public void addDataChangeListener(BleDataChangeCallback callback) {
        BleConnect.getInstance().addDataChangeListener(callback);
    }

    public void addAuthStateListener(BleAuthCallback callback) {
        BleConnect.getInstance().addAuthStateListener(callback);
    }

    public void addSendResultListener(SendResultCallBack callback) {
        BleConnect.getInstance().addSendResultCallback(callback);
    }


//    public void addConnectionStateChangeListener(IBleConnectionCallback callback) {
//        BleConnect.getInstance().addConnectGattCallback(callback);
//    }

//    public boolean isConnected(String mac) {
//        try {
//            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(mac);
//            BleDevice bleDevice = new BleDevice();
//            bleDevice.setDevice(bluetoothDevice);
//            return isConnected(bleDevice);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public int getConnectState(BleDevice bleDevice) {
//        if (bleDevice != null) {
//            return bluetoothManager.getConnectionState(bleDevice.getDevice(),
//                    BluetoothProfile.GATT);
//        } else {
//            return BluetoothProfile.STATE_DISCONNECTED;
//        }
//    }

    public BleScanState getScanSate() {
        return BleScanner.getInstance().getScanState();
    }


    public boolean isSupportBle() {
        return context.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public boolean isBleEnable() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public void enableBluetooth() {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.enable();
        }
    }

    public void disableBluetooth() {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled())
                bluetoothAdapter.disable();
        }
    }

    public void stopLeScan() {
        if (isBleEnable()) {
            BleScanner.getInstance().stopScan();
        }
    }

    public void stopOTA() {
        BleConnect.getInstance().stopOTA();
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }


    //
    private static final int DEFAULT_MAX_MULTIPLE_DEVICE = 7; // 最高连接设备数量
    private static final int DEFAULT_CONNECT_RETRY_INTERVAL = 5000; //重连时间
    private static final int DEFAULT_CONNECT_RETRY_COUNT = 0; // 重连次数
    private int reConnectCount = DEFAULT_CONNECT_RETRY_COUNT;
    private int maxConnectCount = DEFAULT_MAX_MULTIPLE_DEVICE;
    private long reConnectInterval = DEFAULT_CONNECT_RETRY_INTERVAL;
    public static final int DEFAULT_SCAN_TIME = 10000; //扫描时间
    public static final int CONNECT_OVER_TIME = 10000; //连接超时时间
    private MultipleBluetoothController multipleBluetoothController;

    public int getMaxConnectCount() {
        return maxConnectCount;
    }

    public int getReConnectCount() {
        return reConnectCount;
    }

    public long getReConnectInterval() {
        return reConnectInterval;
    }

    public MultipleBluetoothController getMultipleBluetoothController() {
        return multipleBluetoothController;
    }

    public BluetoothGatt connect(XJBleDevice XJBleDevice, IBleAuth iBleAuth,
                                 BleConnectCallback bleConnectCallback) {
        return BleConnect.getInstance().connect(XJBleDevice, iBleAuth, bleConnectCallback);
    }


    public BluetoothGatt connect(String mac, IBleAuth iBleAuth,
                                 BleConnectCallback bleConnectCallback) {
        return BleScanner.getInstance().scanAndConnect(mac, iBleAuth, bleConnectCallback);
    }

    public void startAuth(IBleAuth iBleAuth) {
      BleConnect.getInstance().startAuth(iBleAuth);
    }

    public boolean isConnectedWithDevice(XJBleDevice XJBleDevice) {
        return getConnectState(XJBleDevice) == BluetoothProfile.STATE_CONNECTED;
    }

    public boolean isConnected(String mac) {
        List<XJBleDevice> list = getAllConnectedDevice();
        for (XJBleDevice XJBleDevice : list) {
            if (XJBleDevice != null) {
                if (XJBleDevice.getMac().equals(mac)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取所有连接的设备
     *
     * @return
     */
    public List<XJBleDevice> getAllConnectedDevice() {
        if (multipleBluetoothController == null)
            return null;
        return multipleBluetoothController.getDeviceList();
    }

    public int getConnectState(XJBleDevice XJBleDevice) {
        if (XJBleDevice != null) {
            return bluetoothManager.getConnectionState(XJBleDevice.getDevice(), BluetoothProfile.GATT);
        } else {
            return BluetoothProfile.STATE_DISCONNECTED;
        }
    }

    /**
     * 获取所有连接的设备
     *
     * @return
     */
//    public List<BleDevice> getAllConnectedDevice() {
////        if (multipleBluetoothController == null)
////            return null;
////        return multipleBluetoothController.getDeviceList();
//    }
    public XJBleDevice getConnectedDevice(String mac) {
        if (multipleBluetoothController == null)
            return null;
        XJBleDevice XJBleDevice = null;
        for (XJBleDevice ble : multipleBluetoothController.getDeviceList()
        ) {
            if (ble.getMac().equals(mac)) {
                XJBleDevice = ble;
            }
        }
        return XJBleDevice;
    }


    public void disconnectAllDevice() {
        if (multipleBluetoothController != null) {
            multipleBluetoothController.disconnectAllDevice();
        }
    }

    public void destroy() {
        removeBleObserver();
        removeGpsObserver();
        if (multipleBluetoothController != null) {
            multipleBluetoothController.destroy();
        }
    }

}

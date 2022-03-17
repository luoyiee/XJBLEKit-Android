package cc.xiaojiang.lib.ble;

import static cc.xiaojiang.lib.ble.Constants.DEFAULT_CONNECT_OVER_TIME;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cc.xiaojiang.lib.ble.callback.BleAuthCallback;
import cc.xiaojiang.lib.ble.callback.BleConnectCallback;
import cc.xiaojiang.lib.ble.callback.BleDataChangeCallback;
import cc.xiaojiang.lib.ble.callback.BleDataGetCallback;
import cc.xiaojiang.lib.ble.callback.BleDataSetCallback;
import cc.xiaojiang.lib.ble.callback.BleSnapshotGetCallback;
import cc.xiaojiang.lib.ble.callback.BleWifiConfigCallback;
import cc.xiaojiang.lib.ble.callback.IBleScanCallback;
import cc.xiaojiang.lib.ble.callback.ota.OtaProgressCallBack;
import cc.xiaojiang.lib.ble.callback.ota.OtaResultCallback;
import cc.xiaojiang.lib.ble.callback.ota.OtaVersionCallback;
import cc.xiaojiang.lib.ble.callback.ota.SendResultCallBack;
import cc.xiaojiang.lib.ble.callback.BleStatusCallback;
import cc.xiaojiang.lib.ble.data.BluetoothChangedObserver;
import cc.xiaojiang.lib.ble.data.GpsChangedObserver;
import cc.xiaojiang.lib.ble.callback.GpsStatusCallback;
import cc.xiaojiang.lib.ble.exception.OTAException;
import cc.xiaojiang.lib.ble.exception.OtherException;
import cc.xiaojiang.lib.ble.scan.BleScanState;
import cc.xiaojiang.lib.ble.scan.BleScanner;
import cc.xiaojiang.lib.ble.test.BleBluetooth;
import cc.xiaojiang.lib.ble.test.MultipleBluetoothController;
import cc.xiaojiang.lib.ble.utils.BleLog;
import cc.xiaojiang.lib.ble.utils.ByteUtils;


public class XJBleManager {
    private long lastScanTime = 0;
    private long lastScanTimeNew = 0;
    public static final int REQUEST_ENABLE_BT = 1;
    public static final long SCAN_OVER_TIME = 15000;//扫描超时时间
    public static final long BIND_OVER_TIME = 15000;//绑定超时时间
    public long SCAN_INTERNAL = 6000;//单次扫描间隔时间
    private static final int DEFAULT_MAX_MULTIPLE_DEVICE = 7; // 最高连接设备数量
    private static final int DEFAULT_CONNECT_RETRY_INTERVAL = 5000; //重连时间
    private static final int DEFAULT_CONNECT_RETRY_COUNT = 0; // 重连次数
    private int reConnectCount = DEFAULT_CONNECT_RETRY_COUNT;
    private int maxConnectCount = DEFAULT_MAX_MULTIPLE_DEVICE;
    private long reConnectInterval = DEFAULT_CONNECT_RETRY_INTERVAL;
    private long connectOverTime = DEFAULT_CONNECT_OVER_TIME;
    private MultipleBluetoothController multipleBluetoothController;
    private Context mContext;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    public BluetoothChangedObserver bleObserver;
    public GpsChangedObserver gpsObserver;
    private static final XJBleManager ourInstance = new XJBleManager();
    private Timer timer;
    private Timer newTimer;

    public static XJBleManager getInstance() {
        return ourInstance;
    }

    private XJBleManager() {
    }

    public void init(Context context) {
        if (mContext == null && context != null) {
            mContext = context;
            if (isSupportBle()) {
                bluetoothManager =
                        (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                bluetoothAdapter = bluetoothManager.getAdapter();
                multipleBluetoothController = new MultipleBluetoothController();
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
        return mContext;
    }


    public void startAuth(XJBleDevice bleDevice,IBleAuth iBleAuth) {
        BleBluetooth bleBluetooth = DeviceReady(bleDevice);
        if (bleBluetooth == null) {
            return;
        }
        bleBluetooth.startAuth(bleDevice,iBleAuth);
    }

    public void getSnapshot(XJBleDevice bleDevice, BleSnapshotGetCallback callback) {
        BleBluetooth bleBluetooth = DeviceReady(bleDevice);
        if (bleBluetooth == null) {
            callback.onResult(1,"");
            return;
        }
        bleBluetooth.getSnapshot(callback);
    }

    public void setData(XJBleDevice bleDevice, byte[] payload, BleDataSetCallback callback) {
        BleBluetooth bleBluetooth = DeviceReady(bleDevice);
        if (bleBluetooth == null) {
            callback.onResult(1);
            return;
        }
        bleBluetooth.setData(payload, callback);
    }


    public void getData(XJBleDevice bleDevice, byte[] payload, BleDataGetCallback callback) {
        BleBluetooth bleBluetooth = DeviceReady(bleDevice);
        if (bleBluetooth == null) {
            callback.onResult(1,"");
            return;
        }
        bleBluetooth.getData(payload,callback);
    }


    public void sendApInfoWithSSID(XJBleDevice bleDevice, String ssid, String pwd,
                                   String token, String url, BleWifiConfigCallback callback) {
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
        BleBluetooth bleBluetooth = DeviceReady(bleDevice);
        if (bleBluetooth == null) {
            callback.onBleWifiConfigFailed(1);
            return;
        }
        bleBluetooth.startBleWifiConfig(payloadBuffer.array(), callback);
    }


    public void queryVersion(XJBleDevice bleDevice, IBleAuth iBleAuth, OtaVersionCallback callback) {//查询设备版本信息
        // 固件类型：0x01-表示蓝牙设备固件
        // 0x02-表示MCU设备固件
        // 0x00-表示同时查询以上两个固件版本
        byte[] firmwareTypeArray = ByteUtils.byteToBytes((byte) 0x00);
        BleBluetooth bleBluetooth = DeviceReady(bleDevice);
        if (bleBluetooth == null) {
            callback.onVersionFailed(new OTAException());
            return;
        }
        bleBluetooth.queryVersion(firmwareTypeArray, iBleAuth, callback);
    }

    //升级请求--0xA1
    public void otaRequestOTA(XJBleDevice bleDevice, OtaInfo.ContentBean.ModuleBean bean, OtaResultCallback
            resultCallback, OtaProgressCallBack progressCallBack) {//升级请求--0xA1
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
        BleBluetooth bleBluetooth = DeviceReady(bleDevice);
        if (bleBluetooth == null) {
            return;
        }
        bleBluetooth.otaRequestOTA(payloadBuffer.array(), bean, resultCallback, progressCallBack);
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
            bleObserver = new BluetoothChangedObserver(mContext);
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
            gpsObserver = new GpsChangedObserver(mContext);
            gpsObserver.registerReceiver();
        }
        gpsObserver.setGpsCallbackInner(callback);
    }


    public void startScan(IBleScanCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("IBleScanCallback can not be Null!");
        }
        if (!isBleEnable()) {
            BleLog.e("Bluetooth not enable!");
            callback.onScanStarted(false);
            return;
        }
        BleScanner.getInstance().scan(callback);
    }

    public void startLeScan(IBleScanCallback callback) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        long timeInterval = System.currentTimeMillis() - lastScanTime;
        Log.d("H5", "startLeScan+ timeInterval" + timeInterval + "current" + System.currentTimeMillis() + "scanTime" + lastScanTime);
        if (timeInterval <= SCAN_INTERNAL && lastScanTime != 0) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    timer = null;
                    startScan(callback);
                    lastScanTime = System.currentTimeMillis();
                }
            }, SCAN_INTERNAL - timeInterval);
            return;
        }
        lastScanTime = System.currentTimeMillis();
        startScan(callback);
    }

    public void addDataChangeListener(XJBleDevice bleDevice, BleDataChangeCallback callback) {
        BleBluetooth bleBluetooth = DeviceReady(bleDevice);
        if (bleBluetooth != null) {
            bleBluetooth.addDataChangeListener(callback);
        }
    }

    public void addAuthStateListener(XJBleDevice bleDevice, BleAuthCallback callback) {
        BleBluetooth bleBluetooth = DeviceReady(bleDevice);
        if (bleBluetooth != null) {
            bleBluetooth.addAuthStateListener(callback);
        }
    }

    public void addSendResultListener(XJBleDevice bleDevice, SendResultCallBack callback) {
        BleBluetooth bleBluetooth = DeviceReady(bleDevice);
        if (bleBluetooth != null) {
            bleBluetooth.addSendResultCallback(callback);
        }
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
        return mContext.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
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
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (newTimer != null) {
            newTimer.cancel();
            newTimer = null;
        }
        if (isBleEnable() && XJBleManager.getInstance().getScanSate() == BleScanState.STATE_SCANNING) {
            BleScanner.getInstance().stopScan();
        }
    }

    public void stopOTA(XJBleDevice bleDevice) {
        BleBluetooth bleBluetooth = DeviceReady(bleDevice);
        if (bleBluetooth != null) {
            bleBluetooth.stopOTA();
        }
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public BluetoothAdapter setBluetoothAdapter() {
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return null;
        }
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        return bluetoothManager.getAdapter();
    }


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

    public BluetoothGatt connect(XJBleDevice bleDevice,
                                 BleConnectCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("BleGattCallback can not be Null!");
        }
        if (!isBleEnable()) {
            callback.onConnectFail(bleDevice, new OtherException("Bluetooth not enable!"));
            return null;
        }
        if (bleDevice == null || bleDevice.getDevice() == null) {
            callback.onConnectFail(bleDevice, new OtherException("Not Found Device Exception Occurred!"));
        } else {
            BleBluetooth bleBluetooth = multipleBluetoothController.buildConnectingBle(bleDevice);
            return bleBluetooth.connect(bleDevice, callback);
        }
        return null;
    }


    public void connect(String mac,
                        BleConnectCallback bleConnectCallback) {
        if (newTimer != null) {
            newTimer.cancel();
            newTimer = null;
        }
        long timeInterval = System.currentTimeMillis() - lastScanTimeNew;
        Log.d("H5", "startLeScan+ timeInterval" + timeInterval + "current" + System.currentTimeMillis() + "scanTime" + lastScanTimeNew);
        if (timeInterval <= SCAN_INTERNAL && lastScanTimeNew != 0) {
            newTimer = new Timer();
            newTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    newTimer = null;
                    Log.d("H5", "startLeScan+ timeInterval" + timeInterval + "current" + System.currentTimeMillis() + "scanTime" + lastScanTimeNew);
                    startConnect(mac, bleConnectCallback);
                }
            }, SCAN_INTERNAL - timeInterval);
            return;
        }

        startConnect(mac, bleConnectCallback);
    }

    public BluetoothGatt startConnect(String mac,
                                      BleConnectCallback bleConnectCallback) {
        lastScanTimeNew = System.currentTimeMillis();
        return BleScanner.getInstance().scanAndConnect(mac, bleConnectCallback);
    }

    public boolean isConnected(XJBleDevice bleDevice) {
        return getConnectState(bleDevice) == BluetoothProfile.STATE_CONNECTED;
    }

    public boolean isConnected(String mac) {
        List<XJBleDevice> list = getAllConnectedDevice();
        for (XJBleDevice bleDevice : list) {
            if (bleDevice != null) {
                if (bleDevice.getMac().equals(mac)) {
                    return true;
                }
            }
        }
        return false;
    }


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

    public int getConnectState(XJBleDevice bleDevice) {
        if (bleDevice != null) {
            return bluetoothManager.getConnectionState(bleDevice.getDevice(), BluetoothProfile.GATT);
        } else {
            return BluetoothProfile.STATE_DISCONNECTED;
        }
    }


    public XJBleDevice getConnectedDevice(String mac) {
        if (multipleBluetoothController == null)
            return null;
        XJBleDevice bleDevice = null;
        for (XJBleDevice ble : multipleBluetoothController.getDeviceList()
        ) {
            if (ble.getMac().equals(mac)) {
                bleDevice = ble;
            }
        }
        return bleDevice;
    }

    public void disconnectAllDevice() {
        if (multipleBluetoothController != null) {
            multipleBluetoothController.disconnectAllDevice();
        }
    }

    public void disconnect(XJBleDevice bleDevice) {
        if (multipleBluetoothController != null) {
            multipleBluetoothController.disconnect(bleDevice);
        }
    }

    public void destroy() {
        removeBleObserver();
        removeGpsObserver();
        if (multipleBluetoothController != null) {
            multipleBluetoothController.destroy();
        }
    }

    /**
     * Get operate connect Over Time
     *
     * @return
     */
    public long getConnectOverTime() {
        return connectOverTime;
    }

    /**
     * Set connect Over Time
     *
     * @param time
     * @return BleManager
     */
    public XJBleManager setConnectOverTime(long time) {
        if (time <= 0) {
            time = 100;
        }
        this.connectOverTime = time;
        return this;
    }


    public BleBluetooth DeviceReady(XJBleDevice bleDevice) {
        return multipleBluetoothController.getBleBluetooth(bleDevice);
    }

}

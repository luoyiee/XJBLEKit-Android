package cc.xiaojiang.lib.ble.test;


import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.xiaojiang.lib.ble.XJBleDevice;
import cc.xiaojiang.lib.ble.XJBleManager;

/**
 * 蓝牙设备列表处理，添加、移除，断开
 */
public class MultipleBluetoothController {

    private final BleLruHashMap<String, BleConnect> bleLruHashMap;
    private final HashMap<String, BleConnect> bleTempHashMap;

    public MultipleBluetoothController() {
        bleLruHashMap = new BleLruHashMap<>(XJBleManager.getInstance().getMaxConnectCount());
        bleTempHashMap = new HashMap<>();
    }

    // 获取BleBlueTooth实例
    public synchronized void buildConnectingBle() {
        Log.d("H5", "buildConnectingBle" + bleTempHashMap);
        BleConnect bleConnect = BleConnect.getInstance();
        if (!bleTempHashMap.containsKey(bleConnect.getDeviceKey())) {
            bleTempHashMap.put(bleConnect.getDeviceKey(), bleConnect);
        }
//        return bleConnect;
    }

    public synchronized void removeConnectingBle(BleConnect bleConnect) {
        if (bleConnect == null) {
            return;
        }
        if (bleTempHashMap.containsKey(bleConnect.getDeviceKey())) {
            bleTempHashMap.remove(bleConnect.getDeviceKey());
        }
    }

    public synchronized void addBleBluetooth(BleConnect bleBluetooth) {
        if (bleBluetooth == null) {
            return;
        }
        if (!bleLruHashMap.containsKey(bleBluetooth.getDeviceKey())) {
            bleLruHashMap.put(bleBluetooth.getDeviceKey(), bleBluetooth);
        }
    }

    public synchronized void removeBleBluetooth(BleConnect bleConnect) {
        if (bleConnect == null) {
            return;
        }
        if (bleLruHashMap.containsKey(bleConnect.getDeviceKey())) {
            bleLruHashMap.remove(bleConnect.getDeviceKey());
        }
    }

    public synchronized boolean isContainDevice(XJBleDevice bleDevice) {
        return bleDevice != null && bleLruHashMap.containsKey(bleDevice.getKey());
    }

    //
//    public synchronized boolean isContainDevice(BluetoothDevice bluetoothDevice) {
//        return bluetoothDevice != null && bleLruHashMap.containsKey(bluetoothDevice.getName() + bluetoothDevice.getAddress());
//    }
//
    public synchronized BleConnect getBleBluetooth(XJBleDevice bleDevice) {
        if (bleDevice != null) {
            if (bleLruHashMap.containsKey(bleDevice.getKey())) {
                return bleLruHashMap.get(bleDevice.getKey());
            }
        }
        return null;
    }
//

    /**
     * 断开蓝牙设备的连接
     *
     * @param bleDevice
     */
    public synchronized void disconnect(XJBleDevice bleDevice) {
        if (isContainDevice(bleDevice)) {
            getBleBluetooth(bleDevice).disconnect();
        }
    }


    /**
     * 断开所有蓝牙设备
     */
    public  void disconnectAllDevice() {
        for (Map.Entry<String, BleConnect> stringBleBluetoothEntry : bleLruHashMap.entrySet()) {
            stringBleBluetoothEntry.getValue().disconnect();
            Log.d("H5", "disconnect  bleLruHashMap");
        }
        bleLruHashMap.clear();
//        BleConnect.getInstance().disconnect();
    }

    public  void destroy() {
        Log.d("H5", "destroy");
        if (bleTempHashMap.size() == 0) {
//            Log.d("H5", "bleTempHashMap");
        }

        for (Map.Entry<String, BleConnect> stringBleBluetoothEntry : bleLruHashMap.entrySet()) {
            stringBleBluetoothEntry.getValue().destroy();
            Log.d("H5", "destroy  bleLruHashMap");
        }
        bleLruHashMap.clear();
        for (Map.Entry<String, BleConnect> stringBleBluetoothEntry : bleTempHashMap.entrySet()) {
            stringBleBluetoothEntry.getValue().destroy();
            Log.d("H5", "destroy  bleTempHashMap");
        }
        bleTempHashMap.clear();
    }

    //
    private synchronized List<BleConnect> getBleBluetoothList() {
        List<BleConnect> bleBluetoothList = new ArrayList<>(bleLruHashMap.values());
        Collections.sort(bleBluetoothList, (lhs, rhs) -> lhs.getDeviceKey().compareToIgnoreCase(rhs.getDeviceKey()));
        return bleBluetoothList;
    }

    /**
     * 获取已经连接的蓝牙设备列表
     *
     * @return
     */
    public synchronized List<XJBleDevice> getDeviceList() {
        refreshConnectedDevice();
        List<XJBleDevice> deviceList = new ArrayList<>();
        for (BleConnect bleConnect : getBleBluetoothList()) {
            if (bleConnect != null) {
                deviceList.add(bleConnect.getDevice());
            }
        }
        return deviceList;
    }

    /**
     * 移除未连接的蓝牙设备
     */
    public void refreshConnectedDevice() {
        List<BleConnect> bluetoothList = getBleBluetoothList();
        for (int i = 0; i < bluetoothList.size(); i++) {
            BleConnect bleBluetooth = bluetoothList.get(i);
            if (!XJBleManager.getInstance().isConnected(bleBluetooth.getDevice())) {
                removeBleBluetooth(bleBluetooth);
            }
        }
    }
}

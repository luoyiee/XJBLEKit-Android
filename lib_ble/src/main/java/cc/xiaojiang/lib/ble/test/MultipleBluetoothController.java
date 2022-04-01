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

    private final BleLruHashMap<String, XJBleBluetooth> bleLruHashMap;
    private final HashMap<String, XJBleBluetooth> bleTempHashMap;

    public MultipleBluetoothController() {
        bleLruHashMap = new BleLruHashMap<>(XJBleManager.getInstance().getMaxConnectCount());
        bleTempHashMap = new HashMap<>();
    }

    // 获取BleBlueTooth实例
    public synchronized XJBleBluetooth buildConnectBle(XJBleDevice bleDevice) {
        XJBleBluetooth XJBleBluetooth = new XJBleBluetooth(bleDevice);
        if (!bleTempHashMap.containsKey(XJBleBluetooth.getDeviceKey())) {
            bleTempHashMap.put(XJBleBluetooth.getDeviceKey(), XJBleBluetooth);
        }
        return XJBleBluetooth;
    }

    public synchronized void removeConnectBle(XJBleBluetooth XJBleBluetooth) {
        if (XJBleBluetooth == null) {
            return;
        }
        bleTempHashMap.remove(XJBleBluetooth.getDeviceKey());
    }

    public synchronized void addConnectedBle(XJBleBluetooth XJBleBluetooth) {
        if (XJBleBluetooth == null) {
            return;
        }
        String key = XJBleBluetooth.getDeviceKey();
        if (!bleTempHashMap.containsKey(key)) {
            return;
        }
        if (!bleLruHashMap.containsKey(XJBleBluetooth.getDeviceKey())) {
            bleLruHashMap.put(XJBleBluetooth.getDeviceKey(), XJBleBluetooth);
        }
    }

    public synchronized void removeConnectedBle(XJBleBluetooth XJBleBluetooth) {
        if (XJBleBluetooth == null) {
            return;
        }
        bleLruHashMap.remove(XJBleBluetooth.getDeviceKey());
    }

    public synchronized void removeBle(XJBleBluetooth bleBluetooth) {
        this.removeConnectedBle(bleBluetooth);
        this.removeConnectBle(bleBluetooth);
    }

    public synchronized boolean containConnectedDevice(XJBleDevice bleDevice) {
        return bleDevice != null && bleLruHashMap.containsKey(bleDevice.getKey());
    }

    public synchronized boolean isContainConnectingDevice(XJBleDevice bleDevice) {
        return bleDevice != null && bleTempHashMap.containsKey(bleDevice.getKey());
    }

//    public synchronized boolean isContainDevice(BluetoothDevice bluetoothDevice) {
//        return bluetoothDevice != null && bleLruHashMap.containsKey(bluetoothDevice.getName() + bluetoothDevice.getAddress());
//    }

    public synchronized XJBleBluetooth getConnectedBleBluetooth(XJBleDevice bleDevice) {
        if (bleDevice != null) {
            if (bleLruHashMap.containsKey(bleDevice.getKey())) {
                return bleLruHashMap.get(bleDevice.getKey());
            }
        }
        return null;
    }

    //
    public synchronized XJBleBluetooth getConnectBluetooth(XJBleDevice bleDevice) {
        if (bleDevice != null) {
            if (bleTempHashMap.containsKey(bleDevice.getKey())) {
                return bleTempHashMap.get(bleDevice.getKey());
            }
        }
        return null;
    }

    /**
     * 断开蓝牙设备的连接
     *
     * @param bleDevice
     */
    public synchronized void disconnect(XJBleDevice bleDevice) {
        XJBleBluetooth XJBleBluetooth = getConnectBluetooth(bleDevice);
        if (XJBleBluetooth != null) {
            XJBleBluetooth.disconnect();
        }
    }


//    public void destroy(XJBleDevice bleDevice) {
//
//
////        getBleBluetooth(bleDevice).destroy();
//        bleLruHashMap.remove(bleDevice.getKey());
//        getConnectingBluetooth(bleDevice).destroy();
//        bleTempHashMap.remove(bleDevice.getKey());
//    }


    /**
     * 断开所有蓝牙设备
     */
    public void disconnectAllDevice() {
        for (Map.Entry<String, XJBleBluetooth> stringBleBluetoothEntry : bleLruHashMap.entrySet()) {
            stringBleBluetoothEntry.getValue().disconnect();
        }
        bleLruHashMap.clear();
    }

    public void destroy() {
        for (Map.Entry<String, XJBleBluetooth> stringBleBluetoothEntry : bleLruHashMap.entrySet()) {
            stringBleBluetoothEntry.getValue().destroy();
            Log.d("H5", "destroy  bleLruHashMap");
        }
        bleLruHashMap.clear();
        for (Map.Entry<String, XJBleBluetooth> stringBleBluetoothEntry : bleTempHashMap.entrySet()) {
            stringBleBluetoothEntry.getValue().destroy();
            Log.d("H5", "destroy  bleTempHashMap");
        }
        bleTempHashMap.clear();
    }

    private synchronized List<XJBleBluetooth> getBleBluetoothList() {
        List<XJBleBluetooth> XJBleBluetoothList = new ArrayList<>(bleLruHashMap.values());
        Collections.sort(XJBleBluetoothList, (lhs, rhs) -> lhs.getDeviceKey().compareToIgnoreCase(rhs.getDeviceKey()));
        return XJBleBluetoothList;
    }

    /**
     * 获取所有连接的蓝牙设备列表
     *
     * @return
     */
    public synchronized List<XJBleDevice> getAllDeviceList() {
        List<XJBleDevice> deviceList = new ArrayList<>();
        for (XJBleBluetooth XJBleBluetooth : getBleBluetoothList()) {
            if (XJBleBluetooth != null) {
                deviceList.add(XJBleBluetooth.getDevice());
            }
        }
        return deviceList;
    }

    /**
     * 获取已经连接的蓝牙设备列表
     *
     * @return
     */
    public synchronized List<XJBleDevice> getDeviceList() {
        refreshConnectedDevice();
        List<XJBleDevice> deviceList = new ArrayList<>();
        for (XJBleBluetooth XJBleBluetooth : getBleBluetoothList()) {
            if (XJBleBluetooth != null) {
                deviceList.add(XJBleBluetooth.getDevice());
            }
        }
        return deviceList;
    }

    /**
     * 移除未连接的蓝牙设备
     */
    public void refreshConnectedDevice() {
        List<XJBleBluetooth> bluetoothList = getBleBluetoothList();
        for (int i = 0; i < bluetoothList.size(); i++) {
            XJBleBluetooth XJBleBluetooth = bluetoothList.get(i);
            if (!XJBleManager.getInstance().isConnected(XJBleBluetooth.getDevice())) {
                removeConnectedBle(XJBleBluetooth);
            }
        }
    }
}

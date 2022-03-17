package cc.xiaojiang.lib.ble.test;


import android.bluetooth.BluetoothDevice;
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

    private final BleLruHashMap<String, BleBluetooth> bleLruHashMap;
    private final HashMap<String, BleBluetooth> bleTempHashMap;

    public MultipleBluetoothController() {
        bleLruHashMap = new BleLruHashMap<>(XJBleManager.getInstance().getMaxConnectCount());
        bleTempHashMap = new HashMap<>();
    }

    // 获取BleBlueTooth实例
    public synchronized BleBluetooth buildConnectingBle(XJBleDevice bleDevice) {
        BleBluetooth bleBluetooth = new BleBluetooth(bleDevice);
        if (!bleTempHashMap.containsKey(bleBluetooth.getDeviceKey())) {
            bleTempHashMap.put(bleBluetooth.getDeviceKey(), bleBluetooth);
        }
        return bleBluetooth;
    }

    public synchronized void removeConnectingBle(BleBluetooth bleBluetooth) {
        if (bleBluetooth == null) {
            return;
        }
        if (bleTempHashMap.containsKey(bleBluetooth.getDeviceKey())) {
            bleTempHashMap.remove(bleBluetooth.getDeviceKey());
        }
    }

    public synchronized void addBleBluetooth(BleBluetooth bleBluetooth) {
        if (bleBluetooth == null) {
            return;
        }
        if (!bleLruHashMap.containsKey(bleBluetooth.getDeviceKey())) {
            bleLruHashMap.put(bleBluetooth.getDeviceKey(), bleBluetooth);
        }
    }

    public synchronized void removeBleBluetooth(BleBluetooth bleBluetooth) {
        if (bleBluetooth == null) {
            return;
        }
        if (bleLruHashMap.containsKey(bleBluetooth.getDeviceKey())) {
            bleLruHashMap.remove(bleBluetooth.getDeviceKey());
        }
    }

    public synchronized boolean isContainDevice(XJBleDevice bleDevice) {
        return bleDevice != null && bleLruHashMap.containsKey(bleDevice.getKey());
    }


    public synchronized boolean isContainDevice(BluetoothDevice bluetoothDevice) {
        return bluetoothDevice != null && bleLruHashMap.containsKey(bluetoothDevice.getName() + bluetoothDevice.getAddress());
    }

    public synchronized BleBluetooth getBleBluetooth(XJBleDevice bleDevice) {
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
        for (Map.Entry<String, BleBluetooth> stringBleBluetoothEntry : bleLruHashMap.entrySet()) {
            stringBleBluetoothEntry.getValue().disconnect();
        }
        bleLruHashMap.clear();
    }

    public  void destroy() {
        for (Map.Entry<String, BleBluetooth> stringBleBluetoothEntry : bleLruHashMap.entrySet()) {
            stringBleBluetoothEntry.getValue().destroy();
            Log.d("H5", "destroy  bleLruHashMap");
        }
        bleLruHashMap.clear();
        for (Map.Entry<String, BleBluetooth> stringBleBluetoothEntry : bleTempHashMap.entrySet()) {
            stringBleBluetoothEntry.getValue().destroy();
            Log.d("H5", "destroy  bleTempHashMap");
        }
        bleTempHashMap.clear();
    }

    //
    private synchronized List<BleBluetooth> getBleBluetoothList() {
        List<BleBluetooth> bleBluetoothList = new ArrayList<>(bleLruHashMap.values());
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
        for (BleBluetooth bleBluetooth : getBleBluetoothList()) {
            if (bleBluetooth != null) {
                deviceList.add(bleBluetooth.getDevice());
            }
        }
        return deviceList;
    }

    /**
     * 移除未连接的蓝牙设备
     */
    public void refreshConnectedDevice() {
        List<BleBluetooth> bluetoothList = getBleBluetoothList();
        for (int i = 0; i < bluetoothList.size(); i++) {
            BleBluetooth bleBluetooth = bluetoothList.get(i);
            if (!XJBleManager.getInstance().isConnected(bleBluetooth.getDevice())) {
                removeBleBluetooth(bleBluetooth);
            }
        }
    }
}

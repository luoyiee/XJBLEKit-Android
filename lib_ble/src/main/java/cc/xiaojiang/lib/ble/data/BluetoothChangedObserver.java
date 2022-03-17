package cc.xiaojiang.lib.ble.data;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.lang.ref.WeakReference;

import cc.xiaojiang.lib.ble.callback.BleStatusCallback;


/**
 * 蓝牙状态发生变化时
 * Created by jerry on 2018/8/29.
 */

public class BluetoothChangedObserver {

    private BleStatusCallback bleStatusCallback;
    private BleReceiver mBleReceiver;
    private Context mContext;
    private boolean mReceiverTag = false;   //广播接受者标识

    public BluetoothChangedObserver(Context context) {
        this.mContext = context;
    }

    public void setBleScanCallbackInner(BleStatusCallback bleStatusCallback) {
        this.bleStatusCallback = bleStatusCallback;
    }


    public void registerReceiver() {
        if (!mReceiverTag) {
            mBleReceiver = new BleReceiver(this);
            IntentFilter filter = new IntentFilter();
            mReceiverTag = true;    //标识值 赋值为 true 表示广播已被注册
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            mContext.registerReceiver(mBleReceiver, filter);
        }
    }

    public void unregisterReceiver() {
        try {
            if (mReceiverTag) {   //判断广播是否注册
                mReceiverTag = false;   //Tag值 赋值为false 表示该广播已被注销
                mContext.unregisterReceiver(mBleReceiver);
                bleStatusCallback = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static class BleReceiver extends BroadcastReceiver {
        private WeakReference<BluetoothChangedObserver> mObserverWeakReference;

        public BleReceiver(BluetoothChangedObserver bluetoothChangedObserver) {
            mObserverWeakReference = new WeakReference<BluetoothChangedObserver>(bluetoothChangedObserver);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                BluetoothChangedObserver observer = mObserverWeakReference.get();
                int status = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (status == BluetoothAdapter.STATE_ON) {
                    //   BleLog.e("","系统蓝牙已开启");
                    if (observer.bleStatusCallback != null) {
                        observer.bleStatusCallback.onBluetoothStatusChanged(true);
                    }
//                    ConnectRequest request = Rproxy.getRequest(ConnectRequest.class);
//                    request.openBluetooth();
                } else if (status == BluetoothAdapter.STATE_OFF) {
                    //   BleLog.e("","系统蓝牙已关闭");
                    if (observer.bleStatusCallback != null) {
                        observer.bleStatusCallback.onBluetoothStatusChanged(false);
                    }
//                    //如果正在扫描，则停止扫描
//                    ScanRequest scanRequest = Rproxy.getRequest(ScanRequest.class);
//                    if (scanRequest.isScanning()){
//                        scanRequest.onStop();
//                    }
//                    //解决原生android系统,直接断开系统蓝牙不回调onConnectionStateChange接口问题
//                    ConnectRequest request = Rproxy.getRequest(ConnectRequest.class);
//                    request.closeBluetooth();
                }
            }
        }
    }
}

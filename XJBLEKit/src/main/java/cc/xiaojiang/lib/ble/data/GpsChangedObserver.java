package cc.xiaojiang.lib.ble.data;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.util.Log;

import java.lang.ref.WeakReference;


/**
 * 蓝牙状态发生变化时
 * Created by jerry on 2018/8/29.
 */

public class GpsChangedObserver {

    private GpsStatusCallback gpsStatusCallback;
    private GpsReceiver mGpsReceiver;
    private Context mContext;
    private boolean mReceiverTag = false;   //广播接受者标识

    public GpsChangedObserver(Context context) {
        this.mContext = context;
    }

    public void setGpsCallbackInner(GpsStatusCallback gpsStatusCallback) {
        this.gpsStatusCallback = gpsStatusCallback;
    }


    public void registerReceiver() {
        if (!mReceiverTag) {
            mGpsReceiver = new GpsReceiver(this);
            IntentFilter filter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
            mReceiverTag = true;    //标识值 赋值为 true 表示广播已被注册
            filter.addAction(Intent.ACTION_PROVIDER_CHANGED);
            mContext.registerReceiver(mGpsReceiver, filter);
        }
    }

    public void unregisterReceiver() {
        try {
            if (mReceiverTag) {   //判断广播是否注册
                mReceiverTag = false;   //Tag值 赋值为false 表示该广播已被注销
                mContext.unregisterReceiver(mGpsReceiver);
                gpsStatusCallback = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static class GpsReceiver extends BroadcastReceiver {
        private WeakReference<GpsChangedObserver> mObserverWeakReference;

        public GpsReceiver(GpsChangedObserver gpsChangedObserver) {
            mObserverWeakReference = new WeakReference<GpsChangedObserver>(gpsChangedObserver);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {
                // Make an action or refresh an already managed state.
                GpsChangedObserver observer = mObserverWeakReference.get();
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (isGpsEnabled || isNetworkEnabled) {
                    if (observer.gpsStatusCallback != null) {
                        observer.gpsStatusCallback.onGpsStatusChanged(true);
                    }
                } else {
                    if (observer.gpsStatusCallback != null) {
                        observer.gpsStatusCallback.onGpsStatusChanged(false);
                    }
                    Log.w(this.getClass().getName(), "gpsSwitchStateReceiver.onReceive() location disabled ");
                }
            }
        }
    }
}

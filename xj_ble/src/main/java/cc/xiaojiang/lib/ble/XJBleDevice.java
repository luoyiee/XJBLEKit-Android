package cc.xiaojiang.lib.ble;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import cc.xiaojiang.lib.ble.scan.ManufacturerData;
import lombok.Data;

@Data
public class XJBleDevice implements Parcelable {
    private BluetoothDevice device;
    private ManufacturerData manufacturerData = new ManufacturerData();
    //    @Accessors(prefix = "m")
    private int rssi = 0;
    private String platform = "", id = "", random = "";
    private int cid;
    private byte[] scanRecord;
    private long timestampNanos;

    public XJBleDevice(BluetoothDevice device) {
        this.device = device;
    }

    public XJBleDevice(BluetoothDevice device, int rssi, byte[] scanRecord, long timestampNanos) {
        this.device = device;
        this.scanRecord = scanRecord;
        this.rssi = rssi;
        this.timestampNanos = timestampNanos;
    }

    public XJBleDevice() {
    }

    public String getKey() {
        if (device != null) {
            return device.getName() + device.getAddress();
        }
        return "";
    }

    public String getName() {
        if (device != null) {
            return device.getName();
        }
        return null;
    }

    public String getMac() {
        if (device != null) {
            return device.getAddress();
        }
        return null;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.device, flags);
        dest.writeParcelable(this.manufacturerData, flags);
        dest.writeInt(this.rssi);
        dest.writeString(this.platform);
    }

    protected XJBleDevice(Parcel in) {
        this.device = in.readParcelable(BluetoothDevice.class.getClassLoader());
        this.manufacturerData = in.readParcelable(ManufacturerData.class.getClassLoader());
        this.rssi = in.readInt();
        this.platform = in.readString();
    }

    public static final Creator<XJBleDevice> CREATOR = new Creator<XJBleDevice>() {
        @Override
        public XJBleDevice createFromParcel(Parcel source) {
            return new XJBleDevice(source);
        }

        @Override
        public XJBleDevice[] newArray(int size) {
            return new XJBleDevice[size];
        }
    };

    @NonNull
    @Override
    public String toString() {
        return "BleDevice{" +
                "device=" + device +
                ", manufacturerData=" + manufacturerData.toString() +
                ", rssi=" + rssi +
                ", platform='" + platform + '\'' +
                '}';
    }


    public int getMaxSize() {
        switch (getManufacturerData().getBleVersion()) {
            case 0:
                return 20;
            case 1:
            case 2:
                return 244;
        }
        return 0;
    }
}

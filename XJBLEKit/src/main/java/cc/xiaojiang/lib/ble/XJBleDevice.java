package cc.xiaojiang.lib.ble;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import cc.xiaojiang.lib.ble.scan.ManufacturerData;


public class XJBleDevice implements Parcelable {
    //小匠平台
    public static final String PLATFORM_XJ = "xj";
    //阿里平台
    public static final String PLATFORM_AL = "tm";
    private BluetoothDevice device;
    private ManufacturerData manufacturerData = new ManufacturerData();
    private int rssi = 0;

    private String id = "";
    private String platform = "";
    private String random = "";
    private BluetoothDevice mDevice; // 扫描到的设备实例

    public XJBleDevice(BluetoothDevice device) {
        mDevice = device;
    }

    public String getRandom() {
        return random;
    }

    public void setRandom(String random) {
        this.random = random;
    }

    public XJBleDevice() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }


    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getKey() {
        if (device != null)
            return device.getName() + device.getAddress();
        return "";
    }

    public String getName() {
        if (device != null)
            return device.getName() != null ? device.getName() : "";
        return "";
    }

    public String getMac() {
        if (device != null)
            return device.getAddress();
        return null;
    }


    public ManufacturerData getManufacturerData() {
        return manufacturerData;
    }

    public void setManufacturerData(ManufacturerData manufacturerData) {
        this.manufacturerData = manufacturerData;
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
        dest.writeString(this.random);
    }

    public void readFromParcel(Parcel source) {
        this.device = source.readParcelable(BluetoothDevice.class.getClassLoader());
        this.manufacturerData = source.readParcelable(ManufacturerData.class.getClassLoader());
        this.rssi = source.readInt();
        this.platform = source.readString();
        this.random = source.readString();
    }

    protected XJBleDevice(Parcel in) {
        this.device = in.readParcelable(BluetoothDevice.class.getClassLoader());
        this.manufacturerData = in.readParcelable(ManufacturerData.class.getClassLoader());
        this.rssi = in.readInt();
        this.platform = in.readString();
        this.random = in.readString();
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
        int size = 0;
        switch (getManufacturerData().getBleVersion()) {
            case 0:
                size = 20;
                break;
            case 1:
            case 2:
                size = 244;
                break;
        }
        return size;
    }
}

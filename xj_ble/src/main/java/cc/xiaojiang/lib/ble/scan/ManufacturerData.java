package cc.xiaojiang.lib.ble.scan;

import static cc.xiaojiang.lib.ble.Constants.PLATFORM_AL;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import cc.xiaojiang.lib.ble.XJBleDevice;
import cc.xiaojiang.lib.ble.utils.ByteUtils;
import lombok.Data;

@Data
public class ManufacturerData implements Parcelable {
    private int version = 0, subType = 0, bleVersion = 0, secretType = 0, cid = 0, authStep = 0;
    private byte FMSK;
    private String pid = "", did = "", platform = "";
    private boolean isSupportOta = false;
    private boolean secretAuthEnable = false;
    private boolean safeBroadcast = false;
    private boolean isBound = false;
    private boolean broadcastFeatureFlag = false;
    private boolean otaEnable = false;
    Map<Object, Object> map = new HashMap<>();

    public ManufacturerData() {
    }

    public String getPid() {
        if (PLATFORM_AL.equals(platform)) {
            if (map.get(pid) != null) {
                return String.valueOf(map.get(pid));
            }
        }
        return pid;
    }

    public boolean isSecretAuthEnable() {
        if (map.get(pid) != null) {
            return true;
        }
        return secretAuthEnable;
    }

    public ManufacturerData(byte[] manufacturerSpecificData, String platform) {
        //解析广播数据
        this.platform = platform;
        ByteBuffer manufacturerSpecificDataBuffer = ByteBuffer.wrap(manufacturerSpecificData);
        manufacturerSpecificDataBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byte versionSubtype = manufacturerSpecificDataBuffer.get();//1字节
        version = ByteUtils.getLow4(versionSubtype);
        subType = ByteUtils.getHeight4(versionSubtype);
        FMSK = manufacturerSpecificDataBuffer.get();//1字节
        pid = ByteUtils.getUnsignedInt(manufacturerSpecificDataBuffer.getInt()) + "";//4字节
        map = BleScanner.getInstance().productMap;
        bleVersion = FMSK & 0b00000011;//2
        isSupportOta = ((FMSK >> 2) & 0b00000001) == 1;//1
        secretAuthEnable = ((FMSK >> 3) & 0b00000001) == 1;//1
        secretType = ((FMSK >> 4) & 0b00000001);//1
        safeBroadcast = ((FMSK >> 5) & 0b00000001) == 1;//1
        isBound = ((FMSK >> 6) & 0b00000001) == 1;//1
        byte[] didOrMac = new byte[6];
        for (int i = 5; i >= 0; i--) {
            didOrMac[i] = manufacturerSpecificDataBuffer.get();
        }
        if (PLATFORM_AL.equals(platform)) {
            this.did = getLittleEndianHexString(didOrMac);
        } else {
            if (secretAuthEnable) {
                this.did = getLittleEndianString(didOrMac);
            } else {
                this.did = getLittleEndianHexString(didOrMac);
            }
        }
    }

    @Override
    public String toString() {
        return "ManufacturerData{" +
                "version=" + version +
                ", subType=" + subType +
                ", FMSK=" + FMSK +
                ", pid=" + pid +
                ", did='" + did + '\'' +
                ", bleVersion=" + bleVersion +
                ", isSupportOta=" + isSupportOta +
                ", isNeedAuth=" + secretAuthEnable +
                ", encryptionType=" + secretType +
                ", isSafeBroadcast=" + safeBroadcast +
                ", isBound=" + isBound +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.version);
        dest.writeInt(this.subType);
        dest.writeByte(this.FMSK);
        dest.writeString(this.pid);
        dest.writeString(this.did);
        dest.writeInt(this.bleVersion);
        dest.writeByte(this.isSupportOta ? (byte) 1 : (byte) 0);
        dest.writeByte(this.secretAuthEnable ? (byte) 1 : (byte) 0);
        dest.writeInt(this.secretType);
        dest.writeByte(this.safeBroadcast ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isBound ? (byte) 1 : (byte) 0);
    }

    public void readFromParcel(Parcel source) {
        this.version = source.readInt();
        this.subType = source.readInt();
        this.FMSK = source.readByte();
        this.pid = source.readString();
        this.did = source.readString();
        this.bleVersion = source.readInt();
        this.isSupportOta = source.readByte() != 0;
        this.secretAuthEnable = source.readByte() != 0;
        this.secretType = source.readInt();
        this.safeBroadcast = source.readByte() != 0;
        this.isBound = source.readByte() != 0;
    }

    protected ManufacturerData(Parcel in) {
        this.version = in.readInt();
        this.subType = in.readInt();
        this.FMSK = in.readByte();
        this.pid = in.readString();
        this.did = in.readString();
        this.bleVersion = in.readInt();
        this.isSupportOta = in.readByte() != 0;
        this.secretAuthEnable = in.readByte() != 0;
        this.secretType = in.readInt();
        this.safeBroadcast = in.readByte() != 0;
        this.isBound = in.readByte() != 0;
    }

    public static final Creator<ManufacturerData> CREATOR = new Creator<ManufacturerData>() {
        @Override
        public ManufacturerData createFromParcel(Parcel source) {
            return new ManufacturerData(source);
        }

        @Override
        public ManufacturerData[] newArray(int size) {
            return new ManufacturerData[size];
        }
    };

    public String getLittleEndianHexString(byte[] didOrMac) {
        StringBuilder did = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            did.append(String.format("%02x",
                    ByteUtils.getUnsignedByte(didOrMac[i])));
        }
        return did.toString();
    }

    public String getLittleEndianString(byte[] didOrMac) {
        StringBuilder did = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            did.append((char) Integer.parseInt(String.valueOf(didOrMac[i])));
        }
        return did.toString();
    }
}

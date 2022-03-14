package cc.xiaojiang.lib.ble.scan;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import cc.xiaojiang.lib.ble.XJBleDevice;
import cc.xiaojiang.lib.ble.utils.ByteUtils;
import lombok.Data;

@Data
public class ManufacturerData implements Parcelable {
    private int version = 0, subType = 0, bleVersion = 0, secretType = 0, cid = 0, authStep = 0;
    private byte FMSK;
    private String pid = "", did = "", scanId = "";
    private boolean isSupportOta = false, secretAuthEnable = false, safeBroadcast = false, isBound = false,broadcastFeatureFlag=false,otaEnable=false;

    public ManufacturerData() {
    }

    public ManufacturerData(byte[] manufacturerSpecificData, String platform) {
        //解析广播数据
        ByteBuffer manufacturerSpecificDataBuffer = ByteBuffer.wrap(manufacturerSpecificData);
        manufacturerSpecificDataBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byte versionSubtype = manufacturerSpecificDataBuffer.get();//1字节
        version = ByteUtils.getLow4(versionSubtype);
        subType = ByteUtils.getHeight4(versionSubtype);
        FMSK = manufacturerSpecificDataBuffer.get();//1字节
        pid = ByteUtils.getUnsignedInt(manufacturerSpecificDataBuffer.getInt()) + "";//4字节
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
        StringBuilder did = new StringBuilder();
        StringBuilder scanId = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if (XJBleDevice.PLATFORM_AL.equals(platform)) {
                did.append(String.format("%02x",
                        ByteUtils.getUnsignedByte(didOrMac[i])));
            } else {
                int i1 = Integer.parseInt(String.valueOf(didOrMac[i]));
                if (!secretAuthEnable) {
                    if (secretType == 0) {//一型一密
                        scanId = scanId.append(String.format("%02x", ByteUtils.getUnsignedByte(didOrMac[i])));
                    } else {
                        scanId = scanId.append((char) i1);
                    }
                }
                did.append((char) i1);
            }
        }

        this.did = did.toString();
        this.scanId = scanId.toString();
    }

    @Override
    public String toString() {
        return "ManufacturerData{" +
                "version=" + version +
                ", subType=" + subType +
                ", FMSK=" + FMSK +
                ", pid=" + pid +
                ", scanId=" + scanId +
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
}

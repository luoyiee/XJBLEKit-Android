package cc.xiaojiang.lib.ble.scan;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import cc.xiaojiang.lib.ble.XJBleDevice;
import cc.xiaojiang.lib.ble.utils.ByteUtils;


public class ManufacturerData implements Parcelable {
    private int version = 0;
    private int subType = 0;
    private byte FMSK;
    private String pid = "";
    private String did = "";
    private String scanId = "";
    private int bleVersion = 0;
    private boolean isSupportOta = false;
    private boolean isNeedAuth = false;
    private int encryptionType = 0;
    private boolean isSafeBroadcast = false;
    private boolean isBound = false;

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
        isNeedAuth = ((FMSK >> 3) & 0b00000001) == 1;//1
        encryptionType = ((FMSK >> 4) & 0b00000001);//1
        isSafeBroadcast = ((FMSK >> 5) & 0b00000001) == 1;//1
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
                if (!isNeedAuth) {
                    if (encryptionType == 0) {//一型一密
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getSubType() {
        return subType;
    }

    public void setSubType(int subType) {
        this.subType = subType;
    }

    public byte getFMSK() {
        return FMSK;
    }

    public void setFMSK(byte FMSK) {
        this.FMSK = FMSK;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getScanId() {
        return scanId;
    }

    public void setScanId(String scanId) {
        this.scanId = scanId;
    }

    public int getBleVersion() {
        return bleVersion;
    }

    public void setBleVersion(int bleVersion) {
        this.bleVersion = bleVersion;
    }

    public boolean isSupportOta() {
        return isSupportOta;
    }

    public void setSupportOta(boolean supportOta) {
        isSupportOta = supportOta;
    }

    public boolean isNeedAuth() {
        return isNeedAuth;
    }

    public void setNeedAuth(boolean needAuth) {
        isNeedAuth = needAuth;
    }

    public int getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(int encryptionType) {
        this.encryptionType = encryptionType;
    }

    public boolean isSafeBroadcast() {
        return isSafeBroadcast;
    }

    public void setSafeBroadcast(boolean safeBroadcast) {
        isSafeBroadcast = safeBroadcast;
    }

    public boolean isBound() {
        return isBound;
    }

    public void setBound(boolean bound) {
        isBound = bound;
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
                ", isNeedAuth=" + isNeedAuth +
                ", encryptionType=" + encryptionType +
                ", isSafeBroadcast=" + isSafeBroadcast +
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
        dest.writeByte(this.isNeedAuth ? (byte) 1 : (byte) 0);
        dest.writeInt(this.encryptionType);
        dest.writeByte(this.isSafeBroadcast ? (byte) 1 : (byte) 0);
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
        this.isNeedAuth = source.readByte() != 0;
        this.encryptionType = source.readInt();
        this.isSafeBroadcast = source.readByte() != 0;
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
        this.isNeedAuth = in.readByte() != 0;
        this.encryptionType = in.readInt();
        this.isSafeBroadcast = in.readByte() != 0;
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

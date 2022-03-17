package cc.xiaojiang.lib.ble.data;

import cc.xiaojiang.lib.ble.utils.ByteUtils;

/**
 * Created by facexxyz on 5/13/21.
 */
public class BlePacket {
    private int version;
    private int encrypt;
    private int msgId;
    private int cmdType;
    private byte[] payload;


    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(int encrypt) {
        this.encrypt = encrypt;
    }

    public int getCmdType() {
        return cmdType;
    }

    public void setCmdType(int cmdType) {
        this.cmdType = cmdType;
    }

    @Override
    public String toString() {
        return "BlePacket{" +
                "version=" + version +
                ", encrypt=" + encrypt +
                ", msgId=" + msgId +
                ", cmdType=" + Integer.toHexString(cmdType) +
                ", payload=" + ByteUtils.bytesToHexString(payload) +
                '}';
    }
}

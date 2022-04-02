package cc.xiaojiang.lib.ble.data;

import cc.xiaojiang.lib.ble.utils.ByteUtils;
import lombok.Data;

/**
 * Created by facexxyz on 5/13/21.
 */
@Data
public class BlePacket {
    private int version;
    private int encrypt;
    private int msgId;
    private int cmdType;
    private byte[] payload;

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

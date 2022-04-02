package cc.xiaojiang.lib.ble;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import cc.xiaojiang.lib.ble.data.AttrModel;
import cc.xiaojiang.lib.ble.utils.BleLog;
import cc.xiaojiang.lib.ble.utils.ByteUtils;
import cc.xiaojiang.lib.ble.utils.Utils;


public class PayLoadUtils {
    //type id
    public static final byte PROTOCOL_PROP_TYPE_BOOL = 0;
    public static final byte PROTOCOL_PROP_TYPE_INT8 = 1;
    public static final byte PROTOCOL_PROP_TYPE_UINT8 = 2;
    public static final byte PROTOCOL_PROP_TYPE_INT16 = 3;
    public static final byte PROTOCOL_PROP_TYPE_UINT16 = 4;
    public static final byte PROTOCOL_PROP_TYPE_INT32 = 5;
    public static final byte PROTOCOL_PROP_TYPE_UINT32 = 6;
    public static final byte PROTOCOL_PROP_TYPE_STRING = 11;
    public static final byte PROTOCOL_PROP_TYPE_ARRAY = 14;
    //cmd

    public static final byte CMD_DOWN_VERIFY = (byte) 0x01;
    public static final byte CMD_DOWN_INIT = (byte) 0x02;
    public static final byte CMD_DOWN_REPORT = (byte) 0x03;
    public static final byte CMD_DOWN_SET = (byte) 0x81;
    public static final byte CMD_DOWN_GET = (byte) 0x82;
    public static final byte CMD_DOWN_SNAPSHOT = (byte) 0x84;
    public static final byte CMD_DOWN_WIFICONFIG = (byte) 0x85;
    public static final byte CMD_DOWN_RANDOM = (byte) 0x86;
    public static final byte CMD_DOWN_BIND = (byte) 0x87;

    //ota
    public static final byte CMD_DOWN_VERSION = (byte) 0xA0;
    public static final byte CMD_DOWN_UPDATE_REQUEST = (byte) 0xA1;
    public static final byte CMD_DOWN_SEND_DATA = (byte) 0xA2;
    public static final byte CMD_DOWN_CHECK = (byte) 0xA3;


    //attrIds
    public static final byte ATTR_ID_SSID = (byte) 0xF5;
    public static final byte ATTR_ID_PASSWORD = (byte) 0xF4;
    public static final byte ATTR_ID_BSSID = (byte) 0xF3;
    public static final byte ATTR_ID_TOKEN = (byte) 0xF2;
    public static final byte ATTR_ID_AREA = (byte) 0xEF;
    public static final byte ATTR_URL_AREA = (byte) 0xEE;
    public static final byte ATTR_TYPE_AREA = (byte) 0xED;

    public static byte[] getBoolPayload(byte attrId, boolean value) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(3);
        byteBuffer.put(PROTOCOL_PROP_TYPE_BOOL);
        byteBuffer.put(attrId);
        byteBuffer.put((byte) (value ? 1 : 0));
        return byteBuffer.array();
    }

    public static byte[] getInt8Payload(byte attrId, byte value) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(3);
        byteBuffer.put(PROTOCOL_PROP_TYPE_INT8);
        byteBuffer.put(attrId);
        byteBuffer.put(value);
        return byteBuffer.array();
    }

    public static byte[] getUInt8Payload(byte attrId, byte value) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(3);
        byteBuffer.put(PROTOCOL_PROP_TYPE_UINT8);
        byteBuffer.put(attrId);
        byteBuffer.put(value);
        return byteBuffer.array();
    }

    public static byte[] getInt16Payload(byte attrId, short value) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.put(PROTOCOL_PROP_TYPE_INT16);
        byteBuffer.put(attrId);
        byteBuffer.put(ByteUtils.shortToBytes(value));
        return byteBuffer.array();
    }

    public static byte[] getUInt16Payload(byte attrId, short value) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.put(PROTOCOL_PROP_TYPE_UINT16);
        byteBuffer.put(attrId);
        byteBuffer.put(ByteUtils.shortToBytes(value));
        return byteBuffer.array();
    }

    public static byte[] getInt32Payload(byte attrId, int value) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(6);
        byteBuffer.put(PROTOCOL_PROP_TYPE_INT32);
        byteBuffer.put(attrId);
        byteBuffer.put(ByteUtils.intToBytes(value));
        return byteBuffer.array();
    }

    public static byte[] getUInt32Payload(byte attrId, int value) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(6);
        byteBuffer.put(PROTOCOL_PROP_TYPE_UINT32);
        byteBuffer.put(attrId);
        byteBuffer.put(ByteUtils.intToBytes(value));
        return byteBuffer.array();
    }

    public static byte[] getStringPayload(byte attrId, String value) {
        byte[] bytes = value.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + bytes.length);
        byteBuffer.put(PROTOCOL_PROP_TYPE_STRING);
        byteBuffer.put(attrId);
        byteBuffer.putShort((short) value.length());
        byteBuffer.put(bytes);
        return byteBuffer.array();
    }

    public static byte[] getArrayPayload(byte attrId, byte[] value) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + value.length);
        byteBuffer.put(PROTOCOL_PROP_TYPE_ARRAY);
        byteBuffer.put(attrId);
        byteBuffer.putShort((short) value.length);
        byteBuffer.put(value);
        return byteBuffer.array();
    }

    public static byte[] setData(byte typeId, byte attrId, byte value) {
        switch (typeId) {
            case PROTOCOL_PROP_TYPE_BOOL:
            case PROTOCOL_PROP_TYPE_UINT8:
                return getUInt8Payload(attrId, value);
            case PROTOCOL_PROP_TYPE_INT8:
                return getInt8Payload(attrId, value);
            case PROTOCOL_PROP_TYPE_INT16:
                return getInt16Payload(attrId, value);
            case PROTOCOL_PROP_TYPE_UINT16:
                return getUInt16Payload(attrId, value);
            case PROTOCOL_PROP_TYPE_INT32:
                return getInt32Payload(attrId, value);
            case PROTOCOL_PROP_TYPE_UINT32:
                return getUInt32Payload(attrId, value);
            case PROTOCOL_PROP_TYPE_STRING:
                return getStringPayload(attrId, ByteUtils.byteToHexString(value));
            case PROTOCOL_PROP_TYPE_ARRAY:
                return getArrayPayload(attrId, ByteUtils.byteToBytes(value));
            default:
                return null;
        }
    }


    public static List<AttrModel> decodeAttrModels(byte[] payload) {
        List<AttrModel> attrModels = new ArrayList<>();
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        while (buffer.hasRemaining()) {
            AttrModel attrModel = new AttrModel();
            int typeId = buffer.get();
            int attrId = buffer.get();
            attrModel.setTypeId(typeId);
            attrModel.setAttrId(attrId);
            switch (typeId) {
                case PROTOCOL_PROP_TYPE_BOOL:
                case PROTOCOL_PROP_TYPE_UINT8:
                    int boolValue = ByteUtils.getUnsignedByte(buffer.get());
                    attrModel.setValue(boolValue);
                    attrModels.add(attrModel);
                    break;
                case PROTOCOL_PROP_TYPE_INT8:
                    int int8Value = buffer.get();
                    attrModel.setValue(int8Value);
                    attrModels.add(attrModel);
                    break;
                case PROTOCOL_PROP_TYPE_INT16:
                    int int16Value = buffer.getShort();
                    attrModel.setValue(int16Value);
                    attrModels.add(attrModel);
                    break;
                case PROTOCOL_PROP_TYPE_UINT16:
                    int uint16Value = ByteUtils.getUnsignedShort(buffer.getShort());
                    attrModel.setValue(uint16Value);
                    attrModels.add(attrModel);
                    break;
                case PROTOCOL_PROP_TYPE_INT32:
                    int int32Value = buffer.getInt();
                    attrModel.setValue(int32Value);
                    attrModels.add(attrModel);
                    break;
                case PROTOCOL_PROP_TYPE_UINT32:
                    long uInt32Value = ByteUtils.getUnsignedInt(buffer.getInt());
                    attrModel.setValue(uInt32Value);
                    attrModels.add(attrModel);
                    break;
                case PROTOCOL_PROP_TYPE_STRING:
                    int StrLength = ByteUtils.getUnsignedShort(buffer.getShort());
                    byte[] strings = new byte[StrLength];
                    buffer.get(strings, 0, StrLength);
                    String StringValue = new String(strings);
                    attrModel.setValue(StringValue);
                    attrModels.add(attrModel);
                    break;
                case PROTOCOL_PROP_TYPE_ARRAY:
                    int arrayLength = ByteUtils.getUnsignedShort(buffer.getShort());
                    byte[] Arrays = new byte[arrayLength];
                    buffer.get(Arrays, 0, arrayLength);
//                    StringBuilder arrayBuffer = new StringBuilder();
//                    for (int i = 0; i < Arrays.length - 1; i++) {
//                        arrayBuffer.append(Arrays[i]);
////                        arrayBuffer.append(",");
//                    }
                    String arrayBuffer = ByteUtils.bytesToHexString(Arrays).replace(" ", "");
//                    arrayBuffer.append(Arrays[arrayLength - 1]);
                    attrModel.setValue(arrayBuffer);
                    attrModels.add(attrModel);
                    break;
                default:
                    BleLog.e("unsupported data type: " + ByteUtils.byteToHexString((byte) typeId));

            }
        }
        return attrModels;
    }

    public static String getVersion(byte[] bytes) {
        StringBuilder version = new StringBuilder();

        if (bytes == null) {
            return "0.0.0";
        }

        for (byte aByte : bytes) {
            version.append(ByteUtils.getUnsignedByte(aByte));
        }
        return Utils.joinStr(version.substring(0, 3));
    }


    public static boolean isWithResponse(byte cmd) {
        String cmdString = ByteUtils.byteToHexString(cmd);
        return cmdString.contains("A0") || cmdString.contains("A1") || cmdString.contains("A2") || cmdString.contains("A3");
    }

}

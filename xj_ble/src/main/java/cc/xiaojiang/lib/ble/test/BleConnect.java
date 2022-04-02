//package cc.xiaojiang.lib.ble.test;
//
//import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;
//import static cc.xiaojiang.lib.ble.Constants.UUID_XJ_CHARACTERISTIC_INDICATE;
//import static cc.xiaojiang.lib.ble.Constants.UUID_XJ_CHARACTERISTIC_NOTIFY;
//import static cc.xiaojiang.lib.ble.Constants.UUID_XJ_CHARACTERISTIC_WRITE;
//import static cc.xiaojiang.lib.ble.Constants.UUID_XJ_CHARACTERISTIC_WRITE_WITH_NO_RESPONSE;
//import static cc.xiaojiang.lib.ble.Constants.UUID_XJ_SERVICE;
//
//import android.bluetooth.BluetoothGatt;
//import android.bluetooth.BluetoothGattCallback;
//import android.bluetooth.BluetoothGattCharacteristic;
//import android.bluetooth.BluetoothGattDescriptor;
//import android.bluetooth.BluetoothGattService;
//import android.bluetooth.BluetoothProfile;
//import android.os.Build;
//import android.os.Handler;
//import android.os.Looper;
//import android.os.Message;
//import android.text.TextUtils;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//
//import java.lang.reflect.Method;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.Map;
//import java.util.UUID;
//
//import cc.xiaojiang.lib.ble.BleDevice;
//import cc.xiaojiang.lib.ble.Constants;
//import cc.xiaojiang.lib.ble.IBleAuth;
//import cc.xiaojiang.lib.ble.OtaInfo;
//import cc.xiaojiang.lib.ble.PayLoadUtils;
//import cc.xiaojiang.lib.ble.XJBleManager;
//import cc.xiaojiang.lib.ble.bleInterface.OtaErrType;
//import cc.xiaojiang.lib.ble.callback.BleAuthCallback;
//import cc.xiaojiang.lib.ble.callback.BleConnectCallback;
//import cc.xiaojiang.lib.ble.callback.BleDataChangeCallback;
//import cc.xiaojiang.lib.ble.callback.BleDataSetCallback;
//import cc.xiaojiang.lib.ble.callback.BleWifiConfigCallback;
//import cc.xiaojiang.lib.ble.callback.BleWriteCallback;
//import cc.xiaojiang.lib.ble.callback.ota.OtaProgressCallBack;
//import cc.xiaojiang.lib.ble.callback.ota.OtaResultCallback;
//import cc.xiaojiang.lib.ble.callback.ota.OtaVersionCallback;
//import cc.xiaojiang.lib.ble.callback.ota.SendResultCallBack;
//import cc.xiaojiang.lib.ble.data.BleMsg;
//import cc.xiaojiang.lib.ble.data.BlePacket;
//import cc.xiaojiang.lib.ble.data.SplitWriter;
//import cc.xiaojiang.lib.ble.exception.AuthException;
//import cc.xiaojiang.lib.ble.exception.BleException;
//import cc.xiaojiang.lib.ble.exception.ConnectException;
//import cc.xiaojiang.lib.ble.exception.OTAException;
//import cc.xiaojiang.lib.ble.exception.OtherException;
//import cc.xiaojiang.lib.ble.exception.TimeoutException;
//import cc.xiaojiang.lib.ble.utils.AES;
//import cc.xiaojiang.lib.ble.utils.BleLog;
//import cc.xiaojiang.lib.ble.utils.ByteUtils;
//
//// TODO: 4/3/21 逻辑优化
//public class BleConnect {
//    private final static String TAG = BleConnect.class.getSimpleName();
//    //    private static final int SPLIT_WRITE_NUM = 20;
//    private static int SPLIT_WRITE_NUM;
//    private static final int CONNECT_RETRY_TIME = 2;
//    private BluetoothGatt gatt;
//    private LastState lastState;
//    private BleDevice bleDevice;
//    private OtaInfo.ContentBean.ModuleBean mInfo = new OtaInfo.ContentBean.ModuleBean();
//
//    private BleConnectCallback bleConnectCallback;
//    private BleWriteCallback bleWriteCallback;
//    private SendResultCallBack sendResultCallBack;
//    private String bleKey;
//    private IBleAuth mIBleAuth;
//
//    private byte mMsgId = 1;
//    private int mSendTotal = 0;
//    private int mSendCurrent = 0;
//    private boolean mAuthed = false;
//    private boolean isActiveDisconnect = false;
//    private int reconnectCount = 0;
//    private static final BleConnect ourInstance = new BleConnect();
//    private BleDataSetCallback mBleDataSetCallback;
//    private BleWifiConfigCallback mBleWifiConfigCallback;
//    private BleDataChangeCallback mBleDataChangeCallback;
//    private OtaResultCallback mOtaResultCallback;
//    private OtaProgressCallBack mOtaProgressCallBack;
//    private OtaVersionCallback mOtaVersionCallback;
//    private BleAuthCallback mBleAuthCallback;
//    //接收数据分包计数
//    private int receivedTotal;
//    private int receivedCurrent;
//    private byte[] receivedTotalPayload = new byte[]{};
//    private byte[] otaBytes;
//    private boolean isOTALooping = false;
//    private boolean otaClose = false;
//    int packetSize;
//    public static long time = 0;
//    public static Handler handler = new Handler(Looper.getMainLooper());
//    public boolean isTimeOut = false;
//    public byte bleCmd;
//
//    public BleConnect(BleDevice bleDevice) {
//        this.bleDevice = bleDevice;
//    }
//
//    private BleConnect() {
//    }
//
//    public Runnable sendDelayRun = () -> {
//        if (sendResultCallBack != null) {
//            sendResultCallBack.failed(bleCmd);
//        }
//    };
//
//    private Handler mainHandler = new Handler(Looper.getMainLooper()) {
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                /**
//                 * 连接
//                 */
//                case BleMsg.MSG_CONNECT_FAIL:
//                    disconnect();
//                    refreshDeviceCache();
//                    closeBluetoothGatt();
//                    if ((reconnectCount < CONNECT_RETRY_TIME) && !isActiveDisconnect) {
//                        connect(bleDevice, false, bleConnectCallback);
//                        reconnectCount++;
//                        BleLog.d("reconnect " + reconnectCount + "/" + CONNECT_RETRY_TIME);
//                    } else {
//                        lastState = LastState.CONNECT_FAILURE;
//                        XJBleManager.getInstance().getMultipleBluetoothController().removeConnectingBle(BleConnect.this);
//                        BleConnectStateParameter para = (BleConnectStateParameter) msg.obj;
//                        int status = para.getStatus();
//                        if (bleConnectCallback != null)
//                            bleConnectCallback.onConnectFail(bleDevice, new ConnectException(gatt, status));
//                    }
//                    break;
//                case BleMsg.MSG_DISCONNECTED:
//                    XJBleManager.getInstance().getMultipleBluetoothController().removeBleBluetooth(BleConnect.this);
//                    Log.d("H5", "MSG_DISCONNECTED" + bleConnectCallback);
//                    lastState = LastState.CONNECT_DISCONNECT;
//                    disconnect();
//                    refreshDeviceCache();
//                    closeBluetoothGatt();
//                    mainHandler.removeCallbacksAndMessages(null);
//                    mAuthed = false;
//                    BleConnectStateParameter para = (BleConnectStateParameter) msg.obj;
//                    boolean isActive = para.isActive();
//                    int status = para.getStatus();
//                    if (bleConnectCallback != null) {
//                        bleConnectCallback.onDisConnected(isActiveDisconnect, bleDevice, gatt, status);
//                        if (gatt != null) {
//                            gatt.close();
//                            Log.d("H5", "gatt.close()");
//                            gatt = null;
//                        }
//                    }
//                    break;
//                case BleMsg.MSG_CONNECT_OVER_TIME:
//                    disconnect();
//                    refreshDeviceCache();
//                    closeBluetoothGatt();
//                    lastState = LastState.CONNECT_FAILURE;
//                    XJBleManager.getInstance().getMultipleBluetoothController().removeConnectingBle(BleConnect.this);
//                    if (bleConnectCallback != null)
//                        bleConnectCallback.onConnectFail(bleDevice, new TimeoutException());
//                    break;
//                /**
//                 * 发现服务
//                 */
//                case BleMsg.MSG_DISCOVER_SERVICES:
//                    if (gatt != null) {
//                        boolean discoverServiceResult = gatt.discoverServices();
//                        if (!discoverServiceResult) {
//                            Message message = mainHandler.obtainMessage();
//                            message.what = BleMsg.MSG_DISCOVER_FAIL;
//                            mainHandler.sendMessage(message);
//                        }
//                    } else {
//                        Message message = mainHandler.obtainMessage();
//                        message.what = BleMsg.MSG_DISCOVER_FAIL;
//                        mainHandler.sendMessage(message);
//                    }
//                    break;
//                case BleMsg.MSG_DISCOVER_FAIL:
//                    disconnect();
//                    refreshDeviceCache();
//                    closeBluetoothGatt();
//                    lastState = LastState.CONNECT_FAILURE;
//                    XJBleManager.getInstance().getMultipleBluetoothController().removeConnectingBle(BleConnect.this);
//                    if (bleConnectCallback != null) {
//                        bleConnectCallback.onConnectFail(bleDevice, new OtherException("GATT " +
//                                "discover " +
//                                "services exception occurred!"));
//                    }
//                    break;
//                case BleMsg.MSG_DISCOVER_SUCCESS:
//                    lastState = LastState.CONNECT_CONNECTED;
//                    isActiveDisconnect = false;
//                    mAuthed = false;
//                    XJBleManager.getInstance().getMultipleBluetoothController().removeConnectingBle(BleConnect.this);
//                    XJBleManager.getInstance().getMultipleBluetoothController().addBleBluetooth(BleConnect.this);
//                    BleConnectStateParameter para1 = (BleConnectStateParameter) msg.obj;
//                    if (bleConnectCallback != null) {
//                        bleConnectCallback.onConnectSuccess(bleDevice, gatt, para1.getStatus());
//                    }
//                    //start indicate, delay 50ms
//                    Message message = mainHandler.obtainMessage();
//                    message.what = BleMsg.MSG_CHA_INDICATE_START;
//                    mainHandler.sendMessageDelayed(message, 50);
//                    break;
//                /**
//                 * indicate
//                 */
//                case BleMsg.MSG_CHA_INDICATE_START:
//                    enableIndicate();
//                    break;
//                case BleMsg.MSG_CHA_INDICATE_RESULT:
//                    if ((boolean) msg.obj) {
//                        BleLog.d("errorTest" + "indicate succeed,start notify");
//                        Message message2 = mainHandler.obtainMessage();
//                        message2.what = BleMsg.MSG_CHA_NOTIFY_START;
//                        mainHandler.sendMessageDelayed(message2, 50);
//                    }
//                    break;
//                /**
//                 * notify
//                 */
//                case BleMsg.MSG_CHA_NOTIFY_START:
//                    enableNotify();
//                    break;
//                case BleMsg.MSG_CHA_NOTIFY_RESULT:
//                    if ((boolean) msg.obj) {
//                        BleLog.d("notify succeed");
//                        if (BleDevice.PLATFORM_XJ.equals(bleDevice.getPlatform()) && bleDevice.getManufacturerData().isNeedAuth()) {//小匠加密设备
//                            new Thread(() -> {
//                                String random = mIBleAuth.getRandom(bleDevice);
//                                if (TextUtils.isEmpty(random)) {
//                                    onAuthResult(false);
//                                    return;
//                                }
//                                sendRandom(mIBleAuth.getRandom(bleDevice));
//                            }).start();
//                            BleLog.d("start auth");
//                        } else if (!mAuthed) {
//                            BleLog.d("errorTest" + "MSG_AUTH_SUCCEED");
//                            Message message1 = mainHandler.obtainMessage();
//                            message1.what = BleMsg.MSG_AUTH_SUCCEED;
//                            mainHandler.sendMessage(message1);
//                        }
//                    }
//                    break;
//                /**
//                 * auth
//                 */
//                case BleMsg.MSG_AUTH_RANDOM:
//                    new Thread(() -> {
//                        String random = mIBleAuth.getRandom(bleDevice);
//                        if (TextUtils.isEmpty(random)) {
//                            onAuthResult(false);
//                            return;
//                        }
//                        sendRandom(mIBleAuth.getRandom(bleDevice));
//                    }).start();
//                    break;
//                case BleMsg.MSG_CHA_INDICATE_DATA_CHANGE:
//                    handler.removeCallbacks(sendDelayRun);
//                    BlePacket blePacket = (BlePacket) msg.obj;
////                    BleLog.i(blePacket.toString());
//                    if (blePacket == null) {
//                        return;
//                    }
//                    byte[] payload = blePacket.getPayload();
//                    switch (blePacket.getCmdType()) {
//                        case 0x01:
//                            final String cipher = ByteUtils.bytesToHexString(payload).replaceAll(
//                                    " ", "").toLowerCase();
//                            BleLog.d("received random cipher: " + cipher);
//                            write(new byte[]{0x00}, (byte) 0x01, new BleWriteCallback() {
//                                @Override
//                                public void onWriteSuccess(int current, int total,
//                                                           byte[] justWrite) {
//                                    BleLog.d("onWriteSuccess " + "11111111111");
//
//                                    if (current != total) {
//                                        return;
//                                    }
//                                    new Thread(() -> {
//                                        bleKey = mIBleAuth.getBleKey(bleDevice,
//                                                cipher.toUpperCase());
//                                        if (TextUtils.isEmpty(bleKey)) {
//                                            onAuthResult(false);
//                                            return;
//                                        }
//                                        BleLog.d("get ble key: " + bleKey);
//
//                                        Message message = mainHandler.obtainMessage();
//                                        message.what = BleMsg.MSG_AUTH_SUCCEED;
//                                        mainHandler.sendMessage(message);
//
//                                    }).start();
//                                }
//
//                                @Override
//                                public void onWriteFailure(BleException exception) {
//                                    Message message = mainHandler.obtainMessage();
//                                    message.what = BleMsg.MSG_AUTH_FAILED;
//                                    mainHandler.sendMessage(message);
//                                }
//                            });
//
//                            break;
//                        case 0x03:
//                            try {
//                                mBleDataChangeCallback.onDataChanged(0, PayLoadUtils.CMD_DOWN_REPORT, PayLoadUtils.decodeAttrModels(payload));
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            break;
//
//                        case 0x81:
//                            if (mBleDataSetCallback == null) {
//                                return;
//                            }
//                            if (payload.length != 1) {
//                                return;
//                            }
//                            byte errorCode = payload[0];
//                            if (errorCode == 0) {
//                                mBleDataSetCallback.onDataSendSucceed();
//                            } else {
//                                mBleDataSetCallback.onDataSendFailed(errorCode);
//                            }
//                            mBleDataSetCallback = null;
//                            break;
//
//                        case 0x82:
//                            byte errorCode82 = payload[0];
//                            try {
//                                byte[] payloadReal = ByteUtils.subByte(payload, 1, payload.length - 1);
//                                mBleDataChangeCallback.onDataChanged(errorCode82, PayLoadUtils.CMD_DOWN_GET, PayLoadUtils.decodeAttrModels(payloadReal));
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//
//                            break;
//
//                        case 0x84:
//                            BleLog.d("errorTest" + ByteUtils.toHexStringSplit(payload));
//                            byte errorCode84 = payload[0];
//                            if (errorCode84 != 0) {
////                                for (Map.Entry<String, BleDataChangeCallback> entry : mBleDataChangeCallbacks.entrySet()) {
////                                    entry.getValue().onDataChanged(errorCode84, PayLoadUtils.CMD_DOWN_SNAPSHOT, null);
////                                }
//                                mBleDataChangeCallback.onDataChanged(errorCode84, PayLoadUtils.CMD_DOWN_SNAPSHOT, null);
//
//                                return;
//                            }
//
//                            try {
//                                byte[] payloadReal = ByteUtils.subByte(payload, 1, payload.length - 1);
//                                mBleDataChangeCallback.onDataChanged(0, PayLoadUtils.CMD_DOWN_SNAPSHOT, PayLoadUtils.decodeAttrModels(payloadReal));
//
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            break;
//
//                        case 0x85:
//                            if (mBleWifiConfigCallback == null) {
//                                return;
//                            }
//                            if (payload.length != 1) {
//                                return;
//                            }
//                            byte errorCode85 = payload[0];
//                            if (errorCode85 == 0) {
//                                mBleWifiConfigCallback.onBleWifiConfigSucceed();
//                            } else {
//                                mBleWifiConfigCallback.onBleWifiConfigFailed(errorCode85);
//                            }
//                            break;
//
//
//                        case 0xA0://查询设备版本信息
//                            BleLog.d("received ota A0: " + payload);
//                            byte errorCodeA0 = payload[0];
//                            if (errorCodeA0 != 0) {
//                                Message message1 = mainHandler.obtainMessage();
//                                message1.what = BleMsg.MSG_OTA_VERSION_FAILED;
//                                mainHandler.sendMessage(message1);
//                                return;
//                            } else {
//                                byte[] payloadReal = ByteUtils.subByte(payload, 1, payload.length - 1);
//                                ByteBuffer buffer = ByteBuffer.wrap(payloadReal);//大端模式
//                                buffer.order(ByteOrder.BIG_ENDIAN);
//
//                                StringBuilder version = new StringBuilder();
//
//                                for (int i = 0; i < payloadReal.length; i++) {
//                                    version.append(ByteUtils.getUnsignedByte(payloadReal[i]));
//                                }
//                                String moduleVersion = "";
//                                String mcuVersion = "";
//                                try {
//                                    moduleVersion = ByteUtils.bytesToString(ByteUtils.subByte(payloadReal, 0, 3));
//                                    mcuVersion = ByteUtils.bytesToString(ByteUtils.subByte(payloadReal, 3, 3));
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                                mOtaVersionCallback.onVersionSucceed(mcuVersion, moduleVersion);
//                                BleLog.d("mOtaInfo" + mInfo);
//                            }
//
//                            break;
//
//                        case 0xA1://解析ota请求返回数据
//                            BleLog.d("received ota A1: " + payload);
//                            byte errorCodeA1 = payload[0];
//                            if (errorCodeA1 != 0) {
//                                mOtaResultCallback.onOtaResult(errorCodeA1, mInfo);
//                                return;
//                            } else {
//                                byte[] payloadReal = ByteUtils.subByte(payload, 1, payload.length - 1);
//                                ByteBuffer requestBuffer = ByteBuffer.wrap(payloadReal);
//                                requestBuffer.order(ByteOrder.BIG_ENDIAN);
//                                byte result = requestBuffer.get();//1字节
//                                boolean isAllowUpdate = (result & 0b00000001) == 1;//0-禁止升级，1-允许升级
//                                boolean isSupportBreakPoint = ((result >> 1) & 0b00000001) == 1;//0-不支持断点续传，1-支持断点续传
//                                byte totalPackageNumbers = requestBuffer.get();//单次循环可传输的包个数：取值范围0x00~0x0F，表示1～16包
//                                byte deviceRebootTime = requestBuffer.get();//设备重启最大时间，单位：秒
//                                int lastReceivedFileSize = requestBuffer.getInt();//断点续传前已接收文件大小
//
//                                mInfo.setIsAllowUpdate(isAllowUpdate);
//                                mInfo.setIsSupportBreakPoint(isSupportBreakPoint);
//                                mInfo.setTotalPackageNumbers(totalPackageNumbers);
//                                mInfo.setDeviceRebootTime(deviceRebootTime);
//                                mInfo.setLastReceivedFileSize(lastReceivedFileSize);
//                                mInfo.setDownLoadBytes(otaBytes);
//                                if (isAllowUpdate) {//允许升级
//                                    otaSendData(mInfo, 0);
//                                } else {
//                                    mOtaResultCallback.onOtaResult(OtaErrType.ERR_SYSTEM, mInfo);
//                                }
//                            }
//
//                            break;
//
//                        case 0xA2://升级数据包
//                            long now = System.currentTimeMillis() / 1000;
//                            if (now - time > 3) {
//                                time = now;
//                            }
//
//                            BleLog.i("onRequestSucceed" + "44444444444444");
//                            BleLog.d("received ota A2 " + payload);
//                            byte errorCode3 = payload[0];
//                            if (errorCode3 != 0) {
//                                mOtaResultCallback.onOtaResult(errorCode3, mInfo);
//                                return;
//                            } else {
//                                //解析ota请求返回数据
//                                byte[] payloadReal = ByteUtils.subByte(payload, 1, payload.length - 1);
//                                ByteBuffer dataBuffer = ByteBuffer.wrap(payloadReal);
//                                byte msgId = dataBuffer.get();//最后一次收到正确的帧序
//                                BleLog.d("writeOTA_OK_MsgId " + msgId);
//
//                                int fileSize = dataBuffer.getInt();//设备已接收的正确文件的大小<一次循环>
//                                int progress = 0;
//                                if (otaBytes.length > 0) {
//                                    progress = (int) (100 * (float) fileSize / otaBytes.length);//固件升级进度
//                                    mOtaProgressCallBack.onUpgrade(progress);
//                                }
//                                BleLog.d("writeOTA_fileSize " + fileSize + "progress" + progress);
//
//                                if (fileSize == otaBytes.length) {
//                                    BleLog.d("writeOTA_OK " + "升级成功");
//
//                                    writeNoData(PayLoadUtils.CMD_DOWN_CHECK, new BleWriteCallback() {
//
//                                        @Override
//                                        public void onWriteSuccess(int current, int total,
//                                                                   byte[] justWrite) {
//                                            BleLog.d("onWriteSuccess " + "11111111111" + justWrite.toString());
//
//                                            if (current != total) {
//                                                return;
//                                            }
//
//                                        }
//
//                                        @Override
//                                        public void onWriteFailure(BleException exception) {
//                                            mOtaResultCallback.onOtaResult(OtaErrType.ERR_SYSTEM, mInfo);
//                                        }
//                                    });
//
//                                    return;
//                                }
//
//                                if (msgId == mInfo.getTotalPackageNumbers() - 1) {//单循环升级成功
//                                    mInfo.setLastReceivedFileSize(fileSize);
//                                    otaSendData(mInfo, 0);
//
//                                } else {
//                                    if (!isOTALooping) {
//                                        mInfo.setMsgId(msgId);
//                                        otaSendData(mInfo, msgId + 1);
//                                    }
//                                }
//                            }
//                            break;
//
//                        case 0xA3:// 固件校验回调
//                            if (mOtaResultCallback == null) {
//                                return;
//                            }
//                            if (payload.length != 1) {
//                                return;
//                            }
//
//                            byte errorCode4 = payload[0];
//                            if (errorCode4 == 0) {//固件校验成功
//                                mOtaResultCallback.onOtaResult(OtaErrType.NO_ERROR, mInfo);
//                            } else {//固件校验失败
//                                mOtaResultCallback.onOtaResult(errorCode4, mInfo);
//                            }
//                            break;
//
//
//                        default:
//                            BleLog.d("Unprocessed onCharacteristicChanged！");
//                    }
//                    break;
//                /**
//                 * write
//                 */
//                case BleMsg.MSG_SPLIT_WRITE_NEXT:
//                    if (bleWriteCallback != null) {
//                        bleWriteCallback.onWriteSuccess(mSendCurrent, mSendTotal, (byte[]) msg.obj);
//                    }
//                    break;
//
//                case BleMsg.MSG_AUTH_SUCCEED://
//                    onAuthResult(true);
//                    break;
//
//                case BleMsg.MSG_AUTH_FAILED://
//                    onAuthResult(false);
//                    break;
//
//                case BleMsg.MSG_OTA_VERSION_SUCCEED://
////                    mOtaVersionCallback.onVersionSucceed(mOtaInfo);
//                    break;
//
//                case BleMsg.MSG_OTA_VERSION_FAILED://
//                    mOtaVersionCallback.onVersionFailed(new OTAException());
//                    break;
//
//                case BleMsg.MSG_OTA_DATA:
//                    BleLog.d("MSG_OTA_DATA");
//                    break;
//            }
//        }
//
//    };
//
//
//    private void onAuthResult(boolean isSucceed) {
//        if (mBleAuthCallback == null) {
//            return;
//        }
//        if (isSucceed) {
//            this.mAuthed = true;
//            this.mBleAuthCallback.onAuthSuccess(this.bleDevice);
//            mBleAuthCallback = null;
//        } else {
//            mAuthed = false;
//            this.mBleAuthCallback.onAuthFail(this.bleDevice, new AuthException());
//        }
//        removeAuthStateListener();
//    }
//
//
//    private void sendRandom(String random) {
//        if (TextUtils.isEmpty(random)) {
//            BleLog.e("get random string empty");
//            return;
//        }
//        byte[] randomArray = ByteUtils.strTo16(random);
//        write(randomArray, PayLoadUtils.CMD_DOWN_RANDOM, new BleWriteCallback() {
//            @Override
//            public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                BleLog.e("write random succeed");
//            }
//
//            @Override
//            public void onWriteFailure(BleException exception) {
//                BleLog.e("write random failed");
//            }
//        });
//        BleLog.d("write random: " + random);
//    }
//
//    public static BleConnect getInstance() {
//        return ourInstance;
//    }
//
//
//    public synchronized BluetoothGatt connect(BleDevice bleDevice,
//                                              boolean autoConnect,
//                                              BleConnectCallback callback) {
//        this.bleDevice = bleDevice;
//        XJBleManager.getInstance().getMultipleBluetoothController().buildConnectingBle();
//        if (callback == null) {
//            throw new IllegalArgumentException("BleGattCallback can not be Null!");
//        }
//        BleLog.i("connect device: " + bleDevice.getName()
//                + ",mac: " + bleDevice.getMac()
//                + ",autoConnect: " + autoConnect
//                + ",currentThread: " + Thread.currentThread().getId());
//        bleConnectCallback = callback;
//        SPLIT_WRITE_NUM = bleDevice.getMaxSize();//新增查maxSize
//
//        lastState = LastState.CONNECT_CONNECTING;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            gatt = bleDevice.getDevice().connectGatt(XJBleManager.getInstance().getContext(),
//                    autoConnect, coreGattCallback, TRANSPORT_LE);
//        } else {
//            gatt = bleDevice.getDevice().connectGatt(XJBleManager.getInstance().getContext(),
//                    autoConnect, coreGattCallback);
//        }
//
//        if (gatt != null) {
//            if (bleConnectCallback != null) {
//                bleConnectCallback.onStartConnect();
//            }
//            Message message = mainHandler.obtainMessage();
//            message.what = BleMsg.MSG_CONNECT_OVER_TIME;
//            mainHandler.sendMessageDelayed(message, Constants.DEFAULT_CONNECT_OVER_TIME);
//        } else {
//            disconnect();
//            refreshDeviceCache();
//            closeBluetoothGatt();
//            lastState = LastState.CONNECT_FAILURE;
//            XJBleManager.getInstance().getMultipleBluetoothController().removeConnectingBle(BleConnect.this);
//            if (bleConnectCallback != null)
//                bleConnectCallback.onConnectFail(bleDevice, new OtherException("GATT connect " + "exception occurred!"));
//        }
//        return gatt;
//    }
//
//    public synchronized void refreshDeviceCache() {
//        try {
//            final Method refresh = BluetoothGatt.class.getMethod("refresh");
//            if (refresh != null && gatt != null) {
//                boolean success = (Boolean) refresh.invoke(gatt);
//                BleLog.i("refreshDeviceCache, is success:  " + success);
//            }
//        } catch (Exception e) {
//            BleLog.i("exception occur while refreshing device: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//
//    private BluetoothGattCallback coreGattCallback = new BluetoothGattCallback() {
//        @Override
//        public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int status, int newState) {
//            super.onConnectionStateChange(bluetoothGatt, status, newState);
//            BleLog.i("BluetoothGattCallback：onConnectionStateChange "
//                    + ",status: " + status
//                    + ",newState: " + newState
//                    + ",currentThread: " + Thread.currentThread().getId());
//            gatt = bluetoothGatt;
//            mainHandler.removeMessages(BleMsg.MSG_CONNECT_OVER_TIME);
//
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                if (newState == BluetoothProfile.STATE_CONNECTED) {
//                    Message message = mainHandler.obtainMessage();
//                    message.what = BleMsg.MSG_DISCOVER_SERVICES;
//                    message.arg1 = status;
////                    mainHandler.sendMessageDelayed(message, 50);
//                    mainHandler.sendMessageDelayed(message, 50);
//
//                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                    if (lastState == LastState.CONNECT_CONNECTING) {
//                        Message message = mainHandler.obtainMessage();
//                        message.what = BleMsg.MSG_CONNECT_FAIL;
//                        message.obj = new BleConnectStateParameter(status);
//                        message.arg1 = status;
//                        mainHandler.sendMessage(message);
//
//                    } else if (lastState == LastState.CONNECT_CONNECTED) {
//                        Message message = mainHandler.obtainMessage();
//                        message.what = BleMsg.MSG_DISCONNECTED;
//                        message.arg1 = status;
//                        BleConnectStateParameter para = new BleConnectStateParameter(status);
//                        message.obj = para;
//                        mainHandler.sendMessage(message);
//                    }
//                }
//            } else {// 防止出现status 133
//                Log.d("H5", "防止出现status" + status);
//                Message message = mainHandler.obtainMessage();
//                message.what = BleMsg.MSG_DISCONNECTED;
//                message.arg1 = status;
//                message.obj = new BleConnectStateParameter(status);
//                mainHandler.sendMessage(message);
//            }
//        }
//
//        @Override
//        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int status) {
//            super.onServicesDiscovered(bluetoothGatt, status);
//            BleLog.i("BluetoothGattCallback：onServicesDiscovered ");
//            gatt = bluetoothGatt;
//            Message message = mainHandler.obtainMessage();
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                message.what = BleMsg.MSG_DISCOVER_SUCCESS;
//            } else {
//                message.what = BleMsg.MSG_DISCOVER_FAIL;
//            }
//            message.arg1 = status;
//            message.obj = new BleConnectStateParameter(status);
//            mainHandler.sendMessage(message);
//        }
//
//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt,
//                                            BluetoothGattCharacteristic characteristic) {
//            super.onCharacteristicChanged(gatt, characteristic);
//            byte[] value = characteristic.getValue();
//            BleLog.d("onCharacteristicChanged: " + ByteUtils.bytesToHexString(value));
////            //
//            if (value == null || value.length < 4 || ByteUtils.getUnsignedByte(value[3]) != (value.length - 4)) {
//                BleLog.d("Data length error!");
//                return;
//            }
//            int version = value[0] >> 5;
//            int encrypt = (value[0] >> 4) & 0b0001;
//            int msgId = value[0] & 0x0F;
//            int cmdType = ByteUtils.getUnsignedByte(value[1]);
////            receivedTotal = value[2] >> 4;
//            receivedTotal = ByteUtils.getUnsignedByte(value[2]) >> 4;
//            BleLog.i("receivedTotal:" + receivedTotal);
//            receivedCurrent = value[2] & 0x0F;
//            BleLog.i("receivedCurrent:" + receivedCurrent);
//            byte[] payload = ByteUtils.subByte(value, 4, value[3]);
//            if (BleDevice.PLATFORM_XJ.equals(bleDevice.getPlatform()) && bleDevice.getManufacturerData().isNeedAuth() && mAuthed) {//XJ加密
//                if (TextUtils.isEmpty(bleKey)) {
//                    BleLog.e("call aes with empty key");
//                    return;
//                }
//                payload = AES.decrypt(payload, bleKey);
//            }
//            if (receivedTotal == 0) {
//                receivedTotalPayload = payload;
//                sendReceivedTotalBytes(version, encrypt, msgId, cmdType, receivedTotalPayload);
//                receivedTotalPayload = new byte[]{};
//
//            } else {
//                receivedTotalPayload = ByteUtils.concat(receivedTotalPayload, payload);
//                if (receivedCurrent == receivedTotal) {
//                    sendReceivedTotalBytes(version, encrypt, msgId, cmdType, receivedTotalPayload);
//                    receivedTotalPayload = new byte[]{};
//                }
//            }
//        }
//
//        @Override
//        public void onCharacteristicWrite(BluetoothGatt gatt,
//                                          BluetoothGattCharacteristic characteristic, int status) {
//            super.onCharacteristicWrite(gatt, characteristic, status);
////            BleLog.e("write success: " + ByteUtils.bytesToHexString(characteristic.getValue()));
//            Message message = mainHandler.obtainMessage();
//            message.what = BleMsg.MSG_SPLIT_WRITE_NEXT;
//            message.obj = characteristic.getValue();
//            mainHandler.sendMessage(message);
//        }
//
//        @Override
//        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
//                                      int status) {
//            super.onDescriptorWrite(gatt, descriptor, status);
//            Message message = mainHandler.obtainMessage();
//            byte[] value = descriptor.getValue();
//            //enable indicate
//            if (value.length == 2 && value[0] == BluetoothGattDescriptor.ENABLE_INDICATION_VALUE[0] && value[1] == BluetoothGattDescriptor.ENABLE_INDICATION_VALUE[1]) {
//                message.what = BleMsg.MSG_CHA_INDICATE_RESULT;
//            }
//            //enable notify
//            if (value.length == 2 && value[0] == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE[0] && value[1] == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE[1]) {
//                message.what = BleMsg.MSG_CHA_NOTIFY_RESULT;
//            }
//            message.obj = true;
//            mainHandler.sendMessageDelayed(message, 50);
//        }
//    };
//
//    private void sendReceivedTotalBytes(int version, int encrypt, int msgId, int cmdType,
//                                        byte[] receivedTotalPayload) {
//        Message message = mainHandler.obtainMessage();
//        message.what = BleMsg.MSG_CHA_INDICATE_DATA_CHANGE;
//        BlePacket blePacket = new BlePacket();
//        blePacket.setVersion(version);
//        blePacket.setEncrypt(encrypt);
//        blePacket.setMsgId(msgId);
//        blePacket.setCmdType(cmdType);
//        blePacket.setPayload(receivedTotalPayload);
//        message.obj = blePacket;
//        mainHandler.sendMessage(message);
//    }
//
//
//    public void enableIndicate() {
//        if (gatt == null) {
//            BleLog.e("BluetoothGatt is null, check ble connection!");
//            return;
//        }
//        BluetoothGattService gattService =
//                gatt.getService(UUID.fromString(UUID_XJ_SERVICE));
//        if (gattService == null) {
//            BleLog.e("service uuid not found: " + UUID_XJ_SERVICE);
//            return;
//        }
//        BluetoothGattCharacteristic characteristic =
//                gattService.getCharacteristic(UUID.fromString(UUID_XJ_CHARACTERISTIC_INDICATE));
//        if (characteristic == null) {
//            BleLog.e("indicate characteristic uuid not " +
//                    "found: " + UUID_XJ_CHARACTERISTIC_INDICATE);
//            return;
//        }
//        boolean isSetNotificationSucceed = gatt.setCharacteristicNotification(characteristic, true);
//        if (!isSetNotificationSucceed) {
//            BleLog.e("gatt set CharacteristicNotification fail");
//            return;
//        }
//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
//                UUID.fromString(Constants.UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR));
//        if (descriptor == null) {
//            BleLog.e("descriptor equals null");
//        } else {
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
//            boolean isSetDescriptorSucceed = gatt.writeDescriptor(descriptor);
//            if (!isSetDescriptorSucceed) {
//                BleLog.e("gatt set descriptor fail");
//            }
//        }
//    }
//
//    public void enableNotify() {
//        if (gatt == null) {
//            BleLog.e("BluetoothGatt is null, check ble " +
//                    "connection!");
//            return;
//        }
//        BluetoothGattService gattService =
//                gatt.getService(UUID.fromString(UUID_XJ_SERVICE));
//        if (gattService == null) {
//
//            BleLog.e("service uuid not found: " + UUID_XJ_SERVICE);
//
//            return;
//        }
//        BluetoothGattCharacteristic characteristic =
//                gattService.getCharacteristic(UUID.fromString(UUID_XJ_CHARACTERISTIC_NOTIFY));
//        if (characteristic == null) {
//            BleLog.e("indicate characteristic uuid not " +
//                    "found: " + UUID_XJ_CHARACTERISTIC_NOTIFY);
//            return;
//        }
//        boolean isSetNotificationSucceed = gatt.setCharacteristicNotification(characteristic, true);
//        if (!isSetNotificationSucceed) {
//            BleLog.e("gatt set CharacteristicNotification fail");
//            return;
//        }
//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
//                UUID.fromString(Constants.UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR));
//        if (descriptor == null) {
//            BleLog.e("descriptor equals null");
//        } else {
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            boolean isSetDescriptorSucceed = gatt.writeDescriptor(descriptor);
//            if (!isSetDescriptorSucceed) {
//                BleLog.e("gatt set descriptor fail");
//            }
//        }
//    }
//
//    private byte getByte0() {
//        if (mMsgId > 15) {
//            mMsgId = 1;
//        }
//        return mMsgId++;
//    }
//
//
//    public void writeOTA(byte[] payload, byte cmd, byte msgIndex, BleWriteCallback callback) {
//        BleLog.d("payload:" + ByteUtils.bytesToHexString(payload));
//        //数据长度大于20，需要分包
//        BleLog.d("length=" + payload.length + ", payload: " + ByteUtils.bytesToHexString(payload));
//        byte msgId = getByte0();
//
//        //改动
//        boolean isSplit;
//        if (BleDevice.PLATFORM_XJ.equals(bleDevice.getPlatform()) && bleDevice.getManufacturerData().isNeedAuth()) {//XJ加密
//            isSplit = payload.length + 5 > SPLIT_WRITE_NUM;
//        } else {
//            isSplit = payload.length + 4 > SPLIT_WRITE_NUM;
//        }
//
//        if (ByteUtils.byteToHexString(cmd).contains("A2")) {
//            msgId = msgIndex;//发包
//            BleLog.d("writeOTA_msgId=" + msgId);
//        }
//
//        if (isSplit) {
//            new SplitWriter().splitWrite(payload, cmd, msgId, true, 50, SPLIT_WRITE_NUM, callback);
//        } else {
//            mSendTotal = 0;
//            mSendCurrent = 0;
//            writeSingle(payload, mSendTotal, mSendCurrent, cmd, msgId, callback);
//        }
//    }
//
//
//    public void writeNoData(byte cmd, BleWriteCallback callback) {
//        byte msgId = getByte0();
//        mSendTotal = 0;
//        mSendCurrent = 0;
//        writeSingle(null, mSendTotal, mSendCurrent, cmd, msgId, callback);
//    }
//
//    public void write(byte[] payload, byte cmd, BleWriteCallback callback) {
//        BleLog.d("payload:" + ByteUtils.bytesToHexString(payload));
//        //数据长度大于20，需要分包
//        BleLog.d("length=" + payload.length + ", payload: " + ByteUtils.bytesToHexString(payload));
//        byte msgId = getByte0();
//
//        //改动
//        boolean isSplit;
//        if (BleDevice.PLATFORM_XJ.equals(bleDevice.getPlatform()) && bleDevice.getManufacturerData().isNeedAuth()) {//XJ加密
//            isSplit = payload.length + 5 > SPLIT_WRITE_NUM;
//        } else {
//            isSplit = payload.length + 4 > SPLIT_WRITE_NUM;
//        }
//
//        if (ByteUtils.byteToHexString(cmd).contains("A2")) {
//            msgId = (byte) mInfo.getMsgIndex();//发包
//            BleLog.d("writeOTA_MsgId=" + msgId);
//        }
//
//        if (isSplit) {
//            new SplitWriter().splitWrite(payload, cmd, msgId, true, 50, SPLIT_WRITE_NUM, callback);
//        } else {
//            mSendTotal = 0;
//            mSendCurrent = 0;
//            writeSingle(payload, mSendTotal, mSendCurrent, cmd, msgId, callback);
//        }
//    }
//
//    public void writeSingle(byte[] payload, int total, int current, byte cmd, byte msgId,
//                            BleWriteCallback callback) {
//        mSendTotal = total;
//        mSendCurrent = current;
//        bleWriteCallback = callback;
//        if (gatt == null) {
//            String errorMsg = "BluetoothGatt is null, check ble connection!";
//            if (bleWriteCallback != null) {
//                bleWriteCallback.onWriteFailure(new OtherException(errorMsg));
//            }
//            BleLog.e(errorMsg);
//            handler.removeCallbacks(sendDelayRun);
//            handler.postDelayed(sendDelayRun, 6000);
//            return;
//        }
//        BluetoothGattService gattService =
//                gatt.getService(UUID.fromString(UUID_XJ_SERVICE));
//        if (gattService == null) {
//            String errorMsg = "service uuid not found: " + UUID_XJ_SERVICE;
//            if (bleWriteCallback != null) {
//                bleWriteCallback.onWriteFailure(new OtherException(errorMsg));
//            }
//            BleLog.e(errorMsg);
//            return;
//        }
//
//        BluetoothGattCharacteristic characteristic =
//                gattService.getCharacteristic(UUID.fromString(UUID_XJ_CHARACTERISTIC_WRITE));
//
//        if (PayLoadUtils.isWithResponse(cmd)) {//新增走withNoResponse-----fea9
//            characteristic = gattService.getCharacteristic(UUID.fromString(UUID_XJ_CHARACTERISTIC_WRITE_WITH_NO_RESPONSE));
//        }
//
//        if (characteristic == null) {
//            String errorMsg =
//                    "write characteristic uuid not found: " + UUID_XJ_CHARACTERISTIC_WRITE;
//            if (bleWriteCallback != null) {
//                bleWriteCallback.onWriteFailure(new OtherException(errorMsg));
//            }
//            BleLog.e(errorMsg);
//            return;
//        }
//
//        byte byte0 = msgId;
//
//        if (BleDevice.PLATFORM_XJ.equals(bleDevice.getPlatform()) && bleDevice.getManufacturerData().isNeedAuth() && mAuthed) {//XJ加密
//            byte0 = (byte) (msgId + (0x01 << 4));
//            if (TextUtils.isEmpty(bleKey)) {
//                BleLog.e("call aes with empty key");
//                return;
//            }
//            if (payload != null) {
//                payload = AES.encrypt(payload, bleKey);
//            }
//        }
//        //header-4字节payload-0~N字节
//
//        ByteBuffer totalBuffer = payload != null ? ByteBuffer.allocate(4 + payload.length) : ByteBuffer.allocate(4);
//
//        totalBuffer.put(byte0);
//
//        totalBuffer.put(cmd);
//        totalBuffer.put((byte) ((total << 4) + current));
//        if (payload != null) {
//            totalBuffer.put((byte) payload.length);
//            totalBuffer.put(payload);
//        }
//
//        characteristic.setValue(totalBuffer.array());
//        gatt.writeCharacteristic(characteristic);
//        isTimeOut = false;
//        bleCmd = cmd;
//        handler.removeCallbacks(sendDelayRun);
//        handler.postDelayed(sendDelayRun, 6000);
//
//        // BleLog.d("write: " + ByteUtils.bytesToHexString(totalBuffer.array()) + ", length: " + totalBuffer.array().length);
//        BleLog.d("OTAData: " + ByteUtils.bytesToHexString(totalBuffer.array()) + ", length: " + totalBuffer.array().length);
//
////        if (ByteUtils.byteToHexString(cmd).contains("A2")) {
////            BleLog.d("OTAData: " + ByteUtils.bytesToHexString(totalBuffer.array()) + ", length: " + totalBuffer.array().length);
////        }
//    }
//
//    public BluetoothGatt connect(BleDevice bleDevice, IBleAuth iBleAuth,
//                                 BleConnectCallback bleConnectCallback) {
//        this.mIBleAuth = iBleAuth;
//        return connect(bleDevice, false, bleConnectCallback);
//    }
//
//
//    public synchronized void addConnectGattCallback(BleConnectCallback callback) {
//        bleConnectCallback = callback;
//    }
//
//    public synchronized void removeConnectGattCallback() {
//        bleConnectCallback = null;
//    }
//
//
//    public synchronized void addSendResultCallback(SendResultCallBack callback) {
//        sendResultCallBack = callback;
//    }
//
//
//    public void setData(byte[] payload, BleDataSetCallback callback) {
//        BleLog.e("oxff:" + (int) 0xFF);
//        mBleDataSetCallback = callback;
//        write(payload, PayLoadUtils.CMD_DOWN_SET, new BleWriteCallback() {
//            @Override
//            public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                BleLog.i("onWriteSuccess:" + justWrite.toString());
//            }
//
//            @Override
//            public void onWriteFailure(BleException exception) {
//                BleLog.i("onWriteSuccess:" + exception);
//            }
//        });
//    }
//
//    public void getData(byte[] payload) {
//        BleLog.e("oxff:" + (int) 0xFF);
//        write(payload, PayLoadUtils.CMD_DOWN_GET, new BleWriteCallback() {
//            @Override
//            public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                BleLog.i("onWriteSuccess:" + justWrite.toString());
//            }
//
//            @Override
//            public void onWriteFailure(BleException exception) {
//            }
//        });
//    }
//
//    public void startBleWifiConfig(byte[] payload, String token, BleWifiConfigCallback
//            callback) {
//        mBleWifiConfigCallback = callback;
//        write(payload, PayLoadUtils.CMD_DOWN_WIFICONFIG, new BleWriteCallback() {
//            @Override
//            public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                BleLog.i("onWriteSuccess:" + justWrite.toString());
//            }
//
//            @Override
//            public void onWriteFailure(BleException exception) {
//
//            }
//        });
//    }
//
//
//    public void queryVersion(byte[] payload, IBleAuth iBleAuth, OtaVersionCallback callback) {//检查固件信息
//        mOtaVersionCallback = callback;
//        this.mIBleAuth = iBleAuth;
//        write(payload, PayLoadUtils.CMD_DOWN_VERSION, new BleWriteCallback() {
//            @Override
//
//            public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                BleLog.i("onWriteSuccess:" + justWrite.toString());
//            }
//
//            @Override
//            public void onWriteFailure(BleException exception) {
//                BleLog.e("oxff:" + (int) 0xFF);
//            }
//        });
//    }
//
//
//    public void otaRequestOTA(byte[] payload, OtaInfo.ContentBean.ModuleBean bean, OtaResultCallback resultCallback, OtaProgressCallBack progressCallBack) {
//        otaClose = false;
//        mOtaResultCallback = resultCallback;
//        mOtaProgressCallBack = progressCallBack;
//        otaBytes = bean.getDownLoadBytes();
//        mInfo = bean;
//        write(payload, PayLoadUtils.CMD_DOWN_UPDATE_REQUEST, new BleWriteCallback() {
//            @Override
//            public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                BleLog.i("onWriteSuccess:" + justWrite.toString());
//            }
//
//            @Override
//            public void onWriteFailure(BleException exception) {
//                BleLog.i("onWriteFailure:" + exception);
//            }
//        });
//    }
//
//    public void otaSendData(OtaInfo.ContentBean.ModuleBean bean, int packageIndex) {
//        if (otaClose) {
//            return;
//        }
//        otaBytes = bean.getDownLoadBytes();
//        int receivedFileSize = bean.getLastReceivedFileSize();
//        int fileSize = otaBytes.length;
//        isOTALooping = true;
//        final int[] packetLength = new int[1];
//        if (BleDevice.PLATFORM_XJ.equals(bleDevice.getPlatform()) && bleDevice.getManufacturerData().isNeedAuth()) {//XJ加密
//            packetSize = SPLIT_WRITE_NUM - 5;
//        } else {
//            packetSize = SPLIT_WRITE_NUM - 4;
//        }
//        task(2, bean.getTotalPackageNumbers() - packageIndex, (int index) -> {
//            int i = index + packageIndex;
//            byte[] data = new byte[packetSize];
//            if (fileSize < receivedFileSize + packetSize * i) {
//                return;
//            }
//            boolean isLast = false;
//
//            if (fileSize < receivedFileSize + packetSize * (i + 1)) {
//                if (fileSize - receivedFileSize - packetSize * i < packetSize) {
//                    packetLength[0] = fileSize - receivedFileSize - packetSize * i;
//                    isLast = true;
//                    data = new byte[packetLength[0]];
//                }
//                BleLog.i("packetSize:" + packetSize);
//            }
//            if (!isLast) {
//                System.arraycopy(otaBytes, receivedFileSize + packetSize * i, data, 0, packetSize);
//            } else {
//                System.arraycopy(otaBytes, receivedFileSize + packetSize * i, data, 0, packetLength[0]);
//            }
//            if (i == bean.getTotalPackageNumbers() - 1) {
//                isOTALooping = false;
//            }
//            BleConnect.getInstance().writeOTA(data, PayLoadUtils.CMD_DOWN_SEND_DATA, (byte) i, null);//循环发包
//        });
//    }
//
//
//    interface TaskLoop {
//        void loop(int index);
//    }
//
//    private void task(long interval, int count, TaskLoop callback) {
//        doTask(interval, 0, count, callback);
//    }
//
//    private void doTask(long interval, int index, int count, TaskLoop callback) {
//        if (index >= count) {
//            return;
//        }
//        callback.loop(index);
//        int next = ++index;
//        new Handler(Looper.getMainLooper()).postDelayed(() -> doTask(interval, next, count, callback), interval);
//    }
//
//    public void addDataChangeListener(BleDataChangeCallback callback) {
//        mBleDataChangeCallback = callback;
//    }
//
//    public void addAuthStateListener(BleAuthCallback callback) {
//        this.mBleAuthCallback = callback;
//    }
//
//    public void removeAuthStateListener() {
//        mBleAuthCallback = null;
//    }
//
//    public void removeDataChangeListener() {
//        mBleDataChangeCallback = null;
//    }
//
//    public void stopOTA() {
//        otaClose = true;
//    }
//
//    public synchronized void destroy() {
//        lastState = LastState.CONNECT_IDLE;
////        disconnect();
//        refreshDeviceCache();
//        closeBluetoothGatt();
//        removeConnectGattCallback();
//        mainHandler.removeCallbacksAndMessages(null);
//    }
//
//    private synchronized void closeBluetoothGatt() {
//        if (gatt == null) {
//            return;
//        }
//        gatt.close();
//        Log.d("H5", "closeBluetoothGatt");
//        gatt = null;
//    }
//
//
//    enum LastState {
//        CONNECT_IDLE,
//        CONNECT_CONNECTING,
//        CONNECT_CONNECTED,
//        CONNECT_FAILURE,
//        CONNECT_DISCONNECT
//    }
//
//
//    //新增点
//    public synchronized void disconnect() {
//        if (XJBleManager.getInstance().getBluetoothAdapter() == null || gatt == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized");
//            return;
//        }
//        isActiveDisconnect = true;
//        Log.d("H5", "disconnect" + gatt);
//        gatt.close();
//    }
//
//    public String getDeviceKey() {
//        return bleDevice.getKey();
//    }
//
//    public BleDevice getDevice() {
//        return bleDevice;
//    }
//}

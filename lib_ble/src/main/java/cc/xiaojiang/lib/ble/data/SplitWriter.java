package cc.xiaojiang.lib.ble.data;


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.LinkedList;
import java.util.Queue;

import cc.xiaojiang.lib.ble.PayLoadUtils;
import cc.xiaojiang.lib.ble.callback.BleWriteCallback;
import cc.xiaojiang.lib.ble.exception.BleException;
import cc.xiaojiang.lib.ble.exception.OtherException;
import cc.xiaojiang.lib.ble.test.BleBluetooth;
import cc.xiaojiang.lib.ble.utils.BleLog;

public class SplitWriter {
    private static int SPLIT_WRITE_NUM = 15;
    private BleBluetooth mBleBluetooth;
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private byte[] mData;
    private int mCount;
    private boolean mSendNextWhenLastSuccess;
    private long mIntervalBetweenTwoPackage;
    private BleWriteCallback mCallback;
    private Queue<byte[]> mDataQueue;
    private int mTotalNum;
    private int mPosition;
    private byte mMsgId;
    private byte mCmd;

    public SplitWriter() {
        mHandlerThread = new HandlerThread("splitWriter");
        mHandlerThread.start();

        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == BleMsg.MSG_SPLIT_WRITE_NEXT) {
                    write();
                }
            }
        };
    }

    public void splitWrite(BleBluetooth bleBluetooth,byte[] data, byte cmd, byte msgId,
                           boolean sendNextWhenLastSuccess,
                           long intervalBetweenTwoPackage,
                           int split_write_num,
                           BleWriteCallback callback) {
        mBleBluetooth = bleBluetooth;
        mData = data;
        mMsgId = msgId;
        mCmd = cmd;
        mSendNextWhenLastSuccess = sendNextWhenLastSuccess;
        mIntervalBetweenTwoPackage = intervalBetweenTwoPackage;
        //拆包后，除4字节包头，每包payload长度不超过16
//        mCount = SPLIT_WRITE_NUM;
        SPLIT_WRITE_NUM = split_write_num;

        mCount = SPLIT_WRITE_NUM - 5;//改动
        mCallback = callback;
        splitWrite();
    }

    private void splitWrite() {
        if (mData == null) {
            throw new IllegalArgumentException("data is Null!");
        }
        if (mCount < 1) {
            throw new IllegalArgumentException("split count should higher than 0!");
        }
        mDataQueue = splitByte(mData, mCount);
        mTotalNum = mDataQueue.size();
        mPosition = 0;
        write();
    }

    private void write() {
        if (mDataQueue.peek() == null) {
            release();
            return;
        }

        byte[] data = mDataQueue.poll();
        mBleBluetooth.writeSingle(data, mTotalNum - 1, mPosition,
                mCmd, mMsgId,
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        mPosition = mTotalNum - mDataQueue.size();
                        if (mCallback != null) {
                            mCallback.onWriteSuccess(mPosition, mTotalNum, justWrite);
                        }
                        if (mSendNextWhenLastSuccess) {
                            Message message =
                                    mHandler.obtainMessage(BleMsg.MSG_SPLIT_WRITE_NEXT);
                            mHandler.sendMessageDelayed(message,
                                    mIntervalBetweenTwoPackage);
                        }
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        if (mCallback != null) {
                            mCallback.onWriteFailure(new OtherException("exception occur " +
                                    "while writing: " + exception.getDescription()));
                        }
                        if (mSendNextWhenLastSuccess) {
                            Message message =
                                    mHandler.obtainMessage(BleMsg.MSG_SPLIT_WRITE_NEXT);
                            mHandler.sendMessageDelayed(message,
                                    mIntervalBetweenTwoPackage);
                        }
                    }
                });


        if (!mSendNextWhenLastSuccess) {
            Message message = mHandler.obtainMessage(BleMsg.MSG_SPLIT_WRITE_NEXT);
            mHandler.sendMessageDelayed(message, mIntervalBetweenTwoPackage);
        }
    }

    private void release() {
        mHandlerThread.quit();
        mHandler.removeCallbacksAndMessages(null);
    }


    private static Queue<byte[]> splitByte(byte[] data, int count) {
//        if (count > 20) {
        if (count > SPLIT_WRITE_NUM) {
            BleLog.w("Be careful: split count beyond 20! Ensure MTU higher than 23!");
        }
        Queue<byte[]> byteQueue = new LinkedList<>();
        int pkgCount;
        if (data.length % count == 0) {
            pkgCount = data.length / count;
        } else {
            pkgCount = Math.round(data.length / count + 1);
        }

        if (pkgCount > 0) {
            for (int i = 0; i < pkgCount; i++) {
                byte[] dataPkg;
                int j;
                if (pkgCount == 1 || i == pkgCount - 1) {
                    j = data.length % count == 0 ? count : data.length % count;
                    System.arraycopy(data, i * count, dataPkg = new byte[j], 0, j);
                } else {
                    System.arraycopy(data, i * count, dataPkg = new byte[count], 0, count);
                }
                byteQueue.offer(dataPkg);
            }
        }

        return byteQueue;
    }


}

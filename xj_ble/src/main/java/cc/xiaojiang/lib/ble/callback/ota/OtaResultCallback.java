package cc.xiaojiang.lib.ble.callback.ota;

import cc.xiaojiang.lib.ble.OtaInfo;
import cc.xiaojiang.lib.ble.exception.BleException;

public interface OtaResultCallback {
    void onOtaResult(int errorCode,OtaInfo.ContentBean.ModuleBean bean);
}

package cc.xiaojiang.lib.ble.callback;

import java.util.List;

import cc.xiaojiang.lib.ble.data.AttrModel;

/**
 * Created by facexxyz on 5/12/21.
 */
public interface BleDataChangeCallback {

    void onDataChanged(int errorCode,byte cmd, List<AttrModel> attrModels);
}

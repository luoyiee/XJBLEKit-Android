package cc.xiaojiang.lib.ble.data;

import lombok.Data;

/**
 * Created by facexxyz on 5/13/21.
 */

@Data
public class AttrModel {
    private int typeId;
    private int AttrId;
    private Object value;

    @Override
    public String toString() {
        return "AttrModel{" +
                "typeId=" + typeId +
                ", AttrId=" + AttrId +
                ", value='" + value + '\'' +
                '}';
    }
}

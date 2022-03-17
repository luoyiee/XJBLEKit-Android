package cc.xiaojiang.lib.ble.data;

/**
 * Created by facexxyz on 5/13/21.
 */


public class AttrModel {
    private int typeId;
    private int AttrId;
    private Object value;

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getAttrId() {
        return AttrId;
    }

    public void setAttrId(int attrId) {
        AttrId = attrId;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "AttrModel{" +
                "typeId=" + typeId +
                ", AttrId=" + AttrId +
                ", value='" + value + '\'' +
                '}';
    }
}

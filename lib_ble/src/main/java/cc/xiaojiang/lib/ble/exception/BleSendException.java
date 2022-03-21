package cc.xiaojiang.lib.ble.exception;

import java.io.Serializable;

import lombok.Data;

@Data
public class BleSendException implements Serializable {
    private static final byte NO_ERR = 0x00;
    private static final byte ERR_system = 0x01;
    private static final byte ERR_needAuth = 0x02;
    private static final byte ERR_decode = 0x03;
    private static final byte ERR_invalidPara = 0x04;
    private static final byte ERR_Auth = 0x05;

    private int code;
    private String description;

    public BleSendException(int code) {
        this.code = code;
        description = getDesc(code);
    }

    private String getDesc(int code) {
        String desc = "";
        switch (code) {
            case ERR_system:
                desc = "";
                break;
            case ERR_needAuth:
                break;
            case ERR_decode:
                break;
            case ERR_invalidPara:
                break;
            case ERR_Auth:
                break;
        }
        return desc;
    }

}

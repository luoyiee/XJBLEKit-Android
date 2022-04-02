package cc.xiaojiang.lib.ble;

import android.content.Context;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Data
public class OtaInfo implements Serializable {

    private int taskCount;
    private int taskIndex;
    private int msgIndex;
    private long taskId;
    private String newVersion;
    private int firmwareType;
    private String availableVersion;//可用升级版本
    private Boolean isRunning;
    private Integer remindTypeEnd;

    private int otaType;
    private String mcuVersion;//mcu版本
    private String moduleVersion;//模组版本
    private byte totalPackageNumbers;
    private int lastReceivedFileSize;
    private byte msgId;
    private Boolean isReSend;
    private Boolean isAllowUpdate;
    private Boolean isSupportBreakPoint;
//    private String upgradeDetail;

    private ContentBean content;

    @Data
    public static class ContentBean implements Serializable {
        private long mcuTaskId;
        private ModuleBean module;
        private ModuleBean mcu;
        private long moduleTaskId;

        @Data
        public static class ModuleBean implements Serializable {
            private Integer id;
            private Object product;
            private ModuleData module;
            private Object model;
            private Object productKey;
            private Object productName;
            private Object brandName;
            private String version;
            private String orientVersion;
            private Integer sort;
            private int status;
            private Object reason;
            private Object statusStr;
            private int upgradeType;
            private int firmwareType;
            private int verifyType;
            private Integer remindType;
            private Object verifyDevice;
            private String fileUrl;
            private Integer fileSize;
            private String fileHash;
            private String description;//更新信息
            private String updateDis;//强制更新信息
            private String langDescription;
            private String langUpdateDis;
            private String createTime;
            private String updateTime;


            private long taskId;
            private int msgIndex;
            private byte totalPackageNumbers;
            private byte deviceRebootTime;
            private int lastReceivedFileSize;
            private byte msgId;
            private Boolean isAllowUpdate;
            private Boolean isSupportBreakPoint;
            private byte[]  downLoadBytes;


            @Data
            public static class ModuleData implements Serializable {
                private Integer id;
                private String model;
            }
        }
    }

}

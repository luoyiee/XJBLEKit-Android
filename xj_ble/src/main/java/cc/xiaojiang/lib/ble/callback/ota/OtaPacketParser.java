package cc.xiaojiang.lib.ble.callback.ota;

public class OtaPacketParser {
    private int total;
    private int index = -1;
    private byte[] data;
    private int progress;


    public void set(byte[] data) {
        this.clear();

        this.data = data;
        int length = this.data.length;
        int size = 16;

        if (length % size == 0) {
            total = length / size;
        } else {
            total = (int) Math.floor(length / size + 1);
        }
    }

    public void clear() {
        this.progress = 0;
        this.total = 0;
        this.index = -1;
        this.data = null;
    }

}

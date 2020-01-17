package com.littlewhite.ReceiveFile.ColorCode;

import com.google.zxing.common.BitMatrix;

public class RGBmatrixChannel {
    private BitMatrix data;
    private int channel;

    public long getTimemillis() {
        return timemillis;
    }

    public void setTimemillis(long timemillis) {
        this.timemillis = timemillis;
    }

    private long timemillis;

    public RGBmatrixChannel(BitMatrix data, int channel, long timemillis) {
        this.data = data;
        this.channel = channel;
        this.timemillis = timemillis;
    }

    public BitMatrix getData() {
        return data;
    }

    public void setData(BitMatrix data) {
        this.data = data;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }
}

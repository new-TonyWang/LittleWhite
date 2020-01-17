package com.littlewhite.ReceiveFile.ColorCode;

public class RGBChannel {
    private byte[] data;//颜色数据
    private int channel;//通道1R,2G,3B
    private int width;
    private int height;
    private long timemillis;

    public RGBChannel(byte[] data, int channel, int width, int height, long timemillis) {
        this.data = data;
        this.channel = channel;
        this.width = width;
        this.height = height;
        this.timemillis = timemillis;
    }

    public long getTimemillis() {
        return timemillis;
    }

    public void setTimemillis(long timemillis) {
        this.timemillis = timemillis;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }
}

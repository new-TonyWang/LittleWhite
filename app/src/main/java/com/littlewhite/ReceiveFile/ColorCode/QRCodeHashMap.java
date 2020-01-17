package com.littlewhite.ReceiveFile.ColorCode;

import android.util.Log;

import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.RGBData;
import com.littlewhite.R;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public class QRCodeHashMap {
    private LinkedHashMap hashMap;

    public QRCodeHashMap() {
        this.hashMap = new LinkedHashMap();
    }
    public RGBData addNewNode(RGBmatrixChannel rgBmatrixChannel){
        LinkedList<RGBmatrixChannel> LinkedList = (LinkedList<RGBmatrixChannel>) this.hashMap.get(rgBmatrixChannel.getTimemillis());
        if(LinkedList==null) {
            LinkedList = new LinkedList<RGBmatrixChannel>();
            this.hashMap.put( rgBmatrixChannel.getTimemillis(),LinkedList);
            LinkedList.push(rgBmatrixChannel);
            //Log.e("hash表","生成新链表");
            return null;
        }else{
            if(LinkedList.size()!=2){
                LinkedList.push(rgBmatrixChannel);
                //Log.e("hash表","加入新节点,长度"+LinkedList.size());
                //Log.e("hash表","加入新节点,长度"+rgBmatrixChannel.getChannel());
                return null;
            }else{
                //Log.e("RGBmatrixChannel",",长度"+LinkedList.size());
                RGBmatrixChannel channelfirst = LinkedList.poll();
                RGBmatrixChannel channelsecond = LinkedList.poll();
                RGBmatrixChannel channelthird = rgBmatrixChannel;
               // Log.e("RGBmatrixChannel",",长度"+LinkedList.size());
               // RGBmatrixChannel[] matrixarray= new RGBmatrixChannel[3];
                long end = System.currentTimeMillis();
                Log.e("二值化时间:",(end-rgBmatrixChannel.getTimemillis()+"ms"));
                switch (channelfirst.getChannel()){
                    case R.id.Convert_R:
                        if(channelsecond.getChannel()==R.id.Convert_G){
                            return new RGBData(channelfirst.getData(),channelsecond.getData(),channelthird.getData());
                        }else {
                            return new RGBData(channelfirst.getData(),channelthird.getData(),channelsecond.getData());
                        }

                    case R.id.Convert_G:
                        if(channelsecond.getChannel()==R.id.Convert_R){
                            return new RGBData(channelsecond.getData(),channelfirst.getData(),channelthird.getData());
                        }else {
                            return new RGBData(channelthird.getData(),channelfirst.getData(),channelsecond.getData());
                        }

                    case R.id.Convert_B:
                        if(channelsecond.getChannel()==R.id.Convert_R){
                            return new RGBData(channelsecond.getData(),channelthird.getData(),channelfirst.getData());
                        }else {
                            return new RGBData(channelthird.getData(),channelsecond.getData(),channelfirst.getData());
                        }
                        default:
                            return null;
                }

            }
        }
    }
}

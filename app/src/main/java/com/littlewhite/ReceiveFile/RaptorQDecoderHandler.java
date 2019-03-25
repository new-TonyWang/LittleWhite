package com.littlewhite.ReceiveFile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Switch;

import com.littlewhite.R;


import net.fec.openrq.ArrayDataDecoder;
import net.fec.openrq.EncodingPacket;
import net.fec.openrq.OpenRQ;
import net.fec.openrq.SymbolType;
import net.fec.openrq.decoder.SourceBlockDecoder;
import net.fec.openrq.decoder.SourceBlockState;
import net.fec.openrq.parameters.FECParameters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;


public class RaptorQDecoderHandler extends Handler {
    private ReceiveActivity receiveActivity;
    private int num = 0;//解析了几个
    private int sum;//一共几个
    private double total;//生成check数组的时候使用
    private FECParameters fecParameters;
    private ArrayDataDecoder arrayDataDecoder;
    //private byte[] source;
    private FileOutputStream fos;
     private boolean[] check;//保存已经被检测过的数据包
    private boolean hasinit = false;//表示是否接收到了参数，可以真正开始解码
   // private LinkedBlockingQueue<byte[]> queue;//存放未解码的byte数组
    private File receiveFile;
    public RaptorQDecoderHandler(ReceiveActivity receiveActivity, FECParameters fecParameters, ArrayDataDecoder arrayDataDecoder,File receiveFile) {
        this.receiveActivity = receiveActivity;
        this.fecParameters = fecParameters;
        this.arrayDataDecoder = arrayDataDecoder;
        this.receiveFile = receiveFile;
        try {
            this.fos = new FileOutputStream(this.receiveFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //this.queue = new LinkedBlockingQueue<>();

    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.decode:
                if (!hasinit) {
                    hasinit = true;
                    //byte[] Parameters = message.obj
                    this.fecParameters = FECParameters.ExtractFECParameters((byte[]) message.obj).value();//耗时操作，应该再做一点别的东西
                    this.arrayDataDecoder = OpenRQ.newDecoderWithTwoOverhead(this.fecParameters);
                    this.sum = this.fecParameters.totalSymbols();
                    this.total = this.sum;
                    this.sum+=2;
                    check = new boolean[(int)Math.ceil(this.total*9/5)];

                    //check = new boolean[this.sum];
                    //this.check = new boolean[];
                    //new Thread(this).start();
                }
                decodeRaptor((byte[]) message.obj);
                break;
            case R.id.restart_decode:

                break;
            case R.id.finish:
                Looper.myLooper().quit();
                break;
            case R.id.stop:
                break;
        }
    }

    private void decodeRaptor(byte[] RaptorData) {
        EncodingPacket encodingPacket = null;
        encodingPacket = this.arrayDataDecoder.parsePacket(RaptorData,12,RaptorData.length-12, true).value();
        //System.out.println("encodingPacket.encodingSymbolID():"+encodingPacket.encodingSymbolID());//一直变化，已经解析的个数
        //  System.out.println(encodingPacket.sourceBlockNumber());//
        SourceBlockState sourceBlockState = arrayDataDecoder.sourceBlock(encodingPacket.sourceBlockNumber()).putEncodingPacket(encodingPacket);
        //SourceBlockDecoder sourceBlock = arrayDataDecoder.sourceBlock(encodingPacket.sourceBlockNumber());
        switch(sourceBlockState){
           case INCOMPLETE:
               if(!check[encodingPacket.fecPayloadID()]) {
                   check[encodingPacket.fecPayloadID()] = true;
                   this.num++;
                   SendToReceiveHandler(R.id.finish,this.num, this.sum);
               }
               break;
           case DECODED:
               byte[] CompleteData = arrayDataDecoder.dataArray();
               try {
                   fos.write(CompleteData);
                   fos.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
               SendToUnzip( R.id.finish,++this.num,this.sum);
               Looper.myLooper().quit();
               break;
           case DECODING_FAILURE://解析失败，生成端有误
               Looper.myLooper().quit();
               this.receiveFile.delete();
               break;

       }
    }
    private void SendToReceiveHandler(int what,int CorrectNum,int sum){
        Message message = Message.obtain(receiveActivity.getReceiveHandler(),what,CorrectNum,sum);
       // message.setData(bundle);
        message.sendToTarget();
    }
    private void SendToUnzip(int what,int CorrectNum,int sum){//解压文件
        Bundle bundle = new Bundle();
        ArrayList<String> stringArrayList= new ArrayList<>();
        stringArrayList.add(receiveFile.getAbsolutePath());
        bundle.putStringArrayList("FilePath",stringArrayList);
        Message message = Message.obtain(receiveActivity.getReceiveHandler(),what,CorrectNum,sum);
        message.sendToTarget();

    }

}

package com.littlewhite.ReceiveFile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.littlewhite.R;
import com.littlewhite.ReceiveFile.SqllitUtil.FileInfo;
import com.littlewhite.ReceiveFile.SqllitUtil.SqllitData;


import net.fec.openrq.ArrayDataDecoder;
import net.fec.openrq.EncodingPacket;
import net.fec.openrq.OpenRQ;
import net.fec.openrq.SerializablePacket;
import net.fec.openrq.decoder.SourceBlockState;
import net.fec.openrq.parameters.FECParameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.Objects.requireNonNull;


public class RaptorQDecoderHandler extends Handler {
    private ReceiveActivity receiveActivity;
    private int num = 0;//解析了几个
    private int sum;//一共几个
    private int total;//生成check数组的时候使用
    private FECParameters fecParameters;
    private ArrayDataDecoder arrayDataDecoder;
    //private byte[] source;
    //private StringBuilder stringBuilder;

    private SqllitData sqllitData;
   // private FileInfo receiveFileInfo;
     private boolean[] check;//保存已经被检测过的数据包
    private boolean hasinit = false;//表示是否接收到了参数，可以真正开始解码
   // private LinkedBlockingQueue<byte[]> queue;//存放未解码的byte数组
    private File receivePath;
    private File receiveFile;//表示的是object流的文件
    private ObjectOutputStream objectOutputStream;
    private List<FileInfo> List;
    public RaptorQDecoderHandler(ReceiveActivity receiveActivity,File receivePath,SqllitData sqllitData,List<FileInfo> List) {
        this.receiveActivity = receiveActivity;
       // this.fecParameters = fecParameters;
       // this.arrayDataDecoder = arrayDataDecoder;
        this.receivePath = receivePath;
        this.sqllitData = sqllitData;
        this.List = List;
        //this.receiveFileInfo = new FileInfo(this.receiveFile.getName());
       /* try {
            this.fos = new FileOutputStream(this.receiveFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
        //this.queue = new LinkedBlockingQueue<>();

    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.decode:
                if (!hasinit) {
                    hasinit = true;
                     CompareWithList(this.List,(byte[]) message.obj);
                     this.List = null;
                     this.receivePath = null;
                }

                decodeRaptor((byte[]) message.obj);
                break;
            case R.id.restart_decode:

                break;
            case R.id.finish:
                requireNonNull(Looper.myLooper()).quit();
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
                  /* stringBuilder.append(encodingPacket.fecPayloadID());
                   stringBuilder.append(",");*/
                   try {
                       objectOutputStream.writeObject(encodingPacket.asSerializable());
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
                   // this.sqllitData.UpdateReceivedSymbolNum(this.stringBuilder,this.num);
                   SendToReceiveHandler(this.num, this.sum);
               }
               break;
           case DECODED:
               byte[] CompleteData = arrayDataDecoder.dataArray();
               try {
                   this.receiveFile.delete();
                   this.receiveFile.createNewFile();
                   FileOutputStream fos = new FileOutputStream(this.receiveFile);
                   fos.write(CompleteData);
                   fos.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
                sqllitData.Complete();
               this.sqllitData.CloseSqLiteDatabase();
               SendToUnzip(++this.num,this.sum);
               requireNonNull(Looper.myLooper()).quit();
               break;
           case DECODING_FAILURE://解析失败，生成端有误
               requireNonNull(Looper.myLooper()).quit();
               this.receiveFile.delete();
               break;

       }
    }
    private void decodeFirst(byte[] FirstRaptorData){
        EncodingPacket encodingPacket = null;
        encodingPacket = this.arrayDataDecoder.parsePacket(FirstRaptorData,12,FirstRaptorData.length-12, true).value();
        arrayDataDecoder.sourceBlock(encodingPacket.sourceBlockNumber()).putEncodingPacket(encodingPacket);
        check[encodingPacket.fecPayloadID()] = true;
        this.num++;
        /*stringBuilder.append(encodingPacket.fecPayloadID());
        stringBuilder.append(",");*/
        try {
            objectOutputStream.writeObject(encodingPacket.asSerializable());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //this.sqllitData.UpdateReceivedSymbolNum(this.stringBuilder,this.num);
        SendToReceiveHandler(this.num, this.sum);
        objectOutputStream = MyObjectStream.newInstance(this.receiveFile);
        encodingPacket = null;
    }
    private void  CompareWithList(List<FileInfo> list,byte[] FECParameters){
        //boolean hasFile = false;//标记数据库中是否能找到文件
        //byte[] FECParametersArray ;

        for (FileInfo fileInfo:list){
                if(FECParametersEquals(fileInfo.getFECParameters(), FECParameters)){
                    //数据库中有数据
                    sqllitData.PickFile(fileInfo.getID());
                    RestartDecode(fileInfo);
                    return;
                }
        }
        //数据库中没有数据
       // FileInfo receiveFileInfo = new FileInfo(receiveFile.getName());

        StartNewOne(FECParameters);

        //return false;
    }
    private boolean FECParametersEquals(byte[] a,byte[] b){
        for(int i = 0;i<12;i++){
            if(a[i]!=b[i])return false;
        }
        return true;
    }
    private void InsertNewFile(String fileInfo){
        sqllitData.InsertNewFile(fileInfo);

    }
    private void RestartDecode(FileInfo fileInfo){
        this.fecParameters = FECParameters.ExtractFECParameters(fileInfo.getFECParameters()).value();//耗时操作，应该再做一点别的东西
        this.receiveFile = new File(this.receivePath.getAbsolutePath()+"/"+fileInfo.getFileName());
        this.arrayDataDecoder = OpenRQ.newDecoderWithTwoOverhead(this.fecParameters);//终于加上了断点续传hhhh
        this.sum = this.fecParameters.totalSymbols();
        this.total = this.sum;
        this.sum += 2;
        this.num = fileInfo.getReceivedNum();
       // String ReceivedSymbol = fileInfo.getReceivedSymbolNum();
        //String[] ReceivedSymbolString = ReceivedSymbol.split(",",ReceivedSymbol.length()-1);
        //int length = ReceivedSymbolString.length;
        //int[] ReceivedSymbolint = new int[length];
        check = new boolean[(int) Math.ceil(this.total * 2)];
        /*for (String aReceivedSymbolString : ReceivedSymbolString) {
            if(!aReceivedSymbolString.equals("")){check[Integer.valueOf(aReceivedSymbolString)] = true;}
        }*/
        //stringBuilder = new StringBuilder(ReceivedSymbol);
        objectOutputStream = requireNonNull(MyObjectStream.newInstance(this.receiveFile));
        //this.sqllitData.UpdateFECParameters(fecParameters.asArray(), this.total * 2);
        LinkedBlockingQueue<SerializablePacket> linkedBlockingQueue = ReadObject(this.receiveFile);
        EncodingPacket Epackage = null;
        for(SerializablePacket pac: linkedBlockingQueue){
            Epackage = this.arrayDataDecoder.parsePacket(pac,true).value();
            arrayDataDecoder.sourceBlock(pac.sourceBlockNumber()).putEncodingPacket(Epackage);

            //SourceBlockDecoder sourceBlock = arrayDataDecoder.sourceBlock(encodingPacket.sourceBlockNumber());
            if(!check[Epackage.fecPayloadID()]) {
            check[Epackage.fecPayloadID()] = true;
            this.num++;
            }
        }
    }
    private void StartNewOne(byte[] fecParameters){
        this.fecParameters = FECParameters.ExtractFECParameters(fecParameters).value();//耗时操作，应该再做一点别的东西
        this.receiveFile = initReceiveFile(this.receivePath);
        InsertNewFile(receiveFile.getName());
        this.arrayDataDecoder = OpenRQ.newDecoderWithTwoOverhead(this.fecParameters);//终于加上了断点续传hhhh
        this.sum = this.fecParameters.totalSymbols();
        this.total = this.sum;
        this.sum += 2;
        check = new boolean[(int) Math.ceil(this.total * 2)];
       // stringBuilder = new StringBuilder();
        this.sqllitData.UpdateFECParameters(this.fecParameters.asArray(), this.total * 2);
        objectOutputStream = requireNonNull(MyObjectStream.newInstance(this.receiveFile));
        decodeFirst(fecParameters);
    }
    private File initReceiveFile(File ReceivePath){
        // Log.i(this.getClass().toString(),"启动");
        File receiveFile = new File(ReceivePath.getAbsolutePath()+"/tmp"+System.currentTimeMillis()+".tmp");//后续利用数据库加上端点续传
        if(!ReceivePath.exists()){
            try {
                ReceivePath.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return receiveFile;
    }
    private void SendToReceiveHandler(int CorrectNum, int sum){
        Message message = Message.obtain(receiveActivity.getReceiveHandler(), R.id.update_progress,CorrectNum,sum);
       // message.setData(bundle);
        message.sendToTarget();
    }
    private void SendToUnzip(int CorrectNum, int sum){//解压文件
        Bundle bundle = new Bundle();
        //ArrayList<String> stringArrayList= new ArrayList<>();
       // stringArrayList.add();
        //bundle.putStringArrayList(,stringArrayList);
        bundle.putString("FilePath",receiveFile.getAbsolutePath());
        Message message = Message.obtain(receiveActivity.getReceiveHandler(), R.id.finish,CorrectNum,sum);
        message.sendToTarget();

    }
    public  <T> LinkedBlockingQueue<T> ReadObject(File file) {


        LinkedBlockingQueue<T> arrayList= new LinkedBlockingQueue<>();

        if(file.exists()) {
            ObjectInputStream ois;
            try {
                FileInputStream fn = new FileInputStream(file);
                ois = new ObjectInputStream(fn);
                while (fn.available() > 0) {//代表文件还有内容
                    T p = (T) ois.readObject();//从流中读取对象

                    arrayList.put(p);
                }

                ois.close();//注意在循环外面关闭
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        }
        return arrayList;
    }

}

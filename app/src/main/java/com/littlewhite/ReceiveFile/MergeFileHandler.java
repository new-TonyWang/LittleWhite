package com.littlewhite.ReceiveFile;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.littlewhite.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;


public class MergeFileHandler extends Handler  {//不知道需不需要新建一个线程去执行seek和write
    //private Handler ReceiveHandler;
    private ReceiveActivity receiveActivity;
    private String FileName;
    private Boolean HasFilename = false;
    private RandomAccessFile ram;
    private boolean[] check = null;
   private  int length = 0;
    private File ReceiveFile;
    private long block = 0;//每个二维码容量
    private int  blocklengthdetect = 0;//因为大部分的qrcode长度都相同，只有最后一段长度短，只要比较任意两个qrode的长度，取最大值就行
    private int CorrectNum = 0;//已经完成拼接的数量，每完成一次就+1，直到和num的值相等
    private TestReceive testReceive;
    public MergeFileHandler(ReceiveActivity receiveActivity,File ReceiveFile) {
        Log.i(this.getClass().toString(),"启动");
       // this.ReceiveHandler = handler;
        this.receiveActivity = receiveActivity;
        this.ReceiveFile = ReceiveFile;
        try {
            this.ram = new RandomAccessFile(this.ReceiveFile,"rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        testReceive = new TestReceive(this.ReceiveFile.getParent(),ReceiveFile.getName());
    }
    @Override
    public void handleMessage(Message message) {
        switch (message.what){
            case R.id.Merge:
                MergeFile(message.obj,message.arg1,message.arg2);
                break;
            case R.id.stop:
                //保存信息
                break;
            default:
        }

    }
    private void MergeFile(Object result,int num,int sum){
        if(blocklengthdetect<2){
            if(check==null) {//前两张二维码必须保证完全无误
                check = new boolean[sum+1];//设置文件储存的标记数组
                this.length = sum+1;
            }
        if(result instanceof byte[]){
            byte[] data = (byte[]) result;
            block = block>=data.length?block:data.length;//循环两次，选择最大的长度作为block
        }else{//扫描到文件名
            if(!check[0]) {
                check[0] = true;
                this.FileName = (String) result;
            }
        }
            blocklengthdetect++;
        return;
        }
        if(result instanceof byte[]){//解析的是byte数组
            if((sum==length-1) && !check[num]){//从数量上判断是否为该序列的二维码，从一定程度上提升准确性
                check[num] = true;
                byte[] data = (byte[])result;
                GetFileFromQRCodes(data,this.ram,(num-1)*block);
                CorrectNum++;
                SendToReceiveHandler(CorrectNum,sum);
                testReceive.writelog(num,sum);//写入日志
            }
        }else {//解析的是文件名
            if((sum==length-1) && !check[0]){//从数量上判断是否为该序列的二维码，从一定程度上提升准确性
                check[0] = true;
                this.FileName = (String) result;
                CorrectNum++;
                SendToReceiveHandler(CorrectNum,sum);
                testReceive.writelog(num,sum);//写入日志
            }
        }

        if(CorrectNum==length) {
            testReceive.closewrite();
            closeMerge(this.CorrectNum,sum);
        }
    }

    /**
     * 从很多byte序列来生成文件
     * @param bfile
     * @param ram
     * @param start
     *
     */
    public void GetFileFromQRCodes(byte[] bfile, RandomAccessFile ram, Long start) {
        try {
            ram.seek(start);
            ram.write(bfile);
        } catch (IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

    }
    private void SendToReceiveHandler(int CorrectNum,int sum){
        Message message = Message.obtain(receiveActivity.getHandler(),R.id.update_progress,CorrectNum,sum);
        message.sendToTarget();

    }
    private void closeMerge(int CorrectNum,int sum){
        try {
            this.ram.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ReceiveFile.renameTo(new File(ReceiveFile.getParent()+"/"+this.FileName));
        Message message = Message.obtain(receiveActivity.getHandler(),R.id.finish,CorrectNum,sum);
        message.sendToTarget();
    }

}

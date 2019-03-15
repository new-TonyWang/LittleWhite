package com.littlewhite.ReceiveFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 此类用于输出解析的二维码编号和时间关系的日志
 */
public class TestReceive {
    private RandomAccessFile randomAccessFile;
    private long starttime;
    public TestReceive(String downloadpath,String timestamp){
        File log = new File(downloadpath+"/log"+timestamp+".txt");
        if(log.exists()){
            log.delete();
        }
        try {
            log.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.randomAccessFile = new RandomAccessFile(log,"rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        starttime = System.currentTimeMillis();
    }
    public void writelog(int num,int sum){
        long currenttime = System.currentTimeMillis();
        try {
            randomAccessFile.seek(randomAccessFile.length());
            randomAccessFile.writeUTF(num+" "+(currenttime-starttime)+"\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void closewrite(){
        try {
            this.randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

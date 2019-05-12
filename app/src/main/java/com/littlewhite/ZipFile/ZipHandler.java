package com.littlewhite.ZipFile;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.littlewhite.R;
import com.littlewhite.ReceiveFile.SqllitUtil.SqllitData;
import com.littlewhite.SendFile.SendConfigs;
import com.littlewhite.SendReceive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipHandler extends Handler {
    private static int BLACK_WHITE = 0;
    private static int COLOR = 1;
    private SendReceive sendReceive;
    private SqllitData sqllitData;
    private File zFile;
   // private File Zipfile;
    //private ZipUtil zipUtil= new ZipUtil();
   // private Handler handler;
    public ZipHandler(SendReceive sendReceive,SqllitData sqllitData){
        this.sendReceive =sendReceive;
        this.sqllitData = sqllitData;
       // this.handler = handler;
    }
    @Override
    public void handleMessage(Message message) {
        Bundle bundle= message.getData();
        switch (message.what) {
            case R.id.ZipFile:
                SendConfigs file = (SendConfigs)message.obj;
                this.zFile =  ZipFile(file);
                break;
            case R.id.UnZipFile:
               // Bundle bundle= message.getData();
                String unzipfile = bundle.getString("FilePath");
                UnZipFile(unzipfile);
                File deleteFile = new File(unzipfile);
                deleteFile.delete();
                break;
            case R.id.finish:
                this.zFile.delete();

                Looper.myLooper().quit();
                sendFinishMessage();
                break;
            case R.id.failed:
                Looper.myLooper().quit();
                break;
            default:

        }
    }

    /**
     * 解压
      * @param FilePath
     */
    private  void UnZipFile(String FilePath){
           String Correct =  unzip(FilePath);
           if(Correct != null){
               sqllitData.Changename(Correct);
               sqllitData.CloseSqLiteDatabase();
               Message message= Message.obtain((Handler) sendReceive.getHandler());
               message.what = R.id.finish;
               message.obj = Correct;
               message.sendToTarget();
           }else{
            Message.obtain((Handler) sendReceive.getHandler(),R.id.failed);
           }
        }

    /**
     * 压缩
     * @param
     */
    private  File ZipFile(SendConfigs File){
        File zipDirectoryFile =  zipDirectory(File.getPath());
        if(!Objects.equals(zipDirectoryFile, null)){
            Message message = Message.obtain((Handler) sendReceive.getHandler());
            message.obj = zipDirectoryFile;
            if(File.getQRCodeType()==BLACK_WHITE) {
                message.what = R.id.Encode;
            }
            else if(File.getQRCodeType()==COLOR){
                message.what = R.id.EncodeColor;
            }
            message.sendToTarget();
            return  zipDirectoryFile;
        }else{
            Message message = Message.obtain((Handler) sendReceive.getHandler());
           // message.obj = zipDirectoryFile;
            message.what = R.id.failed;
            message.sendToTarget();
            return null;
        }
    }
    private void sendFinishMessage(){
        Message message = obtainMessage(R.id.finish);
        Handler handler =  (Handler)this.sendReceive.getHandler();
        handler.sendMessageAtFrontOfQueue(message);
    }
    /**
     * 压缩一个文件夹
     *
     * @throws IOException
     */
    private File zipDirectory(String path) {
        File file = new File(path);
        String parent = file.getParent();
        File zipFile = new File(parent, file.getName() + ".zip");
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(zipFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return  null;
        }
        try {
            zip(zos, file, file.getName());
        } catch (IOException e) {
            e.printStackTrace();
            return  null;
        }
        try {
            zos.flush();
            zos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return  null;
        }

        return zipFile;
    }

    /**
     *
     * @param zos
     *            压缩输出流
     * @param file
     *            当前需要压缩的文件
     * @param path
     *            当前文件相对于压缩文件夹的路径
     * @throws IOException
     */
    private void zip(ZipOutputStream zos, File file, String path) throws IOException {
        // 首先判断是文件，还是文件夹，文件直接写入目录进入点，文件夹则遍历
        if (file.isDirectory()) {
            ZipEntry entry = new ZipEntry(path + File.separator);// 文件夹的目录进入点必须以名称分隔符结尾
            zos.putNextEntry(entry);
            File[] files = file.listFiles();
            for (File x : files) {
                zip(zos, x, path + File.separator + x.getName());
            }
        } else {
            FileInputStream fis = new FileInputStream(file);// 目录进入点的名字是文件在压缩文件中的路径
            ZipEntry entry = new ZipEntry(path);
            zos.putNextEntry(entry);// 建立一个目录进入点

            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = fis.read(buf)) != -1) {
                zos.write(buf, 0, len);
            }
            zos.flush();
            fis.close();
            zos.closeEntry();// 关闭当前目录进入点，将输入流移动下一个目录进入点
        }
    }
    /**
     * 解压文件
     *
     * @param unzip
     * @throws IOException
     */
    private String unzip(String unzip){
        File file = new File(unzip);
        String  basePath = file.getParent();
        FileInputStream fis = null;
        String unZipName = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        ZipInputStream zis = new ZipInputStream(fis);
        try {
            unZipName =  unzip(zis,basePath);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return unZipName;
    }

    private String  unzip(ZipInputStream zis,String basepath) throws IOException {
        ZipEntry entry = zis.getNextEntry();
        File file = null;
        String unZipName = null;
        if (entry != null) {
            unZipName = entry.getName();
            file = new File(basepath + File.separator + unZipName);

            if (file.isDirectory()) {
                // 可能存在空文件夹
                if (!file.exists())
                    file.mkdirs();
                unzip(zis,basepath);
            } else {
                File parentFile = file.getParentFile();
                if (parentFile != null && !parentFile.exists())
                    parentFile.mkdirs();
                FileOutputStream fos = new FileOutputStream(file);// 输出流创建文件时必须保证父路径存在
                int len = 0;
                byte[] buf = new byte[1024];
                while ((len = zis.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                }
                fos.flush();
                fos.close();
                zis.closeEntry();
                unzip(zis,basepath);
            }
        }
        return unZipName;
    }


}

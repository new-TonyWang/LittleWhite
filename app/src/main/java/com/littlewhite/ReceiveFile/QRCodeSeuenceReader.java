package com.littlewhite.ReceiveFile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.ColorYUV;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotDataException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.common.CBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import static com.littlewhite.SendFile.AlbumNotifier.TAG;

public class QRCodeSeuenceReader implements Runnable{
    String dirPath;
    List<File> datalist;
    StringBuilder decoderesults = new StringBuilder();
    QRCodeReader reader = new QRCodeReader();
    HashMap<DecodeHintType,Object> hints = new HashMap<>();
    public QRCodeSeuenceReader(String dirPath) {
        this.dirPath = dirPath;
        datalist =new ArrayList<>();
        File file=new File(dirPath);
        hints.put(DecodeHintType.TRY_HARDER,true);
        if(file.exists())

        {

            File[] file1=file.listFiles();

            for (File filename :

                    file1) {

                datalist.add(filename);

            }

        }

    }



    public void run(){
        Result result= null;
        for(File img: datalist){
            if(!img.getAbsolutePath().toLowerCase().endsWith(".jpg")){
                continue;
            }
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(img.getAbsolutePath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap bitmap  = BitmapFactory.decodeStream(fis);
            //Bitmap bitmap = BitmapFactory.(img.getAbsolutePath());
            int[] RGB = new int[bitmap.getHeight()*bitmap.getWidth()];
                    bitmap.getPixels(RGB,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
            RGBLuminanceSource rgbLuminanceSource = new RGBLuminanceSource(bitmap.getWidth(),bitmap.getHeight(),RGB);

            // PlanarYUVLuminanceSource source = receiveActivity.getCameraManager().buildPlanarYUVSource(YUV, width, height);
                BinaryBitmap binmap = new BinaryBitmap(new HybridBinarizer(rgbLuminanceSource));
                try {
                    String name = img.getName();
                    result = reader.decode(binmap,this.hints);

                    decoderesults.append(name);
                    decoderesults.append("--");
                    decoderesults.append(result.getText());
                    decoderesults.append("\r");
                    Log.i(name,"   "+decoderesults.toString());
                    decoderesults.reverse();
                    String parent = img.getParent();

                    img.renameTo(new File(parent + "/" + "a0" + name));

                } catch (NotFoundException e) {
                    String parent = img.getParent();
                    String name = img.getName();
                    img.renameTo(new File(parent + "/" + "not未找到二维码" + name));
                     Log.i(TAG,"未找到二维码");
                    continue;//解析不到的时候跳出该方法
                } catch (FormatException e) {
                    String parent = img.getParent();
                    String name = img.getName();
                    img.renameTo(new File(parent + "/" + "not格式错误" + name));
                    Log.i(TAG,"格式错误");
                    e.printStackTrace();
                } catch (ChecksumException e) {
                    String parent = img.getParent();
                    String name = img.getName();
                    img.renameTo(new File(parent + "/" + "not校验错误" + name));
                    Log.i(TAG,"校验错误");
                }

        }
        }



}

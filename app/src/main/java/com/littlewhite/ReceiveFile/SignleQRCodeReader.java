package com.littlewhite.ReceiveFile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.littlewhite.SendFile.AlbumNotifier.TAG;

public class SignleQRCodeReader implements Runnable{
    String Path;
    File img;
    StringBuilder decoderesults = new StringBuilder();
    QRCodeReader reader = new QRCodeReader();
    HashMap<DecodeHintType,Object> hints = new HashMap<>();
    public SignleQRCodeReader(String dirPath) {
        this.Path = dirPath;
         img=new File(dirPath);
        hints.put(DecodeHintType.TRY_HARDER,true);


    }



    public void run(){
        Result result= null;
            if(!img.getAbsolutePath().toLowerCase().endsWith(".jpg")){
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

package com.littlewhite.ReceiveFile.QRcodeDecoder;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.zxing.PlanarYUVLuminanceSource;
import com.littlewhite.R;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class OutPutGrayHandler extends Handler {



    @Override
    public void handleMessage(Message message){
        switch (message.what){
            case R.id.outputgray:
                Bundle bundle = message.getData();
                bundleThumbnail((PlanarYUVLuminanceSource) message.obj,(String)bundle.getCharSequence("reason"),bundle.getInt("imagecount"));
                break;
            case R.id.finish:
                Looper.myLooper().quit();
                break;
        }

    }
    private static void bundleThumbnail(PlanarYUVLuminanceSource source,String reason,int imagecount) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
       // ByteArrayOutputStream out = new ByteArrayOutputStream();
       // bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        BufferedOutputStream bos = null;
        File file = new File(String.valueOf(Environment.getExternalStoragePublicDirectory("errorQRcodes")));
        if(!file.exists()||!file.isDirectory()){
            file.mkdir();
        }
        file = new File(String.format("%s/%04d%s.jpg",String.valueOf(Environment.getExternalStoragePublicDirectory("errorQRcodes")),imagecount,reason));
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        //bundle.putByteArray(DecodeThread.BARCODE_BITMAP, out.toByteArray());
        //bundle.putFloat(DecodeThread.BARCODE_SCALED_FACTOR, (float) width / source.getWidth());
    }
}

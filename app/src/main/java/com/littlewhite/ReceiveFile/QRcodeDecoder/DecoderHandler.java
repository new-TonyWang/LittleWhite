package com.littlewhite.ReceiveFile.QRcodeDecoder;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.ColorYUV;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.common.CBinarizer;
import com.google.zxing.common.HybridBinarizer;

import com.google.zxing.qrcode.QRCodeReader;
import com.littlewhite.ColorCode.HSVColorTable;
import com.littlewhite.R;
import com.littlewhite.ReceiveFile.ReceiveActivity;

import java.util.HashMap;
import java.util.List;

public class DecoderHandler extends Handler {
    private static final String TAG = DecoderHandler.class.getSimpleName();
    private HashMap<DecodeHintType,Object> hints;
    private QRCodeReader reader;
    private Handler RaptorQDecodeHandler;
    private ReceiveActivity receiveActivity;
    private HSVColorTable  colorTable;
    public DecoderHandler(HashMap<DecodeHintType,Object> hints, Handler RaptorQDecodeHandler, ReceiveActivity receiveActivity,HSVColorTable  colorTable){
       // Log.i(this.getClass().toString(),"启动");
        this.hints = hints;
        this.RaptorQDecodeHandler = RaptorQDecodeHandler;
        this.receiveActivity = receiveActivity;
        this.colorTable = colorTable;
        this.reader = new QRCodeReader();
    }
    @Override
    public void handleMessage(Message message) {
        switch (message.what){
            case R.id.decode:
                //long start = System.currentTimeMillis();
                    decode((byte[])message.obj,message.arg1,message.arg2);
                //long end = System.currentTimeMillis();

                //System.out.println("彩色二维码解析时长" + (end - start) + "ms");
                break;
            case R.id.decodeColor:
               // long start = System.currentTimeMillis();
                decodeColor((byte[])message.obj,message.arg1,message.arg2);
               // long end = System.currentTimeMillis();
               // System.out.println("彩色二维码解析时长" + (end - start) + "ms");
                break;
            case R.id.decodeRGB:
                // long start = System.currentTimeMillis();
                decodeRGB((byte[])message.obj,message.arg1,message.arg2);
                // long end = System.currentTimeMillis();
                // System.out.println("彩色二维码解析时长" + (end - start) + "ms");
                break;
            case R.id.finish:
              Message finish =  obtainMessage(R.id.finish);
                this.RaptorQDecodeHandler.sendMessageAtFrontOfQueue(finish);
               // message;
                Looper.myLooper().quit();
                reader.reset();
                break;
            case R.id.stop:
                //保存信息
                Looper.myLooper().quit();
        }
    }
    private void decode(byte[] YUV,int width,int height)  {
        Result result = null;

        PlanarYUVLuminanceSource source = receiveActivity.getCameraManager().buildPlanarYUVSource(YUV, width, height);

       // PlanarYUVLuminanceSource source = receiveActivity.getCameraManager().buildPlanarYUVSource(YUV, width, height);

        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                result = reader.decodetobytearry(bitmap, this.hints);
            } catch (ReaderException e) {
               // Log.i(TAG,"解析失败");
                return;//解析不到的时候跳出该方法

            }
            sendToRaptorDecoder(((List<byte[]>) result.getResultMetadata().get(ResultMetadataType.BYTE_SEGMENTS)).get(0));

        }

    }
    private void decodeColor(byte[] YUV,int width,int height)  {
        Result result= null;

        ColorYUV source = receiveActivity.getCameraManager().buildColorYUVSource(YUV, width, height);

        // PlanarYUVLuminanceSource source = receiveActivity.getCameraManager().buildPlanarYUVSource(YUV, width, height);

        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new CBinarizer(source));
            try {
                result = reader.decodecolorCode(bitmap, this.hints,colorTable);
            } catch (ReaderException e) {
                // Log.i(TAG,"解析失败");
                return;//解析不到的时候跳出该方法
            }
            sendToRaptorDecoder(((List<byte[]>) result.getResultMetadata().get(ResultMetadataType.BYTE_SEGMENTS)).get(0));
        }
    }
    private void decodeRGB(byte[] YUV,int width,int height)  {
        Result result= null;

        ColorYUV source = receiveActivity.getCameraManager().buildColorYUVSource(YUV, width, height);

        // PlanarYUVLuminanceSource source = receiveActivity.getCameraManager().buildPlanarYUVSource(YUV, width, height);

        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new CBinarizer(source));
            try {
                result = reader.decodeRGBCode(bitmap, this.hints);
            } catch (ReaderException e) {
                // Log.i(TAG,"解析失败");
                return;//解析不到的时候跳出该方法
            }
            sendToRaptorDecoder(((List<byte[]>) result.getResultMetadata().get(ResultMetadataType.BYTE_SEGMENTS)).get(0));
        }
    }
    private void sendToRaptorDecoder(byte[] result){
        //Log.i(TAG,"解析成功"+(++i)+"个");
        Message message = Message.obtain(this.RaptorQDecodeHandler,R.id.decode,result);
        message.sendToTarget();
    }
}

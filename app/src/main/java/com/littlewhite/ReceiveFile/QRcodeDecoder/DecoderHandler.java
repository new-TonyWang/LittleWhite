package com.littlewhite.ReceiveFile.QRcodeDecoder;

import android.os.Bundle;
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
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.CBinarizer;
import com.google.zxing.common.HybridBinarizer;

import com.google.zxing.common.RGBData;
import com.google.zxing.qrcode.QRCodeReader;
import com.littlewhite.ColorCode.HSVColorTable;
import com.littlewhite.R;
import com.littlewhite.ReceiveFile.ColorCode.Bbinary;
import com.littlewhite.ReceiveFile.ColorCode.Gbinary;
import com.littlewhite.ReceiveFile.ColorCode.RGBChannel;
import com.littlewhite.ReceiveFile.ColorCode.Rbinary;
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
    private Rbinary rbinary;
    private OutPutGray outPutGray;
    private int iamgecount = 0;//用于记录解析成功和错误的总数
    private boolean startcapture = false;
    private Gbinary gbinary;
    private Bbinary bbinary;
    private static final int BLOCK_SIZE_POWER = 3;
    private static final int BLOCK_SIZE = 1 << BLOCK_SIZE_POWER; // ...0100...00，1往左移动3位==8
    private static final int BLOCK_SIZE_MASK = BLOCK_SIZE - 1;   // ...0011...11，==7
    private static final int MINIMUM_DIMENSION = BLOCK_SIZE * 5;//==40
    private static final int MIN_DYNAMIC_RANGE = 24;//
    static {
        System.loadLibrary("cbinarizer");
    }
    public DecoderHandler(HashMap<DecodeHintType,Object> hints, OutPutGray outPutGray, ReceiveActivity receiveActivity,HSVColorTable  colorTable){
        // Log.i(this.getClass().toString(),"启动");
        this.hints = hints;
        this.outPutGray = outPutGray;
        //this.RaptorQDecodeHandler = RaptorQDecodeHandler;
        this.receiveActivity = receiveActivity;
        this.colorTable = colorTable;
        this.reader = new QRCodeReader();
    }
    public DecoderHandler(HashMap<DecodeHintType,Object> hints, Handler RaptorQDecodeHandler, ReceiveActivity receiveActivity,HSVColorTable  colorTable){
       // Log.i(this.getClass().toString(),"启动");
        this.hints = hints;
        this.RaptorQDecodeHandler = RaptorQDecodeHandler;
        this.receiveActivity = receiveActivity;
        this.colorTable = colorTable;
        this.reader = new QRCodeReader();
    }
    public DecoderHandler(HashMap<DecodeHintType, Object> hints, Handler raptorQDecodeHandler,
                          ReceiveActivity receiveActivity, HSVColorTable colorTable, Rbinary rbinary, Gbinary gbinary, Bbinary bbinary,OutPutGray outPutGray) {
        this.hints = hints;
        this.reader = new QRCodeReader();
        RaptorQDecodeHandler = raptorQDecodeHandler;
        this.receiveActivity = receiveActivity;
        this.colorTable = colorTable;
        this.rbinary = rbinary;
        this.gbinary = gbinary;
        this.bbinary = bbinary;
        this.outPutGray = outPutGray;
    }
    @Override
    public void handleMessage(Message message) {
        switch (message.what){
            case R.id.decode://解析黑白
                long start = System.currentTimeMillis();
                    decode((byte[])message.obj,message.arg1,message.arg2);
                long end = System.currentTimeMillis();

                System.out.println("二维码解析时长" + (end - start) + "ms");
                break;
            case R.id.decodeColor://解析HSV
               // long start = System.currentTimeMillis();
                decodeColor((byte[])message.obj,message.arg1,message.arg2);
               // long end = System.currentTimeMillis();
               // System.out.println("彩色二维码解析时长" + (end - start) + "ms");
                break;
            case R.id.decodeRGB://完成YUV-RGB的格式转换，然后再将RGB三东通道数据分别发给三个二值化的线程
                 //long start = System.currentTimeMillis();
                decodeRGB((byte[])message.obj,message.arg1,message.arg2);
                 //long end = System.currentTimeMillis();
                 //System.out.println("彩色二维码解析时长" + (end - start) + "ms");
                break;
            case R.id.finish:
              Message finish =  obtainMessage(R.id.finish);
                this.RaptorQDecodeHandler.sendMessageAtFrontOfQueue(finish);
                this.rbinary.getHandler().sendMessageAtFrontOfQueue(finish);
                this.gbinary.getHandler().sendMessageAtFrontOfQueue(finish);
                this.bbinary.getHandler().sendMessageAtFrontOfQueue(finish);
               // message;
                Looper.myLooper().quit();
                reader.reset();
                break;
            case R.id.decodepureQRCode:
                decodepureQR((byte[])message.obj,message.arg1,message.arg2);
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
            } catch (ChecksumException e) {
                Log.i(TAG,"校验失败");
                return;//解析不到的时候跳出该方法

            } catch (FormatException e) {
                Log.i(TAG,"格式错误");
                return;//解析不到的时候跳出该方法
            } catch (NotFoundException e) {
                Log.i(TAG,"未找到二维码");
                return;//解析不到的时候跳出该方法
            }
            sendToRaptorDecoder(((List<byte[]>) result.getResultMetadata().get(ResultMetadataType.BYTE_SEGMENTS)).get(0));

        }

    }

    /**
     * 解析标准协议的二维码
     * @param Y
     * @param width
     * @param height
     */
    private void decodepureQR(byte[] Y,int width,int height)  {
        Result result = null;

        PlanarYUVLuminanceSource source = receiveActivity.getCameraManager().buildPlanarYUVSource(Y, width, height);

        // PlanarYUVLuminanceSource source = receiveActivity.getCameraManager().buildPlanarYUVSource(YUV, width, height);

        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                result = reader.decode(bitmap);
                Log.i("结果：",result.getText());

                //sendToOutputGrayerror(source,"成功",++this.iamgecount);
            } catch (ChecksumException e) {
                Log.i(TAG,"校验失败");
                //sendToOutputGrayerror(source,"校验失败",++this.iamgecount);
                return;//解析不到的时候跳出该方法
            } catch (FormatException e) {
                Log.i(TAG,"格式错误");
                //sendToOutputGrayerror(source,"格式错误",++this.iamgecount);
                return;//解析不到的时候跳出该方法
            } catch (NotFoundException e) {
                if (startcapture) {
                    //sendToOutputGrayerror(source, "未找到二维码", ++this.iamgecount);
                    Log.i(TAG, "未找到二维码");
                    return;//解析不到的时候跳出该方法
                }
            }
            //sendToRaptorDecoder(((List<byte[]>) result.getResultMetadata().get(ResultMetadataType.BYTE_SEGMENTS)).get(0));

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

    /**
     * 解析RGB二维码
     * @param YUV
     * @param width
     * @param height
     */
    private void decodeRGB(byte[] YUV,int width,int height)  {
        //Result result= null;

        ColorYUV source = receiveActivity.getCameraManager().buildColorYUVSource(YUV, width, height);

        // PlanarYUVLuminanceSource source = receiveActivity.getCameraManager().buildPlanarYUVSource(YUV, width, height);

        if (source != null) {
            //BinaryBitmap bitmap = new BinaryBitmap(new CBinarizer(source));
            int subWidth = width >> BLOCK_SIZE_POWER;
            if ((width & BLOCK_SIZE_MASK) != 0) {
                subWidth++;
            }
            int subHeight = height >> BLOCK_SIZE_POWER;
            if ((height & BLOCK_SIZE_MASK) != 0) {
                subHeight++;
            }

                byte[] uv = source.getUVMatrix();//获取UV通道
                byte[] luminances = source.getMatrix();//获取Y通道
                byte[] RC = new byte[luminances.length];
                byte[] GC = new byte[luminances.length];
                byte[] BC = new byte[luminances.length];
                convertToRGB(luminances, uv, RC, GC, BC, source.getWidth(), source.getHeight());//格式转换
                long time = System.currentTimeMillis();
                RGBChannel channelR = new RGBChannel(RC,R.id.Convert_R,source.getWidth(),source.getHeight(),time);
                RGBChannel channelG = new RGBChannel(GC,R.id.Convert_G,source.getWidth(),source.getHeight(),time);
                RGBChannel channelB = new RGBChannel(BC,R.id.Convert_B,source.getWidth(),source.getHeight(),time);
                Message messageR = Message.obtain(this.rbinary.getHandler(),R.id.Binarize,subWidth,subHeight,channelR);
                messageR.sendToTarget();
            Message messageG = Message.obtain(this.gbinary.getHandler(),R.id.Binarize,subWidth,subHeight,channelG);
            messageG.sendToTarget();
            Message messageB = Message.obtain(this.bbinary.getHandler(),R.id.Binarize,subWidth,subHeight,channelB);
            messageB.sendToTarget();
                // luminances = calculateY(luminances, subWidth, subHeight, width, height);
                //outputmatrix(luminances,uv,width,height,true,Math.random());
                //outputRGB(RGB,width,height,"rgb");
               // RGBData rgbData= bitmap.getRGBData();
                //result = reader.decodeRGBCode(rgbData, this.hints);

            //sendToRaptorDecoder(((List<byte[]>) result.getResultMetadata().get(ResultMetadataType.BYTE_SEGMENTS)).get(0));
        }
    }
    private void sendToRaptorDecoder(byte[] result){
        //Log.i(TAG,"解析成功"+(++i)+"个");
        Message message = Message.obtain(this.RaptorQDecodeHandler,R.id.decode,result);
        message.sendToTarget();
    }
    private void sendToOutputGrayerror(PlanarYUVLuminanceSource source,String reason,int imagecount){
        //Log.i(TAG,"解析成功"+(++i)+"个");
        if(!startcapture){
            startcapture = true;
            return;
        }
        Bundle bundle= new Bundle();
        bundle.putCharSequence("reason",reason);
        bundle.putInt("imagecount",imagecount);
        Message message = Message.obtain(this.outPutGray.getHandler(),R.id.outputgray,source);
        message.setData(bundle);
        message.sendToTarget();
    }
    private native int convertToRGB(byte[] luminances, byte[] uv,
                                    byte[] R,
                                    byte[] G,
                                    byte[] B,
                                    int width,
                                    int height);
}

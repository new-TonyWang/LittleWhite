package com.littlewhite.ReceiveFile.QRcodeDecoder;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.common.HybridBinarizer;

import com.google.zxing.qrcode.QRCodeReader;
import com.littlewhite.R;
import com.littlewhite.ReceiveFile.ReceiveActivity;

import java.util.HashMap;
import java.util.List;

public class DecoderHandler extends Handler {
    private static final String TAG = MultiDecoderHandler.class.getSimpleName();
    private HashMap<DecodeHintType,Object> hints;
    private QRCodeReader reader;
    private Handler RaptorQDecodeHandler;
    private ReceiveActivity receiveActivity;
    int i;
    public DecoderHandler(HashMap<DecodeHintType,Object> hints, Handler RaptorQDecodeHandler, ReceiveActivity receiveActivity){
        Log.i(this.getClass().toString(),"启动");
        this.hints = hints;
        this.RaptorQDecodeHandler = RaptorQDecodeHandler;
        this.receiveActivity = receiveActivity;
        this.reader = new QRCodeReader();
    }
    @Override
    public void handleMessage(Message message) {
        switch (message.what){
            case R.id.decode:
                    decode((byte[])message.obj,message.arg1,message.arg2);
                break;
            case R.id.finish:
                Looper.myLooper().quit();
                reader.reset();
                break;
            case R.id.stop:
                //保存信息
        }
    }
    private void decode(byte[] YUV,int width,int height)  {
        Result result = null;
        PlanarYUVLuminanceSource source = receiveActivity.getCameraManager().buildLuminanceSource(YUV, width, height);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                result = reader.decodetobytearry(bitmap, this.hints);
            } catch (ReaderException e) {
                return;//解析不到的时候跳出该方法
            }
            sendToRaptorDecoder(((List<byte[]>) result.getResultMetadata().get(ResultMetadataType.BYTE_SEGMENTS)).get(0));

        }

    }
    private void sendToRaptorDecoder(byte[] result){
        Log.i(TAG,"解析成功"+(++i)+"个");
        Message message = Message.obtain(this.RaptorQDecodeHandler,R.id.decode,result);
        message.sendToTarget();
    }
}

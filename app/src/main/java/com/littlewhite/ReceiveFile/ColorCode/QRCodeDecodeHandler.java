package com.littlewhite.ReceiveFile.ColorCode;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.common.RGBData;
import com.google.zxing.qrcode.QRCodeReader;
import com.littlewhite.R;
import com.littlewhite.ReceiveFile.ReceiveActivity;

import java.util.HashMap;
import java.util.List;

import static com.littlewhite.SendFile.AlbumNotifier.TAG;

public class QRCodeDecodeHandler extends Handler {
    private Handler raptorQDecoderHandler;
    private HashMap<DecodeHintType, Object> decodeHints = new HashMap<DecodeHintType, Object>();
    private ReceiveActivity activity;
    private QRCodeHashMap qrCodeHashMap;

    public QRCodeDecodeHandler(Handler raptorQDecoderHandler, ReceiveActivity activity) {
        this.raptorQDecoderHandler = raptorQDecoderHandler;
        this.activity = activity;
        this.qrCodeHashMap = new QRCodeHashMap();
        setHints();
    }
    private void setHints(){
        //decodeHints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
        //decodeHints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.);
        decodeHints.put(DecodeHintType.FILEDATA,Boolean.TRUE);
    }
    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.decode:
            RGBData rgbData = this.qrCodeHashMap.addNewNode((RGBmatrixChannel) message.obj);//由于R，G，B三个线程完成二值化的顺序不一定相同所以这里起到了线程同步的作用
            Result result = null;
            if (rgbData != null) {
                QRCodeReader reader = new QRCodeReader();
                try {
                    result = reader.decodeRGBCode(rgbData, this.decodeHints);//解析二维码
                } catch (Exception e) {
                   // Log.e(TAG, "解析失败");
                    return;
                }
                sendToRaptorDecoder(((List<byte[]>) result.getResultMetadata().get(ResultMetadataType.BYTE_SEGMENTS)).get(0));//传给raptorQ进行纠错
            }
            break;
            case R.id.finish:
                Looper.myLooper().quit();
        }
    }
    private void sendToRaptorDecoder(byte[] result){
        //Log.i(TAG,"解析成功"+(++i)+"个");
        Message message = Message.obtain(this.raptorQDecoderHandler,R.id.decode,result);
        message.sendToTarget();
    }


}

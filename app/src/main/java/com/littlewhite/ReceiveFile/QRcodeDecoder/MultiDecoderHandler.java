package com.littlewhite.ReceiveFile.QRcodeDecoder;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.littlewhite.R;
import com.littlewhite.ReceiveFile.ReceiveActivity;

import java.util.HashMap;
import java.util.List;

public class MultiDecoderHandler extends Handler {
    private static final String TAG = MultiDecoderHandler.class.getSimpleName();
    private HashMap<DecodeHintType,Object> hints;
    private QRCodeMultiReader reader;
    private Handler MergeFileHandler;
    private ReceiveActivity receiveActivity;
    int i;
    public MultiDecoderHandler(HashMap<DecodeHintType,Object> hints, Handler MergeFileHandler, ReceiveActivity receiveActivity){
        Log.i(this.getClass().toString(),"启动");
        this.hints = hints;
        this.MergeFileHandler = MergeFileHandler;
        this.receiveActivity = receiveActivity;
        this.reader = new QRCodeMultiReader();
    }
    @Override
    public void handleMessage(Message message) {
    switch (message.what){
        case R.id.decode:
            try {
                decode((byte[])message.obj,message.arg1,message.arg2);
            } catch (FormatException e) {
                e.printStackTrace();
            }
            break;
        case R.id.finish:
            Looper.myLooper().quit();
            reader.reset();
            System.gc();
            break;
        case R.id.stop:
            //保存信息
    }
    }
    private void decode(byte[] YUV,int width,int height) throws FormatException {
        Result[] results = null;
        PlanarYUVLuminanceSource source = receiveActivity.getCameraManager().buildLuminanceSource(YUV, width, height);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                results = reader.decodeMultiple(bitmap, this.hints);
            } catch (ReaderException re) {
                return;
            }
            if (results.length !=2) {
                return;
            }
            //byte[] data0 = ((List<byte[]>)results[0].getResultMetadata().get(ResultMetadataType.BYTE_SEGMENTS)).get(0);//一般情况为数据
            //byte[] data1 = ((List<byte[]>)results[1].getResultMetadata().get(ResultMetadataType.BYTE_SEGMENTS)).get(1);//一般情况为编号
            int indexof = 0;//0位为num,1位位sum
            String indexs = null;
            Object data = null;
            //String filename = null;
            switch (results[0].getMode()) {
                case DATA:
                    indexs = results[1].getText();
                    data = ((List<byte[]>) results[0].getResultMetadata().get(ResultMetadataType.BYTE_SEGMENTS)).get(0);
                    //resultCargo = new Cargo(data);
                    break;
                case ALPHANUMERIC:
                    indexs = results[0].getText();
                    data = (byte[]) ((List<byte[]>) results[1].getResultMetadata().get(ResultMetadataType.BYTE_SEGMENTS)).get(0);
                    //resultCargo = new Cargo(data);
                    break;
                case BYTE:
                    indexs = results[1].getText();
                    data = (String) results[0].getText();
                    //resultCargo = new Cargo(filename);
                    break;
                default:
                    throw FormatException.getFormatInstance();

            }
            indexof = indexs.indexOf("/");
            // int[] index = { ,)};
            try {
                sendToMerge(data, Integer.parseInt(indexs.substring(0, indexof)), Integer.parseInt(indexs.substring(indexof + 1)));
                //Log.i(this.getClass().toString(),"num:"+Integer.parseInt(indexs.substring(0, indexof))+"sum"+Integer.parseInt(indexs.substring(indexof + 1)));
            }catch (Exception e){

            }
        }

    }
    private void sendToMerge(Object result,int num,int sum){
        Log.i(TAG,"解析成功"+(++i)+"个");
            Message message = Message.obtain(this.MergeFileHandler,R.id.Merge,num,sum,result);
            message.sendToTarget();
    }
}

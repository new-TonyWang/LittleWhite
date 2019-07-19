package com.littlewhite.SendFile;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.littlewhite.R;

import net.fec.openrq.EncodingPacket;
import net.fec.openrq.OpenRQ;
import net.fec.openrq.encoder.DataEncoder;
import net.fec.openrq.encoder.SourceBlockEncoder;
import net.fec.openrq.parameters.FECParameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;

import static net.fec.openrq.parameters.ParameterChecker.minAllowedNumSourceBlocks;
import static android.content.ContentValues.TAG;

public class RaptorEncoderHandler extends Handler {

    private SendFileActivity sendFileActivity;
    private Hashtable<EncodeHintType, Object> encodeHints = new Hashtable<EncodeHintType, Object>();
    private DataEncoder arrayDataEncoder;
    private FFMPEGHandler ffmpegHandler;
    // private LinkedBlockingQueue<byte[]> linkedBlockingQueue;
    private FECParameters fecParameters;
    private String QRCodeDir;
    private String parentDir ;
    private Boolean isrunning;
    private SendConfigs sendConfigs;
   // private File encodeFile;
    public RaptorEncoderHandler(SendFileActivity sendFileActivity,FFMPEGHandler ffmpegHandler) {
        this.sendFileActivity = sendFileActivity;
        this.ffmpegHandler = ffmpegHandler;
    }
    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.Init:
                this.sendConfigs = (SendConfigs) message.obj;
                initEncode();
                break;
            case R.id.Encode:
                this.sendFileActivity.getSendFileHandler().sendEmptyMessage(R.id.RaptorEncodePhase);
                if(encodeData((File)message.obj)) {
                    EncodeComplete();
                }else {
                    Log.e("raptor","出错");
                }
                break;
            case R.id.EncodeHSV:
                this.sendFileActivity.getSendFileHandler().sendEmptyMessage(R.id.RaptorEncodePhase);
                if(encodeColorData((File)message.obj)) {
                    EncodeComplete();
                }else {

                }
                break;
            case R.id.EncodeRGB:
                this.sendFileActivity.getSendFileHandler().sendEmptyMessage(R.id.RaptorEncodePhase);
                if(encodeRGBData((File)message.obj)) {
                    EncodeComplete();
                }else {

                }
                break;
            case R.id.finish:
                sendFinishMessage();
                Looper.myLooper().quit();
                break;
            case R.id.failed:
                Looper.myLooper().quit();
                break;

        }
    }

    /**
     * 加密的初始化操作
     */
    private void initEncode(){
        encodeHints.put(EncodeHintType.ERROR_CORRECTION, sendConfigs.getErrorCorrectionLevel());
        encodeHints.put(EncodeHintType.MARGIN, 1);

        //String filename = "file.docx";
        StringBuilder FileName = new StringBuilder(sendConfigs.getPath().substring(0, sendConfigs.getPath().lastIndexOf(".")));
        String pureFileName  = FileName.substring(FileName.lastIndexOf("/"),FileName.length());
        //this.QRCodeDir = sendConfigs.getPath().substring(0, sendConfigs.getPath().lastIndexOf("."));//获取了文件名前缀，用于创建文件夹
        this.parentDir = this.sendFileActivity.getExternalFilesDir("send/"+pureFileName+System.currentTimeMillis()).getAbsolutePath();
        //String sendPath =  this.sendFileActivity.getExternalFilesDir("send/"+parentDir).getAbsolutePath();
        makedir(parentDir);
        switch (sendConfigs.getQRCodeType()){
            case R.id.BW:
                this.QRCodeDir = parentDir+"/QRCodes";//黑白
                break;
            case R.id.HSV:
                this.QRCodeDir = parentDir+"/HSVCodes";//HSV
                break;
            case R.id.RGB:
                this.QRCodeDir = parentDir+"/RGBCodes";//RGB
                break;
        }
        makedir(QRCodeDir);
        QRCodeDir=QRCodeDir+"/%d.jpg";
       // Log.i("QRCodeDir是",QRCodeDir);
        //System.out.println(QRCodeDir);

    }

    /**
     * 加密黑白
     */
    public  boolean encodeData(File encodeFile) {
        byte[] file = readAllBytes(encodeFile);
        int num = minAllowedNumSourceBlocks(file.length,this.sendConfigs.getQRCodeCapacity());
        this.fecParameters = FECParameters.newParameters(file.length,this.sendConfigs.getQRCodeCapacity(),num);
        this.arrayDataEncoder = OpenRQ.newEncoder(file,0,fecParameters);
       // int i = 0;
        try {
        for (SourceBlockEncoder sbEnc :  this.arrayDataEncoder.sourceBlockIterable()) {

                encodeSourceBlock(sbEnc);

            // i++;
        }
        } catch (Exception e) {
            return  false;
        }
        return true;
        //System.out.println("encodeSourceBlocknum"+i);
    }

    /**
     * 加密彩色
     */
    public  boolean encodeColorData(File encodeFile) {
       // int i = 0;
        byte[] file = readAllBytes(encodeFile);
        int num = minAllowedNumSourceBlocks(file.length,this.sendConfigs.getQRCodeCapacity());
        this.fecParameters = FECParameters.newParameters(file.length,this.sendConfigs.getQRCodeCapacity(),num);
        this.arrayDataEncoder = OpenRQ.newEncoder(file,0,fecParameters);
        try {
        for (SourceBlockEncoder sbEnc : this.arrayDataEncoder.sourceBlockIterable()) {
            encodeColorSourceBlock(sbEnc);
           // i++;
        }
    } catch (Exception e) {
       return  false;
    }
        return true;
    }

    /**
     * 加密RGB识别的二维码
     * @param encodeFile
     * @return
     */
    public  boolean encodeRGBData(File encodeFile) {
        // int i = 0;
        byte[] file = readAllBytes(encodeFile);
        int num = minAllowedNumSourceBlocks(file.length,this.sendConfigs.getQRCodeCapacity());
        this.fecParameters = FECParameters.newParameters(file.length,this.sendConfigs.getQRCodeCapacity(),num);
        this.arrayDataEncoder = OpenRQ.newEncoder(file,0,fecParameters);
        try {
            for (SourceBlockEncoder sbEnc : this.arrayDataEncoder.sourceBlockIterable()) {
                encodeRGBSourceBlock(sbEnc);
                // i++;
            }
        } catch (Exception e) {
            return  false;
        }
        return true;
    }
    private  void encodeSourceBlock(SourceBlockEncoder sbEnc) throws IOException, WriterException {
        int i = 1;
        // int m = 0;
        int j = 0;
        byte[] data = null;
        Bitmap bitmap = Bitmap.createBitmap(sendConfigs.getWidth(), sendConfigs.getHeight(), Bitmap.Config.ARGB_8888);
        for (EncodingPacket pac : sbEnc.sourcePacketsIterable()) {//缂栫爜
            // sendPacket(pac);
            data = pac.asArray();
           // byte[] mlgb = null;
            //鐢熸垚浜岀淮鐮�

            data = mergedata(this.fecParameters.asArray(),data);

                QrcodeUtil.encodebytearry(String.format(this.QRCodeDir,i),bitmap, data, sendConfigs.getWidth(), sendConfigs.getHeight());
                //zxingutil.preEncodeColorByte(String.format("src/kaiti/%d.jpg",i).toString(),"jpg", data, 900, 900);

					/*try {
						//mlgb = zxingutil.decodetobytearry(new File(String.format("src/sendtest/%d.png",i)));
					} catch (NotDataException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(Arrays.equals(data, mlgb)) {
					System.out.println(String.format("%d.png",i)+"鐩稿悓");
					}else {
						System.out.println(String.format("%d.png",i)+"涓嶅悓");
					}
					*/



            // this.linkedBlockingQueue.notifyAll();

            i++;
            // m++;
        }
        data = null;

        // System.out.println(sbEnc.);
       // System.out.println("EncodingPacket"+i);
        // number of repair symbols
        int nr = numberOfRepairSymbols(i);
        // System.out.println("nr:"+);
        // send nr repair symbols

        for (EncodingPacket pac : sbEnc.repairPacketsIterable(nr)) {
            //  sendPacket(pac);
            data = pac.asArray();
            // byte[] mlgb = null;
            try {
                data = mergedata(this.fecParameters.asArray(),data);
                QrcodeUtil.encodebytearry(String.format(this.QRCodeDir,i+j),bitmap, data, sendConfigs.getWidth(), sendConfigs.getHeight());
                //zxingutil.preEncodeColorByte(String.format("src/kaiti/%d.jpg",i+j).toString(),"jpg", data, 600, 600);
                    /*
					try {
						File sb = new File(String.format("src/testmp4/%d.png",i+j));
						mlgb = zxingutil.decodetobytearry(sb);
						if(Arrays.equals(data, mlgb)) {
						System.out.println(String.format("%d.png",i+j)+"鐩稿悓");
						}else {
							System.out.println(String.format("%d.png",i+j)+"涓嶅悓");
						}
					}
					 */
            } catch (WriterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // if ((j & 0x1) == 0) {//閸嬭埖鏆�

            //this.linkedBlockingQueue.notifyAll();
            //  }
            j++;

        }

        //System.out.println(j);
    }

    private  void encodeColorSourceBlock(SourceBlockEncoder sbEnc) throws IOException, WriterException {
        int i = 1;
        // int m = 0;
        int j = 0;
        byte[] data = null;
        Bitmap bitmap = Bitmap.createBitmap(sendConfigs.getWidth(), sendConfigs.getHeight(), Bitmap.Config.ARGB_8888);
        for (EncodingPacket pac : sbEnc.sourcePacketsIterable()) {//缂栫爜
            // sendPacket(pac);
            data = pac.asArray();
            //byte[] mlgb = null;
            //鐢熸垚浜岀淮鐮�

            data = mergedata(this.fecParameters.asArray(),data);
            try {
                QrcodeUtil.preEncodeColorByte(String.format(this.QRCodeDir,i),bitmap, this.encodeHints,data, sendConfigs.getWidth(), sendConfigs.getHeight());

            } catch (WriterException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
					/*try {
						//mlgb = zxingutil.decodetobytearry(new File(String.format("src/sendtest/%d.png",i)));
					} catch (NotDataException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(Arrays.equals(data, mlgb)) {
					System.out.println(String.format("%d.png",i)+"鐩稿悓");
					}else {
						System.out.println(String.format("%d.png",i)+"涓嶅悓");
					}
					*/



            // this.linkedBlockingQueue.notifyAll();

            i++;
            // m++;
        }
        data = null;

        // System.out.println(sbEnc.);
        //System.out.println("EncodingPacket"+i);
        // number of repair symbols
        int nr = numberOfRepairSymbols(i);
        // System.out.println("nr:"+);
        // send nr repair symbols

        for (EncodingPacket pac : sbEnc.repairPacketsIterable(nr)) {//绾犻敊
            //  sendPacket(pac);
            data = pac.asArray();
            // byte[] mlgb = null;

                data = mergedata(this.fecParameters.asArray(),data);
                QrcodeUtil.preEncodeColorByte(String.format(this.QRCodeDir,i+j),bitmap, this.encodeHints,data, sendConfigs.getWidth(), sendConfigs.getHeight());
                //zxingutil.preEncodeColorByte(String.format("src/kaiti/%d.jpg",i+j).toString(),"jpg", data, 600, 600);
                    /*
					try {
						File sb = new File(String.format("src/testmp4/%d.png",i+j));
						mlgb = zxingutil.decodetobytearry(sb);
						if(Arrays.equals(data, mlgb)) {
						System.out.println(String.format("%d.png",i+j)+"鐩稿悓");
						}else {
							System.out.println(String.format("%d.png",i+j)+"涓嶅悓");
						}
					}
					 */

            // if ((j & 0x1) == 0) {//閸嬭埖鏆�

            //this.linkedBlockingQueue.notifyAll();
            //  }
            j++;

        }

        //System.out.println(j);
    }

    /**
     *rgb颜色
     * @param sbEnc
     * @throws IOException
     * @throws WriterException
     */
    private  void encodeRGBSourceBlock(SourceBlockEncoder sbEnc) throws IOException, WriterException {
        int i = 1;
        // int m = 0;
        int j = 0;
        byte[] data = null;
        Bitmap bitmap = Bitmap.createBitmap(sendConfigs.getWidth(), sendConfigs.getHeight(), Bitmap.Config.ARGB_8888);
        for (EncodingPacket pac : sbEnc.sourcePacketsIterable()) {//缂栫爜
            // sendPacket(pac);
            data = pac.asArray();
            //byte[] mlgb = null;
            //鐢熸垚浜岀淮鐮�

            data = mergedata(this.fecParameters.asArray(),data);
            try {
                QrcodeUtil.preEncodeRGBByte(String.format(this.QRCodeDir,i),bitmap, this.encodeHints,data, sendConfigs.getWidth(), sendConfigs.getHeight());

            } catch (WriterException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
					/*try {
						//mlgb = zxingutil.decodetobytearry(new File(String.format("src/sendtest/%d.png",i)));
					} catch (NotDataException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(Arrays.equals(data, mlgb)) {
					System.out.println(String.format("%d.png",i)+"鐩稿悓");
					}else {
						System.out.println(String.format("%d.png",i)+"涓嶅悓");
					}
					*/



            // this.linkedBlockingQueue.notifyAll();

            i++;
            // m++;
        }
        data = null;

        // System.out.println(sbEnc.);
        //System.out.println("EncodingPacket"+i);
        // number of repair symbols
        int nr = numberOfRepairSymbols(i);
        // System.out.println("nr:"+);
        // send nr repair symbols

        for (EncodingPacket pac : sbEnc.repairPacketsIterable(nr)) {//绾犻敊
            //  sendPacket(pac);
            data = pac.asArray();
            // byte[] mlgb = null;

            data = mergedata(this.fecParameters.asArray(),data);
            QrcodeUtil.preEncodeRGBByte(String.format(this.QRCodeDir,i+j),bitmap, this.encodeHints,data, sendConfigs.getWidth(), sendConfigs.getHeight());
            //zxingutil.preEncodeColorByte(String.format("src/kaiti/%d.jpg",i+j).toString(),"jpg", data, 600, 600);
                    /*
					try {
						File sb = new File(String.format("src/testmp4/%d.png",i+j));
						mlgb = zxingutil.decodetobytearry(sb);
						if(Arrays.equals(data, mlgb)) {
						System.out.println(String.format("%d.png",i+j)+"鐩稿悓");
						}else {
							System.out.println(String.format("%d.png",i+j)+"涓嶅悓");
						}
					}
					 */

            // if ((j & 0x1) == 0) {//閸嬭埖鏆�

            //this.linkedBlockingQueue.notifyAll();
            //  }
            j++;

        }

        //System.out.println(j);
    }

    /**
     * 二维码生成成功
     */
    private void EncodeComplete(){
                Message message  = Message.obtain(this.ffmpegHandler,R.id.GenerateVideo,new FFmpegInOutPath(this.QRCodeDir.substring(0,this.QRCodeDir.lastIndexOf("/")),this.parentDir));
                message.sendToTarget();

    }
    private void sendFinishMessage(){
        Message message = obtainMessage(R.id.finish);
       this.ffmpegHandler.sendMessageAtFrontOfQueue(message);
    }
    /**
     * 二维码生成失败
     */
    private void EncodeFailed(){
        Message message  = Message.obtain(this.ffmpegHandler,R.id.failed);
        message.sendToTarget();

    }
    private static int numberOfRepairSymbols(int EncodingPacketsize){
        return EncodingPacketsize;
    }

    private byte[] mergedata(byte[] FEC,byte[] symbol) {
        byte[] c= Arrays.copyOf(FEC, FEC.length+symbol.length);
        System.arraycopy(symbol, 0, c, FEC.length, symbol.length);
        return c;
    }
    private byte[] readAllBytes(File encodeFile){
        FileInputStream fileInputStream = null;
        ByteBuffer byteBuffer = null;
        try {
            fileInputStream = new FileInputStream(encodeFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        FileChannel fileChannel = fileInputStream.getChannel();
        try {
             byteBuffer = ByteBuffer.allocate((int) fileChannel.size());
            while ((fileChannel.read(byteBuffer)) > 0) {
                // do nothing
                // System.out.println("reading");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                fileChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return byteBuffer.array();
    }
    public void makedir(String sendPath){//生成存放二维码的目录，同时返回目录路径
        // String  dirpath = path+"/"+new Date().getTime();
        //String dirpath = path+"/1";
        File file = null;
        try {
            file = new File(sendPath);
            if (!file.exists()) {
                file.mkdir();
            }else{

                //提示文件夹已存在，是否覆写
            }
        } catch (Exception e) {
            Log.i("error:", e+"");
        }

        Log.i(TAG,sendPath);
        //return sendPath;
    }
}

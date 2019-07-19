package com.littlewhite.SendFile;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Color.BLACK;
import static android.graphics.Color.BLUE;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;

public class QrcodeUtil {
    /**
     * 生成byte数组成的二维码
     * @param
     * @throws IOException
     */
    public static void encodebytearry(String imgpath,Bitmap bitmap, byte[] content, int width, int height)
            throws WriterException, IOException {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);// 纠错级别
        //hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");// 字符编码格式
        hints.put(EncodeHintType.MARGIN, 1);// 外边距Margin
        //hints.put(EncodeHintType.QR_VERSION,40);
        // hints.put(EncodeHintType.QR_VERSION, 2);
        // hints.put(EncodeHintType.);
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        // 内存中图片需要制定一个boolean[][]->BitMatrix
       // Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? BLACK : WHITE;
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        File file = new File(imgpath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 处理byte[]，将其拆分
     * @param imgpath
     *
     * @param data
     * @param width
     * @param height
     */
    public static void preEncodeColorByte(String imgpath,Bitmap bitmap,Hashtable<EncodeHintType, Object> hints,byte[] data,int width, int height) throws IOException, WriterException {
        int dataLen = data.length;
        int remain = dataLen%3;
        int singleLen = dataLen/3;
        switch (remain){
            case 0:
                cutByte(0,0,data,singleLen,imgpath,bitmap,hints,width,height);
                break;
            case 1:
                cutByte(1,0,data,singleLen,imgpath,bitmap,hints,width,height);
                break;
            case 2:
                cutByte(1,1,data,singleLen,imgpath,bitmap,hints,width,height);
                break;

        }

    }
    private static void cutByte(int a,int b,byte[] data ,int singleLen,String imgpath,Bitmap bitmap,Hashtable<EncodeHintType, Object> hints,int width,int height) throws IOException, WriterException {
        byte[] data1 = new byte[singleLen+a];
        byte[] data2 = new byte[singleLen+b];
        byte[] data3 = new byte[singleLen];
        System.arraycopy(data,0,data1,0,singleLen+a);
        System.arraycopy(data,singleLen+a,data2,0,singleLen+b);
        System.arraycopy(data,2*singleLen+a+b,data3,0,singleLen);
        encodeColorByte(imgpath, bitmap,hints, data1, data2,data3,width,height);
    }
    /**
     * 使用彩色二维码传输文件
     * @param imgpath
     * @param
     * @param content1
     * @param content2
     * @param content3
     * @param width
     * @param height
     * @throws WriterException
     * @throws IOException
     */
    public static void encodeColorByte(String imgpath, Bitmap bitmap,Hashtable<EncodeHintType, Object> hints, byte[] content1, byte[] content2,
                                       byte[] content3, int width, int height) throws WriterException, IOException {
       // Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        //hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);// 纠错级别
        //hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");// 字符编码格式
        //hints.put(EncodeHintType.MARGIN, 1);// 外边距Margin
        //hints.put(EncodeHintType.QR_VERSION,40);
        // hints.put(EncodeHintType.);
        // 获取控制级别
        BitMatrix[] bitMatrix = new MultiFormatWriter().encode(content1, content2, content3, BarcodeFormat.QR_CODE,
                width, height, hints);
        BitMatrix bitMatrix1 = bitMatrix[0];
        BitMatrix bitMatrix2 = bitMatrix[1];
        BitMatrix bitMatrix3 = bitMatrix[2];
        // 内存中图片需要制定一个boolean[][]->BitMatrix
        //BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                // img.setRGB(x, y, (bitMatrix2.get(x, y)?Color.BLACK.getRGB():
                // Color.WHITE.getRGB()));//首层涂红
                if (bitMatrix1.get(x, y) && bitMatrix2.get(x, y) && bitMatrix3.get(x, y)) {// 全部都包括
                    pixels[offset+x] = BLACK;
                } // 涂黑
                else if (!bitMatrix1.get(x, y) && !bitMatrix2.get(x, y) && !bitMatrix3.get(x, y)) {// 全部都不包括
                    pixels[offset+x] = WHITE;
                } // 涂白
                else if (bitMatrix1.get(x, y) && !bitMatrix2.get(x, y) && !bitMatrix3.get(x, y)) {
                    pixels[offset+x] = RED;
                } // 涂红
                else if (!bitMatrix1.get(x, y) && bitMatrix2.get(x, y) && !bitMatrix3.get(x, y)) {
                    pixels[offset+x] = GREEN;
                } // 涂绿
                else if (!bitMatrix1.get(x, y) && !bitMatrix2.get(x, y) && bitMatrix3.get(x, y)) {
                    pixels[offset+x] = BLUE;
                } // 涂蓝
                else if (bitMatrix1.get(x, y) && bitMatrix2.get(x, y) && !bitMatrix3.get(x, y)) {
                    pixels[offset+x] = 0xFFFFFF00;
                } // 红+绿
                else if (bitMatrix1.get(x, y) && !bitMatrix2.get(x, y) && bitMatrix3.get(x, y)) {
                    pixels[offset+x] = 0xFFFF00FF;
                } // 红+蓝
                else if (!bitMatrix1.get(x, y) && bitMatrix2.get(x, y) && bitMatrix3.get(x, y)) {
                    pixels[offset+x] = 0xFF00FFFF;
                } // 蓝+绿

            }
            // if(bitMatrix1.get(x, y)) {}
            // img.setRGB(x, y, (bitMatrix1.get(x, y)?new Color(255,0,0,85).getRGB():new
            // Color(255,255,255,0).getRGB()));//首层涂红
            // img.setRGB(x, y, (bitMatrix2.get(x, y)?new Color(0,255,0,85).getRGB():new
            // Color(255,255,255,0).getRGB()));//第二层涂绿
            // img.setRGB(x, y, (bitMatrix3.get(x, y)?new Color(0,0,255,85).getRGB():new
            // Color(255,255,255,0).getRGB()));//最后一层蓝

            // img.setR
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        File file = new File(imgpath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 处理byte[]，将其拆分
     * @param imgpath
     *
     * @param data
     * @param width
     * @param height
     */
    public static void preEncodeRGBByte(String imgpath,Bitmap bitmap,Hashtable<EncodeHintType, Object> hints,byte[] data,int width, int height) throws IOException, WriterException {
        int dataLen = data.length;
        int remain = dataLen%3;
        int singleLen = dataLen/3;
        switch (remain){
            case 0:
                cutRGBByte(0,0,data,singleLen,imgpath,bitmap,hints,width,height);
                break;
            case 1:
                cutRGBByte(1,0,data,singleLen,imgpath,bitmap,hints,width,height);
                break;
            case 2:
                cutRGBByte(1,1,data,singleLen,imgpath,bitmap,hints,width,height);
                break;

        }

    }
    private static void cutRGBByte(int a,int b,byte[] data ,int singleLen,String imgpath,Bitmap bitmap,Hashtable<EncodeHintType, Object> hints,int width,int height) throws IOException, WriterException {
        byte[] data1 = new byte[singleLen+a];
        byte[] data2 = new byte[singleLen+b];
        byte[] data3 = new byte[singleLen];
        System.arraycopy(data,0,data1,0,singleLen+a);
        System.arraycopy(data,singleLen+a,data2,0,singleLen+b);
        System.arraycopy(data,2*singleLen+a+b,data3,0,singleLen);
        encodeRGBByte(imgpath, bitmap,hints, data1, data2,data3,width,height);
    }
    /**
     * 使用彩色二维码传输文件
     * @param imgpath
     * @param
     * @param content1
     * @param content2
     * @param content3
     * @param width
     * @param height
     * @throws WriterException
     * @throws IOException
     */
    public static void encodeRGBByte(String imgpath, Bitmap bitmap,Hashtable<EncodeHintType, Object> hints, byte[] content1, byte[] content2,
                                       byte[] content3, int width, int height) throws WriterException, IOException {
        // Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        //hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);// 纠错级别
        //hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");// 字符编码格式
        //hints.put(EncodeHintType.MARGIN, 1);// 外边距Margin
        //hints.put(EncodeHintType.QR_VERSION,40);
        // hints.put(EncodeHintType.);
        // 获取控制级别
        BitMatrix[] bitMatrix = new MultiFormatWriter().encode(content1, content2, content3, BarcodeFormat.QR_CODE,
                width, height, hints);
        BitMatrix bitMatrix1 = bitMatrix[0];
        BitMatrix bitMatrix2 = bitMatrix[1];
        BitMatrix bitMatrix3 = bitMatrix[2];
        // 内存中图片需要制定一个boolean[][]->BitMatrix
        //BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                // img.setRGB(x, y, (bitMatrix2.get(x, y)?Color.BLACK.getRGB():
                // Color.WHITE.getRGB()));//首层涂红
                if (bitMatrix1.get(x, y) && bitMatrix2.get(x, y) && bitMatrix3.get(x, y)) {// 全部都包括
                    pixels[offset+x] = BLACK;
                } // 涂黑
                else if (!bitMatrix1.get(x, y) && !bitMatrix2.get(x, y) && !bitMatrix3.get(x, y)) {// 全部都不包括
                    pixels[offset+x] = WHITE;
                } // 涂白
                else if (bitMatrix1.get(x, y) && !bitMatrix2.get(x, y) && !bitMatrix3.get(x, y)) {
                    pixels[offset+x] = 0xFF00FFFF;
                } // 涂红
                else if (!bitMatrix1.get(x, y) && bitMatrix2.get(x, y) && !bitMatrix3.get(x, y)) {
                    pixels[offset+x] = 0xFFFF00FF;
                } // 涂绿
                else if (!bitMatrix1.get(x, y) && !bitMatrix2.get(x, y) && bitMatrix3.get(x, y)) {
                    pixels[offset+x] = 0xFFFFFF00;
                } // 涂蓝
                else if (bitMatrix1.get(x, y) && bitMatrix2.get(x, y) && !bitMatrix3.get(x, y)) {
                    pixels[offset+x] = 0xFF0000FF;
                } // 红+绿
                else if (bitMatrix1.get(x, y) && !bitMatrix2.get(x, y) && bitMatrix3.get(x, y)) {
                    pixels[offset+x] = 0xFF00FF00;
                } // 红+蓝
                else if (!bitMatrix1.get(x, y) && bitMatrix2.get(x, y) && bitMatrix3.get(x, y)) {
                    pixels[offset+x] = 0xFFFF0000;
                } // 蓝+绿

            }
            // if(bitMatrix1.get(x, y)) {}
            // img.setRGB(x, y, (bitMatrix1.get(x, y)?new Color(255,0,0,85).getRGB():new
            // Color(255,255,255,0).getRGB()));//首层涂红
            // img.setRGB(x, y, (bitMatrix2.get(x, y)?new Color(0,255,0,85).getRGB():new
            // Color(255,255,255,0).getRGB()));//第二层涂绿
            // img.setRGB(x, y, (bitMatrix3.get(x, y)?new Color(0,0,255,85).getRGB():new
            // Color(255,255,255,0).getRGB()));//最后一层蓝

            // img.setR
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        File file = new File(imgpath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

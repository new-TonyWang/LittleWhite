package com.littlewhite.ReceiveFile.ColorCode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.zxing.LuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.RGBData;
import com.littlewhite.R;
import com.littlewhite.ReceiveFile.ReceiveActivity;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class RGBbinaryHandler extends Handler {

    private static final int BLOCK_SIZE_POWER = 3;
    private static final int BLOCK_SIZE = 1 << BLOCK_SIZE_POWER; // ...0100...00，1往左移动3位==8
    private static final int BLOCK_SIZE_MASK = BLOCK_SIZE - 1;   // ...0011...11，==7
    private static final int MINIMUM_DIMENSION = BLOCK_SIZE * 5;//==40
    private static final int MIN_DYNAMIC_RANGE = 24;//
    protected Handler handler;
    protected ReceiveActivity receiveActivity;

    public RGBbinaryHandler(Handler handler, ReceiveActivity receiveActivity) {
        this.handler = handler;
        this.receiveActivity = receiveActivity;
    }

    static {
        System.loadLibrary("cbinarizer");
    }


    //private RGBData rgbData;


    public RGBmatrixChannel getRGBData(byte[] channel,int RGB,int subWidth,int subHeight,int width,int height,long timemillis)  {

      //  long start = System.currentTimeMillis();

       // LuminanceSource source = getLuminanceSource();
       // int width = source.getWidth();
        //int height = source.getHeight();

      //  byte[] luminances = source.getMatrix();//灰度数组

        //byte[] uv = source.getUVMatrix();//获取UV矩阵
        // byte[] RGB = new byte[luminances.length*4];
        // byte[] H = new byte[luminances.length];
        //byte[] S = new byte[luminances.length];
        //byte[] V = new byte[luminances.length];
       // byte[] channel = new byte[luminances.length];
       // byte[] GC = new byte[luminances.length];
        //byte[] BC = new byte[luminances.length];
       // getRGBchannel(luminances, uv, channel, width, height);

        // luminances = calculateY(luminances, subWidth, subHeight, width, height);
        //outputmatrix(luminances,uv,width,height,true,Math.random());
        //outputRGB(RGB,width,height,"rgb");
        int[][] blackPoints = calculateBlackPoints(channel, subWidth, subHeight, width, height);
        BitMatrix newMatrix = new BitMatrix(width, height);
        switch (RGB){
            case R.id.Convert_R:
                calculateThresholdForBlock(channel, subWidth, subHeight, width, height, blackPoints, newMatrix);
                break;
            case R.id.Convert_G:
                calculateThresholdForBlock(channel, subWidth, subHeight, width, height, blackPoints, newMatrix);
                break;
            case R.id.Convert_B:
                calculateThresholdForBlock(channel, subWidth, subHeight, width, height, blackPoints, newMatrix);
                break;

        }
        //int[][] blackPoints = calculateBlackPoints(RC, subWidth, subHeight, width, height);
        //blackPoints = calculateBlackPoints(GC, subWidth, subHeight, width, height);

        //blackPoints = calculateBlackPoints(BC, subWidth, subHeight, width, height);

       // long end = System.currentTimeMillis();
        //System.out.println("二值化时长" + (end - start) + "ms");


       //  rgbData = new RGBData(newMatrixR,newMatrixG,newMatrixB);

        return new RGBmatrixChannel(newMatrix,RGB,timemillis);
    }

    //System.out.println("二值化后的BitMatrix宽:"+matrix.getWidth()+"，高:"+matrix.getHeight());
    /*
    try {
		outputimage(matrix,"src/matrix.png");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	*/

    private void outputmatrix(byte[] y,byte[] uv, int width, int height, boolean israw, double dou) {
        byte[] data = new byte[y.length + uv.length];
        System.arraycopy(y, 0, data, 0, y.length);
        //for(int i = y.length;i<uv.length;i++){
        //   data[i] = (byte)128;
        //}
        System.arraycopy(uv, 0, data, y.length, uv.length);
        Rect rect = new Rect(0, 0, width, height);
        YuvImage yuvImg = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
        yuvImg.compressToJpeg(rect, 100, outputstream);
        Bitmap rawbitmap = BitmapFactory.decodeByteArray(outputstream.toByteArray(), 0, outputstream.size());
        try {
            outputstream.flush();
            outputstream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream bos = null;
        try {
            //rawbitmap.compress()
            if (israw) {
                bos = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/" + dou + "raw.png");
                rawbitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            } else {
                bos = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/" + dou + "notraw.png");
                rawbitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            }
            bos.flush();
            bos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void outputRGB(byte[] ARGB, int width, int height, String dou) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(ARGB));
        FileOutputStream bos = null;
        try {
            bos = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/" + dou + "ARGB.png");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);

    }



    /**
     * For each block in the image, calculate the average black point using a 5x5 grid
     * of the blocks around it. Also handles the corner cases (fractional blocks are computed based
     * on the last pixels in the row/column which are also used in the previous block).
     * 对于图像中的每个块，使用其周围块的5x5网格计算平均黑点。
     * 还处理角情况(分块是根据行/列中的最后一个像素计算的，这些像素也在前一个块中使用)。
     */
    private static void calculateThresholdForBlock(byte[] luminances,
                                                   int subWidth,
                                                   int subHeight,
                                                   int width,
                                                   int height,
                                                   int[][] blackPoints,
                                                   BitMatrix matrix) {
        int maxYOffset = height - BLOCK_SIZE;
        int maxXOffset = width - BLOCK_SIZE;
        for (int y = 0; y < subHeight; y++) {
            int yoffset = y << BLOCK_SIZE_POWER;
            if (yoffset > maxYOffset) {
                yoffset = maxYOffset;
            }
            int top = cap(y, 2, subHeight - 3);
            for (int x = 0; x < subWidth; x++) {
                int xoffset = x << BLOCK_SIZE_POWER;
                if (xoffset > maxXOffset) {
                    xoffset = maxXOffset;
                }
                int left = cap(x, 2, subWidth - 3);
                int sum = 0;
                for (int z = -2; z <= 2; z++) {
                    int[] blackRow = blackPoints[top + z];
                    sum += blackRow[left - 2] + blackRow[left - 1] + blackRow[left] + blackRow[left + 1] + blackRow[left + 2];
                }
                int average = sum / 25;
                thresholdBlock(luminances, xoffset, yoffset, average, width, matrix);
            }
        }
    }

    private static int cap(int value, int min, int max) {
        return value < min ? min : value > max ? max : value;
    }

    /**
     * Applies a single threshold to a block of pixels.
     * 对像素块应用单个阈值。
     */
    private static void thresholdBlock(byte[] luminances,
                                       int xoffset,
                                       int yoffset,
                                       int threshold,
                                       int stride,
                                       BitMatrix matrix) {
        for (int y = 0, offset = yoffset * stride + xoffset; y < BLOCK_SIZE; y++, offset += stride) {
            for (int x = 0; x < BLOCK_SIZE; x++) {
                // Comparison needs to be <= so that black == 0 pixels are black even if the threshold is 0.
                if ((luminances[offset + x] & 0xFF) <= threshold) {
                    matrix.set(xoffset + x, yoffset + y);
                }
            }
        }
    }

    /**
     * Calculates a single black point for each block of pixels and saves it away.
     * See the following thread for a discussion of this algorithm:
     * http://groups.google.com/group/zxing/browse_thread/thread/d06efa2c35a7ddc0
     */
    private static int[][] calculateBlackPoints(byte[] luminances,
                                                int subWidth,
                                                int subHeight,
                                                int width,
                                                int height) {
        int maxYOffset = height - BLOCK_SIZE;
        int maxXOffset = width - BLOCK_SIZE;
        int[][] blackPoints = new int[subHeight][subWidth];
        for (int y = 0; y < subHeight; y++) {
            int yoffset = y << BLOCK_SIZE_POWER;
            if (yoffset > maxYOffset) {
                yoffset = maxYOffset;
            }
            for (int x = 0; x < subWidth; x++) {
                int xoffset = x << BLOCK_SIZE_POWER;
                if (xoffset > maxXOffset) {
                    xoffset = maxXOffset;
                }
                int sum = 0;
                int min = 0xFF;
                int max = 0;
                for (int yy = 0, offset = yoffset * width + xoffset; yy < BLOCK_SIZE; yy++, offset += width) {
                    for (int xx = 0; xx < BLOCK_SIZE; xx++) {
                        int pixel = luminances[offset + xx] & 0xFF;
                        sum += pixel;
                        // still looking for good contrast
                        if (pixel < min) {
                            min = pixel;
                        }
                        if (pixel > max) {
                            max = pixel;
                        }
                    }
                    // short-circuit min/max tests once dynamic range is met
                    if (max - min > MIN_DYNAMIC_RANGE) {
                        // finish the rest of the rows quickly
                        for (yy++, offset += width; yy < BLOCK_SIZE; yy++, offset += width) {
                            for (int xx = 0; xx < BLOCK_SIZE; xx++) {
                                sum += luminances[offset + xx] & 0xFF;
                            }
                        }
                    }
                }

                // The default estimate is the average of the values in the block.
                int average = sum >> (BLOCK_SIZE_POWER * 2);
                if (max - min <= MIN_DYNAMIC_RANGE) {
                    // If variation within the block is low, assume this is a block with only light or only
                    // dark pixels. In that case we do not want to use the average, as it would divide this
                    // low contrast area into black and white pixels, essentially creating data out of noise.
                    //
                    // The default assumption is that the block is light/background. Since no estimate for
                    // the level of dark pixels exists locally, use half the min for the block.
                    /*
                     * 如果块内的变化很小，则假设这是一个只有亮像素或暗像素的块。
                     * 在这种情况下，我们不想使用平均值，因为它会把这个低对比度的区域分割成黑白像素，本质上是在噪声中创建数据。
                     * 默认的假设是块是光/背景。由于本地不存在对暗像素级别的估计，因此对块使用最小值的一半。
                     */
                    average = min / 2;

                    if (y > 0 && x > 0) {
                        // Correct the "white background" assumption for blocks that have neighbors by comparing
                        // the pixels in this block to the previously calculated black points. This is based on
                        // the fact that dark barcode symbology is always surrounded by some amount of light
                        // background for which reasonable black point estimates were made. The bp estimated at
                        // the boundaries is used for the interior.

                        // The (min < bp) is arbitrary but works better than other heuristics that were tried.
                        /*
                         * //通过比较块中的像素和之前计算的黑色点，纠正“白色背景”的假设。
                         * 这是基于这样一个事实，即暗条码符号总是被一些光背景包围，对这些光背景进行合理的黑点估计。
                         * 边界处的bp值用于内部。(min < bp)是任意的，但比尝试过的其他启发式方法更有效。
                         */
                        int averageNeighborBlackPoint =
                                (blackPoints[y - 1][x] + (2 * blackPoints[y][x - 1]) + blackPoints[y - 1][x - 1]) / 4;
                        if (min < averageNeighborBlackPoint) {
                            average = averageNeighborBlackPoint;
                        }
                    }
                }
                blackPoints[y][x] = average;
            }
        }
        return blackPoints;
    }

    private static byte[] calculateY(byte[] luminances,
                                     int subWidth,
                                     int subHeight,
                                     int width,
                                     int height) {
        int maxYOffset = height - BLOCK_SIZE;
        int maxXOffset = width - BLOCK_SIZE;
        int[][] blackPoints = new int[subHeight][subWidth];
        for (int y = 0; y < subHeight; y++) {
            int yoffset = y << BLOCK_SIZE_POWER;
            if (yoffset > maxYOffset) {
                yoffset = maxYOffset;
            }
            for (int x = 0; x < subWidth; x++) {
                int xoffset = x << BLOCK_SIZE_POWER;
                if (xoffset > maxXOffset) {
                    xoffset = maxXOffset;
                }
                int sum = 0;
                int min = 0xFF;//255
                int max = 0;//0
                for (int yy = 0, offset = yoffset * width + xoffset; yy < BLOCK_SIZE; yy++, offset += width) {
                    for (int xx = 0; xx < BLOCK_SIZE; xx++) {
                        int pixel = luminances[offset + xx] & 0xFF;
                        sum += pixel;
                        // still looking for good contrast
                        if (pixel < min) {
                            min = pixel;
                        }
                        if (pixel > max) {
                            max = pixel;
                        }
                    }
                    // short-circuit min/max tests once dynamic range is met
                    if (max - min > MIN_DYNAMIC_RANGE) {
                        // finish the rest of the rows quickly
                        for (yy++, offset += width; yy < BLOCK_SIZE; yy++, offset += width) {
                            for (int xx = 0; xx < BLOCK_SIZE; xx++) {
                                sum += luminances[offset + xx] & 0xFF;
                            }
                        }
                    }
                }

                // The default estimate is the average of the values in the block.
                int average = sum >> (BLOCK_SIZE_POWER * 2);
                if (max - min <= MIN_DYNAMIC_RANGE) {
                    // If variation within the block is low, assume this is a block with only light or only
                    // dark pixels. In that case we do not want to use the average, as it would divide this
                    // low contrast area into black and white pixels, essentially creating data out of noise.
                    //
                    // The default assumption is that the block is light/background. Since no estimate for
                    // the level of dark pixels exists locally, use half the min for the block.
                    average = min / 2;

                    if (y > 0 && x > 0) {
                        // Correct the "white background" assumption for blocks that have neighbors by comparing
                        // the pixels in this block to the previously calculated black points. This is based on
                        // the fact that dark barcode symbology is always surrounded by some amount of light
                        // background for which reasonable black point estimates were made. The bp estimated at
                        // the boundaries is used for the interior.

                        // The (min < bp) is arbitrary but works better than other heuristics that were tried.
                        int averageNeighborBlackPoint =
                                (blackPoints[y - 1][x] + (2 * blackPoints[y][x - 1]) + blackPoints[y - 1][x - 1]) / 4;
                        if (min < averageNeighborBlackPoint) {
                            average = averageNeighborBlackPoint;
                        }
                    }
                }
                blackPoints[y][x] = average;
                for (int yy = 0, offset = yoffset * width + xoffset; yy < BLOCK_SIZE; yy++, offset += width) {
                    for (int xx = 0; xx < BLOCK_SIZE; xx++) {
                        luminances[offset + xx] = (byte) average;
                    }
                }
            }
        }
        return luminances;

    }
    protected void send_to_QRDecoder(RGBmatrixChannel rgBmatrixChannel){
        Message message = Message.obtain(this.handler,R.id.decode,rgBmatrixChannel);
            message.sendToTarget();
    }
    protected void send_to_finish(){
        Looper.myLooper().quit();
       Message message = Message.obtain(this.handler,R.id.finish);
       message.sendToTarget();
    }

}
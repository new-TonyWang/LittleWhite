package com.google.zxing.common;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.os.Environment;

import com.google.zxing.NotFoundException;
import com.littlewhite.ColorCode.HSVColorTable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public  class ColorGridSampler extends GridSampler {
    @Override
    public BitMatrix sampleGrid(BitMatrix image,
                                int dimensionX,
                                int dimensionY,
                                float p1ToX, float p1ToY,
                                float p2ToX, float p2ToY,
                                float p3ToX, float p3ToY,
                                float p4ToX, float p4ToY,
                                float p1FromX, float p1FromY,
                                float p2FromX, float p2FromY,
                                float p3FromX, float p3FromY,
                                float p4FromX, float p4FromY) throws NotFoundException {

        PerspectiveTransform transform = PerspectiveTransform.quadrilateralToQuadrilateral(
                p1ToX, p1ToY, p2ToX, p2ToY, p3ToX, p3ToY, p4ToX, p4ToY,
                p1FromX, p1FromY, p2FromX, p2FromY, p3FromX, p3FromY, p4FromX, p4FromY);

        return sampleGrid(image, dimensionX, dimensionY, transform);
    }

    @Override
    public BitMatrix sampleGrid(BitMatrix image, int dimensionX, int dimensionY, PerspectiveTransform transform) throws NotFoundException {
        return null;
    }

    @Override
    public BitMatrix[] ColorGrid(HsvData image, int dimensionX, int dimensionY, PerspectiveTransform transform, HSVColorTable hsvColorTable) throws NotFoundException {
        if (dimensionX <= 0 || dimensionY <= 0) {
            throw NotFoundException.getNotFoundInstance();
        }
        BitMatrix bitsR = new BitMatrix(dimensionX, dimensionY);
        BitMatrix bitsG = new BitMatrix(dimensionX, dimensionY);
        BitMatrix bitsB = new BitMatrix(dimensionX, dimensionY);
        float[] points = new float[2 * dimensionX];
        for (int y = 0; y < dimensionY; y++) {
            int max = points.length;
            float iValue = y + 0.5f;
            for (int x = 0; x < max; x += 2) {
                points[x] = (float) (x / 2) + 0.5f;
                points[x + 1] = iValue;
            }
            transform.transformPoints(points);
            // Quick check to see if points transformed to something inside the image;
            // sufficient to check the endpoints
            checkAndNudgePoints(image.getBitMatrix(), points);
            try {
                for (int x = 0; x < max; x += 2) {
                    int xx = Math.round(points[x]);
                    int yy = Math.round(points[x+1]);
                   // int color = hsvColorTable.getColor(calculateAvgH(image,xx,yy),calculateAvgS(image,xx,yy),calculateAvgV(image,xx,yy));
                    int color = hsvColorTable.getColor(image.getH(xx,yy),image.getS(xx,yy),image.getV(xx,yy));
                    switch (color) {
                        case 1://红
                            bitsR.set(x / 2, y);
                            break;
                        case 2://黄
                            bitsR.set(x / 2, y);
                            bitsG.set(x / 2, y);
                            break;
                        case 3://绿
                            bitsG.set(x / 2, y);
                            break;
                        case 4://青
                            bitsG.set(x / 2, y);
                            bitsB.set(x / 2, y);
                            break;
                        case 5://蓝
                            bitsB.set(x / 2, y);
                            break;
                        case 6://品红
                            bitsR.set(x / 2, y);
                            bitsB.set(x / 2, y);
                            break;
                        case 7://黑
                            bitsR.set(x / 2, y);
                            bitsG.set(x / 2, y);
                            bitsB.set(x / 2, y);
                            break;
                        case 8://白

                            break;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                // This feels wrong, but, sometimes if the finder patterns are misidentified, the resulting
                // transform gets "twisted" such that it maps a straight line of points to a set of points
                // whose endpoints are in bounds, but others are not. There is probably some mathematical
                // way to detect this about the transformation that I don't know yet.
                // This results in an ugly runtime exception despite our clever checks above -- can't have
                // that. We could check each point's coordinates but that feels duplicative. We settle for
                // catching and wrapping ArrayIndexOutOfBoundsException.
                throw NotFoundException.getNotFoundInstance();
            }
        }

       // return bits;
        BitMatrix[] Bits = {bitsR,bitsG,bitsB};

       // outputimage(bits);

        outputimage(bitsR,1);
        outputimage(bitsG,2);
        outputimage(bitsB,3);

        return Bits;
    }
    private int calculateAvgH(HsvData image,int x,int y){

        return (image.getH(x,y-1)+image.getH(x-1,y)+image.getH(x,y)+image.getH(x+1,y)+image.getH(x,y+1))/5;
    }
    private int calculateAvgS(HsvData image,int x,int y){

        return (image.getS(x,y-1)+image.getS(x-1,y)+image.getH(x,y)+image.getS(x+1,y)+image.getS(x,y+1))/5;
    }
    private int calculateAvgV(HsvData image,int x,int y){

        return (image.getV(x,y-1)+image.getV(x-1,y)+image.getV(x,y)+image.getV(x+1,y)+image.getH(x,y+1))/5;
    }
    protected void outputimage(BitMatrix bitMatrix,int index)  {
        int length = bitMatrix.getBits().length;
        int height = bitMatrix.getHeight();
        int width = bitMatrix.getWidth();
        //Bitmap.Config Config =
        Bitmap image = Bitmap.createBitmap(bitMatrix.getWidth(), bitMatrix.getHeight(), Bitmap.Config.ARGB_8888);
        for(int y = 0;y<height;y++) {
            for(int x = 0;x<width;x++) {
                image.setPixel(x, y, (bitMatrix.get(x, y) ? 0x00000000 : 0xFFFFFFFF));
            }
        }
        File file = null;
        switch (index) {
            case 1:
             file = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "red.jpg"));

            break;//File myCaptureFile = new File( + fileName);
            case 2:
                file = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "green.jpg"));

                break;//File myCaptureFile = new File( + fileName);
            case 3:
                file = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "blue.jpg"));

                break;//File myCaptureFile = new File( + fileName);
        }
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        image.compress(Bitmap.CompressFormat.JPEG, 100, bos);

        try {
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

package com.google.zxing.common;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.os.Environment;

import com.google.zxing.NotFoundException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ColorGridSampler extends GridSampler {
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
    public BitMatrix[] ColorGrid(HsvData image, int dimensionX, int dimensionY, PerspectiveTransform transform) throws NotFoundException {
        if (dimensionX <= 0 || dimensionY <= 0) {
            throw NotFoundException.getNotFoundInstance();
        }
        BitMatrix bits = new BitMatrix(dimensionX, dimensionY);
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
                    int xx = (int) points[x];
                    int yy = (int)points[x+1];

                     if (((image.getH(xx,yy)>=0&&image.getH(xx,yy)<=14)||
                            (image.getH(xx,yy)>=159&&image.getH(xx,yy)<=180))
                            &&(image.getS(xx,yy)>=43&&image.getS(xx,yy)<=255)
                            &&(image.getV(xx,yy)>=46&&image.getV(xx,yy)<=255)) {
                        // 红
                        bits.set(x / 2, y);
                    }
                   else if ((image.getH(xx,yy)>=15&&image.getH(xx,yy)<=45)

                            &&(image.getS(xx,yy)>=43&&image.getS(xx,yy)<=255)

                            &&(image.getV(xx,yy)>=46&&image.getV(xx,yy)<=255)) {
                       // 黄
                        bits.set(x / 2, y);
                    }


                     else if (((image.getH(xx,yy))>=135&&(image.getH(xx,yy))<=160)
                             &&(image.getS(xx,yy)>=43&&image.getS(xx,yy)<=255)
                             &&(image.getV(xx,yy)>=46&&image.getV(xx,yy)<=255)) {
                        // 品红
                        bits.set(x / 2, y);
                    }
<<<<<<< HEAD
                     else if (((image.getH(xx,yy))>=0&&(image.getH(xx,yy))<=180)
                             &&(image.getS(xx,yy)>=0&&image.getS(xx,yy)<=255)
                             &&(image.getV(xx,yy)>=0&&image.getV(xx,yy)<=149)) {
                         // 黑
                         bits.set(x / 2, y);
                     }
=======
>>>>>>> 03b1350728049c5018ac862b6001afb0b15c7134
                    else{

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
        BitMatrix[] Bits = {bits,bits,bits};
<<<<<<< HEAD
       // outputimage(bits);
=======
        outputimage(bits);
>>>>>>> 03b1350728049c5018ac862b6001afb0b15c7134
        return Bits;
    }

    protected void outputimage(BitMatrix bitMatrix)  {
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
        File file = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"test.jpg"));
        //File myCaptureFile = new File( + fileName);

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

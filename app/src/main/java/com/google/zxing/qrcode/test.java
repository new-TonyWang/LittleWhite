package com.google.zxing.qrcode;

import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.detector.FinderPatternFinder;
import com.google.zxing.qrcode.detector.FinderPatternInfo;

import java.util.HashMap;
import java.util.Map;

public class test {
    public static void main(String[] args) {
         int[][] POSITION_DETECTION_PATTERN = {
                {0,0, 0, 0, 0, 0, 0, 0,0},
                {0,1, 1, 1, 1, 1, 1, 1,0},
                {0,1, 0, 0, 0, 0, 0, 1,0},
                {0,1, 0, 1, 1, 1, 0, 1,0},
                {0,1, 0, 1, 1, 1, 0, 1,0},
                {0,1, 0, 1, 1, 1, 0, 1,0},
                {0,1, 0, 0, 0, 0, 0, 1,0},
                {0,1, 1, 1, 1, 1, 1, 1,0},
                {0,0, 0, 0, 0, 0, 0, 0,0},
        };
        int dim = 9;
        BitMatrix matrix = new BitMatrix(dim,dim);
        for(int i =0;i<dim;i++ ){
            for (int j = 0;j<dim;j++){
                if(POSITION_DETECTION_PATTERN[i][j]==1) {
                    matrix.set(j,i);
                }
            }
        }
        System.out.print(matrix.toString());
       // FinderPatternFinder finder = new FinderPatternFinder(image, resultPointCallback);//找到定位图标!!!!重点来了
        HashMap<DecodeHintType, Object> decodeHints = new HashMap<DecodeHintType, Object>();
        decodeHints.put(DecodeHintType.TRY_HARDER,null);
        FinderPatternFinder finderPatternFinder = new FinderPatternFinder(matrix);
        try {
            FinderPatternInfo info = finderPatternFinder.find(decodeHints);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }

    }
}

package com.littlewhite.ColorCode;

public class HSVColorTable {
    private  int ColorTable[][][] = new int [181][256][256];
    public HSVColorTable(){
        for(int avgH = 0;avgH<181;avgH++){
            for(int avgS = 0;avgS < 256;avgS++){
                for(int avgV = 0;avgV<256;avgV++){
                    if((avgH <= 14 || avgH >= 159) && avgS >= 43 && avgV >= 46){//红
                        ColorTable[avgH][avgS][avgV] = 1;
                    }
                    else if(avgH >= 15 && avgH <= 45 && avgS >= 43 && avgV >= 46){//黄
                        ColorTable[avgH][avgS][avgV] = 2;
                    }
                    else if(avgH >= 46 && avgH <= 77 && avgS >= 43 && avgV >= 46){//绿
                        ColorTable[avgH][avgS][avgV] = 3;
                    }
                    else if(avgH >= 78 && avgH <= 109 && avgS >= 43 && avgV >= 46){//青
                        ColorTable[avgH][avgS][avgV] = 4;
                    }
                    else if(avgH >= 110 && avgH <= 134 && avgS >= 43 && avgV >= 46){//蓝
                        ColorTable[avgH][avgS][avgV] = 5;
                    }
                    else if(avgH >= 135 && avgH <= 160 && avgS >= 43 && avgV >= 46){//品红
                        ColorTable[avgH][avgS][avgV] = 6;
                    }
                    else if(avgV <= 149){//黑
                        ColorTable[avgH][avgS][avgV] = 7;
                    }
                    else {//白
                        ColorTable[avgH][avgS][avgV] = 8;
                    }
                }
            }
        }
    }

    public int getColor(int H,int S,int V) {
        return ColorTable[H][S][V];
    }
}

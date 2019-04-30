package com.google.zxing.common;

public class HsvData {
    private byte[] H;
    private byte[] S;
    private byte[] V;
    private BitMatrix bitMatrix;

    public int getH(int x,int y) {
        int a = H[(y-1)*bitMatrix.getWidth()+x]&0xff;
        return  a;
    }


    public int getS(int x,int y) {
        return  S[(y-1)*bitMatrix.getWidth()+x]&0xff;
    }



    public int getV(int x,int y) {
        return  V[(y-1)*bitMatrix.getWidth()+x]&0xff;
    }



    public BitMatrix getBitMatrix() {
        return bitMatrix;
    }

    public void setBitMatrix(BitMatrix bitMatrix) {
        this.bitMatrix = bitMatrix;
    }

    public HsvData(
            byte[] H,
             byte[] S,
             byte[] V,
             BitMatrix bitMatrix){
        this.H = H;
        this.S = S;
        this.V = V;
        this.bitMatrix = bitMatrix;
    }

}

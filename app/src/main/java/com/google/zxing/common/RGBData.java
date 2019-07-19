package com.google.zxing.common;

public class RGBData {
    private BitMatrix bitMatrixR;
    private BitMatrix bitMatrixG;
    private BitMatrix bitMatrixB;

    public RGBData(BitMatrix bitMatrixR, BitMatrix bitMatrixG, BitMatrix bitMatrixB) {
        this.bitMatrixR = bitMatrixR;
        this.bitMatrixG = bitMatrixG;
        this.bitMatrixB = bitMatrixB;
    }

    public BitMatrix getBitMatrixR() {
        return bitMatrixR;
    }

    public BitMatrix getBitMatrixG() {
        return bitMatrixG;
    }

    public BitMatrix getBitMatrixB() {
        return bitMatrixB;
    }
}

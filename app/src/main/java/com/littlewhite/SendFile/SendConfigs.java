package com.littlewhite.SendFile;

import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class SendConfigs {
   private int FPS;
   private String path;
   private int width;
   private int height;
   private int QRCodeType;
   private String errorCorrectionLevel;
   private int QRCodeCapacity;

    public String getPath() {
        return path;
    }

    public SendConfigs(String path, int FPS, int width, int height, int QRCodeType, String errorCorrectionLevel, int QRCodeCapacity) {
        this.path = path;
        this.FPS = FPS;
        this.width = width;
        this.height = height;
        this.QRCodeType = QRCodeType;
        this.errorCorrectionLevel = errorCorrectionLevel;
        this.QRCodeCapacity = QRCodeCapacity;
    }

    public SendConfigs(){

    }
    public int getFPS() {
        return FPS;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getQRCodeType() {
        return QRCodeType;
    }

    public Object getErrorCorrectionLevel() {
        switch (this.errorCorrectionLevel){
            case "L":
               return ErrorCorrectionLevel.L;
            case "M":
                return ErrorCorrectionLevel.M;
            case "Q":
                return ErrorCorrectionLevel.Q;
            case "H":
                return ErrorCorrectionLevel.H;
        }
        return errorCorrectionLevel;
    }

    public int getQRCodeCapacity() {
        return QRCodeCapacity;
    }
}

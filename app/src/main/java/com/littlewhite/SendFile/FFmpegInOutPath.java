package com.littlewhite.SendFile;

public class FFmpegInOutPath {
   private String inPath;
   private String outPath;

    public FFmpegInOutPath(String inPath, String outPath) {
        this.inPath = inPath;
        this.outPath = outPath;
    }

    public String getInPath() {
        return inPath;
    }

    public String getOutPath() {
        return outPath;
    }
}

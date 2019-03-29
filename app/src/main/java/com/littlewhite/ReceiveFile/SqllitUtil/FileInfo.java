package com.littlewhite.ReceiveFile.SqllitUtil;

public class FileInfo {
    private int ID;
    private String FileName;
    private int TotalSymbolNum;
    private String ReceivedSymbolNum;
    private Long commonFecOTI;
    private int schemeSpecFecOTI;
    private boolean HasComplete;

    /**
     * 从数据库读取信息用此构造
     * @param ID
     * @param FileName
     * @param TotalSymbolNum
     * @param ReceivedSymbolNum
     * @param commonFecOTI
     * @param schemeSpecFecOTI
     */
    public FileInfo(int ID, String FileName, int TotalSymbolNum, String ReceivedSymbolNum, Long commonFecOTI, int schemeSpecFecOTI,int HasComplete){
        this.ID = ID;
        this.FileName = FileName;
        this.TotalSymbolNum = TotalSymbolNum;
        this.ReceivedSymbolNum = ReceivedSymbolNum;
        this.commonFecOTI = commonFecOTI;
        this.schemeSpecFecOTI = schemeSpecFecOTI;
        if(HasComplete==1) {
            this.HasComplete = true;
        }else{
            this.HasComplete = false;
        }
    }

    /**
     * 接收新文件的时候使用
     * @param
     * @param FileName
     */
    public FileInfo( String FileName){
        this.ID = 0;
        this.FileName = FileName;
        this.TotalSymbolNum = 0;
        this.ReceivedSymbolNum = null;
        this.commonFecOTI = 0L;
        this.schemeSpecFecOTI = 0;
        this.HasComplete = false;

    }
    public void setReceivedSymbolNum(String receivedSymbolNum) {
        ReceivedSymbolNum = receivedSymbolNum;
    }

    public Long getCommonFecOTI() {
        return commonFecOTI;
    }

    public void setCommonFecOTI(Long commonFecOTI) {
        this.commonFecOTI = commonFecOTI;
    }

    public int getSchemeSpecFecOTI() {
        return schemeSpecFecOTI;
    }

    public void setSchemeSpecFecOTI(int schemeSpecFecOTI) {
        this.schemeSpecFecOTI = schemeSpecFecOTI;
    }


    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fileName) {
        FileName = fileName;
    }

    public int getTotalSymbolNum() {
        return TotalSymbolNum;
    }

    public void setTotalSymbolNum(int totalSymbolNum) {
        TotalSymbolNum = totalSymbolNum;
    }

    public String getReceivedSymbolNum() {
        return ReceivedSymbolNum;
    }

    public int[] getReceivedSymbolNumArray() {
        int l =ReceivedSymbolNum.lastIndexOf(",");
        String[] ReceivedSymbolString= ReceivedSymbolNum.split(",",ReceivedSymbolNum.length()-1);
        int length = ReceivedSymbolString.length;
        int[] ReceivedSymbolint = new int[length];
        for (int i = 0;i<length;i++) {
            ReceivedSymbolint[i] = Integer.valueOf(ReceivedSymbolString[i]);
        }
        return ReceivedSymbolint;
    }

}

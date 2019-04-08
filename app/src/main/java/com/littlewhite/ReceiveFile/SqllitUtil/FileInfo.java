package com.littlewhite.ReceiveFile.SqllitUtil;

public class FileInfo {
    private int ID;
    private String FileName;
   // private int TotalSymbolNum;
    //private String ReceivedSymbolNum;
    //private Long commonFecOTI;
    //private int schemeSpecFecOTI;
    private byte[] FECParameters;
    private boolean HasComplete;
    private int TotalSymbolNum;
    private int ReceivedNum;

    public int getTotalSymbolNum() {
        return TotalSymbolNum;
    }

    public void setTotalSymbolNum(int totalSymbolNum) {
        TotalSymbolNum = totalSymbolNum;
    }

    public int getReceivedNum() {
        return ReceivedNum;
    }

    public void setReceivedNum(int receivedNum) {
        ReceivedNum = receivedNum;
    }

    /**
     * 从数据库读取信息用此构造
     * @param ID
     * @param FileName
     *
     */
    public FileInfo(int ID, String FileName, int TotalSymbolNum,int ReceivedNum, byte[] FECParameters,int HasComplete){
        this.ID = ID;
        this.FileName = FileName;
       // this.TotalSymbolNum = TotalSymbolNum;
      //  this.ReceivedSymbolNum = ReceivedSymbolNum;
        this.FECParameters = FECParameters;
        //this.commonFecOTI = commonFecOTI;
        //this.schemeSpecFecOTI = schemeSpecFecOTI;
        this.TotalSymbolNum = TotalSymbolNum;
        this.ReceivedNum = ReceivedNum;
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
       // this.TotalSymbolNum = 0;
       // this.ReceivedSymbolNum = null;
        this.HasComplete = false;

    }
    public FileInfo( int ID,String FileName){
        this.ID = ID;
        this.FileName = FileName;
        // this.TotalSymbolNum = 0;
       // this.ReceivedSymbolNum = null;
        this.HasComplete = false;

    }
   /* public void setReceivedSymbolNum(String receivedSymbolNum) {
        ReceivedSymbolNum = receivedSymbolNum;
    }*/

    public byte[] getFECParameters() {
        return FECParameters;
    }

    public void setFECParameters(byte[] FECParameters) {
        this.FECParameters = FECParameters;
    }

    public boolean isHasComplete() {
        return HasComplete;
    }

    public void setHasComplete(boolean hasComplete) {
        HasComplete = hasComplete;
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

    /*public String getReceivedSymbolNum() {
        return ReceivedSymbolNum;
    }

    public int[] getReceivedSymbolNumArray() {
        //int l =ReceivedSymbolNum.lastIndexOf(",");
        String[] ReceivedSymbolString= ReceivedSymbolNum.split(",",ReceivedSymbolNum.length()-1);
        int length = ReceivedSymbolString.length;
        int[] ReceivedSymbolint = new int[length];
        for (int i = 0;i<length;i++) {
            ReceivedSymbolint[i] = Integer.valueOf(ReceivedSymbolString[i]);
        }
        return ReceivedSymbolint;
    }
*/
}

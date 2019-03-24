package net.fec.openrq;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import net.fec.openrq.encoder.DataEncoder;
import net.fec.openrq.encoder.SourceBlockEncoder;
import net.fec.openrq.parameters.FECParameters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

import static net.fec.openrq.parameters.ParameterChecker.minAllowedNumSourceBlocks;

public class sendtest implements  Runnable{
   // private byte[] data;
    private DataEncoder arrayDataEncoder;
    private LinkedBlockingQueue<byte[]> linkedBlockingQueue;
    private  FECParameters fecParameters;
    private Boolean isrunning;
    //public static final long MAX_DATA_LEN = maxAllowedDataLength(1830, MAX_DEC_MEM);
    public sendtest(LinkedBlockingQueue<byte[]> linkedBlockingQueue,boolean isrunning,Path zippath){
    this.linkedBlockingQueue = linkedBlockingQueue;
    this.isrunning = true;
       Path inputPath= Paths.get("D:\\workspace\\idea\\OpenRQ\\src\\image0.png");
      //  Path inputPath= Paths.get("D:\\workspace\\idea\\OpenRQ\\src\\image0.png");
        byte[] result =null;
        try {
            result = Files.readAllBytes(inputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.linkedBlockingQueue = linkedBlockingQueue;

        int num = minAllowedNumSourceBlocks(result.length,500);
        this.fecParameters = FECParameters.newParameters(result.length,500,num);
        /*try {
            this.linkedBlockingQueue.put(this.fecParameters.asArray());
            System.out.println("this.fecParameters.asArray().length="+this.fecParameters.asArray().length);//12
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        // this.fecParameters = FECParameters.deriveParameters(result.length,1830,18300);
        this.arrayDataEncoder = OpenRQ.newEncoder(result,0,fecParameters);//閸掓繂顫愰崠鏍︾娑擃亜濮炵�靛棗娅�
        //this.fecParameters.
    }

    @Override
    public void run() {

            encodeData(this.arrayDataEncoder);
            //this.arrayDataEncoder.sourceBlockIterable();


    }
    public  void encodeData(DataEncoder dataEnc) {
        int i = 0;
        for (SourceBlockEncoder sbEnc : dataEnc.sourceBlockIterable()) {
            encodeSourceBlock(sbEnc);
            i++;
        }
        //System.out.println(i);
    }
    private  void encodeSourceBlock(SourceBlockEncoder sbEnc) {
        int i = 0;
        int m = 0;
        int j = 0;
        byte[] data = null;
        synchronized (this.linkedBlockingQueue) {
            for (EncodingPacket pac : sbEnc.sourcePacketsIterable()) {//閻㈢喐鍨氶弫鐗堝祦閸栵拷
                // sendPacket(pac);
                data = pac.asArray();
               data =  mergedata(this.fecParameters.asArray(),data);
                    if((i&0x1)==0) {//婵傚洦鏆�
                        try {
                            this.linkedBlockingQueue.put(data);
                            if (!this.isrunning) {
                                return;
                            }
                            System.out.println("濠ф劖鏆熼幑锟�: " + m + " " + data.length);
                            System.out.println("pac.fecPayloadID():" + pac.fecPayloadID());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                   // this.linkedBlockingQueue.notifyAll();

                i++;
                m++;
            }

            //System.out.println(i);
            System.out.println("EncodingPacket"+i);
            // number of repair symbols
            int nr = numberOfRepairSymbols(i);
            System.out.println("nr:"+nr);
            // send nr repair symbols

            for (EncodingPacket pac : sbEnc.repairPacketsIterable(nr)) {//閻㈢喐鍨氱痪鐘绘晩閸栵拷
                //  sendPacket(pac);
                data = pac.asArray();
               // if ((j & 0x1) == 0) {//閸嬭埖鏆�
                        try {
                            data =  mergedata(this.fecParameters.asArray(),data);
                            this.linkedBlockingQueue.put(data);
                           System.out.println("RepairPacket" + j + " " + data.length);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //this.linkedBlockingQueue.notifyAll();
              //  }
                j++;

            }

            //System.out.println(j);
        }

        // encode the source block
    }
    private static int numberOfRepairSymbols(double EncodingPacketsize){
        return (int)Math.ceil(EncodingPacketsize*4/5);
    }

    /**
     * Encodes a specific source block from a data encoder.
     *
     * @param dataEnc
     *            A data encoder
     * @param sbn
     *            A "source block number": the identifier of the source block to be encoded
     */
    public  void encodeBlock(DataEncoder dataEnc, int sbn) {//閸旂姴鐦戦悧鐟扮暰閸э拷

        SourceBlockEncoder sbEnc = dataEnc.sourceBlock(sbn);
        encodeSourceBlock(sbEnc);
    }
    private byte[] mergedata(byte[] FEC,byte[] symbol) {
        byte[] c= Arrays.copyOf(FEC, FEC.length+symbol.length);

        //鐏忓摴閺佹壆绮嶅ǎ璇插閸掓澘鍑＄紒蹇撴儓閺堝¨閺佹壆绮嶉惃鍒㈤弫鎵矋娑擃厼骞�

        System.arraycopy(symbol, 0, c, FEC.length, symbol.length);
        return c;
    }



}


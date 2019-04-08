/*
 * Copyright 2014 OpenRQ Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.fec.openrq;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Objects;

import net.fec.openrq.util.io.BufferOperation;
import net.fec.openrq.util.io.ByteBuffers;


/**
 * Container of source symbol array data.
 */
final class ArraySourceSymbol implements SourceSymbol {

    /*
     * Requires valid parameters.
     */
    static ArraySourceSymbol newSymbol(byte[] srcDataArray, int symbolOff, int symbolSize) {//濮濄倗抦mutableList娑擃厾娈戦弫鐗堝祦閿涳拷

        final int transportSize = Math.min(symbolSize, srcDataArray.length - symbolOff);//最后一个容量小
        return new ArraySourceSymbol(srcDataArray, symbolOff, symbolSize, transportSize);
    }


    private final byte[] srcDataArray;//閺佺増宓佸┃锟�
    private final int symbolOff;//濮ｅ繋绔撮崸妤冩畱閸嬪繒些

    private final int codeSize;//閸ф娈戠�瑰綊鍣�
   // private FileChannel fileChannel;
    private final ByteBuffer transportBuf;//鐎涙ɑ婀侀弫鐗堝祦濠ф劕鎷皁ffset閻ㄥ垺uffer


    private ArraySourceSymbol(byte[] srcDataArray, int symbolOff, int codeSize, int transportSize) {

        this.srcDataArray = Objects.requireNonNull(srcDataArray);
        this.symbolOff = symbolOff;
       // this.fileChannel = fileChannel;
        this.codeSize = codeSize;

        this.transportBuf = prepareTransportBuffer(srcDataArray, symbolOff, transportSize);//文件数据，offset，传输大小
    }


    private static ByteBuffer prepareTransportBuffer(byte[] array, int off, int len) {

        // need to return a slice of the wrapped buffer,
        // otherwise the buffer position will be equal to off
        return ByteBuffer.wrap(array, off, len).slice();
    }

    @Override
    public int codeSize() {

        return codeSize;
    }

    @Override
    public void getCodeData(ByteBuffer dst) {

        getCodeData(dst, BufferOperation.ADVANCE_POSITION);
    }

    @Override
    public void getCodeData(ByteBuffer dst, BufferOperation op) {

        final int pos = dst.position();
        final int lim = dst.limit();
        final int remaining = lim - pos;
        if (remaining < codeSize()) throw new BufferOverflowException();

        dst.put(srcDataArray, symbolOff, transportSize());
        ByteBuffers.putZeros(dst, codeSize() - transportSize());

        op.apply(dst, pos, dst.position());
    }

    @Override
    public void putCodeData(ByteBuffer src) {

        putCodeData(src, BufferOperation.ADVANCE_POSITION);
    }

    @Override
    public void putCodeData(ByteBuffer src, BufferOperation op) {

        //final int pos = src.position();
       // System.out.println("前"+src.position());
       // src.get(srcDataArray, symbolOff, transportSize());
       // System.out.println("后"+src.position());
        //System.out.println("codeSize"+codeSize);
       // src.limit(transportSize());
       /* try {
            //src.flip();
            src.position(0);
            //fileChannel.position(symbolOff);
            //fileChannel.write(src);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        final int pos = src.position();

        src.get(srcDataArray, symbolOff, transportSize());

        src.position(pos + codeSize()); // always advance by codeSize() bytes

        op.apply(src, pos, src.position());
        //src.position(pos + codeSize()); // always advance by codeSize() bytes
        src.position(0);
        //op.apply(src, pos, src.position());
    }

    @Override
    public int transportSize() {

        return transportBuf.remaining();
    }

    @Override
    public ByteBuffer transportData() {

        return transportBuf.asReadOnlyBuffer();
    }

    @Override
    public void putTransportData(ByteBuffer src) {

        putTransportData(src, BufferOperation.ADVANCE_POSITION);
    }

    @Override
    public void putTransportData(ByteBuffer src, BufferOperation op) {

        final int pos = src.position();
        //System.out.println(transportSize());

        src.get(srcDataArray, symbolOff, transportSize());
       //src.flip();

        /*try {
            //src.limit(transportSize());
            src.position(0);
            //this.transportBuf.;
            //this.transportBuf.position(0);

           // fileChannel.
           // fileChannel.write(src);
            //src.array();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        op.apply(src, pos, src.position());
        src.position(0);
    }
}

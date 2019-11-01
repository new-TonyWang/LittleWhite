/*
 * Copyright 2007 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.qrcode.decoder;

import android.nfc.Tag;
import android.util.Log;

import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotDataException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.common.DetectorResult;
import com.google.zxing.common.reedsolomon.GenericGF;
import com.google.zxing.common.reedsolomon.ReedSolomonDecoder;
import com.google.zxing.common.reedsolomon.ReedSolomonException;
import com.littlewhite.ColorCode.HSVColorTable;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static com.littlewhite.SendFile.AlbumNotifier.TAG;

/**
 * <p>The main class which implements QR Code decoding -- as opposed to locating and extracting
 * the QR Code from an image.</p>
 *
 * @author Sean Owen
 */
public final class Decoder {

  private final ReedSolomonDecoder rsDecoder;

  public Decoder() {
    rsDecoder = new ReedSolomonDecoder(GenericGF.QR_CODE_FIELD_256);
  }

  public DecoderResult decode(boolean[][] image) throws ChecksumException, FormatException, NotDataException {
    return decode(image, null);
  }

  /**
   * <p>Convenience method that can decode a QR Code represented as a 2D array of booleans.
   * "true" is taken to mean a black module.</p>
   *
   * @param image booleans representing white/black QR Code modules
   * @param hints decoding hints that should be used to influence decoding
   * @return text and bytes encoded within the QR Code
   * @throws FormatException if the QR Code cannot be decoded
   * @throws ChecksumException if error correction fails
 * @throws NotDataException 
   */
  public DecoderResult decode(boolean[][] image, Map<DecodeHintType,?> hints)
      throws ChecksumException, FormatException, NotDataException {
    return decode(BitMatrix.parse(image), hints);
  }

  public DecoderResult decode(BitMatrix bits) throws ChecksumException, FormatException {
    return decode(bits, null);
  }

  /**
   * <p>Decodes a QR Code represented as a {@link BitMatrix}. A 1 or "true" is taken to mean a black module.</p>
   *
   * @param bits booleans representing white/black QR Code modules
   * @param hints decoding hints that should be used to influence decoding
   * @return text and bytes encoded within the QR Code
   * @throws FormatException if the QR Code cannot be decoded
   * @throws ChecksumException if error correction fails
 * @throws NotDataException 
   */
  public DecoderResult decode(BitMatrix bits, Map<DecodeHintType,?> hints)
      throws FormatException, ChecksumException {

    // Construct a parser and read version, error-correction level
    BitMatrixParser parser = new BitMatrixParser(bits);
   
    FormatException fe = null;
    ChecksumException ce = null;
    try {
      return decode(parser, hints);
    } catch (FormatException e) {
      fe = e;
    } catch (ChecksumException e) {
      ce = e;
    }

    try {

      // Revert the bit matrix
      parser.remask();

      // Will be attempting a mirrored reading of the version and format info.
      parser.setMirror(true);

      // Preemptively read the version.
      parser.readVersion();

      // Preemptively read the format information.
      parser.readFormatInformation();

      /*
       * Since we're here, this means we have successfully detected some kind
       * of version and format information when mirrored. This is a good sign,
       * that the QR code may be mirrored, and we should try once more with a
       * mirrored content.
       */
      // Prepare for a mirrored reading.
      parser.mirror();

      DecoderResult result = decode(parser, hints);

      // Success! Notify the caller that the code was mirrored.
      result.setOther(new QRCodeDecoderMetaData(true));

      return result;

    } catch (FormatException | ChecksumException e) {
      // Throw the exception from the original reading
      if (fe != null) {
        throw fe;
      }
      throw ce; // If fe is null, this can't be
    }
  }

  /**
   * 解析彩色二维码专用
   *
   * @param hints
   * @return
   * @throws FormatException
   * @throws ChecksumException
   */
  public DecoderResult decodeColorCode(DetectorResult[] detectorResults, Map<DecodeHintType,?> hints)
          throws FormatException, ChecksumException {

    // Construct a parser and read version, error-correction level
    //BitMatrixParser parser = new BitMatrixParser(bits);
    BitMatrixParser[] parsers = new BitMatrixParser[3];
    for(int i = 0;i<3;i++){
      parsers[i] = new BitMatrixParser(detectorResults[i].getBits());
    }

    FormatException fe = null;
    ChecksumException ce = null;
    try {
      return decodeColorCode(parsers, hints);
    } catch (FormatException e) {
      fe = e;
      throw fe;
    } catch (ChecksumException e) {
      ce = e;
      throw ce;
    }

  }
  private DecoderResult decode(BitMatrixParser parser, Map<DecodeHintType,?> hints)
      throws FormatException, ChecksumException {
    long start = System.currentTimeMillis();
    FormatInformation formatInfo = parser.readFormatInformation();
    Version version = parser.readVersion();
    ErrorCorrectionLevel ecLevel = formatInfo.getErrorCorrectionLevel();//纠错版本
    DataMask dataMask = DataMask.values()[formatInfo.getDataMask()];
    //Version version = parser.readVersion();
   // ErrorCorrectionLevel ecLevel = parser.readFormatInformation().getErrorCorrectionLevel();//纠错版本

    // Read codewords
    byte[] codewords = parser.readCodewords(version,dataMask);//读取位矩阵中表示查找器模式的bit，按顺序排列，以重建二维码中包含的码字字节。
    // Separate into data blocks
   // DataBlock[] dataBlocks = DataBlock.getDataBlocks(codewords, version, ecLevel);
    DataBlock[] dataBlocks = DataBlock.getDataBlocksInNewWay(codewords, version, ecLevel);
    // Count total number of data bytes
    int totalBytes = 0;
    for (DataBlock dataBlock : dataBlocks) {
      totalBytes += dataBlock.getNumDataCodewords();
    }
    byte[] resultBytes = new byte[totalBytes];
    int resultOffset = 0;
    long end = System.currentTimeMillis();
    Log.i(TAG,"数据提取时间:"+(end-start)+"ms");
    start= System.currentTimeMillis();

    // Error-correct and copy data blocks together into a stream of bytes
    for (DataBlock dataBlock : dataBlocks) {
      byte[] codewordBytes = dataBlock.getCodewords();
      int numDataCodewords = dataBlock.getNumDataCodewords();
      correctErrors(codewordBytes, numDataCodewords);
      for (int i = 0; i < numDataCodewords; i++) {
        resultBytes[resultOffset++] = codewordBytes[i];
      }
    }
end =  System.currentTimeMillis();
    Log.i(TAG,"纠错时间:"+(end-start)+"ms");
    // Decode the contents of that stream of bytes

    if(hints!=null&&hints.containsKey(DecodeHintType.FILEDATA)) {
    	return	DecodedBitStreamParser.decodetobyte(resultBytes, version, ecLevel, hints);
    }
    return DecodedBitStreamParser.decode(resultBytes, version, ecLevel, hints);
  }

  private DecoderResult decodeColorCode(BitMatrixParser[] parsers, Map<DecodeHintType,?> hints)
          throws FormatException, ChecksumException {
    FormatInformation formatInfo = parsers[0].readFormatInformation();
    Version version = parsers[0].readVersion();
    ErrorCorrectionLevel ecLevel = formatInfo.getErrorCorrectionLevel();//纠错版本
    DataMask dataMask = DataMask.values()[formatInfo.getDataMask()];
    LinkedBlockingQueue<byte[]> resultThreeBytes = new LinkedBlockingQueue<>();
    for(int j = 0;j<3;j++) {
      // Read codewords
      byte[] codewords = parsers[j].readCodewords(version,dataMask);//读取位矩阵中表示查找器模式的bit，按顺序排列，以重建二维码中包含的码字字节。
      // Separate into data blocks
      DataBlock[] dataBlocks = DataBlock.getDataBlocks(codewords, version, ecLevel);

      // Count total number of data bytes
      int totalBytes = 0;
      for (DataBlock dataBlock : dataBlocks) {
        totalBytes += dataBlock.getNumDataCodewords();
      }
      byte[] resultBytes = new byte[totalBytes];
      int resultOffset = 0;

      // Error-correct and copy data blocks together into a stream of bytes
      for (DataBlock dataBlock : dataBlocks) {
        byte[] codewordBytes = dataBlock.getCodewords();
        int numDataCodewords = dataBlock.getNumDataCodewords();
        correctErrors(codewordBytes, numDataCodewords);
        for (int i = 0; i < numDataCodewords; i++) {
          resultBytes[resultOffset++] = codewordBytes[i];
        }
      }
      try {
        resultThreeBytes.put(resultBytes);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    // Decode the contents of that stream of bytes
      return	DecodedBitStreamParser.decodeColorpayload(resultThreeBytes, version, ecLevel, hints);
  }
  /**
   * <p>Given data and error-correction codewords received, possibly corrupted by errors, attempts to
   * correct the errors in-place using Reed-Solomon error correction.</p>
   *
   * @param codewordBytes data and error correction codewords
   * @param numDataCodewords number of codewords that are data bytes
   * @throws ChecksumException if error correction fails
   */
  private void correctErrors(byte[] codewordBytes, int numDataCodewords) throws ChecksumException {
    int numCodewords = codewordBytes.length;
    // First read into an array of ints
    int[] codewordsInts = new int[numCodewords];
    for (int i = 0; i < numCodewords; i++) {
      codewordsInts[i] = codewordBytes[i] & 0xFF;
    }
    try {
      rsDecoder.decode(codewordsInts, codewordBytes.length - numDataCodewords);
    } catch (ReedSolomonException ignored) {
      throw ChecksumException.getChecksumInstance();
    }
    // Copy back into array of bytes -- only need to worry about the bytes that were data
    // We don't care about errors in the error-correction codewords
    for (int i = 0; i < numDataCodewords; i++) {
      codewordBytes[i] = (byte) codewordsInts[i];
    }
  }

}

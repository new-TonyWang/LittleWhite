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

package com.google.zxing.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotDataException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.common.DetectorResult;
import com.google.zxing.common.RGBData;
import com.google.zxing.qrcode.decoder.Decoder;
import com.google.zxing.qrcode.decoder.QRCodeDecoderMetaData;
import com.google.zxing.qrcode.detector.ColorDetector;
import com.google.zxing.qrcode.detector.Detector;
import com.google.zxing.qrcode.detector.RGBDetector;
import com.littlewhite.ColorCode.HSVColorTable;

import java.util.List;
import java.util.Map;

/**
 * This implementation can detect and decode QR Codes in an image.
 *
 * @author Sean Owen
 */
public class QRCodeReader implements Reader {

  private static final ResultPoint[] NO_POINTS = new ResultPoint[0];

  private final Decoder decoder = new Decoder();

  protected final Decoder getDecoder() {
    return decoder;
  }

  /**
   * Locates and decodes a QR code in an image.
   *
   * @return a String representing the content encoded by the QR code
   * @throws NotFoundException if a QR code cannot be found
   * @throws FormatException if a QR code cannot be decoded
   * @throws ChecksumException if error correction fails
   */
  @Override
  public Result decode(BinaryBitmap image) throws NotFoundException, ChecksumException, FormatException {
    return decode(image, null);
  }
  //定位二维码
  public final DetectorResult LocateQRcode(BinaryBitmap image, Map<DecodeHintType,?> hints) throws NotFoundException, ChecksumException, FormatException {
	  DetectorResult detectorResult = new Detector(image.getBlackMatrix()).detect(hints);//定位二维码
	return detectorResult;   
  }
  //和定位二维码同时使用
  public final Result decode(DetectorResult detectorResult,Map<DecodeHintType,?> hints) throws NotFoundException, ChecksumException, FormatException {
	    DecoderResult decoderResult;
	    ResultPoint[] points;
	   decoderResult = decoder.decode(detectorResult.getBits(), hints);//解析二维码
	      points = detectorResult.getPoints();
	    // If the code was mirrored: swap the bottom-left and the top-right points.
	    if (decoderResult.getOther() instanceof QRCodeDecoderMetaData) {
	      ((QRCodeDecoderMetaData) decoderResult.getOther()).applyMirroredCorrection(points);
	    }

	    Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), points, BarcodeFormat.QR_CODE);
	    List<byte[]> byteSegments = decoderResult.getByteSegments();
	    if (byteSegments != null) {
	      result.putMetadata(ResultMetadataType.BYTE_SEGMENTS, byteSegments);
	    }
	    String ecLevel = decoderResult.getECLevel();
	    if (ecLevel != null) {
	      result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, ecLevel);
	    }
	    if (decoderResult.hasStructuredAppend()) {
	      result.putMetadata(ResultMetadataType.STRUCTURED_APPEND_SEQUENCE,
	                         decoderResult.getStructuredAppendSequenceNumber());
	      result.putMetadata(ResultMetadataType.STRUCTURED_APPEND_PARITY,
	                         decoderResult.getStructuredAppendParity());
	    }
	    return result;
	  }
	  
  
  @Override
  public final Result decode(BinaryBitmap image, Map<DecodeHintType,?> hints)
      throws NotFoundException, ChecksumException, FormatException {
    DecoderResult decoderResult;
    ResultPoint[] points;
    if (hints != null && hints.containsKey(DecodeHintType.PURE_BARCODE)) {
      BitMatrix bits = extractPureBits(image.getBlackMatrix());
      decoderResult = decoder.decode(bits);
      points = NO_POINTS;
    } else {
      DetectorResult detectorResult = new Detector(image.getBlackMatrix()).detect(hints);//寻找二维码定位码，透视变换，将二维码点阵化
      decoderResult = decoder.decode(detectorResult.getBits(), hints);//解析二维码
      points = detectorResult.getPoints();
    }

    // If the code was mirrored: swap the bottom-left and the top-right points.
    if (decoderResult.getOther() instanceof QRCodeDecoderMetaData) {
      ((QRCodeDecoderMetaData) decoderResult.getOther()).applyMirroredCorrection(points);
    }

    Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), points, BarcodeFormat.QR_CODE);
    List<byte[]> byteSegments = decoderResult.getByteSegments();
    if (byteSegments != null) {
      result.putMetadata(ResultMetadataType.BYTE_SEGMENTS, byteSegments);
    }
    String ecLevel = decoderResult.getECLevel();
    if (ecLevel != null) {
      result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, ecLevel);
    }
    if (decoderResult.hasStructuredAppend()) {
      result.putMetadata(ResultMetadataType.STRUCTURED_APPEND_SEQUENCE,
                         decoderResult.getStructuredAppendSequenceNumber());
      result.putMetadata(ResultMetadataType.STRUCTURED_APPEND_PARITY,
                         decoderResult.getStructuredAppendParity());
    }
    return result;
  }
  /**
   * 解析成byte数组
   * @param image
   * @param hints
   * @return
   * @throws NotFoundException
   * @throws ChecksumException
   * @throws FormatException
 * @throws NotDataException 
   */
  public final Result decodetobytearry(BinaryBitmap image, Map<DecodeHintType,?> hints)
	      throws NotFoundException, ChecksumException, FormatException {
	    DecoderResult decoderResult;
	    ResultPoint[] points;
	      DetectorResult detectorResult = new Detector(image.getBlackMatrix()).detect(hints);//image.getBlackMatrix()方法将图片二值化，一个耗时操作。寻找二维码定位码，透视变换，将二维码点阵化
	      decoderResult = decoder.decode(detectorResult.getBits(), hints);//解析二维码
	      //System.out.println("bits宽:"+detectorResult.getBits().getWidth()+"bits高:"+detectorResult.getBits().getHeight());//获取到的是二维码的行列数
	      points = detectorResult.getPoints();
	    // If the code was mirrored: swap the bottom-left and the top-right points.
	      /*
	    if (decoderResult.getOther() instanceof QRCodeDecoderMetaData) {
	      ((QRCodeDecoderMetaData) decoderResult.getOther()).applyMirroredCorrection(points);
	    }*/
	    Result result = new Result( decoderResult.getRawBytes(), points, BarcodeFormat.QR_CODE,decoderResult.getMode());
	    List<byte[]> byteSegments = decoderResult.getByteSegments();
	    if (byteSegments != null) {
	      result.putMetadata(ResultMetadataType.BYTE_SEGMENTS, byteSegments);
	    }
	   // String ecLevel = decoderResult.getECLevel();
	     // result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, ecLevel);
	    
	    /*
	    if (decoderResult.hasStructuredAppend()) {
	      result.putMetadata(ResultMetadataType.STRUCTURED_APPEND_SEQUENCE,
	                         decoderResult.getStructuredAppendSequenceNumber());
	      result.putMetadata(ResultMetadataType.STRUCTURED_APPEND_PARITY,
	                         decoderResult.getStructuredAppendParity());
	    }
	    */
	    return result;
	  }
    public final Result decodecolorCode(BinaryBitmap image, Map<DecodeHintType,?> hints, HSVColorTable hsvColorTable)
            throws NotFoundException, ChecksumException, FormatException {
        DecoderResult decoderResult;
        ResultPoint[] points;
        DetectorResult[] detectorResults = new ColorDetector(image.gethsvData()).detect(hints,hsvColorTable);//寻找二维码定位码，透视变换，将二维码点阵化
           decoderResult = decoder.decodeColorCode(detectorResults, hints);//解析二维码
           //System.out.println("bits宽:"+detectorResult.getBits().getWidth()+"bits高:"+detectorResult.getBits().getHeight());//获取到的是二维码的行列数
           points = detectorResults[0].getPoints();
           // If the code was mirrored: swap the bottom-left and the top-right points.
	      /*
	    if (decoderResult.getOther() instanceof QRCodeDecoderMetaData) {
	      ((QRCodeDecoderMetaData) decoderResult.getOther()).applyMirroredCorrection(points);
	    }*/
           Result result = new Result(decoderResult.getRawBytes(), points, BarcodeFormat.QR_CODE, decoderResult.getMode());
          // results[i] = result;
           List<byte[]> byteSegments = decoderResult.getByteSegments();
           if (byteSegments != null) {
               result.putMetadata(ResultMetadataType.BYTE_SEGMENTS, byteSegments);
           }
           // String ecLevel = decoderResult.getECLevel();
           // result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, ecLevel);

	    /*
	    if (decoderResult.hasStructuredAppend()) {
	      result.putMetadata(ResultMetadataType.STRUCTURED_APPEND_SEQUENCE,
	                         decoderResult.getStructuredAppendSequenceNumber());
	      result.putMetadata(ResultMetadataType.STRUCTURED_APPEND_PARITY,
	                         decoderResult.getStructuredAppendParity());
	    }

	    */

        return result;
    }
    public final Result decodeRGBCode(RGBData image, Map<DecodeHintType,?> hints)
            throws NotFoundException, ChecksumException, FormatException {
        DecoderResult decoderResult;
        ResultPoint[] points;
        DetectorResult[] detectorResults = new RGBDetector(image).detect(hints);//寻找二维码定位码，透视变换，将二维码点阵化
        decoderResult = decoder.decodeColorCode(detectorResults, hints);//解析二维码
        //System.out.println("bits宽:"+detectorResult.getBits().getWidth()+"bits高:"+detectorResult.getBits().getHeight());//获取到的是二维码的行列数
        points = detectorResults[0].getPoints();
        // If the code was mirrored: swap the bottom-left and the top-right points.
	      /*
	    if (decoderResult.getOther() instanceof QRCodeDecoderMetaData) {
	      ((QRCodeDecoderMetaData) decoderResult.getOther()).applyMirroredCorrection(points);
	    }*/
        Result result = new Result(decoderResult.getRawBytes(), points, BarcodeFormat.QR_CODE, decoderResult.getMode());
        // results[i] = result;
        List<byte[]> byteSegments = decoderResult.getByteSegments();
        if (byteSegments != null) {
            result.putMetadata(ResultMetadataType.BYTE_SEGMENTS, byteSegments);
        }
        // String ecLevel = decoderResult.getECLevel();
        // result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, ecLevel);

	    /*
	    if (decoderResult.hasStructuredAppend()) {
	      result.putMetadata(ResultMetadataType.STRUCTURED_APPEND_SEQUENCE,
	                         decoderResult.getStructuredAppendSequenceNumber());
	      result.putMetadata(ResultMetadataType.STRUCTURED_APPEND_PARITY,
	                         decoderResult.getStructuredAppendParity());
	    }

	    */

        return result;
    }

  @Override
  public void reset() {
    // do nothing
  }
    
    
    
  /**
   * This method detects a code in a "pure" image -- that is, pure monochrome image
   * which contains only an unrotated, unskewed, image of a code, with some white border
   * around it. This is a specialized method that works exceptionally fast in this special
   * case.
   *
   * @
   */
  private static BitMatrix extractPureBits(BitMatrix image) throws NotFoundException {

    int[] leftTopBlack = image.getTopLeftOnBit();
    int[] rightBottomBlack = image.getBottomRightOnBit();
    if (leftTopBlack == null || rightBottomBlack == null) {
      throw NotFoundException.getNotFoundInstance();
    }

    float moduleSize = moduleSize(leftTopBlack, image);

    int top = leftTopBlack[1];
    int bottom = rightBottomBlack[1];
    int left = leftTopBlack[0];
    int right = rightBottomBlack[0];

    // Sanity check!
    if (left >= right || top >= bottom) {
      throw NotFoundException.getNotFoundInstance();
    }

    if (bottom - top != right - left) {
      // Special case, where bottom-right module wasn't black so we found something else in the last row
      // Assume it's a square, so use height as the width
      right = left + (bottom - top);
      if (right >= image.getWidth()) {
        // Abort if that would not make sense -- off image
        throw NotFoundException.getNotFoundInstance();
      }
    }

    int matrixWidth = Math.round((right - left + 1) / moduleSize);
    int matrixHeight = Math.round((bottom - top + 1) / moduleSize);
    if (matrixWidth <= 0 || matrixHeight <= 0) {
      throw NotFoundException.getNotFoundInstance();
    }
    if (matrixHeight != matrixWidth) {
      // Only possibly decode square regions
      throw NotFoundException.getNotFoundInstance();
    }

    // Push in the "border" by half the module width so that we start
    // sampling in the middle of the module. Just in case the image is a
    // little off, this will help recover.
    int nudge = (int) (moduleSize / 2.0f);
    top += nudge;
    left += nudge;

    // But careful that this does not sample off the edge
    // "right" is the farthest-right valid pixel location -- right+1 is not necessarily
    // This is positive by how much the inner x loop below would be too large
    int nudgedTooFarRight = left + (int) ((matrixWidth - 1) * moduleSize) - right;
    if (nudgedTooFarRight > 0) {
      if (nudgedTooFarRight > nudge) {
        // Neither way fits; abort
        throw NotFoundException.getNotFoundInstance();
      }
      left -= nudgedTooFarRight;
    }
    // See logic above
    int nudgedTooFarDown = top + (int) ((matrixHeight - 1) * moduleSize) - bottom;
    if (nudgedTooFarDown > 0) {
      if (nudgedTooFarDown > nudge) {
        // Neither way fits; abort
        throw NotFoundException.getNotFoundInstance();
      }
      top -= nudgedTooFarDown;
    }

    // Now just read off the bits
    BitMatrix bits = new BitMatrix(matrixWidth, matrixHeight);
    for (int y = 0; y < matrixHeight; y++) {
      int iOffset = top + (int) (y * moduleSize);
      for (int x = 0; x < matrixWidth; x++) {
        if (image.get(left + (int) (x * moduleSize), iOffset)) {
          bits.set(x, y);
        }
      }
    }
    return bits;
  }

  private static float moduleSize(int[] leftTopBlack, BitMatrix image) throws NotFoundException {
    int height = image.getHeight();
    int width = image.getWidth();
    int x = leftTopBlack[0];
    int y = leftTopBlack[1];
    boolean inBlack = true;
    int transitions = 0;
    while (x < width && y < height) {
      if (inBlack != image.get(x, y)) {
        if (++transitions == 5) {
          break;
        }
        inBlack = !inBlack;
      }
      x++;
      y++;
    }
    if (x == width || y == height) {
      throw NotFoundException.getNotFoundInstance();
    }
    return (x - leftTopBlack[0]) / 7.0f;
  }

}

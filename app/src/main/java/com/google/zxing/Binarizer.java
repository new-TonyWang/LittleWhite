/*
 * Copyright 2009 ZXing authors
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

package com.google.zxing;

import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HsvData;

/**
 * This class hierarchy provides a set of methods to convert luminance data to 1 bit data.
 * It allows the algorithm to vary polymorphically, for example allowing a very expensive
 * thresholding technique for servers and a fast one for mobile. It also permits the implementation
 * to vary, e.g. a JNI version for Android and a Java fallback version for other platforms.
 *这个类层次结构提供了一组将亮度数据转换为1位数据的方法。
 *它允许算法以多形态变化，例如，允许服务器使用非常昂贵的阈值技术，允许移动设备使用快速的阈值技术。
 *它还允许实现有所不同，例如针对Android的JNI版本和针对其他平台的Java回退版本。
 * @author dswitkin@google.com (Daniel Switkin)
 */
public abstract class Binarizer {

  private final LuminanceSource source;

  protected Binarizer(LuminanceSource source) {
    this.source = source;
  }

  public final LuminanceSource getLuminanceSource() {
    return source;
  }

  /**
   * Converts one row of luminance data to 1 bit data. May actually do the conversion, or return
   * cached data. Callers should assume this method is expensive and call it as seldom as possible.
   * This method is intended for decoding 1D barcodes and may choose to apply sharpening.
   * For callers which only examine one row of pixels at a time, the same BitArray should be reused
   * and passed in with each call for performance. However it is legal to keep more than one row
   * at a time if needed.
   *
   *将一行亮度数据转换为1位数据。可以实际执行转换，或者返回缓存的数据。
   *调用者应该假设这个方法开销很大，并且尽可能少调用它。此方法适用于解码一维条码，可选择锐化。
   *对于每次只检查一行像素的调用者，应该重用相同的位数组，并在每次调用时传递相同的位数组以提高性能。
   *但是，如果需要，一次保留多行是合法的。
   * @param y The row to fetch, which must be in [0, bitmap height)
   * @param row An optional preallocated array. If null or too small, it will be ignored.
   *            If used, the Binarizer will call BitArray.clear(). Always use the returned object.
   * @return The array of bits for this row (true means black).
   * @throws NotFoundException if row can't be binarized
   */
  public abstract BitArray getBlackRow(int y, BitArray row) throws NotFoundException;

  /**
   * Converts a 2D array of luminance data to 1 bit data. As above, assume this method is expensive
   * and do not call it repeatedly. This method is intended for decoding 2D barcodes and may or
   * may not apply sharpening. Therefore, a row from this matrix may not be identical to one
   * fetched using getBlackRow(), so don't mix and match between them.
   *
   * @return The 2D array of bits for the image (true means black).
   * @throws NotFoundException if image can't be binarized to make a matrix
   */
  public abstract BitMatrix getBlackMatrix() throws NotFoundException;
  public abstract HsvData getHsvData() throws NotFoundException;
  /**
   * Creates a new object with the same type as this Binarizer implementation, but with pristine
   * state. This is needed because Binarizer implementations may be stateful, e.g. keeping a cache
   * of 1 bit data. See Effective Java for why we can't use Java's clone() method.
   *
   * @param source The LuminanceSource this Binarizer will operate on.
   * @return A new concrete Binarizer implementation object.
   */
  public abstract Binarizer createBinarizer(LuminanceSource source);

  public final int getWidth() {
    return source.getWidth();
  }

  public final int getHeight() {
    return source.getHeight();
  }

}

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

package com.google.zxing.common;


import java.io.File;
import java.io.IOException;



import com.google.zxing.Binarizer;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;

/**
 * This class implements a local thresholding algorithm, which while slower than the
 * GlobalHistogramBinarizer, is fairly efficient for what it does. It is designed for
 * high frequency images of barcodes with black data on white backgrounds. For this application,
 * it does a much better job than a global blackpoint with severe shadows and gradients.
 * However it tends to produce artifacts on lower frequency images and is therefore not
 * a good general purpose binarizer for uses outside ZXing.
 *
 * This class extends GlobalHistogramBinarizer, using the older histogram approach for 1D readers,
 * and the newer local approach for 2D readers. 1D decoding using a per-row histogram is already
 * inherently local, and only fails for horizontal gradients. We can revisit that problem later,
 * but for now it was not a win to use local blocks for 1D.
 *
 * This Binarizer is the default for the unit tests and the recommended class for library users.
 *这个类实现了一个局部阈值化算法，虽然它比GlobalHistogramBinarizer慢，但是对于它所做的工作是相当有效的。
 *它是专为具有白色背景的黑色数据的条形码的高频图像设计的。对于这个应用程序，
 *它比带有严重阴影和渐变的全局黑点做得更好。然而，它容易在低频图像上产生伪影，
 *因此不是ZXing以外的一种通用的二值化器。该类扩展了GlobalHistogramBinarizer，
 *对一维阅读器使用较老的直方图方法，对二维阅读器使用较新的本地方法。
 *使用每行直方图的1D解码本来就是局部的，只在水平梯度上失败。
 *我们可以稍后再讨论这个问题，但是目前使用本地块进行1D并不是一种胜利。
 *这个二进制器是单元测试的默认值，也是库用户推荐的类。
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class HybridBinarizer extends GlobalHistogramBinarizer {

  // This class uses 5x5 blocks to compute local luminance, where each block is 8x8 pixels.
  // So this is the smallest dimension in each axis we can accept.
  private static final int BLOCK_SIZE_POWER = 3;
  private static final int BLOCK_SIZE = 1 << BLOCK_SIZE_POWER; // ...0100...00，1往左移动3位==8
  private static final int BLOCK_SIZE_MASK = BLOCK_SIZE - 1;   // ...0011...11，==7
  private static final int MINIMUM_DIMENSION = BLOCK_SIZE * 5;//==40
  private static final int MIN_DYNAMIC_RANGE = 24;//

  private BitMatrix matrix;

  public HybridBinarizer(LuminanceSource source) {
    super(source);
  }

  /**
   * Calculates the final BitMatrix once for all requests. This could be called once from the
   * constructor instead, but there are some advantages to doing it lazily, such as making
   * profiling easier, and not doing heavy lifting when callers don't expect it.
   * 对所有请求计算一次最终的bits矩阵。
   * 这可以从构造函数中调用一次，但是延迟执行也有一些优点，例如使分析更容易，
   * 并且在调用者不期望的情况下不执行繁重的提升工作。
   */
  //在qrcode解析的时候设置阈值
  @Override
  public BitMatrix getBlackMatrix() throws NotFoundException {
    if (matrix != null) {
      return matrix;
    }
    LuminanceSource source = getLuminanceSource();
    int width = source.getWidth();
    int height = source.getHeight();
    if (width >= MINIMUM_DIMENSION && height >= MINIMUM_DIMENSION) {
      byte[] luminances = source.getMatrix();//灰度数组
      int subWidth = width >> BLOCK_SIZE_POWER;
      if ((width & BLOCK_SIZE_MASK) != 0) {
        subWidth++;
      }
      int subHeight = height >> BLOCK_SIZE_POWER;
      if ((height & BLOCK_SIZE_MASK) != 0) {
        subHeight++;
      }
      int[][] blackPoints = calculateBlackPoints(luminances, subWidth, subHeight, width, height);

      BitMatrix newMatrix = new BitMatrix(width, height);
      calculateThresholdForBlock(luminances, subWidth, subHeight, width, height, blackPoints, newMatrix);
      matrix = newMatrix;
    } else {
      // If the image is too small, fall back to the global histogram approach.
      matrix = super.getBlackMatrix();
    }
    //System.out.println("二值化后的BitMatrix宽:"+matrix.getWidth()+"，高:"+matrix.getHeight());

//    try {
//		outputimage(matrix,"src/matrix.png");
//	} catch (IOException e) {
//		e.printStackTrace();
//	}

    return matrix;
  }


  @Override
  public Binarizer createBinarizer(LuminanceSource source) {
    return new HybridBinarizer(source);
  }

  /**
   * For each block in the image, calculate the average black point using a 5x5 grid
   * of the blocks around it. Also handles the corner cases (fractional blocks are computed based
   * on the last pixels in the row/column which are also used in the previous block).
   * 对于图像中的每个块，使用其周围块的5x5网格计算平均黑点。
   * 还处理角情况(分块是根据行/列中的最后一个像素计算的，这些像素也在前一个块中使用)。
   */
  private static void calculateThresholdForBlock(byte[] luminances,
                                                 int subWidth,
                                                 int subHeight,
                                                 int width,
                                                 int height,
                                                 int[][] blackPoints,
                                                 BitMatrix matrix) {
    int maxYOffset = height - BLOCK_SIZE;
    int maxXOffset = width - BLOCK_SIZE;
    for (int y = 0; y < subHeight; y++) {
      int yoffset = y << BLOCK_SIZE_POWER;
      if (yoffset > maxYOffset) {
        yoffset = maxYOffset;
      }
      int top = cap(y, 2, subHeight - 3);
      for (int x = 0; x < subWidth; x++) {
        int xoffset = x << BLOCK_SIZE_POWER;
        if (xoffset > maxXOffset) {
          xoffset = maxXOffset;
        }
        int left = cap(x, 2, subWidth - 3);
        int sum = 0;
        for (int z = -2; z <= 2; z++) {
          int[] blackRow = blackPoints[top + z];
          sum += blackRow[left - 2] + blackRow[left - 1] + blackRow[left] + blackRow[left + 1] + blackRow[left + 2];
        }
        int average = sum / 25;
        thresholdBlock(luminances, xoffset, yoffset, average, width, matrix);
      }
    }
  }

  private static int cap(int value, int min, int max) {
    return value < min ? min : value > max ? max : value;
  }

  /**
   * Applies a single threshold to a block of pixels.
   * 对像素块应用单个阈值。
   */
  private static void thresholdBlock(byte[] luminances,
                                     int xoffset,
                                     int yoffset,
                                     int threshold,
                                     int stride,
                                     BitMatrix matrix) {
    for (int y = 0, offset = yoffset * stride + xoffset; y < BLOCK_SIZE; y++, offset += stride) {
      for (int x = 0; x < BLOCK_SIZE; x++) {
        // Comparison needs to be <= so that black == 0 pixels are black even if the threshold is 0.
        if ((luminances[offset + x] & 0xFF) <= threshold) {
          matrix.set(xoffset + x, yoffset + y);
        }
      }
    }
  }

  /**
   * Calculates a single black point for each block of pixels and saves it away.
   * See the following thread for a discussion of this algorithm:
   *  http://groups.google.com/group/zxing/browse_thread/thread/d06efa2c35a7ddc0
   */
  private static int[][] calculateBlackPoints(byte[] luminances,
                                              int subWidth,
                                              int subHeight,
                                              int width,
                                              int height) {
    int maxYOffset = height - BLOCK_SIZE;
    int maxXOffset = width - BLOCK_SIZE;
    int[][] blackPoints = new int[subHeight][subWidth];
    for (int y = 0; y < subHeight; y++) {
      int yoffset = y << BLOCK_SIZE_POWER;
      if (yoffset > maxYOffset) {
        yoffset = maxYOffset;
      }
      for (int x = 0; x < subWidth; x++) {
        int xoffset = x << BLOCK_SIZE_POWER;
        if (xoffset > maxXOffset) {
          xoffset = maxXOffset;
        }
        int sum = 0;
        int min = 0xFF;
        int max = 0;
        for (int yy = 0, offset = yoffset * width + xoffset; yy < BLOCK_SIZE; yy++, offset += width) {
          for (int xx = 0; xx < BLOCK_SIZE; xx++) {
            int pixel = luminances[offset + xx] & 0xFF;
            sum += pixel;
            // still looking for good contrast
            if (pixel < min) {
              min = pixel;
            }
            if (pixel > max) {
              max = pixel;
            }
          }
          // short-circuit min/max tests once dynamic range is met
          if (max - min > MIN_DYNAMIC_RANGE) {
            // finish the rest of the rows quickly
            for (yy++, offset += width; yy < BLOCK_SIZE; yy++, offset += width) {
              for (int xx = 0; xx < BLOCK_SIZE; xx++) {
                sum += luminances[offset + xx] & 0xFF;
              }
            }
          }
        }

        // The default estimate is the average of the values in the block.
        int average = sum >> (BLOCK_SIZE_POWER * 2);
        if (max - min <= MIN_DYNAMIC_RANGE) {
          // If variation within the block is low, assume this is a block with only light or only
          // dark pixels. In that case we do not want to use the average, as it would divide this
          // low contrast area into black and white pixels, essentially creating data out of noise.
          //
          // The default assumption is that the block is light/background. Since no estimate for
          // the level of dark pixels exists locally, use half the min for the block.
          /*
           * 如果块内的变化很小，则假设这是一个只有亮像素或暗像素的块。
           * 在这种情况下，我们不想使用平均值，因为它会把这个低对比度的区域分割成黑白像素，本质上是在噪声中创建数据。
           * 默认的假设是块是光/背景。由于本地不存在对暗像素级别的估计，因此对块使用最小值的一半。
           */
        	average = min / 2;

          if (y > 0 && x > 0) {
            // Correct the "white background" assumption for blocks that have neighbors by comparing
            // the pixels in this block to the previously calculated black points. This is based on
            // the fact that dark barcode symbology is always surrounded by some amount of light
            // background for which reasonable black point estimates were made. The bp estimated at
            // the boundaries is used for the interior.

            // The (min < bp) is arbitrary but works better than other heuristics that were tried.
            /*
             * //通过比较块中的像素和之前计算的黑色点，纠正“白色背景”的假设。
             * 这是基于这样一个事实，即暗条码符号总是被一些光背景包围，对这些光背景进行合理的黑点估计。
             * 边界处的bp值用于内部。(min < bp)是任意的，但比尝试过的其他启发式方法更有效。
             */
        	  int averageNeighborBlackPoint =
                (blackPoints[y - 1][x] + (2 * blackPoints[y][x - 1]) + blackPoints[y - 1][x - 1]) / 4;
            if (min < averageNeighborBlackPoint) {
              average = averageNeighborBlackPoint;
            }
          }
        }
        blackPoints[y][x] = average;
      }
    }
    return blackPoints;
  }

}

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

/**
 * <p>Encapsulates a block of data within a QR Code. QR Codes may split their data into
 * multiple blocks, each of which is a unit of data and error-correction codewords. Each
 * is represented by an instance of this class.</p>
 *
 * @author Sean Owen
 */
final class DataBlock {

  private final int numDataCodewords;
  private final byte[] codewords;

  private DataBlock(int numDataCodewords, byte[] codewords) {
    this.numDataCodewords = numDataCodewords;
    this.codewords = codewords;
  }

  /**
   * <p>When QR Codes use multiple data blocks, they are actually interleaved.
   * That is, the first byte of data block 1 to n is written, then the second bytes, and so on. This
   * method will separate the data into original blocks.</p>
   *
   * @param rawCodewords bytes as read directly from the QR Code
   * @param version version of the QR Code
   * @param ecLevel error-correction level of the QR Code
   * @return DataBlocks containing original bytes, "de-interleaved" from representation in the
   *         QR Code
   */
  static DataBlock[] getDataBlocks(byte[] rawCodewords,
                                   Version version,
                                   ErrorCorrectionLevel ecLevel) {

    if (rawCodewords.length != version.getTotalCodewords()) {
      throw new IllegalArgumentException();
    }

    // Figure out the number and size of data blocks used by this version and
    // error correction level
    Version.ECBlocks ecBlocks = version.getECBlocksForLevel(ecLevel);

    // First count the total number of data blocks
    int totalBlocks = 0;//5
    Version.ECB[] ecBlockArray = ecBlocks.getECBlocks();//长度=2
    for (Version.ECB ecBlock : ecBlockArray) {
      totalBlocks += ecBlock.getCount();
    }

    // Now establish DataBlocks of the appropriate size and number of data codewords
    DataBlock[] result = new DataBlock[totalBlocks];
    int numResultBlocks = 0;
    for (Version.ECB ecBlock : ecBlockArray) {
      for (int i = 0; i < ecBlock.getCount(); i++) {
        int numDataCodewords = ecBlock.getDataCodewords();//获取每一个block中数据的字符总数
        int numBlockCodewords = ecBlocks.getECCodewordsPerBlock() + numDataCodewords;//获取每一块中EC和数据字符总数
        result[numResultBlocks++] = new DataBlock(numDataCodewords, new byte[numBlockCodewords]);//存储以上两个数据，为下面纠错作准备
      }
    }

    // All blocks have the same amount of data, except that the last n
    // (where n may be 0) have 1 more byte. Figure out where these start.
    int shorterBlocksTotalCodewords = result[0].codewords.length;//短block数据和BC的总长度
    int longerBlocksStartAt = result.length - 1;//最长的一定是最后一个
    while (longerBlocksStartAt >= 0) {
      int numCodewords = result[longerBlocksStartAt].codewords.length;
      if (numCodewords == shorterBlocksTotalCodewords) {//从最后一个开始查，判断倒数第几个的长度比其他的更长
        break;
      }
      longerBlocksStartAt--;
    }
    longerBlocksStartAt++;

    int shorterBlocksNumDataCodewords = shorterBlocksTotalCodewords - ecBlocks.getECCodewordsPerBlock();//短的block的数据长度
    // The last elements of result may be 1 element longer;
    // first fill out as many elements as all of them have
    int rawCodewordsOffset = 0;
    for (int i = 0; i < shorterBlocksNumDataCodewords; i++) {
      for (int j = 0; j < numResultBlocks; j++) {//假设小block中数据长度为i，一共中有j个block，它是一起取所有block的一部分数据，而不是先将一个完整的block数据取完，然后再取第二个
        result[j].codewords[i] = rawCodewords[rawCodewordsOffset++];
      }
    }
    // Fill out the last data block in the longer ones
    for (int j = longerBlocksStartAt; j < numResultBlocks; j++) {//取长的block，方法同上
      result[j].codewords[shorterBlocksNumDataCodewords] = rawCodewords[rawCodewordsOffset++];
    }
    // Now add in error correction blocks
    int max = result[0].codewords.length;
    for (int i = shorterBlocksNumDataCodewords; i < max; i++) {//在每一个block的数据后面放入纠错数据
      for (int j = 0; j < numResultBlocks; j++) {//纠错数据的放入和上文相同
        int iOffset = j < longerBlocksStartAt ? i : i + 1;//如果是大block，由于数据比其他长一个，则起始地址加一
        result[j].codewords[iOffset] = rawCodewords[rawCodewordsOffset++];
      }
    }
    return result;
  }

  int getNumDataCodewords() {
    return numDataCodewords;
  }

  /**
   * 使用自己设计的排列，将每一块中连在一起的数据和纠错码读取
   * @param rawCodewords
   * @param version
   * @param ecLevel
   * @return
   */
  static DataBlock[] getDataBlocksInNewWay(byte[] rawCodewords,
                                   Version version,
                                   ErrorCorrectionLevel ecLevel) {

    if (rawCodewords.length != version.getTotalCodewords()) {
      throw new IllegalArgumentException();
    }

    // Figure out the number and size of data blocks used by this version and
    // error correction level
    Version.ECBlocks ecBlocks = version.getECBlocksForLevel(ecLevel);

    // First count the total number of data blocks
    int totalBlocks = 0;//5
    Version.ECB[] ecBlockArray = ecBlocks.getECBlocks();//长度=2
    for (Version.ECB ecBlock : ecBlockArray) {
      totalBlocks += ecBlock.getCount();
    }

    // Now establish DataBlocks of the appropriate size and number of data codewords
    DataBlock[] result = new DataBlock[totalBlocks];
    int numResultBlocks = 0;
    for (Version.ECB ecBlock : ecBlockArray) {
      for (int i = 0; i < ecBlock.getCount(); i++) {
        int numDataCodewords = ecBlock.getDataCodewords();//获取每一个block中数据的字符总数
        int numBlockCodewords = ecBlocks.getECCodewordsPerBlock() + numDataCodewords;//获取每一块中EC和数据字符总数
        result[numResultBlocks++] = new DataBlock(numDataCodewords, new byte[numBlockCodewords]);//存储以上两个数据，为下面纠错作准备
      }
    }

    // All blocks have the same amount of data, except that the last n
    // (where n may be 0) have 1 more byte. Figure out where these start.
    int shorterBlocksTotalCodewords = result[0].codewords.length;//短block数据和BC的总长度
    int longerBlocksStartAt = result.length - 1;//最长的一定是最后一个
    while (longerBlocksStartAt >= 0) {
      int numCodewords = result[longerBlocksStartAt].codewords.length;
      if (numCodewords == shorterBlocksTotalCodewords) {//从最后一个开始查，判断倒数第几个的长度比其他的更长
        break;
      }
      longerBlocksStartAt--;
    }
    longerBlocksStartAt++;

    int shorterBlocksNumDataCodewords = shorterBlocksTotalCodewords - ecBlocks.getECCodewordsPerBlock();//短的block的数据长度
    // The last elements of result may be 1 element longer;
    // first fill out as many elements as all of them have
    int rawCodewordsOffset = 0;
    int max = result[0].codewords.length;
    for (int j = 0; j < numResultBlocks; j++) {
      if(j < longerBlocksStartAt) {//长度小的
        for (int i = 0; i < shorterBlocksNumDataCodewords; i++) {
          result[j].codewords[i] = rawCodewords[rawCodewordsOffset++];
        }
        for (int i = shorterBlocksNumDataCodewords; i < max; i++) {//在每一个block的数据后面继续放入纠错
          int iOffset= i;
          result[j].codewords[iOffset] = rawCodewords[rawCodewordsOffset++];
        }
      }else{//后几个长度长的
        for (int i = 0; i < shorterBlocksNumDataCodewords+1; i++) {
          result[j].codewords[i] = rawCodewords[rawCodewordsOffset++];
        }
        for (int i = shorterBlocksNumDataCodewords+1; i < max+1; i++) {//在每一个block的数据后面继续放入纠错

          result[j].codewords[i] = rawCodewords[rawCodewordsOffset++];
        }

      }
    }

    return result;
  }

  byte[] getCodewords() {
    return codewords;
  }

}

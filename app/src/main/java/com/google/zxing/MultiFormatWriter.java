/*
 * Copyright 2008 ZXing authors
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

import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Map;

/**
 * This is a factory class which finds the appropriate Writer subclass for the
 * BarcodeFormat requested and encodes the barcode with the supplied contents.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class MultiFormatWriter implements Writer {

	@Override
	public BitMatrix encode(String contents, BarcodeFormat format, int width, int height) throws WriterException {
		return encode(contents, format, width, height, null);
	}

	@Override
	public BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints)
			throws WriterException {

		Writer writer;
			writer = new QRCodeWriter();
			
		return writer.encode(contents, format, width, height, hints);
	}
	/**
	 * 加密byte数组
	 * @param contents
	 * @param format
	 * @param width
	 * @param height
	 * @param hints
	 * @return
	 * @throws WriterException
	 */
	public BitMatrix encode(byte[] contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints)
			throws WriterException {

		Writer writer =  new QRCodeWriter();;
		return writer.encode(contents, format, width, height, hints);
	}

	@Override
	public BitMatrix[] encode(String contents1, String contents2, String contents3, BarcodeFormat format, int width,
			int height, Map<EncodeHintType, ?> hints) throws WriterException {

		Writer writer;
		writer = new QRCodeWriter();
		
		return writer.encode(contents1, contents2, contents3, format, width, height, hints);
	}

	public BitMatrix[] encode(byte[] contents1, byte[] contents2, byte[] contents3, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints)
			throws WriterException {

		Writer writer =  new QRCodeWriter();;
		return writer.encode(contents1,contents2,contents3, format, width, height, hints);
	}
	

}

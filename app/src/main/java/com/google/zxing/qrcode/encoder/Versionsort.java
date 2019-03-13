package com.google.zxing.qrcode.encoder;

import com.google.zxing.common.BitArray;
import com.google.zxing.qrcode.decoder.Version;
import com.google.zxing.qrcode.decoder.Mode;
public class Versionsort {
		private Version version;
		private Mode mode;
		private BitArray bitArray;
		private String content;
		public Versionsort(Version version,Mode mode,BitArray bitArray,String content) {
			this.version = version;
			this.mode = mode;
			this.bitArray = bitArray;
			this.content = content;
		}
		public Versionsort(Version version) {
			
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public Mode getMode() {
			return mode;
		}
		public void setMode(Mode mode) {
			this.mode = mode;
		}
		
		
		public Version getVersion() {
			return version;
		}
		public void setVersion(Version version) {
			this.version = version;
		}
		public BitArray getBitArray() {
			return bitArray;
		}
		public void setBitArray(BitArray bitArray) {
			this.bitArray = bitArray;
		} 
}

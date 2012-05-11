package scriptease.translator.io.tools;

import java.io.IOException;
import java.io.RandomAccessFile;


public class FileReader {
	private static boolean useBigEndian = true;
	
	public static String readString(RandomAccessFile moduleSource, int numOfChars) {
		return new String(readBytes(moduleSource, numOfChars));
	}

	public static int readIntLSB(RandomAccessFile moduleSource) {
		byte[] bytes = readBytes(moduleSource, 4);
		return BitwiseConverter.byteArrToInt(BitwiseConverter.reverseEndian(bytes, 4));
	}

	public static byte[] readBytes(RandomAccessFile moduleSource, int numBytes){
		byte[] bytes = new byte[numBytes];
		try {
			moduleSource.read(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if( useBigEndian ){
			bytes = BitwiseConverter.reverseEndian(bytes, 4);
		}
		return bytes;
	}

}

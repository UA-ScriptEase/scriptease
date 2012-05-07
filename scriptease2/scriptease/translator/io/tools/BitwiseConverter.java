package scriptease.translator.io.tools;

/**
 * Convenience tools for authors of SE translators. The methods, herein, are
 * intended to solve common problems which arise when moving data between
 * disparate representations, ie. converting a game module into an SE tree may
 * be troublesome since the two representations may have different data types.
 * Eg. NWScript supports uint, Java doesn't => use
 * BitwiseConverter.IntToUnsignedInt() to create an equivalent representation of
 * a uint, for use in Java.
 * 
 * @author jtduncan
 * 
 */
public class BitwiseConverter {

	/**
	 * Java does not provide native support for unsigned integers. This is only
	 * a problem when a scripting language (such as NWScript) needs such
	 * support. This method works around Java's shortcoming by producing a
	 * number of type 'long', which evaluates to the unsigned equivalent to the
	 * signed integer passed in.
	 * 
	 * @param signedInt
	 * @return Number (of type long) representing the unsigned decoding of the
	 *         bits which make up 'signedInt'
	 */
	public static long IntToUnsignedInt(int signedInt) {
		/*
		 * First, we make sure there are leading zeros by putting an int value
		 * into a long container. Methinks that the apparent no-op here (bitwise
		 * and with 0xFF..L), which I stole from an online article, is just a
		 * hacky way of forcing Java to treat the int as unsigned. This is
		 * needed since Java has no native support for unsigned ints.
		 */
		return (signedInt) & 0xFFFFFFFFL;
	}

	public static int byteArrToInt(byte[] byteArr) {
		int intVal = 0;
		int byteCounter = 3;
		for (byte curByte : byteArr) {
			// Guard against Java signed-byte issue wherein a byte value of
			// sufficiently large value is decoded in 2's complement (wrong
			// value).
			int firstByte = 0x000000FF & curByte;
			short uByte = (short) firstByte;

			intVal |= uByte << byteCounter * Byte.SIZE;
			byteCounter--;
			if (byteCounter < 0) {
				// we can only fit 4 bytes in an int
				break;
			}
		}

		return intVal;
	}

	public static short byteArrToShort(byte[] byteArr) {
		short shortVal = 0;
		int byteCounter = 1;
		for (byte curByte : byteArr) {
			// Guard against Java signed-byte issue wherein a byte value of
			// sufficiently large value is decoded in 2's complement (wrong
			// value).
			int firstByte = 0x000000FF & curByte;
			short uByte = (short) firstByte;

			shortVal |= uByte << byteCounter * Byte.SIZE;
			byteCounter--;
			if (byteCounter < 0) {
				// we can only fit 4 bytes in an int
				break;
			}
		}

		return shortVal;
	}
	
	public static byte[] shortToByteArray(short value) {
		byte[] byteArray;

		// Guard against Java signed-byte issue wherein a byte value of
		// sufficiently large value is decoded in 2's complement (wrong
		// value).
		byteArray = new byte[] { (byte) (value >>> 8 & 0xff),
				(byte) (value & 0xff) };
		return byteArray;
	}
	
	public static byte[] intToByteArray(int value) {
		byte[] byteArray;

		// Guard against Java signed-byte issue wherein a byte value of
		// sufficiently large value is decoded in 2's complement (wrong
		// value).
		byteArray = new byte[] {
				(byte) (value >>> 24 & 0xff),
				(byte) (value >>> 16 & 0xff),
				(byte) (value >>> 8 & 0xff),
				(byte) (value & 0xff) };
		return byteArray;
	}

	/**
	 * Converts a long to a byte array.
	 * 
	 * @param value
	 *            The value to convert.
	 * @return The array version of the value.
	 */
	public static byte[] longToByteArray(long value) {
		byte[] byteArray;

		// & 0xff Guards against Java signed-byte issue wherein a byte value of
		// sufficiently large value is decoded in 2's complement (wrong
		// value).
		byteArray = new byte[] { (byte) (value >>> 56 & 0xff),
				(byte) (value >>> 48 & 0xff), (byte) (value >>> 40 & 0xff),
				(byte) (value >>> 32 & 0xff), (byte) (value >>> 24 & 0xff),
				(byte) (value >>> 16 & 0xff), (byte) (value >>> 8 & 0xff),
				(byte) (value & 0xff) };
		return byteArray;
	}

	public static long byteArrToLong(byte[] byteArr) {
		int rVal = 0;
		int byteCounter = 7;
		for (byte curByte : byteArr) {
			// Guard against Java signed-byte issue wherein a byte value of
			// sufficiently large value is decoded in 2's complement (wrong
			// value).
			int firstByte = 0x000000FF & curByte;
			short uByte = (short) firstByte;

			rVal |= uByte << byteCounter * Byte.SIZE;
			byteCounter--;
			if (byteCounter < 0) {
				// we can only fit 8 bytes in a long
				break;
			}
		}

		return rVal;
	}

	/**
	 * Takes a variable size byte array and returns an array which is ordered in
	 * reverse-endian from the original.
	 * 
	 * @param nonLSBArray
	 *            array of bytes with array.length
	 * @return an int which is the LSB version of the byte array.
	 */
	public static byte[] reverseEndian(byte[] nonLSBArray, int wordSize) {
		for (int i = 0; i < nonLSBArray.length; i += wordSize) {
			int endIndex = i + wordSize - 1;
			if (!(endIndex < nonLSBArray.length)) {
				byte[] wordSizeByteNonLSB = new byte[wordSize];
				for (int index = 0; index < nonLSBArray.length; index++) {
					wordSizeByteNonLSB[index] = nonLSBArray[index];
				}
				nonLSBArray = wordSizeByteNonLSB;
			}

			for (int j = 0; j < wordSize / 2; j++) {
				int curIndex = i + j;
				byte curByte = nonLSBArray[curIndex];
				nonLSBArray[curIndex] = nonLSBArray[endIndex - j];
				nonLSBArray[endIndex - j] = curByte;
			}
		}
		return nonLSBArray;
	}
}

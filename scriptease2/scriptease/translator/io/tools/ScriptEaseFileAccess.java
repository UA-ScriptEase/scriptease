package scriptease.translator.io.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * General use reader that extends the functionality of RandomAccessFile for
 * easy reading of signed/unsigned, little/big endian numbers of various
 * standard lengths, and strings.
 * 
 * @author jtduncan
 * @author remiller
 * 
 */
public class ScriptEaseFileAccess extends RandomAccessFile {
	public static final int BYTE_BYTE_LENGTH = 1;
	public static final int SHORT_BYTE_LENGTH = 2;
	public static final int INT_BYTE_LENGTH = 4;
	public static final int LONG_BYTE_LENGTH = 8;
	public static final int DOUBLE_BYTE_LENGTH = 8;

	private boolean readOnly = false;

	public ScriptEaseFileAccess(File file, String mode)
			throws FileNotFoundException {
		super(file, mode);

		if (mode.equals("r")) {
			this.readOnly = true;
		}
	}

	public boolean isReadOnly() {
		return this.readOnly;
	}

	/**
	 * Reads <code>length</code> characters as a string.
	 * 
	 * @param length
	 * @return The string as read from the file.
	 * @throws IOException
	 */
	public String readString(int length) throws IOException {
		String read = new String(this.readBytes(length));

		// take everything before the first null character
		String[] beforeNull = read.split("\0");
		if (beforeNull != null && beforeNull.length > 0)
			return beforeNull[0];
		else
			return "";
	}

	/**
	 * Writes <code>text</code> as a string to the file. This has the same
	 * effect as <code>writeString(text, text.length())</code>
	 * 
	 * @param text
	 *            The string to write out.
	 * @throws IOException
	 * @see {@link #writeString(String, int)}
	 */
	public void writeString(String text) throws IOException {
		this.writeString(text, text.length());
	}

	/**
	 * Writes <code>text</code> as a string to the file. <br>
	 * <br>
	 * If <code>length</code> is greater than <code>text.length()</code>, then
	 * nulls will be written until <code>length</code> characters is met. In
	 * contrast, if <code>length</code> is less than <code>text.length()</code>,
	 * then only the first <code>length</code> characters are written.
	 * 
	 * @param text
	 *            The string to write out.
	 * @param length
	 *            The number of characters to write to disk.
	 * @throws IOException
	 */
	public void writeString(String text, int length) throws IOException {
		// append nulls until we hit the length quota
		while (text.length() < length) {
			text += "\0";
		}

		this.writeBytes(text);
	}

	/**
	 * Reads a signed, 16-bit integer.
	 * 
	 * @param reverseEndianess
	 *            Use this boolean to reverse the endianess of the result.
	 * @return A short as read from the Stream.
	 * @throws IOException
	 */
	public short readShort(boolean reverseEndianess) throws IOException {
		byte[] bytes = this.readBytes(SHORT_BYTE_LENGTH);

		if (reverseEndianess) {
			bytes = BitwiseConverter.reverseEndian(bytes, SHORT_BYTE_LENGTH);
		}

		return BitwiseConverter.byteArrToShort(bytes);
	}

	/**
	 * Writes a signed, 16-bit integer to disk.
	 * 
	 * @param reverseEndianess
	 *            Use this boolean to reverse the endianess of the result.
	 * @return A short as read from the Stream.
	 * @throws IOException
	 */
	public void writeShort(short value, boolean reverseEndianess)
			throws IOException {
		if (reverseEndianess) {
			byte[] bytes = BitwiseConverter.shortToByteArray(value);
			bytes = BitwiseConverter.reverseEndian(bytes, SHORT_BYTE_LENGTH);
			value = BitwiseConverter.byteArrToShort(bytes);
		}

		super.writeShort(value);
	}

	/**
	 * Reads a signed, 32-bit integer.
	 * 
	 * @param reverseEndianess
	 *            Use this boolean to reverse the endianess of the result.
	 * @return An int as read from the Stream.
	 * @throws IOException
	 */
	public int readInt(boolean reverseEndianess) throws IOException {
		byte[] bytes = readBytes(INT_BYTE_LENGTH);

		if (reverseEndianess) {
			bytes = BitwiseConverter.reverseEndian(bytes, INT_BYTE_LENGTH);
		}

		return BitwiseConverter.byteArrToInt(bytes);
	}

	/**
	 * Writes an int in either big or little endian formats.
	 * 
	 * @param value
	 *            The value to write.
	 * @param reverseEndianess
	 *            Reverse the endianness of the result.
	 * @throws IOException
	 */
	public void writeInt(int value, boolean reverseEndianess)
			throws IOException {
		byte[] bytes = BitwiseConverter.intToByteArray(value);

		if (reverseEndianess) {
			bytes = BitwiseConverter.reverseEndian(bytes, INT_BYTE_LENGTH);
		}

		this.writeBytes(bytes);
	}

	/**
	 * Reads a signed, 64-bit long.
	 * 
	 * @param reverseEndianess
	 *            Use this boolean to reverse the endianess of the result.
	 * @return A long as read from the Stream.
	 * @throws IOException
	 */
	public long readLong(boolean reverseEndianess) throws IOException {
		byte[] bytes = readBytes(LONG_BYTE_LENGTH);

		if (reverseEndianess) {
			bytes = BitwiseConverter.reverseEndian(bytes, LONG_BYTE_LENGTH);
		}

		return BitwiseConverter.byteArrToLong(bytes);
	}

	/**
	 * Writes a long in either big or little endian formats.
	 * 
	 * @param value
	 *            The value to write.
	 * @param reverseEndianess
	 *            Reverse the endianness of the result.
	 * @throws IOException
	 */
	public void writeLong(long value, boolean reverseEndianess)
			throws IOException {
		byte[] bytes = BitwiseConverter.longToByteArray(value);

		if (reverseEndianess) {
			bytes = BitwiseConverter.reverseEndian(bytes, INT_BYTE_LENGTH);
		}

		this.writeBytes(bytes);
	}

	/**
	 * Reads a double in either big or little endian formats.
	 * 
	 * @param reverseEndianess
	 *            Reverse the endianness of the result.
	 * @throws IOException
	 */

	// this doesn't do anything because it's not important to implement now.
	@Deprecated
	public double readDouble(boolean reverseEndianess) {
		// byte[] bytes = readBytes(LONG_BYTE_LENGTH);
		//
		// if (reverseEndianess) {
		// bytes = BitwiseConverter.reverseEndian(bytes, LONG_BYTE_LENGTH);
		// }
		//
		// return BitwiseConverter.byteArrToDouble(bytes);
		return -1;
	}

	/**
	 * Writes a double in either big or little endian formats.
	 * 
	 * @param value
	 *            The value to write.
	 * @param reverseEndianess
	 *            Reverse the endianness of the result.
	 * @throws IOException
	 */
	// this doesn't do anything because it's not important to implement now.
	@Deprecated
	public void writeDouble(double value, boolean reverseEndianess) {
		// byte[] bytes = BitwiseConverter.doubleToByteArray(value);
		//
		// if (reverseEndianess) {
		// bytes = BitwiseConverter.reverseEndian(bytes, INT_BYTE_LENGTH);
		// }
		//
		// this.writeBytes(bytes);
	}

	/**
	 * Reads an unsigned, 16-bit short and stores it in an integer.
	 * 
	 * @param reverseEndianess
	 *            Use this boolean to reverse the endianess of the result.
	 * @return The <code>short</code> stored in an <code>int</code> as read from
	 *         the stream. The short must be stored in an int to prevent Java
	 *         from considering it signed.
	 * @throws IOException
	 */
	public int readUnsignedShort(boolean reverseEndianess) throws IOException {
		byte[] bytes = readBytes(SHORT_BYTE_LENGTH);

		if (reverseEndianess) {
			bytes = BitwiseConverter.reverseEndian(bytes, SHORT_BYTE_LENGTH);
		}

		return BitwiseConverter.byteArrToInt(bytes);
	}

	/**
	 * Reads an unsigned 32-bit integer and stores it in a long.
	 * 
	 * @return <code>Long</code> version of the unsigned <code>int</code>. Must
	 *         be stored in a long to prevent Java from considering it signed.
	 * @throws IOException
	 */
	public long readUnsignedInt(boolean reverseEndianess) throws IOException {
		byte[] bytes = readBytes(INT_BYTE_LENGTH);

		if (reverseEndianess) {
			bytes = BitwiseConverter.reverseEndian(bytes, INT_BYTE_LENGTH);
		}

		return BitwiseConverter.byteArrToLong(bytes);
	}

	/**
	 * Reads an unsigned, 64-bit long and stores in in a BigInteger.
	 * 
	 * @param reverseEndianess
	 *            Use this boolean to reverse the endianess of the result.
	 * @return A BigInteger storing the 64-bit unsigned long as read from the
	 *         Stream.
	 * @throws IOException
	 */
	public BigInteger readUnsignedLong(boolean reverseEndianess)
			throws IOException {
		byte[] bytes = readBytes(LONG_BYTE_LENGTH);
		String longString = "00000000";

		if (reverseEndianess) {
			bytes = BitwiseConverter.reverseEndian(bytes, LONG_BYTE_LENGTH);
		}

		longString += Long.toString(BitwiseConverter.byteArrToLong(bytes));

		return new BigInteger(longString);
	}

	/**
	 * Reads <code>numBytes</code> bytes of data from the stream.
	 * 
	 * @param numBytes
	 *            The number of bytes to read.
	 * @return An array of bytes read from the input stream.
	 * @throws IOException
	 */
	public byte[] readBytes(int numBytes) throws IOException {
		ByteBuffer bytes = ByteBuffer.allocate(numBytes);
		int readLength = super.read(bytes.array());

		if (readLength < 0)
			throw new IOException("Tried to read past the end of the file.");
		if (readLength > numBytes)
			throw new IOException(
					"Tried to read more bytes than was supposed to.");

		return bytes.array();
	}

	/**
	 * Writes <code>numBytes</code> bytes of data to disk.
	 * <code>reverseEndianness</code> flips the result's endianness.
	 * 
	 * @param bytes
	 *            An array of bytes to write.
	 * @throws IOException
	 */
	public void writeBytes(byte[] bytes) throws IOException {
		this.writeBytes(bytes, false);
	}

	/**
	 * Writes <code>numBytes</code> bytes of data to disk.
	 * <code>reverseEndianness</code> flips the result's endianness.
	 * 
	 * @param bytes
	 *            An array of bytes to write.
	 * @param reverseEndianess
	 *            Use this boolean to reverse the endianess of the result.
	 * @throws IOException
	 */
	public void writeBytes(byte[] bytes, boolean reverseEndianess)
			throws IOException {
		if (reverseEndianess) {
			bytes = BitwiseConverter.reverseEndian(bytes, bytes.length);
		}

		super.write(bytes);
	}

	/**
	 * Writes <code>nullCount</code> nulls to the file.
	 * 
	 * @param nullCount
	 *            The number of nulls to write.
	 * @throws IOException
	 */
	public void writeNullBytes(int nullCount) throws IOException {
		String nulls = "";

		for (int i = 0; i < nullCount; i++) {
			nulls += "\0";
		}

		this.writeBytes(nulls.getBytes());
	}

	public void writeUnsignedInt(long value, boolean reverseEndianess)
			throws IOException {
		byte[] bytes = BitwiseConverter
				.intToByteArray((int) (value & 0xFFFFFFFFL));

		if (reverseEndianess) {
			bytes = BitwiseConverter.reverseEndian(bytes, INT_BYTE_LENGTH);
		}

		this.writeBytes(bytes);
	}

	public void writeUnsignedLong(long value, boolean reverseEndianess)
			throws IOException {
		byte[] bytes = BitwiseConverter
				.longToByteArray((long) (value & 0xFFFFFFFFL));

		if (reverseEndianess) {
			bytes = BitwiseConverter.reverseEndian(bytes, INT_BYTE_LENGTH);
		}

		this.writeBytes(bytes);
	}
}
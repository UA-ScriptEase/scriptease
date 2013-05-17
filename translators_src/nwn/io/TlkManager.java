package io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import scriptease.translator.Translator.DescriptionKeys;
import scriptease.translator.io.tools.ScriptEaseFileAccess;

/**
 * Encapsulate knowledge of and policy for indexed lookup of CExoLocString type
 * fields. See BioWare's GFF and TLK documentation for more details.<br>
 * <br>
 * This is a singleton class.
 * 
 * @author remiller
 */
public class TlkManager {
	private static final TlkManager instance = new TlkManager();

	private static final String DEFAULT_TLK_FILENAME = "dialog.tlk";

	private static final int CUSTOM_TLK_BIT = 0x01000000;

	private TlkFile defaultTLK;
	private TlkFile customTLK;

	public static TlkManager getInstance() {
		return TlkManager.instance;
	}

	private TlkManager() {
		final String nwnRoot;
		final File defaultTlkFile;

		nwnRoot = ErfFile.getTranslator().getProperty(
				DescriptionKeys.GAME_DIRECTORY).trim();

		defaultTlkFile = new File(nwnRoot, TlkManager.DEFAULT_TLK_FILENAME);

		try {
			// TODO: Also load the dialogf.tlk file
			this.defaultTLK = new TlkFile(defaultTlkFile);
		} catch (IOException e) {
			System.err.println("Can't read " + defaultTlkFile.getAbsoluteFile()
					+ ", reason: " + e.getMessage());
		}

		// TODO: be able to read custom TLK files
	}

	/**
	 * Retrieves the string by the given ID from the appropriate language file.
	 * 
	 * @param stringRef
	 *            ID of the string as read from some GFF
	 * @return Internationalized string from appropriate TLK table
	 */
	public String lookup(long stringRef) {
		TlkFile file = null;

		// first, check if it is a valid String Ref, as per documentation
		// section 2.2
		if ((stringRef != 0xFFFFFFFF)) {
			// then, we check stringRef's custom tlk bit
			if ((stringRef & CUSTOM_TLK_BIT) != 0) {
				file = this.customTLK;
			} else {
				file = this.defaultTLK;
			}
		}

		return file == null ? "" : file.get(stringRef);
	}

	/**
	 * Represents a "TLK" file, in particular dialog.tlk. TLK files are the
	 * files that store all of the internationalised strings for the game. See
	 * BioWare documentation for more details on Tlk tables. <br>
	 * <br>
	 * This class was copied and modified from ScriptEase 1's TlkFile.java. The
	 * original author was unattributed.
	 * 
	 * @author Unknown
	 * @author remiller
	 */
	private class TlkFile {
		private static final String TLK_VERSION = "V3.0";
		private static final String TLK_FILE_TYPE = "TLK";

		// offset to the start of the String Data Table. 5 fields, 4 bytes each
		private static final int STRING_DATA_TABLE_OFFSET = 4 * 5;

		// String flag masks
		private static final int STRING_FLAG_TEXT_PRESENT = 0x0001;

		private final String[] strings;

		public TlkFile(File f) throws IOException, FileNotFoundException {
			final ScriptEaseFileAccess reader;
			final long stringCount;
			String fileType;
			String version;
			long stringEntriesOffset;

			// "r" = read-only
			reader = new ScriptEaseFileAccess(f, "r");

			fileType = reader.readString(4);
			version = reader.readString(4);
			reader.skipBytes(4); // skip language id
			stringCount = reader.readUnsignedInt(true);
			stringEntriesOffset = reader.readUnsignedInt(true);

			// ensure the correct file type and version
			if (!fileType.trim().equals(TLK_FILE_TYPE)) {
				reader.close();
				throw new IllegalArgumentException(
						"Illegal file type passed to TLK constructor.");
			} else if (!version.trim().equals(TLK_VERSION)) {
				reader.close();
				throw new IllegalArgumentException("Unknown version. Expected "
						+ TLK_VERSION + " but received " + version + ".");
			}

			this.strings = new String[(int) stringCount];

			this.readData(reader, stringCount, stringEntriesOffset);
		}

		private void readData(ScriptEaseFileAccess reader, long stringCount,
				long stringEntriesOffset) throws IOException {
			long flags;
			long offsetToString; // offset to the string itself
			long stringSize;
			String value;
			long stringDataBookmark = TlkFile.STRING_DATA_TABLE_OFFSET;

			for (int i = 0; i < stringCount; i++) {
				reader.seek(stringDataBookmark);

				flags = reader.readUnsignedInt(true);

				// skip the sound file ResRef,
				// sound volume variance (4) and
				// sound pitch variance (4)
				reader.skipBytes(ErfKey.RESREF_MAX_LENGTH + 4 + 4);

				offsetToString = reader.readUnsignedInt(true);
				stringSize = reader.readUnsignedInt(true);

				// ignore the Sound Length, too,
				reader.skipBytes(4);

				// remember where we are
				stringDataBookmark = reader.getFilePointer();

				if ((flags & 0x1) != TlkFile.STRING_FLAG_TEXT_PRESENT) {
					value = "";
				} else {
					// go read the string contents
					reader.seek(stringEntriesOffset + offsetToString);

					value = reader.readString((int) stringSize);
				}

				this.strings[i] = value;
			}
		}

		/**
		 * Gets the string associated with the supplied <code>stringRef</code>
		 * from the TLK file.
		 * 
		 * @param stringRef
		 *            ID of the string to retrieve from the talk table.
		 * @return The string referenced by the stringRef
		 */
		public String get(long stringRef) {
			final String string;

			// by the docs, page 1, we should return "" for all 1s.
			if (stringRef == 0xFFFFFFFF)
				return "";

			// mask off the first two bits, as per section 2.2 of documentation
			stringRef = (stringRef & 0x00FFFFFF);

			if (stringRef >= this.strings.length) {
				throw new IllegalArgumentException("Impossible string ref: "
						+ stringRef + ", TLK table string size = "
						+ this.strings.length);
			}

			string = this.strings[(int) stringRef];

			return string == null ? "" : string;
		}
	}
}

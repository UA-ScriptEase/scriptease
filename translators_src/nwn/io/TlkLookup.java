package io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import scriptease.translator.TranslatorManager;
import scriptease.translator.Translator.DescriptionKeys;
import scriptease.translator.io.tools.ScriptEaseFileAccess;

/**
 * Encapsulate knowledge of and policy for indexed lookup of StringRef type
 * elements. See BioWare's GFF and TLK documentation for more details.<br>
 * <br>
 * This is a singleton class.<br>
 * <br>
 * This class was copied from ScriptEase 1's TlkLookup.java. The original author
 * was unattributed.
 * 
 * @author Unknown
 * @author remiller
 */
public class TlkLookup {


	private static final TlkLookup instance = new TlkLookup();

	private static final String DEFAULT_TLK_FILENAME = "dialog";

	private static final String TLK_EXTENSION = "tlk";

	// private static String CUSTOM_TLK_KEY = "Mod_CustomTlk";

	private static final int CUSTOM_TLK_BIT = 0x01000000;

	private TlkFile defaultTLK;
	private TlkFile customTLK;

	public static TlkLookup getInstance() {
		return TlkLookup.instance;
	}

	private TlkLookup() {
		final File nwnRoot;

		nwnRoot = new File(TranslatorManager.getInstance()
				.getTranslator(ErfFile.NEVERWINTER_NIGHTS)
				.getProperty(DescriptionKeys.GAME_DIRECTORY).trim());

		// Open dialog.tlk in NWN install directory.
		File defaultTlkFile = new File(nwnRoot, TlkLookup.DEFAULT_TLK_FILENAME
				+ "." + TlkLookup.TLK_EXTENSION);

		try {
			this.defaultTLK = new TlkFile(defaultTlkFile); // TODO: Also use the
															// dialogf.tlk file
		} catch (IOException e) {
			System.err.println("Can't read " + defaultTlkFile.getAbsoluteFile()
					+ ", reason: " + e.getMessage());
		}

		// TODO: be able to read custom TLK files
		// if (mf != null) {
		// /*
		// * Now see if this module has a custom .tlk.
		// */
		// ItpFile moduleifo = new ItpFile(mf.resource(new ResRef("module",
		// ResRef.tiIFO)));
		//
		// ItpStringElt customkey = (ItpStringElt) moduleifo.root().element(
		// CUSTOM_TLK_KEY);
		// if (customkey != null && !customkey.value().equals("")) {
		// File customtlkfile = new File(new File(nwnroot, "tlk"),
		// customkey.value() + "." + TlkLookup.TLK_EXTENSION);
		// try {
		// customTLK = new TlkFile(customtlkfile);
		// } catch (Exception e) {
		// System.err.println("Can't read "
		// + customtlkfile.getAbsoluteFile() + ", reason: "
		// + e.getMessage());
		//
		// }
		// }
		// }
	}

	/**
	 * Retrieves the string by the given ID from the appropriate file.
	 * 
	 * @param stringRef
	 *            ID of the string as read from some GFF
	 * @return Internationalised string from appropriate TLK table
	 * @throws IOException
	 */
	public String lookup(long stringRef) throws IOException {
		String result = null;

		// first, check if it is a valid String Ref, as per documentation
		// section 2.2
		if ((stringRef == 0xFFFFFFFF)) {
			return "";
		}

		// then, we check stringRef's custom tlk bit
		if ((stringRef & CUSTOM_TLK_BIT) != 0) {
			result = customTLK.get(stringRef);
		} else {
			result = defaultTLK.get(stringRef);
		}

		return result;
	}

	/**
	 * Represents a "tlk" file, in particular dialog.tlk. Tlk files are the
	 * files that store all of the internationalised strings for the game. See
	 * BioWare documentation for more details on Tlk tables. <br>
	 * <br>
	 * This class was copied from ScriptEase 1's TlkFile.java. The original
	 * author was unattributed.
	 * 
	 * @author Unknown
	 * @author remiller
	 */
	private class TlkFile {

		// Byte offsets of elements in the TLK header.
		private static final int STRING_COUNT_LOCATION = 12;
		private static final int STRING_ENTIRES_OFFSET_LOCATION = 16;

		// location of the start of the String Data Table
		private static final int STRING_DATA_TABLE_LOCATION = 20;
		private static final int STRING_DATA_TABLE_ELEMENT_SIZE = 40;

		// String flags
		private static final int STRING_FLAG_TEXT_PRESENT = 0x0001;

		private ScriptEaseFileAccess reader;

		private long stringCount;
		private long stringEntriesOffset;

		public TlkFile(File f) throws IOException, FileNotFoundException {
			// "r" = read-only
			this.reader = new ScriptEaseFileAccess(f, "r");

			if (this.reader != null) {
				// ensure the correct file
				String fileType = this.reader.readString(4);
				if (!fileType.trim().equals("TLK")) {
					throw new IllegalArgumentException(
							"Illegal file type passed to TLK constructor.");
				}

				// store the string count and offset only
				this.reader.seek(TlkFile.STRING_COUNT_LOCATION);
				this.stringCount = this.reader.readUnsignedInt(true);

				this.reader.seek(TlkFile.STRING_ENTIRES_OFFSET_LOCATION);
				this.stringEntriesOffset = this.reader.readUnsignedInt(true);
			}
		}

		/**
		 * Gets the string associated with the supplied <code>stringRef</code>
		 * from the tlk file.
		 * 
		 * @param stringRef
		 *            ID of the string to retrieve from the talk table.
		 * @return The string referenced by the stringRef
		 * @throws IOException
		 */
		public String get(long stringRef) throws IOException {
			long flags;
			long offsetToString; // offset to the string itself
			long stringSize;
			String retrievedString = "";

			// mask off the first two int bits, as per section 2.2 of
			// documentation
			stringRef = (stringRef & 0x00FFFFFF);

			if (stringRef >= this.stringCount) {
				throw new IllegalArgumentException("Impossible string ref: "
						+ stringRef + ", TLK table string count = "
						+ this.stringCount);
			}

			if (this.reader == null) {
				return Long.toString(stringRef);
			}

			this.reader.seek(TlkFile.STRING_DATA_TABLE_LOCATION
					+ (stringRef * STRING_DATA_TABLE_ELEMENT_SIZE));

			flags = this.reader.readUnsignedInt(true);
			this.reader.skipBytes(16); // skip the sound file ResRef
			this.reader.skipBytes(4); // skip the sound volume variance
			this.reader.skipBytes(4); // skip the sound pitch variance
			offsetToString = this.reader.readUnsignedInt(true);
			stringSize = this.reader.readUnsignedInt(true);
			// I'm ignoring the sound length at the end of the header, too

			// mask the text-is-present flag for safety
			if ((flags & 0x1) == TlkFile.STRING_FLAG_TEXT_PRESENT) {
				this.reader.seek(this.stringEntriesOffset + offsetToString);

				retrievedString = this.reader.readString((int) stringSize);
			}

			return retrievedString;
		}
	}
}

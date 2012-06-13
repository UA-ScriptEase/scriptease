package io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import scriptease.controller.apimanagers.GameTypeManager;
import scriptease.translator.TranslatorManager;
import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.model.GameConversation;
import scriptease.translator.io.model.GameConversationNode;
import scriptease.translator.io.model.IdentifiableGameConstant;
import scriptease.translator.io.tools.ScriptEaseFileAccess;

/**
 * Reads and stores a GFF file from a NWN module. See NWN documentation for
 * details on the GFF format.<br>
 * <br>
 * 
 * <b>Important note:</b> The offsets in GenericFileFormat (ex: fieldOffset) <b>
 * do not</b> include the GFF's offset from the start of the ERF. Remember to
 * always add the GFF's <code>gffOffset</code> to its offsets.
 * 
 * @author mfchurch
 * @author remiller
 * 
 */
public class GenericFileFormat {
	private static final String TYPE_SOUND_BP = "UTS";
	private static final String TYPE_WAYPOINT_BP = "UTW";
	private static final String TYPE_TRIGGER_BP = "UTT";
	private static final String TYPE_CREATURE_BP = "UTC";
	private static final String TYPE_ENCOUNTER_BP = "UTE";
	private static final String TYPE_MERCHANT_BP = "UTM";
	private static final String TYPE_PLACEABLE_BP = "UTP";
	private static final String TYPE_ITEM_BP = "UTI";
	private static final String TYPE_DOOR_BP = "UTD";
	private static final String TYPE_DIALOGUE_BP = "DLG";
	private static final String TYPE_JOURNAL_BP = "JRL";
	private static final String TYPE_MODULE_BP = "IFO";
	public static final String TYPE_GAME_INSTANCE_FILE = "GIT";
	private final String resRef;
	private final String fileType;
	private final String version;
	private final long structOffset;
	private final long structCount;
	private final long fieldOffset;
	private final long fieldCount;
	private final long labelOffset;
	private final long labelCount;
	private final long fieldDataOffset;
	private long fieldDataCount;
	private long fieldIndicesOffset;
	private final long fieldIndicesCount;
	private long listIndicesOffset;
	private final long listIndicesCount;
	
	private GenericFileFormat genericFileFormat = this;


	/**
	 * location of this GFF from the start of the parent ERF file. This is reset
	 * upon writing.
	 */
	private long gffOffset;

	private final List<GffStruct> structArray;
	private final List<GffField> fieldArray;
	private final List<String> labelArray;
	/**
	 * Maps the field label to the new data for that field
	 */
	private final Map<GffField, String> changedFieldMap;

	/**
	 * This is a bit of a hack. The dataCache is the byte data from the file. It
	 * should be cached immediately prior to writing, to avoid caching the file
	 * unnecessarily during runtime. I then go about writing this dataCache to
	 * the disk and <i>then</i> write the changes over that. - remiller
	 */
	// private byte[] dataCache;
	private byte[] beforeFieldIndicesArray;
	private byte[] afterFieldIndicesArray;

	// Ill want to move you somewhere else

	private GameConstant objectRepresentation;

	/**
	 * Length of the Labels in the GFF, from GFF documentation section 3.5
	 */
	private static final int LABEL_BYTE_LENGTH = 16;

	public GenericFileFormat(String resRef, ScriptEaseFileAccess reader,
			long filePosition) throws IOException {
		this.resRef = resRef;
		this.gffOffset = filePosition;

		reader.seek(filePosition);

		// read GFF HEADER DATA
		this.fileType = reader.readString(4);
		this.version = reader.readString(4);
		this.structOffset = reader.readUnsignedInt(true);
		this.structCount = reader.readUnsignedInt(true);
		this.fieldOffset = reader.readUnsignedInt(true);
		this.fieldCount = reader.readUnsignedInt(true);
		this.labelOffset = reader.readUnsignedInt(true);
		this.labelCount = reader.readUnsignedInt(true);
		this.fieldDataOffset = reader.readUnsignedInt(true);
		this.fieldDataCount = reader.readUnsignedInt(true);
		this.fieldIndicesOffset = reader.readUnsignedInt(true);
		this.fieldIndicesCount = reader.readUnsignedInt(true);
		this.listIndicesOffset = reader.readUnsignedInt(true);
		this.listIndicesCount = reader.readUnsignedInt(true);
		// end HEADER DATA

		this.structArray = new ArrayList<GffStruct>((int) this.structCount);
		this.fieldArray = new ArrayList<GffField>((int) this.fieldCount);
		this.labelArray = new ArrayList<String>((int) this.labelCount);
		this.changedFieldMap = new HashMap<GffField, String>();

		if (!this.version.equals("V3.2"))
			throw new IOException(
					"NWN GFF: Cannot read a GFF whose version is not V3.2.");

		this.readStructs(reader);
		this.readFields(reader);
		this.readLabels(reader);

		this.buildObject(reader);
	}

	public long getFieldIndicesOffset() {
		return this.fieldIndicesOffset;
	}

	public long getListIndicesOffset() {
		return this.listIndicesOffset;
	}

	public void setFieldIndicesOffset(long fieldIndicesOffset) {
		this.fieldIndicesOffset = fieldIndicesOffset;
	}

	public void setFieldDataCount(long fieldDataCount) {
		this.fieldDataCount = fieldDataCount;
	}

	public long getFieldDataCount() {
		return this.fieldDataCount;
	}
	
	public String getFileType() {
		return this.fileType;
	}

	public void setListIndicesOffset(long listIndicesOffset) {
		this.listIndicesOffset = listIndicesOffset;
	}
	
	public List<GffField> getFieldArray() {
		return this.fieldArray;
	}
	
	public List<String> getLabelArray() {
		return this.labelArray;
	}
	
	public String getResRef() {
		return this.resRef;
	}
	
	/**
	 * Returns the git list label that corresponds to the filetype.
	 * @return
	 */
	public String getGITListLabel() {
		String gitLabel;
		String fileType = this.fileType.trim();
		
		if(fileType.equals(TYPE_SOUND_BP)) {
			gitLabel = "SoundList";
		} else if (fileType.equals(TYPE_WAYPOINT_BP)) {
			gitLabel = "WaypointList";
		} else if (fileType.equals(TYPE_TRIGGER_BP)) {
			gitLabel = "TriggerList";
		} else if (fileType.equals(TYPE_CREATURE_BP)) {
			gitLabel = "Creature List";
		} else if (fileType.equals(TYPE_ENCOUNTER_BP)) {
			gitLabel = "Encounter List";
		} else if (fileType.equals(TYPE_MERCHANT_BP)) {
			gitLabel = "StoreList";
		} else if (fileType.equals(TYPE_PLACEABLE_BP)) {
			gitLabel = "Placeable List";
		} else if (fileType.equals(TYPE_ITEM_BP)) {
			gitLabel = "List";
		} else if (fileType.equals(TYPE_DOOR_BP)) {
			gitLabel = "Door List";
		} else
			gitLabel = "nothing";
		
		return gitLabel;
	}

	/**
	 * Returns true if fields in the GFF have changed
	 * 
	 * @return
	 */
	public boolean hasChanges() {
		return !this.changedFieldMap.isEmpty();
	}

	/**
	 * Calculates the size difference in bytes of the changed gff compared to
	 * the original. Used to make sure the GFF file is not made smaller by
	 * ScriptEase 2.
	 * 
	 * @param reader
	 * @return
	 */
	public int calculateSizeDifference(ScriptEaseFileAccess reader) {
		int sizeDifference = 0;
		for (Entry<GffField, String> entry : this.changedFieldMap.entrySet()) {
			final GffField key = entry.getKey();
			final String newValue = entry.getValue();
			final String oldValue;
			try {
				oldValue = key.readString(reader);
			} catch (IOException e) {
				System.err.println("Unable to read " + key);
				e.printStackTrace();
				continue;
			}
			// add the byte array length difference to the sizeDifference
			sizeDifference += (newValue.getBytes().length - oldValue.getBytes().length);
		}
		return sizeDifference;
	}

	/**
	 * Builds the game objects based on their specific NWN type
	 * 
	 * @param reader
	 * @throws IOException
	 */
	private void buildObject(ScriptEaseFileAccess reader) throws IOException {
		final IdentifiableGameConstant representation;
		final String name;

		if (this.isValuable()) {
			// read the object name
			name = this.readName(reader);

			// conversations
			if (this.fileType.trim().equalsIgnoreCase(
					GenericFileFormat.TYPE_DIALOGUE_BP)) {
				// get the top level struct
				final GffStruct topLevel = this.getTopLevelStruct();

				representation = new NWNConversation(reader, name, topLevel);
			}
			// other types
			else {
				final String tag;
				// get the object tag
				tag = this.readTag(reader);

				// get the object type
				String type = this.getScriptEaseType();

				ArrayList<String> objectTypes = new ArrayList<String>(1);
				objectTypes.add(type);

				representation = new NWNObject(this.resRef, objectTypes, name,
						tag);
			}
			// clean up
			this.removeGeneratedReferences(reader);
		} else {
			representation = null;
		}

		this.objectRepresentation = representation;
	}

	/**
	 * Gets the top level struct from the struct array, returns null if nothing
	 * is found
	 * 
	 * @return
	 */
	public GffStruct getTopLevelStruct() {
		for (GffStruct struct : this.structArray)
			if (struct.isTopLevelStruct())
				return struct;
		return null;
	}

	/**
	 * Reads the name field of the GFF. The actual field(s) that are relevant
	 * can change from type to type.
	 * 
	 * @param reader
	 *            the Stream to read from.
	 * @return
	 * @throws IOException
	 */
	private String readName(ScriptEaseFileAccess reader) throws IOException {
		final String type = this.fileType.trim();
		String name;

		// creature blueprint
		if (type.equalsIgnoreCase(GenericFileFormat.TYPE_CREATURE_BP)) {
			final String lastName;

			name = this.readField(reader, "FirstName");
			lastName = this.readField(reader, "LastName");

			name += lastName == null ? "" : " " + lastName;
		}
		// door, placeable, item, merchant/store blueprints
		else if (type.equalsIgnoreCase(GenericFileFormat.TYPE_DOOR_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_PLACEABLE_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_MERCHANT_BP)) {
			name = this.readField(reader, "LocName");
		}
		// item, encounter, trigger, waypoint blueprints
		else if (type.equalsIgnoreCase(GenericFileFormat.TYPE_ITEM_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_ENCOUNTER_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_TRIGGER_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_WAYPOINT_BP)) {
			name = this.readField(reader, "LocalizedName");
		}
		// sound, conversation and other blueprints
		else {
			name = this.resRef;
		}

		return name;
	}

	/**
	 * Reads the tag from this GFF, if it has one. Otherwise, it returns an
	 * empty string.
	 * 
	 * @param reader
	 *            The reader to read from.
	 * @return The tag of this GFF, or an empty string.
	 * @throws IOException
	 */
	private String readTag(ScriptEaseFileAccess reader) throws IOException {
		final String tag;
		final String type = this.fileType.trim();

		if (type.equalsIgnoreCase("ARE")
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_CREATURE_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_DOOR_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_ENCOUNTER_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_ITEM_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_MERCHANT_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_PLACEABLE_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_SOUND_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_TRIGGER_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_WAYPOINT_BP)) {
			tag = this.readField(reader, "Tag");
		} else
			tag = "";
		return tag;
	}

	/**
	 * Remove generated references when we read it in, so that when we write it
	 * out, the naming and references are correct.
	 * 
	 * @param reader
	 * @throws IOException
	 */
	private void removeGeneratedReferences(ScriptEaseFileAccess reader)
			throws IOException {
		final GameTypeManager typeManager = TranslatorManager.getInstance()
				.getTranslator(ErfFile.NEVERWINTER_NIGHTS).getGameTypeManager();
		final Collection<String> scriptSlots;
		String reference;

		// remove all generates script referencesd

		scriptSlots = typeManager.getSlots(getScriptEaseType());
		for (String slotName : scriptSlots) {
			reference = this.readField(reader, slotName);

			// don't keep references to ScriptEase-generated files
			if (ErfFile.isScriptEaseGenerated(reference)) {

				// TODO: get the default script for the patterns file
				final String originalScript = "";

				this.setField(slotName, originalScript);
			}
		}
	}

	/**
	 * @return the objectRepresentation
	 */
	public final GameConstant getObjectRepresentation() {
		return objectRepresentation;
	}

	/**
	 * Returns an equivalent ScriptEase GameType for the NWNObject Type. It will
	 * default to the first type if the NWN type is not found.
	 * 
	 * @return a GameType representing the NWN type.
	 */
	public String getScriptEaseType() {
		String type = null;
		final String typeString = this.fileType.trim();
		if (typeString.equalsIgnoreCase(GenericFileFormat.TYPE_CREATURE_BP)) {
			type = "creature";
		} else if (typeString.equalsIgnoreCase(GenericFileFormat.TYPE_DOOR_BP)) {
			type = "door";
		} else if (typeString
				.equalsIgnoreCase(GenericFileFormat.TYPE_ENCOUNTER_BP)) {
			type = "encounter";
		} else if (typeString.equalsIgnoreCase(GenericFileFormat.TYPE_ITEM_BP)) {
			type = "item";
		} else if (typeString
				.equalsIgnoreCase(GenericFileFormat.TYPE_MERCHANT_BP)) {
			type = "merchant";
		} else if (typeString
				.equalsIgnoreCase(GenericFileFormat.TYPE_PLACEABLE_BP)) {
			type = "placeable";
		} else if (typeString.equalsIgnoreCase(GenericFileFormat.TYPE_SOUND_BP)) {
			type = "sound";
		} else if (typeString
				.equalsIgnoreCase(GenericFileFormat.TYPE_TRIGGER_BP)) {
			type = "trigger";
		} else if (typeString
				.equalsIgnoreCase(GenericFileFormat.TYPE_WAYPOINT_BP)) {
			type = "waypoint";
		} else if (typeString
				.equalsIgnoreCase(GenericFileFormat.TYPE_DIALOGUE_BP)) {
			type = NWNConversation.DIALOGUE;
		} else if (typeString
				.equalsIgnoreCase(GenericFileFormat.TYPE_JOURNAL_BP)) {
			type = "journal";
		} else if (typeString
				.equalsIgnoreCase(GenericFileFormat.TYPE_MODULE_BP)) {
			type = "module";
		}
		if (type == null) {
			System.err
					.println("Could not convert NWN type to ScriptEase type. Defaulting type to first available");
			type = GameTypeManager.DEFAULT_VOID_TYPE;
		}
		return type;
	}

	private String readField(ScriptEaseFileAccess reader, String label)
			throws IOException {
		String fieldData = null;

		// find the field with the supplied label
		GffField field = findFieldForLabel(label);

		fieldData = field.readString(reader);

		return fieldData;
	}

	/**
	 * Searches this GFF's field list for the field that matches the supplied
	 * label.
	 * 
	 * @param label
	 *            The label to search by.
	 * @return The field that matches the given label, or <code>null</code> if
	 *         the field
	 */
	protected GffField findFieldForLabel(String label) {
		String searchLabel;
		for (GffField field : this.fieldArray) {
			searchLabel = this.labelArray.get((int) field.getLabelIndex());

			if (searchLabel.equalsIgnoreCase(label)) {
				return field;
			}
		}

		throw new IllegalStateException(
				"Tried to read a field from a GFF type that does not include the field "
						+ label);
	}

	/**
	 * Determines if this GFF is of a type that is useful to ScriptEase. If
	 * ScriptEase needs to be able to read new types, they should be added here. <br>
	 * <br>
	 * A potential future improvement could be to take the list of important
	 * data types and store them in a file as part of a data-driven approach.
	 * 
	 * @return if this GFF is important to ScriptEase
	 */
	private boolean isValuable() {
		String typeString = this.fileType.trim();

		ArrayList<String> importantTypes = new ArrayList<String>();

		importantTypes.add(GenericFileFormat.TYPE_DIALOGUE_BP); // conversation
		// file
		importantTypes.add(GenericFileFormat.TYPE_MODULE_BP); // module
		// TODO: determine if journal files are important
		importantTypes.add(GenericFileFormat.TYPE_JOURNAL_BP); // journal file
		importantTypes.add(GenericFileFormat.TYPE_CREATURE_BP); // creature
		// blueprint
		importantTypes.add(GenericFileFormat.TYPE_DOOR_BP); // door blueprint
		importantTypes.add(GenericFileFormat.TYPE_ENCOUNTER_BP); // encounter
		// blueprint
		importantTypes.add(GenericFileFormat.TYPE_ITEM_BP); // item blueprint
		importantTypes.add(GenericFileFormat.TYPE_MERCHANT_BP); // merchant/store
		// blueprint
		importantTypes.add(GenericFileFormat.TYPE_PLACEABLE_BP); // placeable
		// blueprint
		importantTypes.add(GenericFileFormat.TYPE_SOUND_BP); // sound blueprint
		importantTypes.add(GenericFileFormat.TYPE_TRIGGER_BP); // trigger
		// blueprint
		importantTypes.add(GenericFileFormat.TYPE_WAYPOINT_BP); // waypoint
		// blueprint

		// file
		importantTypes.add(GenericFileFormat.TYPE_GAME_INSTANCE_FILE);
		// GIT File for instances

		return importantTypes.contains(typeString);
	}

	private final void readFields(ScriptEaseFileAccess reader)
			throws IOException {
		reader.seek(this.gffOffset + this.fieldOffset);

		int numFields = (int) this.fieldCount;

		for (int i = 0; i < numFields; i++) {
			this.fieldArray.add(new GffField(reader, i));
		}
	}

	private final void readStructs(ScriptEaseFileAccess reader)
			throws IOException {
		reader.seek(this.gffOffset + this.structOffset);

		int numFields = (int) this.structCount;

		for (int i = 0; i < numFields; i++) {
			this.structArray.add(new GffStruct(reader));
		}
	}

	private final void readLabels(ScriptEaseFileAccess reader)
			throws IOException {
		reader.seek(this.gffOffset + this.labelOffset);

		int numLabels = (int) this.labelCount;

		for (int i = 0; i < numLabels; i++) {
			this.labelArray.add(reader
					.readString(GenericFileFormat.LABEL_BYTE_LENGTH));
		}
	}

	@Override
	public String toString() {
		String stringRep = "";

		stringRep += "GFF [Type:" + this.fileType + "](" + this.resRef
				+ this.fieldCount + ", " + this.fieldOffset + ", "
				+ this.fieldDataCount + ", " + this.fieldDataOffset + ", "
				+ this.fieldIndicesCount + ", " + this.fieldIndicesOffset
				+ ", " + this.listIndicesCount + ", " + this.listIndicesOffset
				+ ", " + this.structCount + ", " + this.structOffset + ")\n";

		for (GffField field : this.fieldArray) {
			stringRep += "  - FieldTypeID: " + field.typeNumber + "\n";
		}

		return stringRep;
	}

	/**
	 * Caches this GFF directly to memory.
	 * 
	 * @param reader
	 *            The file to cache from.
	 * @param gffSize
	 *            The length of this GFF in bytes.
	 * @throws IOException
	 */
	public void cacheData(ScriptEaseFileAccess reader, int gffSize)
			throws IOException {
		reader.seek(this.gffOffset);
		// this.dataCache = reader.readBytes(gffSize);

		final long bytesBeforeFieldIndicesOffset = this.fieldIndicesOffset;
		this.beforeFieldIndicesArray = reader
				.readBytes((int) bytesBeforeFieldIndicesOffset);

		final long bytesAfterFieldIndicesOffset = gffSize
				- bytesBeforeFieldIndicesOffset;
		reader.seek(this.gffOffset + this.fieldIndicesOffset);
		this.afterFieldIndicesArray = reader
				.readBytes((int) bytesAfterFieldIndicesOffset);
	}

	/**
	 * Writes this GFF to disk as per the BioWare documentation.
	 * 
	 * @param writer
	 *            The file to write to.
	 * @throws IOException
	 */
	public void write(ScriptEaseFileAccess writer, long filePosition)
			throws IOException {
		// update the filePosition for future writes
		this.gffOffset = filePosition;

		// first, write out the cached version of this GFF so we can update it
		// writer.seek(this.gffOffset);
		// writer.writeBytes(this.dataCache, false);

		// write up to the FieldIndicesArray
		writer.seek(this.gffOffset);
		writer.writeBytes(this.beforeFieldIndicesArray, false);

		// now we update the file's data.
		// write the updated header
		writer.seek(this.gffOffset);
		this.writeHeader(writer);

		for (GffField changedField : this.changedFieldMap.keySet()) {
			// write out the field data again because it may have updated
			// TODO: This changedField.getFieldOffset may be blatantly wrong.
			// writer.seek(this.fieldOffset + changedField.getFieldOffset());
			// changedField.write(writer);

			// right now I'm only concerned with updating the script slots
			// - remiller
			if (!changedField.isResRefType())
				throw new RuntimeException(
						"Can't write anything but slot refs. The module is probably corrupted now. Oops.");

			changedField.writeFieldData(writer, this.gffOffset
					+ this.fieldDataOffset,
					this.changedFieldMap.get(changedField));
		}

		// write the FieldIndiciesArray and whatever is below it
		writer.seek(this.gffOffset + this.fieldIndicesOffset);
		writer.writeBytes(this.afterFieldIndicesArray, false);

		// clear the data cache since we don't need it any more.
		// this.dataCache = null;
		this.beforeFieldIndicesArray = null;
		this.afterFieldIndicesArray = null;
	}

	/**
	 * Writes this GFF's header information to disk.
	 * 
	 * @throws IOException
	 */
	private void writeHeader(ScriptEaseFileAccess writer) throws IOException {
		writer.writeString(this.fileType, 4);
		writer.writeString(this.version, 4);
		writer.writeUnsignedInt(this.structOffset, true);
		writer.writeUnsignedInt(this.structCount, true);
		writer.writeUnsignedInt(this.fieldOffset, true);
		writer.writeUnsignedInt(this.fieldCount, true);
		writer.writeUnsignedInt(this.labelOffset, true);
		writer.writeUnsignedInt(this.labelCount, true);
		writer.writeUnsignedInt(this.fieldDataOffset, true);
		writer.writeUnsignedInt(this.fieldDataCount, true);
		writer.writeUnsignedInt(this.fieldIndicesOffset, true);
		writer.writeUnsignedInt(this.fieldIndicesCount, true);
		writer.writeUnsignedInt(this.listIndicesOffset, true);
		writer.writeUnsignedInt(this.listIndicesCount, true);
	}

	/**
	 * Simple class for reading and representing GFF Structs. Based off table
	 * 3.3 in GFF documentation.
	 * 
	 * All of the values in a GFF Struct are DWORDs.
	 * 
	 * Struct.Type - Programmer-defined integer ID
	 * 
	 * Struct.DataOrDataOffset - If Struct.FieldCount = 1, this is an index into
	 * the Field Array. If Struct.FieldCount > 1, this is a byte offset into the
	 * Field Indices array, where there is an array of DWORDs having a number of
	 * elements equal to Struct.FieldCount. Each one of these DWORDs is an index
	 * into the Field Array.
	 * 
	 * Struct.FieldCount - Number of fields in this Struct
	 * 
	 * @author remiller
	 * @author mfchurch
	 * 
	 */
	public class GffStruct {
		private final long typeNumber;
		private final long dataOrDataOffset;
		private final long fieldCount;

		public GffStruct(ScriptEaseFileAccess reader) throws IOException {
			this.typeNumber = reader.readUnsignedInt(true);
			this.dataOrDataOffset = reader.readUnsignedInt(true);
			this.fieldCount = reader.readUnsignedInt(true);
		}

		@Override
		public String toString() {
			return "GffStruct [" + typeNumber + ", " + dataOrDataOffset + ", "
					+ fieldCount + "]";
		}

		public GenericFileFormat getGFF() {
			return genericFileFormat;
		}

		/**
		 * Reads the fields from the given GffStruct and returns them in a List.
		 * 
		 * @param reader
		 * @param struct
		 * @return
		 * @throws IOException
		 */
		protected List<GffField> readGffFields(ScriptEaseFileAccess reader)
				throws IOException {
			final int fieldCount = (int) this.fieldCount;
			final int dataOrDataOffset = (int) this.dataOrDataOffset;
			final List<GffField> fields = new ArrayList<GffField>(fieldCount);

			/*
			 * If Struct.FieldCount = 1, dataOrDataOffset is an index into the
			 * Field Array.
			 */
			if (fieldCount == 1) {
				final GffField field = fieldArray.get(dataOrDataOffset);
				fields.add(field);
			}
			/*
			 * If Struct.FieldCount > 1, dataOrDataOffset is a byte offset into
			 * the Field Indices array, where there is an array of DWORDs having
			 * a number of elements equal to Struct.FieldCount. Each one of
			 * these DWORDs is an index into the Field Array.
			 */
			else {
				reader.seek(gffOffset + fieldIndicesOffset + dataOrDataOffset);

				List<Long> indices = new ArrayList<Long>(fieldCount);
				for (long i = 0; i < fieldCount; i++) {
					long index = reader.readUnsignedInt(true);
					indices.add(index);
				}

				for (long index : indices) {
					final GffField field = fieldArray.get((int) index);
					fields.add(field);
				}
			}
			return fields;
		}

		/**
		 * Determines if this is the top level struct.
		 * 
		 * @return Whether this is the top level struct
		 */
		public final boolean isTopLevelStruct() {
			// documentation states the top level struct always has a type
			// number of 0xFFFFFFFF, which is -1 when considered signed
			return this.typeNumber == 0xFFFFFFFF;
		}
	}

	/**
	 * Conversation Struct as defined in Table 2.1 of the Conversation
	 * documentation
	 * 
	 * @author mfchurch
	 * 
	 */
	public class NWNConversation implements GameConversation {
		public static final String DIALOGUE = "dialogue";
		// ResRef of script to run when the conversation is aborted, such as by
		// combat, hitting the ESC key, or saving a game in the middle of
		// conversation.
		private String endConverAbort;
		// ResRef of script to run when the conversation ends normally.
		private String endConversation;
		// List of NPC Dialog Structs. StructID = list index.
		private List<NPCEntryDialogue> entryList;
		// The startingList is the list of all lines of dialog that appear at
		// the root level of the conversation tree.
		private List<EntriesSyncStruct> startingList;
		// List of Player Dialog Structs. StructID = list index.
		private List<PlayerReplyDialogue> replyList;
		private String resRef;

		public NWNConversation(ScriptEaseFileAccess reader, String resRef,
				GffStruct struct) throws IOException {
			this.resRef = resRef;
			// parse the important conversation fields
			this.build(reader, struct);
		}

		/**
		 * Gets the NWNDialog specified by the given resRef. Assumes the resRef
		 * is in the format: list_index
		 * 
		 * @param resRef
		 * @return
		 */
		public DialogueLine getDialogLine(String resRef) {
			final String[] split = resRef.split("_");
			final String list = split[0];
			final Integer index = new Integer(split[1]);
			if (list.equalsIgnoreCase(PlayerReplyDialogue.PLAYER_REPLY_LIST)) {
				return replyList.get(index);
			} else if (list.equalsIgnoreCase(NPCEntryDialogue.NPC_ENTRY_LIST)) {
				return entryList.get(index);
			} else
				throw new IllegalArgumentException("Invalid NWNDialog resRef");
		}

		/**
		 * Parse the conversation fields we care about from the stored fields
		 * 
		 * @param reader
		 */
		private void build(ScriptEaseFileAccess reader, GffStruct struct)
				throws IOException {
			final List<GffField> fields = struct.readGffFields(reader);

			// parse the fields that we care about
			for (GffField field : fields) {
				final String label = labelArray
						.get((int) field.getLabelIndex());

				// Conversation Fields
				if (label.equals("EndConverAbort")) {
					endConverAbort = field.readString(reader);
				} else if (label.equals("EndConversation")) {
					endConversation = field.readString(reader);
				} else if (label.equals("EntryList")) {
					entryList = new ArrayList<NPCEntryDialogue>();
					List<GffStruct> readList = field.readList(reader);
					for (GffStruct aStruct : readList) {
						entryList.add(new NPCEntryDialogue(reader, aStruct));
					}
				} else if (label.equals("ReplyList")) {
					replyList = new ArrayList<PlayerReplyDialogue>();
					List<GffStruct> readList = field.readList(reader);
					for (GffStruct aStruct : readList) {
						replyList.add(new PlayerReplyDialogue(reader, aStruct));
					}
				} else if (label.equals("StartingList")) {
					startingList = new ArrayList<EntriesSyncStruct>();
					List<GffStruct> readList = field.readList(reader);
					for (GffStruct aStruct : readList) {
						EntriesSyncStruct sync = new EntriesSyncStruct(reader,
								aStruct);
						startingList.add(sync);
					}
				}

			}
			for (NPCEntryDialogue entry : this.entryList) {
				entry.resolveReplies();
			}

			for (PlayerReplyDialogue reply : this.replyList) {
				reply.resolveEntries();
			}

			for (EntriesSyncStruct entry : this.startingList) {
				entry.updateReference();
			}
		}

		private NPCEntryDialogue getElementFromEntryList(int index) {
			if (this.entryList != null && index >= 0
					&& index < this.entryList.size()) {
				return entryList.get(index);
			}
			return null;
		}

		private PlayerReplyDialogue getElementFromReplyList(int index) {
			if (this.replyList != null && index >= 0
					&& index < this.replyList.size()) {
				return this.replyList.get(index);
			}
			throw new IllegalStateException("Invalid Reply Reference");
		}

		@Override
		public String getResolutionText() {
			return this.resRef;
		}

		@Override
		public String getName() {
			return this.resRef;
		}

		@Override
		public String toString() {
			return this.getName();
		}

		@Override
		public Collection<String> getTypes() {
			ArrayList<String> types = new ArrayList<String>();
			types.add(DIALOGUE);
			return types;
		}

		@Override
		public List<GameConversationNode> getConversationRoots() {
			List<GameConversationNode> list = new ArrayList<GameConversationNode>(
					startingList.size());
			for (EntriesSyncStruct entry : this.startingList) {
				list.add(entry.getReference());
			}
			return list;
		}

		@Override
		public String getTemplateID() {
			return this.resRef;
		}

		/**
		 * SyncStruct as defined in section 2.3 of the Conversation
		 * documentation.
		 * 
		 * @author mfchurch
		 * 
		 */
		public abstract class DialogSyncStruct {
			protected int index;
			protected GffField active;
			private boolean isLink;

			public DialogSyncStruct(ScriptEaseFileAccess reader,
					GffStruct struct) throws IOException {
				// parse the important dialog fields
				this.build(reader, struct);
			}

			private void build(ScriptEaseFileAccess reader, GffStruct struct)
					throws IOException {
				final List<GffField> fields = struct.readGffFields(reader);

				// Read the reference fields
				for (GffField field : fields) {
					final String label = labelArray.get((int) field
							.getLabelIndex());

					// Dialog Fields
					if (label.equals("Active")) {
						this.active = field;
					}
					// Index into the Top-Level Struct EntryList
					else if (label.equals("Index")) {
						this.index = new Integer(field.readString(reader));
					}
					// Field 'IsChild' is 1 when it is a link. Because that
					// totally makes sense, BioWare.
					else if (label.equals("IsChild")) {
						this.isLink = field.readString(reader).equals("1");
					}
				}
			}

			protected abstract void updateReference();

			public abstract DialogueLine getReference();

			public boolean isLink() {
				return this.isLink;
			}
		}

		/**
		 * Represents a EntriesList Sync Struct as detailed in Table 2.3.3 of
		 * the Conversation documentation.
		 * 
		 * @author mfchurch
		 * 
		 */
		public class EntriesSyncStruct extends DialogSyncStruct {

			public EntriesSyncStruct(ScriptEaseFileAccess reader,
					GffStruct struct) throws IOException {
				super(reader, struct);
			}

			@Override
			protected void updateReference() {
				// append the data to the existing entry
				final NPCEntryDialogue entry = this.getReference();
				if (active != null && entry != null) {
					entry.active = this.active;
					entry.index = this.index;
					entry.isLink = this.isLink();
				} else
					throw new IllegalStateException("Invalid EntriesSyncStruct");
			}

			@Override
			public NPCEntryDialogue getReference() {
				return getElementFromEntryList(index);
			}
		}

		/**
		 * Represents a RepliesList Sync Struct as detailed in Table 2.3.2 of
		 * the Conversation documentation.
		 * 
		 * @author mfchurch
		 * 
		 */
		public class RepliesSyncStruct extends DialogSyncStruct {

			public RepliesSyncStruct(ScriptEaseFileAccess reader,
					GffStruct struct) throws IOException {
				super(reader, struct);
			}

			@Override
			protected void updateReference() {
				// append the data to the existing entry
				final PlayerReplyDialogue reply = getReference();
				if (active != null && reply != null) {
					reply.active = this.active;
					reply.index = this.index;
					reply.isLink = this.isLink();
				} else
					throw new IllegalStateException("Invalid RepliesSyncStruct");
			}

			@Override
			public PlayerReplyDialogue getReference() {
				return getElementFromReplyList(index);
			}
		}

		/**
		 * Dialog Struct as defined in Table 2.2.1 of the Conversation
		 * documentation.
		 * 
		 * @author mfchurch
		 * 
		 */
		public abstract class DialogueLine implements GameConversationNode {
			// Dialog line resref format: dialogResRef_list_index
			public static final String DIALOG_LINE_REF_REGEX = "[a-zA-Z0-9]+_[a-zA-Z]+_[0-9]";
			private static final String DIALOG_LINE = "dialogue_line";
			// ResRef of conditional script to run to determine if this line of
			// conversation appears to the player.
			protected GffField active;
			// ResRef of script to run when showing this line
			protected GffField script;
			// Localized text to display to the user for this line of dialog
			protected String text;
			// Index representing the location of the Dialog in the conversation
			protected int index;

			protected boolean isLink;

			public DialogueLine(ScriptEaseFileAccess reader, GffStruct struct)
					throws IOException {
				// parse the important dialog fields
				this.build(reader, struct);
			}

			public String getConversationResRef() {
				return resRef;
			}

			/**
			 * Parse the dialog fields we care about from the stored fields
			 * 
			 * @param reader
			 */
			protected void build(ScriptEaseFileAccess reader, GffStruct struct)
					throws IOException {
				final List<GffField> fields = struct.readGffFields(reader);

				for (GffField field : fields) {
					final String label = labelArray.get((int) field
							.getLabelIndex());

					// Dialog Fields
					if (label.equals("Text")) {
						text = field.readString(reader);
					}
					// 'script' = when dialogue line is reached
					else if (label.equals("Script")) {
						script = field;
					}
				}
			}

			@Override
			public Collection<String> getTypes() {
				ArrayList<String> arrayList = new ArrayList<String>(1);
				arrayList.add(DIALOG_LINE);
				return arrayList;
			}

			@Override
			public String getResolutionText() {
				return getTemplateID();
			}

			@Override
			public String getName() {
				return text;
			}

			public GffField getField(String field) {
				// 'active' = Should dialogue line be displayed (filter)
				if (field.equalsIgnoreCase("Active")) {
					return this.active;
				}
				// 'script' = When dialogue line is displayed
				else if (field.equalsIgnoreCase("Script")) {
					return this.script;
				} else
					throw new IllegalArgumentException(
							"Dialog does not have the field " + field);
			}

			@Override
			public boolean isLink() {
				return false;
			}

			@Override
			public boolean isTerminal() {
				return this.getChildren().size() == 0;
			}
		}

		/**
		 * A Dialog Struct contained in the Player ReplyList contains all the
		 * Fields listed in Table 2.2.1, plus those Fields listed in Table 2.2.2
		 * of the Conversation documentation
		 * 
		 * @author mfchurch
		 * 
		 */
		public class PlayerReplyDialogue extends DialogueLine {
			// List of Sync Structs describing the list of possible NPC replies
			// to this line of player dialog.. Struct ID = list index.
			private List<EntriesSyncStruct> entryPointers;

			public final static String PLAYER_REPLY_LIST = "replylist";

			public PlayerReplyDialogue(ScriptEaseFileAccess reader,
					GffStruct struct) throws IOException {
				super(reader, struct);
			}

			public void resolveEntries() {
				if (entryPointers == null)
					throw new IllegalStateException(
							"EntryList has not been initialized yet!");
				for (EntriesSyncStruct entry : entryPointers) {
					entry.updateReference();
				}
			}

			@Override
			protected void build(ScriptEaseFileAccess reader, GffStruct struct)
					throws IOException {
				super.build(reader, struct);

				final List<GffField> fields = struct.readGffFields(reader);

				entryPointers = new ArrayList<EntriesSyncStruct>();

				for (GffField field : fields) {
					final String label = labelArray.get((int) field
							.getLabelIndex());

					// List of Sync Structs describing the list of possible NPC
					// replies to this line of player dialog
					if (label.equals("EntriesList")) {
						List<GffStruct> readList = field.readList(reader);
						for (GffStruct aStruct : readList) {
							final EntriesSyncStruct sync = new EntriesSyncStruct(
									reader, aStruct);
							entryPointers.add(sync);
						}
					}
				}
			}

			@Override
			public String toString() {
				return "PlayerReplyDialog [" + this.text + "]";
			}

			/**
			 * Returns a copy of the npc entry list
			 */
			@Override
			public List<NPCEntryDialogue> getChildren() {
				final List<NPCEntryDialogue> list = new ArrayList<NPCEntryDialogue>(
						this.entryPointers.size());
				for (EntriesSyncStruct entry : this.entryPointers) {
					list.add(entry.getReference());
				}
				return list;
			}

			/**
			 * Resref for the dialog line since they don't actually have one
			 * 
			 * @see DIALOG_LINE_REF_REGEX
			 */
			@Override
			public String getTemplateID() {
				return resRef + "_" + PLAYER_REPLY_LIST + "_" + index;
			}
		}

		/**
		 * A Dialog Struct contained in the NPC EntryList contains all the
		 * Fields found in a Dialog Struct as detailed in Table 2.2.1, plus
		 * those Fields listed in Table 2.2.3 of the Conversation documentation
		 * 
		 * @author mfchurch
		 * 
		 */
		public class NPCEntryDialogue extends DialogueLine {
			// List of Sync Structs describing the list of possible Player
			// replies
			// to this line of NPC dialog. Struct ID = list index.
			private List<RepliesSyncStruct> replyPointers;

			// Tag of the speaker. Blank if the speaker is the conversation
			// owner.
			private String speaker;

			public final static String NPC_ENTRY_LIST = "entrylist";

			public NPCEntryDialogue(ScriptEaseFileAccess reader,
					GffStruct struct) throws IOException {
				super(reader, struct);
			}

			/**
			 * Triggers all of the replyList Pointers to push their Active and
			 * Index values to what they are referencing. This should be done
			 * _after_ the EntryList and ReplyList have been read in, otherwise
			 * we cannot guarantee the index is valid.
			 */
			public void resolveReplies() {
				if (replyPointers == null)
					throw new IllegalStateException(
							"ReplyList has not been initialized yet!");
				for (RepliesSyncStruct reply : replyPointers) {
					reply.updateReference();
				}
			}

			@Override
			protected void build(ScriptEaseFileAccess reader, GffStruct struct)
					throws IOException {
				super.build(reader, struct);

				replyPointers = new ArrayList<RepliesSyncStruct>();

				final List<GffField> fields = struct.readGffFields(reader);

				for (GffField field : fields) {
					final String label = labelArray.get((int) field
							.getLabelIndex());

					// List of Sync Structs describing the list of possible
					// Player replies to this line of NPC dialog.
					if (label.equals("RepliesList")) {
						List<GffStruct> readList = field.readList(reader);
						for (GffStruct aStruct : readList) {
							final RepliesSyncStruct sync = new RepliesSyncStruct(
									reader, aStruct);
							replyPointers.add(sync);
						}
					} else if (label.equals("Speaker")) {
						speaker = field.readString(reader);
					}
				}
			}

			@Override
			public String toString() {
				return "NPCEntryDialog [" + getName() + "]";
			}

			/**
			 * Returns a copy of the player replies list
			 */
			@Override
			public List<PlayerReplyDialogue> getChildren() {
				final List<PlayerReplyDialogue> list = new ArrayList<PlayerReplyDialogue>(
						this.replyPointers.size());
				for (RepliesSyncStruct reply : this.replyPointers) {
					list.add(reply.getReference());
				}
				return list;
			}

			/**
			 * Resref for the dialog line since they don't actually have one
			 * 
			 * @see DIALOG_LINE_REF_REGEX
			 */
			@Override
			public String getTemplateID() {
				return resRef + "_" + NPC_ENTRY_LIST + "_" + index;
			}
		}
	}

	/**
	 * Simple container for GFF Field data. Based off table 3.4a in GFF
	 * documentation.
	 * 
	 * @author remiller
	 * 
	 */
	public class GffField {
		private static final int MAX_RESREF_LENGTH = 16;
		private static final int TYPE_NUM_CEXOLOCSTRING = 12;
		private static final int TYPE_NUM_RESREF = 11;
		private static final int TYPE_NUM_CEXOSTRING = 10;
		private static final int TYPE_NUM_DOUBLE = 9;
		private static final int TYPE_NUM_INT64 = 7;
		private static final int TYPE_NUM_DWORD64 = 6;
		private final long typeNumber;
		private final long labelIndex;
		private long dataOrDataOffset; // this can increase if its an offset
		
		// this isn't part of the original struct. Added so that field knows
		// where it lives within the Field Array. - remiller
		private long fieldIndex;

		private GffField(ScriptEaseFileAccess reader, long index)
				throws IOException {
			this.typeNumber = reader.readUnsignedInt(true);
			this.labelIndex = reader.readUnsignedInt(true);
			this.dataOrDataOffset = reader.readUnsignedInt(true);
			this.fieldIndex = index;
		}

		@Override
		public String toString() {
			return "GffField [" + labelArray.get((int) labelIndex) + "]";
		}

		public void write(ScriptEaseFileAccess writer) throws IOException {
			writer.writeUnsignedInt(this.typeNumber, true);
			writer.writeUnsignedInt(this.labelIndex, true);
			writer.writeUnsignedInt(this.dataOrDataOffset, true);
		}
		
		public GenericFileFormat getGFF(){
			return genericFileFormat;
		}

		/**
		 * @return Unsigned int in a long representing the type.
		 */
		public long getType() {
			return typeNumber;
		}

		/**
		 * @return the labelIndex
		 */
		public long getLabelIndex() {
			return labelIndex;
		}

		/**
		 * @return the dataOrDataOffset
		 */
		private long getDataOrDataOffset() {
			return dataOrDataOffset;
		}

		/**
		 * Sums the data offset with the given delta.
		 * 
		 * @param delta
		 */
		protected void increaseOffset(int delta) {
			this.dataOrDataOffset += delta;
		}

		/**
		 * Gets the offset of this field within the Field Array.
		 * 
		 * @return the offset of this field within the Field Array.
		 */
		protected long getFieldOffset() {
			// 4 * 3 is for the three unsigned int (4-byte) data members
			int size = (4 * 3);
			return this.fieldIndex * size;
		}

		public GffStruct readGffStruct(ScriptEaseFileAccess reader)
				throws IOException {
			// Normally, a Field's DataOrDataOffset value would be a byte offset
			// into the Field Data Block, but for a Struct, it is an index into
			// the Struct Array.
			if (this.isStructType()) {
				return structArray.get((int) this.dataOrDataOffset);
			} else
				throw new IllegalStateException(
						"GffField does not contain a GffStruct.");
		}

		public List<GffStruct> readList(ScriptEaseFileAccess reader)
				throws IOException {
			if (this.isListType()) {
				List<GffStruct> list = new ArrayList<GffStruct>();

				// The starting address of a List is specified in its Field's
				// DataOrDataOffset value as a byte offset into the Field
				// Indices Array, at which is located a List element.
				reader.seek(gffOffset + listIndicesOffset
						+ this.dataOrDataOffset);

				// The first DWORD is the Size of the List, and it specifies how
				// many Struct elements the List contains.
				long size = reader.readUnsignedInt(true);

				// There are Size DWORDS after that, each one an index into the
				// Struct Array.
				List<Long> indices = new ArrayList<Long>((int) size);
				for (long i = 0; i < size; i++) {
					long index = reader.readUnsignedInt(true);
					indices.add(index);
				}
				// Resolve the indices
				for (long index : indices) {
					final GffStruct struct = structArray.get((int) index);
					list.add(struct);
				}

				return list;
			} else
				throw new IllegalStateException(
						"GffField does not contain a GffList.");
		}

		/**
		 * Reads this field's data and converts it to a string.
		 * 
		 * @param reader
		 *            The reader to read from.
		 * @return The field data as a string.
		 * @throws IOException
		 *             If everything goes to hell.
		 */
		public String readString(ScriptEaseFileAccess reader)
				throws IOException {
			String fieldData = null;

			if (!this.isComplexType()) { // just data
				fieldData = Long.toString(this.dataOrDataOffset);
			} else if (!this.isListType() && !this.isStructType()) {
				// get to the data
				reader.seek(gffOffset + fieldDataOffset + this.dataOrDataOffset);

				long length;

				switch ((int) this.getType()) {
				case GffField.TYPE_NUM_DWORD64:
					fieldData = Long.toString(reader.readUnsignedInt(true));
					break;
				case GffField.TYPE_NUM_INT64:
					fieldData = Integer.toString(reader.readInt(true));
					break;
				case GffField.TYPE_NUM_DOUBLE:
					fieldData = Double.toString(reader.readDouble());
					break;
				case GffField.TYPE_NUM_CEXOSTRING:
					length = reader.readUnsignedInt(true);

					fieldData = reader.readString((int) length);
					break;
				case GffField.TYPE_NUM_RESREF:
					length = reader.readByte();

					fieldData = reader.readString((int) length);

					break;
				case GffField.TYPE_NUM_CEXOLOCSTRING:
					length = reader.readUnsignedInt(true);
					long stringRef = reader.readUnsignedInt(true);

					long stringCount = reader.readUnsignedInt(true);

					// internationalization
					if (stringCount == 0) {
						TlkLookup stringTable = TlkLookup.getInstance();

						fieldData = stringTable.lookup(stringRef);
					}
					// Always take the english version of the string,
					// because. 8--> :) -mfchurch
					else {
						for (int i = 0; i < stringCount; i++) {
							/* int stringID = */reader.readInt(true);
							int stringLen = reader.readInt(true);
							fieldData = reader.readString(stringLen);
						}
					}
					break;
				}
			} else
				throw new IllegalStateException(
						"GffField does not contain a String.");

			return fieldData;
		}

		public void writeFieldData(ScriptEaseFileAccess writer,
				long fieldDataOffset, String newData) throws IOException {
			// non-complex types are inherently handled by writing the field
			// itself, since that's where they are stored, so we just deal with
			// complex types here.
			if (!this.isComplexType()) {
				return;
			}

			// a bunch of stuff is disabled in this control flow. I just need to
			// get script slots working, and I don't have the time to make
			// everything else work, especially since we may not need much else.
			// --remiller
			if (this.isStructType()) {
				System.err.println("I can't write a struct yet!");
			} else if (this.isListType()) {
				System.err.println("I can't write a list yet!");
			} else {
				// get to the data location
				writer.seek(fieldDataOffset + this.getDataOrDataOffset());

				// long length;

				switch ((int) this.getType()) {
				case GffField.TYPE_NUM_DWORD64:
					// Long.toString(reader.writeUnsignedInt(true));
					System.err.println("I can't write a DWord64 yet!");
					break;
				case GffField.TYPE_NUM_INT64:
					// Integer.toString(reader.writeInt(true));
					System.err.println("I can't write an Int64 yet!");
					break;
				case GffField.TYPE_NUM_DOUBLE:
					// Double.toString(reader.writeDouble());
					System.err.println("I can't write a Double yet!");
					break;
				case GffField.TYPE_NUM_CEXOSTRING:
					// length = reader.readUnsignedInt(true);

					// fieldData = reader.readString((int) length);
					System.err.println("I can't write a CExoString yet!");

					break;
				case GffField.TYPE_NUM_RESREF:
					if (newData.length() > GffField.MAX_RESREF_LENGTH)
						newData = newData.substring(0,
								GffField.MAX_RESREF_LENGTH);

					newData = newData.toLowerCase();

					writer.writeByte(newData.length());
					
					writer.writeString(newData, newData.length());

					break;
				case GffField.TYPE_NUM_CEXOLOCSTRING:
					// length = reader.readUnsignedInt(true);
					// long stringRef = reader.writeUnsignedInt(true);

					// TlkLookup stringTable = TlkLookup.getInstance();

					// stringTable.lookup(stringRef);

					System.err.println("I can't write a CExoLocString yet!");

					break;
				}
			}
		}

		public final boolean isResRefType() {
			return this.typeNumber == GffField.TYPE_NUM_RESREF;
		}

		/**
		 * Determines whether the type is a complex type (meaning the field data
		 * is located in the field data block of the GFF, or elsewhere for
		 * structs/lists) or a simple type. This check is based on table 3.4b in
		 * the GFF documentation.
		 * 
		 * @return Whether this field is a complex type or not.
		 */
		public final boolean isComplexType() {
			long type = this.getType();

			return (type > 5) && (type != 8);
		}

		/**
		 * Determines whether the type is a struct type, meaning the field data
		 * is located in the Struct Array of the GFF. This check is based on
		 * table 3.4b in the GFF documentation.
		 * 
		 * @return Whether this field is a complex type or not.
		 */
		public final boolean isStructType() {
			long type = this.getType();

			return (type == 14);
		}

		/**
		 * Determines whether the type is a list type, meaning the field data
		 * points to a location located in the List Indices Array of the GFF,
		 * which has further data that points into the Structs Array. This check
		 * is based on table 3.4b in the GFF documentation.
		 * 
		 * @return Whether this field is a complex type or not.
		 */
		public final boolean isListType() {
			long type = this.getType();

			return (type == 15);
		}
	}

	public void setField(GffField field, String newData) {
		if (field == null)
			throw new IllegalStateException("Invalid GffField");

		if (field.isComplexType() && field.isListType() && field.isStructType()
				&& field.getDataOrDataOffset() > 0)
			System.out.println();

		this.changedFieldMap.put(field, newData);
	}

	public void setField(String fieldLabel, String newData) {
		GffField field = this.findFieldForLabel(fieldLabel);
		this.setField(field, newData);
	}

	/**
	 * Calculates and returns the number of bytes required to store this GFF.
	 * 
	 * @return The number of bytes required to store this GFF.
	 */
	public int getByteLength() {
		System.err.println("Not calculating GFF size yet!");
		// TODO Auto-generated method stub
		return 0;
	}
}

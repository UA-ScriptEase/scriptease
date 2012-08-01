package io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.controller.apimanagers.GameTypeManager;
import scriptease.translator.TranslatorManager;
import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.model.Slot;
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
	private static final String GFF_VERSION = "V3.2";
	private static final String TYPE_SOUND_BP = "UTS";
	private static final String TYPE_WAYPOINT_BP = "UTW";
	private static final String TYPE_TRIGGER_BP = "UTT";
	private static final String TYPE_CREATURE_BP = "UTC";
	private static final String TYPE_ENCOUNTER_BP = "UTE";
	private static final String TYPE_MERCHANT_BP = "UTM";
	private static final String TYPE_PLACEABLE_BP = "UTP";
	private static final String TYPE_ITEM_BP = "UTI";
	private static final String TYPE_DOOR_BP = "UTD";
	public static final String TYPE_DIALOGUE_BP = "DLG";
	private static final String TYPE_JOURNAL_BP = "JRL";
	private static final String TYPE_MODULE_BP = "IFO";
	private static final String TYPE_GAME_INSTANCE_FILE = "GIT";

	// header data
	private final String fileType;
	// end header data

	// file data
	private final List<GffStruct> structArray;
	private final List<GffField> fieldArray;
	private final List<String> labelArray;
	private List<Long> fieldIndicesArray;
	private List<List<Long>> listIndicesArray;
	// end file data

	// added by us for convenience
	private final String resRef;

	/**
	 * location of this GFF from the start of the parent ERF file. This is reset
	 * upon writing.
	 */
	private long filePosition;

	/**
	 * File size is the size of this gff in bytes
	 */
	private long fileSize = -1;

	/**
	 * Stored variable to keep the same instance over time.
	 */
	private GameConstant representation;

	/**
	 * Length of the Labels in the GFF, from GFF documentation section 3.5
	 */
	private static final int LABEL_BYTE_LENGTH = 16;

	protected GenericFileFormat(String resRef, ScriptEaseFileAccess reader,
			long filePosition) throws IOException {
		final String version;
		final long structOffset;
		final long structCount;
		final long fieldOffset;
		final long fieldCount;
		final long labelOffset;
		final long labelCount;
		final long fieldDataOffset;
		// final long fieldDataCount; // nothing actually uses field data count
		final long fieldIndicesOffset;
		final long fieldIndicesCount;
		final long listIndicesOffset;
		final long listIndicesCount;

		this.resRef = resRef;
		this.filePosition = filePosition;

		// read GFF Header Data, as from GFF doc 3.2
		reader.seek(this.filePosition);

		fileType = reader.readString(4);
		version = reader.readString(4);
		structOffset = reader.readUnsignedInt(true);
		structCount = reader.readUnsignedInt(true);
		fieldOffset = reader.readUnsignedInt(true);
		fieldCount = reader.readUnsignedInt(true);
		labelOffset = reader.readUnsignedInt(true);
		labelCount = reader.readUnsignedInt(true);
		fieldDataOffset = reader.readUnsignedInt(true);
		/* fieldDataCount = */reader.readUnsignedInt(true);
		fieldIndicesOffset = reader.readUnsignedInt(true);
		fieldIndicesCount = reader.readUnsignedInt(true);
		listIndicesOffset = reader.readUnsignedInt(true);
		listIndicesCount = reader.readUnsignedInt(true);

		this.structArray = new ArrayList<GffStruct>((int) structCount);
		this.fieldArray = new ArrayList<GffField>((int) fieldCount);
		this.labelArray = new ArrayList<String>((int) labelCount);

		this.fieldIndicesArray = new ArrayList<Long>((int) fieldIndicesCount);
		this.listIndicesArray = new ArrayList<List<Long>>(
				(int) listIndicesCount);

		if (!version.equals(GFF_VERSION))
			throw new IOException(
					"NWN GFF: Cannot read a GFF whose version is not V3.2.");

		this.readStructs(reader, structOffset, structCount, fieldIndicesOffset);
		this.readFields(reader, fieldOffset, fieldCount, fieldDataOffset);
		this.readLabels(reader, labelOffset, labelCount);
		this.readFieldIndices(reader, fieldIndicesOffset, fieldIndicesCount);
		this.readListIndices(reader, listIndicesOffset, listIndicesCount);
	}

	private String getFileType() {
		return this.fileType;
	}

	private String getResRef() {
		return this.resRef;
	}

	/**
	 * Gets the label for the git list that this GFF's instances would live in.
	 * 
	 * @return the appropriate label for the list to store this giff's
	 *         instances.
	 */
	private String getGITListLabel() {
		String gitLabel;
		String fileType = this.fileType.trim();

		if (fileType.equals(TYPE_SOUND_BP)) {
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
	 * Gets the top level struct from the struct array, returns null if nothing
	 * is found
	 * 
	 * @return
	 */
	protected GffStruct getTopLevelStruct() {
		for (GffStruct struct : this.structArray)
			if (struct.isTopLevelStruct())
				return struct;
		return null;
	}

	/**
	 * Gets the value of the name field of the GFF. The actual field(s) that are
	 * relevant can change from type to type.
	 * 
	 * @param reader
	 *            the Stream to read from.
	 * @return
	 * @throws IOException
	 */
	private String getName() {
		final String type = this.fileType.trim();
		String name;

		// creature blueprint
		if (type.equalsIgnoreCase(GenericFileFormat.TYPE_CREATURE_BP)) {
			final String lastName;

			name = this.findFieldForLabel("FirstName").getStringData();
			lastName = this.findFieldForLabel("LastName").getStringData();

			name += lastName == null ? "" : " " + lastName;
		}
		// door, placeable, item, merchant/store blueprints
		else if (type.equalsIgnoreCase(GenericFileFormat.TYPE_DOOR_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_PLACEABLE_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_MERCHANT_BP)) {
			name = this.findFieldForLabel("LocName").getStringData();
		}
		// item, encounter, trigger, waypoint blueprints
		else if (type.equalsIgnoreCase(GenericFileFormat.TYPE_ITEM_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_ENCOUNTER_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_TRIGGER_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_WAYPOINT_BP)) {
			name = this.findFieldForLabel("LocalizedName").getStringData();
		}
		// module blueprints
		else if (type.equalsIgnoreCase(GenericFileFormat.TYPE_MODULE_BP)) {
			name = this.findFieldForLabel("Mod_Name").getStringData();
		}
		// TODO The journal blueprint names have been disabled because they
		// actually have internal structs we need to pull data from.

		// // journal blueprints
		// else if (type.equalsIgnoreCase(GenericFileFormat.TYPE_JOURNAL_BP)) {
		// name = this.readField(reader, "Name");
		// }

		// sound, conversation and other blueprints
		// TODO These should have their specific names.
		else {
			name = this.resRef;
		}

		return name;
	}

	/**
	 * Gets the tag from this GFF, if it has one. Otherwise, it returns an empty
	 * string.
	 * 
	 * @return The tag of this GFF, or an empty string.
	 */
	private String readTag() {
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
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_WAYPOINT_BP)
		// || type.equalsIgnoreCase(GenericFileFormat.TYPE_JOURNAL_BP)
		) {
			tag = this.findFieldForLabel("Tag").getStringData();
		} else if (type.equalsIgnoreCase(GenericFileFormat.TYPE_MODULE_BP)) {
			// BioWare, why you no consistent?
			tag = this.findFieldForLabel("Mod_Tag").getStringData();
		} else
			tag = "";

		return tag;
	}

	/**
	 * Remove generated references when we read it in, so that when we write it
	 * out, the naming and references are correct.
	 */
	private void removeGeneratedReferences() {
		final GameTypeManager typeManager = TranslatorManager.getInstance()
				.getTranslator(ErfFile.NEVERWINTER_NIGHTS).getGameTypeManager();
		final Collection<String> scriptSlots;
		String reference;

		// remove all generated script references

		scriptSlots = typeManager.getSlots(getScriptEaseType());
		for (String slotName : scriptSlots) {
			reference = this.findFieldForLabel(slotName).getStringData();

			// don't keep references to ScriptEase-generated files
			if (ErfFile.isScriptEaseGenerated(reference)) {
				// TODO: get the default script for the patterns file
				final String originalScript = "";

				this.setField(slotName, originalScript);
			}
		}
	}

	/**
	 * Extracts a ScriptEase representation of this GFF.
	 * 
	 * @return The ScriptEase version of this GFF object that.
	 */
	public GameConstant getObjectRepresentation() {
		if (!this.isValuable()) {
			return null;
		}

		// First time through? Build it.
		if (this.representation == null) {
			final String name;

			// read the object name
			name = this.getName();

			// conversations
			if (this.fileType.trim().equalsIgnoreCase(
					GenericFileFormat.TYPE_DIALOGUE_BP)) {
				// get the top level struct
				final GffStruct topLevel = this.getTopLevelStruct();

				this.representation = new NWNConversation(name + "."
						+ TYPE_DIALOGUE_BP, topLevel);
			}
			// other types
			else {
				final String tag;
				// get the object tag
				tag = this.readTag();

				// get the object type
				String type = this.getScriptEaseType();

				ArrayList<String> objectTypes = new ArrayList<String>(1);
				objectTypes.add(type);

				this.representation = new NWNObject(this.resRef + "."
						+ this.getFileType(), objectTypes, name, tag);
			}

			// clean up
			this.removeGeneratedReferences();
		}

		return this.representation;
	}

	/**
	 * Returns an equivalent ScriptEase GameType for the NWNObject Type. It will
	 * default to the first type if the NWN type is not found.
	 * 
	 * @return a GameType representing the NWN type.
	 */
	protected String getScriptEaseType() {
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
					.println("NWN GFF: Could not convert NWN type \""
							+ type
							+ "\" to ScriptEase type. Defaulting type to first available");
			type = GameTypeManager.DEFAULT_VOID_TYPE;
		}
		return type;
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
				"Tried to read a field from a GFF type (" + this.getFileType()
						+ ") that does not include the field " + label);
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
		importantTypes.add(GenericFileFormat.TYPE_DIALOGUE_BP);
		importantTypes.add(GenericFileFormat.TYPE_MODULE_BP);
		importantTypes.add(GenericFileFormat.TYPE_JOURNAL_BP);
		importantTypes.add(GenericFileFormat.TYPE_CREATURE_BP);
		importantTypes.add(GenericFileFormat.TYPE_DOOR_BP);
		importantTypes.add(GenericFileFormat.TYPE_ENCOUNTER_BP);
		importantTypes.add(GenericFileFormat.TYPE_ITEM_BP);
		importantTypes.add(GenericFileFormat.TYPE_MERCHANT_BP);
		importantTypes.add(GenericFileFormat.TYPE_PLACEABLE_BP);
		importantTypes.add(GenericFileFormat.TYPE_SOUND_BP);
		importantTypes.add(GenericFileFormat.TYPE_TRIGGER_BP);
		importantTypes.add(GenericFileFormat.TYPE_WAYPOINT_BP);
		importantTypes.add(GenericFileFormat.TYPE_GAME_INSTANCE_FILE);

		return importantTypes.contains(typeString);
	}

	private final void readFields(ScriptEaseFileAccess reader,
			long fieldOffset, long fieldCount, long fieldDataOffset)
			throws IOException {
		reader.seek(this.filePosition + fieldOffset);

		for (int i = 0; i < (int) fieldCount; i++) {
			this.fieldArray.add(new GffField(reader, i, fieldDataOffset));
		}
	}

	private final void readStructs(ScriptEaseFileAccess reader,
			long structOffset, long structCount, long fieldIndicesOffset)
			throws IOException {
		reader.seek(this.filePosition + structOffset);

		for (long i = 0; i < structCount; i++) {
			this.structArray.add(new GffStruct(reader, fieldIndicesOffset));
		}
	}

	private final void readLabels(ScriptEaseFileAccess reader,
			long labelOffset, long labelCount) throws IOException {
		reader.seek(this.filePosition + labelOffset);

		for (long i = 0; i < labelCount; i++) {
			this.labelArray.add(reader
					.readString(GenericFileFormat.LABEL_BYTE_LENGTH));
		}
	}

	private final void readFieldIndices(ScriptEaseFileAccess reader,
			long fieldIndicesOffset, long fieldIndicesCount) throws IOException {
		reader.seek(this.filePosition + fieldIndicesOffset);

		for (long i = 0; i < fieldIndicesCount; i++) {
			this.fieldIndicesArray.add(reader.readUnsignedInt(true));
		}
	}

	private final void readListIndices(ScriptEaseFileAccess reader,
			long listIndicesOffset, long listIndicesCount) throws IOException {
		reader.seek(this.filePosition + listIndicesOffset);

		// for each list, load the elements of that list
		for (long i = 0; i < listIndicesCount; i++) {
			long numElements = reader.readUnsignedInt(true);
			List<Long> indexList = new ArrayList<Long>((int) numElements);

			for (long j = 0; j < numElements; j++) {
				indexList.add(reader.readUnsignedInt(true));
			}

			// ... and add that list like a boss.
			this.listIndicesArray.add(indexList);
		}
	}

	@Override
	public String toString() {
		String stringRep = "";

		stringRep += "GFF [" + this.resRef + "Type:" + this.fileType + "]\n";

		for (GffField field : this.fieldArray) {
			stringRep += field.toString() + "\n";
		}

		return stringRep;
	}

	/**
	 * Writes this GFF to disk as per the BioWare documentation.
	 * 
	 * @param writer
	 *            The file to write to.
	 * @throws IOException
	 */
	protected void write(ScriptEaseFileAccess writer, long filePosition)
			throws IOException {
		long structsOffset = 4 * 14; // 14 header entries, 4 bytes each
		long fieldsOffset;
		long labelsOffset;
		long fieldDataOffset;
		long fieldIndicesArrayOffset;
		long listIndicesArrayOffset;

		// update the filePosition for future writes
		this.filePosition = filePosition;

		// Write file's meat data first
		fieldsOffset = this.writeStructs(writer, structsOffset);
		labelsOffset = this.writeFields(writer, fieldsOffset);
		fieldDataOffset = this.writeLabels(writer, labelsOffset);
		fieldIndicesArrayOffset = this.writeFieldDataBlock(writer,
				fieldDataOffset);
		listIndicesArrayOffset = this.writeFieldIndices(writer,
				fieldIndicesArrayOffset);
		this.fileSize = this.writeListIndices(writer, listIndicesArrayOffset);

		// write the now fully-known header data
		this.writeHeader(writer, structsOffset, fieldsOffset, labelsOffset,
				fieldDataOffset, fieldIndicesArrayOffset,
				listIndicesArrayOffset);
	}

	private long writeStructs(ScriptEaseFileAccess writer, long offset)
			throws IOException {
		long origin = writer.getFilePointer();
		writer.seek(this.filePosition + offset);

		for (GffStruct struct : this.structArray) {
			struct.write(writer);
		}

		return writer.getFilePointer() - origin;
	}

	private long writeFields(ScriptEaseFileAccess writer, long offset)
			throws IOException {
		long origin = writer.getFilePointer();
		writer.seek(this.filePosition + offset);

		for (GffField field : this.fieldArray) {
			field.write(writer);
		}

		return writer.getFilePointer() - origin;
	}

	private long writeLabels(ScriptEaseFileAccess writer, long offset)
			throws IOException {
		long origin = writer.getFilePointer();
		writer.seek(this.filePosition + offset);

		for (String label : this.labelArray) {
			writer.writeString(label, GenericFileFormat.LABEL_BYTE_LENGTH);
		}

		return writer.getFilePointer() - origin;
	}

	private long writeFieldDataBlock(ScriptEaseFileAccess writer, long offset)
			throws IOException {
		long origin = writer.getFilePointer();
		writer.seek(this.filePosition + offset);

		// complex types that are not lists or structs get written to the field
		// data block.
		for (GffField field : this.fieldArray) {
			if (field.isComplexType() && !field.isListType()
					&& !field.isStructType()) {
				field.writeFieldData(writer, offset);
			}
		}

		return writer.getFilePointer() - origin;
	}

	private long writeFieldIndices(ScriptEaseFileAccess writer, long offset)
			throws IOException {
		long origin = writer.getFilePointer();
		writer.seek(this.filePosition + offset);

		for (Long index : this.fieldIndicesArray) {
			writer.writeUnsignedInt(index, true);
		}

		return writer.getFilePointer() - origin;
	}

	private long writeListIndices(ScriptEaseFileAccess writer, long offset)
			throws IOException {
		long origin = writer.getFilePointer();
		writer.seek(this.filePosition + offset);

		// each list is size followed by the list of indexes, as per
		// GFF docs 3.8
		for (List<Long> indexList : this.listIndicesArray) {
			writer.writeUnsignedInt(indexList.size(), true);

			for (Long index : indexList) {
				writer.writeUnsignedInt(index, true);
			}
		}

		return writer.getFilePointer() - origin;
	}

	// /**
	// * DLG file specific writing code. Called in
	// * {@link #write(ScriptEaseFileAccess, long)}.
	// *
	// * @param writer
	// * @return List of all entries that were written to the DLG file.
	// * @throws IOException
	// */
	// private List<Entry<GffField, String>> writeDLGFile(
	// ScriptEaseFileAccess writer) throws IOException {
	// List<Entry<GffField, String>> activeList = new ArrayList<Entry<GffField,
	// String>>();
	// List<Long> dataOffsetList = new ArrayList<Long>();
	// for (Entry<GffField, String> entry : this.changedFieldMap.entrySet()) {
	// GffField key = entry.getKey();
	// if (key.getGFF().getLabelArray().get((int) key.getLabelIndex())
	// .equals("Active")) {
	// activeList.add(entry);
	// dataOffsetList.add(Long.valueOf(key.getDataOrDataOffset()));
	// }
	// }
	//
	// Comparator<Entry<GffField, String>> entryComparator = new
	// Comparator<Entry<GffField, String>>() {
	//
	// @Override
	// public int compare(Entry<GffField, String> entry1,
	// Entry<GffField, String> entry2) {
	//
	// String entryString1 = entry1.getValue();
	//
	// String entryString2 = entry2.getValue();
	//
	// int resRefComparison = entryString1.compareTo(entryString2);
	// // BioWare reverses the order of these, so we need to, too.
	// return -resRefComparison;
	//
	// }
	// };
	// Collections.sort(activeList, entryComparator);
	//
	// // Required Variables for DLG File Specific Code
	// if (dataOffsetList.size() > 0) {
	// long firstOffset = Collections.min(dataOffsetList).longValue();
	// // Write out the fields in the "Active" list
	// for (Entry<GffField, String> entry : activeList) {
	// final GffField changedField = entry.getKey();
	// // DLG File Specific Code:
	// // write out the field data again because it may have updated
	// writer.seek(this.filePosition + this.fieldOffset
	// + changedField.getFieldOffset());
	//
	// final String newValue = entry.getValue();
	// System.out.println("Old Offset Value: "
	// + changedField.dataOrDataOffset);
	// changedField.setDataOrDataOffset(firstOffset);
	// changedField.write(writer);
	// firstOffset += newValue.getBytes().length + 1;
	//
	// System.out.println("New Offset Value: "
	// + changedField.dataOrDataOffset);
	//
	// // Only updates script slots
	// if (!changedField.isResRefType())
	// throw new RuntimeException(
	// "Can't write anything but slot refs. The module is probably corrupted now. Oops.");
	//
	// changedField.writeFieldData(writer, this.filePosition
	// + this.fieldDataOffset,
	// this.changedFieldMap.get(changedField));
	// }
	// }
	// return activeList;
	// }

	/**
	 * Writes this GFF's header information to disk.
	 * 
	 * @param listIndicesArrayOffset
	 * @param fieldIndicesArrayOffset
	 * @param fieldDataOffset
	 * @param labelsOffset
	 * @param fieldsOffset
	 * @param structsOffset
	 * 
	 * @throws IOException
	 */
	private void writeHeader(ScriptEaseFileAccess writer, long structOffset,
			long fieldsOffset, long labelsOffset, long fieldDataOffset,
			long fieldIndicesArrayOffset, long listIndicesArrayOffset)
			throws IOException {
		writer.seek(this.filePosition);

		writer.writeString(this.fileType, 4);
		writer.writeString(GFF_VERSION, 4);
		writer.writeUnsignedInt(structOffset, true);
		writer.writeUnsignedInt(this.structArray.size(), true);
		writer.writeUnsignedInt(fieldsOffset, true);
		writer.writeUnsignedInt(this.fieldArray.size(), true);
		writer.writeUnsignedInt(labelsOffset, true);
		writer.writeUnsignedInt(this.labelArray.size(), true);
		writer.writeUnsignedInt(fieldDataOffset, true);
		writer.writeUnsignedInt(fieldIndicesArrayOffset - fieldDataOffset, true);
		writer.writeUnsignedInt(fieldIndicesArrayOffset, true);
		writer.writeUnsignedInt(this.fieldIndicesArray.size(), true);
		writer.writeUnsignedInt(listIndicesArrayOffset, true);
		writer.writeUnsignedInt(this.listIndicesArray.size(), true);
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
	protected class GffStruct {
		private final long typeNumber;
		private final long dataOrDataOffset;
		private final long fieldCount;
		private final List<GffField> fields;

		/**
		 * Builds a new GFF struct that is read from the given reader, and whose
		 * fields are to be located at <code>fieldIndicesOffset</code>
		 * 
		 * @param reader
		 *            The reader to read from.
		 * @param fieldIndicesOffset
		 *            The offset in the GFF file to the
		 * @throws IOException
		 *             if the cat came back the very next day.
		 */
		private GffStruct(ScriptEaseFileAccess reader, long fieldIndicesOffset)
				throws IOException {
			this.typeNumber = reader.readUnsignedInt(true);
			this.dataOrDataOffset = reader.readUnsignedInt(true);
			this.fieldCount = reader.readUnsignedInt(true);

			this.fields = this.readGffFields(reader, fieldIndicesOffset);
		}

		/**
		 * Writes this struct to disk at the location that the given writer is
		 * currently pointing to.
		 * 
		 * @param writer
		 * @throws IOException
		 */
		protected void write(ScriptEaseFileAccess writer) throws IOException {
			writer.writeUnsignedInt(this.typeNumber, true);
			writer.writeUnsignedInt(this.dataOrDataOffset, true);
			writer.writeUnsignedInt(this.fieldCount, true);
		}

		@Override
		public String toString() {
			return "GffStruct [" + typeNumber + ", " + dataOrDataOffset + ", "
					+ fieldCount + "]";
		}

		/**
		 * Reads the fields from the given GffStruct and returns them in a List.
		 * 
		 * @param reader
		 * @param fieldIndicesOffset
		 *            The offset to the Field Indices Array where struct fields
		 *            are stored.
		 * @return
		 * @throws IOException
		 */
		private List<GffField> readGffFields(ScriptEaseFileAccess reader,
				long fieldIndicesOffset) throws IOException {
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
				reader.seek(filePosition + fieldIndicesOffset
						+ dataOrDataOffset);

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
		 * Gets the fields from this struct.
		 * 
		 * @return this struct's fields.
		 */
		protected List<GffField> getGffFields() {
			return new ArrayList<GffField>(this.fields);
		}

		/**
		 * Determines if this is the top level struct.
		 * 
		 * @return Whether this is the top level struct
		 */
		protected boolean isTopLevelStruct() {
			// documentation states the top level struct always has a type
			// number of 0xFFFFFFFF, which is -1 when considered signed
			return this.typeNumber == 0xFFFFFFFF;
		}

		protected void removeScriptEaseReferences() {
			final List<GffField> fields = this.getGffFields();
			final Collection<Slot> slots = TranslatorManager.getInstance()
					.getTranslator(ErfFile.NEVERWINTER_NIGHTS).getSlotManager()
					.getEventSlots();
			final Collection<String> slotNames = new ArrayList<String>();
			for (Slot slot : slots) {
				slotNames.add(slot.getKeyword());
			}
			String fieldValue;

			// go through each script field and fix the reference
			for (GffField field : fields) {
				// only bother looking at script slot fields.
				if (!slotNames.contains(field.getLabel())) {
					continue;
				}

				if (field.isStructType()) {
					field.getGffStruct().removeScriptEaseReferences();
				} else if (field.isListType()) {
					for (GffStruct struct : field.getList()) {
						struct.removeScriptEaseReferences();
					}
				} else {
					fieldValue = field.getStringData();

					if (ErfFile.isScriptEaseGenerated(fieldValue)) {
						field.setData("");
					}
				}
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
	protected class GffField {
		private static final int MAX_RESREF_LENGTH = 16;
		private static final int TYPE_NUM_CEXOLOCSTRING = 12;
		private static final int TYPE_NUM_RESREF = 11;
		private static final int TYPE_NUM_CEXOSTRING = 10;
		private static final int TYPE_NUM_DOUBLE = 9;
		private static final int TYPE_NUM_INT64 = 7;
		private static final int TYPE_NUM_DWORD64 = 6;
		private static final int TYPE_NUM_STRUCT = 14;
		private static final int TYPE_NUM_LIST = 15;
		private final long typeNumber;
		private final long labelIndex;
		private long dataOrDataOffset; // this can increase if its an offset

		// this isn't part of the original struct. Added so that field knows
		// where it lives within the Field Array. - remiller
		private long fieldIndex;

		// Field data variables that store the field's data for "complex types"
		// (see isComplex() for more)
		private String fieldDataString;
		private double fieldDataDouble;
		private long fieldDataDWord64;
		private long fieldDataInt64;

		// end field data variables

		private GffField(ScriptEaseFileAccess reader, long index,
				long fieldDataOffset) throws IOException {
			this.typeNumber = reader.readUnsignedInt(true);
			this.labelIndex = reader.readUnsignedInt(true);
			this.dataOrDataOffset = reader.readUnsignedInt(true);
			this.fieldIndex = index;

			this.readData(reader, fieldDataOffset);
		}

		@Override
		public String toString() {
			return "GffField [" + this.getName() + ", type: " + this.typeNumber
					+ "]";
		}

		/**
		 * Writes out the GffField data to the writer.
		 * 
		 * @param writer
		 * @throws IOException
		 */
		protected void write(ScriptEaseFileAccess writer) throws IOException {
			writer.writeUnsignedInt(this.typeNumber, true);
			writer.writeUnsignedInt(this.labelIndex, true);
			writer.writeUnsignedInt(this.dataOrDataOffset, true);
		}

		/**
		 * @return Unsigned int in a long representing the type.
		 */
		private long getType() {
			return typeNumber;
		}

		/**
		 * @return the labelIndex
		 */
		protected long getLabelIndex() {
			return labelIndex;
		}

		/**
		 * @return the dataOrDataOffset
		 */
		protected long getDataOrDataOffset() {
			return dataOrDataOffset;
		}

		/**
		 * Sets the DataOrDataOffset to the passed long.
		 * 
		 * @param offset
		 */
		protected void setDataOrDataOffset(long offset) {
			this.dataOrDataOffset = offset;
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

		/**
		 * Finds the GFFStruct that this field points to.
		 * 
		 * @return the struct pointed to by this field.
		 */
		protected GffStruct getGffStruct() {
			if (!this.isStructType()) {
				throw new IllegalStateException(
						"GffField does not contain a GffStruct.");
			}

			/*
			 * Normally, a Field's DataOrDataOffset value would be a byte offset
			 * into the Field Data Block, but for a Struct, it is an index into
			 * the Struct Array.
			 */
			return structArray.get((int) this.dataOrDataOffset);
		}

		/**
		 * Finds the list of GFFStructs that this field points to.<br>
		 * <br>
		 * The list is a list in the List Indices Array, and those lists are
		 * lists of indexes into the Struct Array. The struct array is queried
		 * by this method to resolve the list and it's those structs that get
		 * returned (in the same order as the indexes).
		 * 
		 * @return the resolved list of GFFStructs that this field is a list of.
		 * @throws IllegalStateException
		 *             if the stored dataOrDataOffset doesn't point to a list in
		 *             the List Indices Array.
		 */
		protected List<GffStruct> getList() {
			if (!this.isListType()) {
				throw new IllegalStateException(
						"GffField does not contain a GffList.");
			}

			final List<GffStruct> structs = new ArrayList<GffStruct>();

			// this is the offset in bytes into the list array
			int countedOffset = 0;

			for (List<Long> list : listIndicesArray) {
				countedOffset += calculateListByteSize(list);

				if (countedOffset == this.dataOrDataOffset) {
					// Found it. Resolve the indices.
					for (long index : list) {
						final GffStruct struct = structArray.get((int) index);
						structs.add(struct);
					}

					return structs;
				} else if (countedOffset > this.dataOrDataOffset) {
					break;
				}
			}

			throw new IllegalStateException(
					"ERF list "
							+ this.getName()
							+ " could not be located. This is either because the stored offset is wrong, or the stored lists are wrong.");
		}

		private String getName() {
			return GenericFileFormat.this.labelArray.get((int) this.labelIndex);
		}

		/**
		 * Reads and stores the data of this field.
		 * 
		 * @param reader
		 * @return
		 * @throws IOException
		 *             if everything goes to hell.
		 */
		protected void readData(ScriptEaseFileAccess reader,
				long fieldDataOffset) throws IOException {
			if (!this.isComplexType()) {
				// if simple type, we already have the data in dataOrDataOffset
				// if list or struct, data is loaded into the structArray or
				// listIndicesArray
				return;
			}

			// get to the data
			reader.seek(filePosition + fieldDataOffset + this.dataOrDataOffset);

			long length;

			switch ((int) this.getType()) {
			case GffField.TYPE_NUM_DWORD64:
				this.fieldDataDWord64 = reader.readUnsignedInt(true);
				break;
			case GffField.TYPE_NUM_INT64:
				this.fieldDataInt64 = reader.readLong(true);
				break;
			case GffField.TYPE_NUM_DOUBLE:
				// this.fieldDataDouble = reader.readDouble(true);
				// break;

				throw new IllegalStateException("I can't read a Double yet!");
			case GffField.TYPE_NUM_CEXOSTRING:
				length = reader.readUnsignedInt(true);

				this.fieldDataString = reader.readString((int) length);
				break;
			case GffField.TYPE_NUM_RESREF:
				length = reader.readByte();

				this.fieldDataString = reader.readString((int) length);

				break;
			case GffField.TYPE_NUM_CEXOLOCSTRING:
				length = reader.readUnsignedInt(true);
				long stringRef = reader.readUnsignedInt(true);
				long stringCount = reader.readUnsignedInt(true);

				// internationalization
				if (stringCount == 0) {
					TlkLookup stringTable = TlkLookup.getInstance();

					this.fieldDataString = stringTable.lookup(stringRef);
				}
				// Always take the english version of the string, just
				// because. -mfchurch
				else {
					for (int i = 0; i < stringCount; i++) {
						/* int stringID = */reader.readInt(true);
						int stringLen = reader.readInt(true);
						this.fieldDataString = reader.readString(stringLen);
					}
				}
				break;
			default:
				// This should never happen, unless we missed a type or we
				// somehow hit a snag while reading unsigned ints and
				// casting them?
				throw new IllegalStateException(
						"I don't know what type this is: " + this.getType());
			}
		}

		/**
		 * Sets this field's data to the value contained in the given string (to
		 * be converted to whatever type is expected).
		 * 
		 * @param value
		 *            The string containing the value to be used.
		 */
		protected void setData(String value) {
			if (!this.isComplexType()) { // just data
				this.dataOrDataOffset = Long.valueOf(value);
			} else if (this.isListType() || this.isStructType()) {
				throw new IllegalStateException("GffField " + this.getName()
						+ " cannot be set to value" + value
						+ " because is a Struct (" + this.isStructType()
						+ ") or List (" + this.isListType() + ") type.");
			} else {
				switch ((int) this.getType()) {
				case GffField.TYPE_NUM_DWORD64:
					this.fieldDataDWord64 = Long.valueOf(value);
				case GffField.TYPE_NUM_INT64:
					this.fieldDataInt64 = Long.valueOf(value);
				case GffField.TYPE_NUM_DOUBLE:
					this.fieldDataDouble = Double.valueOf(value);
				case GffField.TYPE_NUM_CEXOSTRING:
				case GffField.TYPE_NUM_RESREF:
				case GffField.TYPE_NUM_CEXOLOCSTRING:
					this.fieldDataString = value;
				default:
					// This should never happen, unless we missed a type or we
					// somehow hit a snag while reading unsigned ints and
					// casting them?
					throw new IllegalStateException(
							"I don't know what type this is: " + this.getType());
				}
			}
		}

		/**
		 * Reads this field's data and converts it to a string.
		 * 
		 * @param reader
		 *            The reader to read from.
		 * @return The field data as a string.
		 * @throws IllegalStateException
		 *             if the field is a Struct or a List.
		 */
		protected String getStringData() {
			if (!this.isComplexType()) { // just data
				return Long.toString(this.dataOrDataOffset);
			} else if (this.isListType() || this.isStructType()) {
				throw new IllegalStateException(
						"GffField "
								+ this.getName()
								+ " does not contain a String. It contains a struct or a list.");
			} else {
				switch ((int) this.getType()) {
				case GffField.TYPE_NUM_DWORD64:
					return Long.toString(this.fieldDataDWord64);
				case GffField.TYPE_NUM_INT64:
					return Long.toString(this.fieldDataInt64);
				case GffField.TYPE_NUM_DOUBLE:
					return Double.toString(this.fieldDataDouble);
				case GffField.TYPE_NUM_CEXOSTRING:
				case GffField.TYPE_NUM_RESREF:
				case GffField.TYPE_NUM_CEXOLOCSTRING:
					return this.fieldDataString;
				default:
					// This should never happen, unless we missed a type or we
					// somehow hit a snag while reading unsigned ints and
					// casting them?
					throw new IllegalStateException(
							"I don't know what type this is: " + this.getType());
				}
			}
		}

		/**
		 * Writes fields whose data ends up in the Field Data Block. If this is
		 * called on a field that does not have data that belongs in the Field
		 * Data Block, IOException is thrown and you should be ashamed of
		 * yourself. Bad programmer! Bad!
		 * 
		 * @param writer
		 *            the writer to write to.
		 * @param fieldDataOffset
		 *            the offset to the Field Data Block.
		 * @throws IOException
		 *             if monkeys start wearing rubber pants.
		 */
		protected void writeFieldData(ScriptEaseFileAccess writer,
				long fieldDataOffset) throws IOException {
			/*
			 * Non-complex types are inherently handled by writing the field
			 * itself, since that's where they are stored, so we just deal with
			 * complex types here. Similar deal with structs and lists - they're
			 * stored separately.
			 */
			if (!this.isComplexType() || this.isListType()
					|| this.isStructType()) {
				throw new IllegalStateException(
						"Can't write simple types, lists, or structs in GFF files this way.");
			}

			// get to the data location
			writer.seek(fieldDataOffset + this.getDataOrDataOffset());

			switch ((int) this.getType()) {
			case GffField.TYPE_NUM_DWORD64:
				writer.writeUnsignedLong(this.fieldDataDWord64, true);
				break;
			case GffField.TYPE_NUM_INT64:
				writer.writeLong(this.fieldDataInt64, true);
				break;
			case GffField.TYPE_NUM_DOUBLE:
				// writer.writeDouble(this.fieldDataDouble, true);
				// break;

				throw new IllegalStateException("I can't write a Double yet!");
			case GffField.TYPE_NUM_CEXOSTRING:
				writer.writeUnsignedInt(this.fieldDataString.length(), true);

				writer.writeString(this.fieldDataString,
						this.fieldDataString.length());

				break;
			case GffField.TYPE_NUM_RESREF:
				String newData = this.fieldDataString.toLowerCase();

				if (newData.length() > GffField.MAX_RESREF_LENGTH)
					newData = newData.substring(0, GffField.MAX_RESREF_LENGTH);

				writer.writeByte(newData.length());

				writer.writeString(newData, newData.length());

				break;
			case GffField.TYPE_NUM_CEXOLOCSTRING:
				// length = reader.readUnsignedInt(true);
				// long stringRef = reader.writeUnsignedInt(true);
				//
				// TlkLookup stringTable = TlkLookup.getInstance();
				//
				// stringTable.lookup(stringRef);
				//
				// break;

				throw new IllegalStateException(
						"I can't write a CExoLocString yet!");

			default:
				// This should never happen, unless we missed a type or we
				// somehow hit a snag while reading unsigned ints and
				// casting them?
				throw new IllegalStateException(
						"While writing, I don't know what type this is: "
								+ this.getType());
			}
		}

		/**
		 * Determines whether the type is a complex type (meaning the field data
		 * is located in the field data block of the GFF, or elsewhere for
		 * structs/lists) or a simple type. This check is based on table 3.4b in
		 * the GFF documentation.
		 * 
		 * @return Whether this field is a complex type or not.
		 */
		protected boolean isComplexType() {
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
		private final boolean isStructType() {
			long type = this.getType();

			return (type == GffField.TYPE_NUM_STRUCT);
		}

		/**
		 * Determines whether the type is a list type, meaning the field data
		 * points to a location located in the List Indices Array of the GFF,
		 * which has further data that points into the Structs Array. This check
		 * is based on table 3.4b in the GFF documentation.
		 * 
		 * @return Whether this field is a complex type or not.
		 */
		private final boolean isListType() {
			long type = this.getType();

			return (type == GffField.TYPE_NUM_LIST);
		}

		/**
		 * Gets this field's label as stored in the label arrays indexed by
		 * {@link #getLabelIndex()}.
		 * 
		 * @return the field's label.
		 */
		protected String getLabel() {
			return GenericFileFormat.this.labelArray.get((int) this
					.getLabelIndex());
		}
	}

	protected void setField(GffField field, String newData) {
		if (field == null)
			throw new NullPointerException(
					"Null GffField given when setting field value.");

		field.setData(newData);
	}

	protected void setField(String fieldLabel, String newData) {
		GffField field = this.findFieldForLabel(fieldLabel);
		this.setField(field, newData);
	}

	/**
	 * Calculates and returns the number of bytes required to store this GFF.
	 * 
	 * @return The number of bytes required to store this GFF.
	 */
	protected long getByteLength() {
		return this.fileSize;
	}

	/**
	 * Calculates the size in bytes of an List Indices Array list.
	 * 
	 * @param list
	 * @return
	 */
	private static int calculateListByteSize(List<Long> list) {
		// size+1 because they store the size as a DWORD before the
		// elements, * 4 because DWORDs are 4 bytes each
		return (list.size() + 1) * 4;
	}

	protected void removeScriptEaseReferences() {
		// String fileType = this.getFileType().trim();

		// if (fileType
		// .equalsIgnoreCase(GenericFileFormat.TYPE_GAME_INSTANCE_FILE)) {

		GffStruct gitFileStruct = this.getTopLevelStruct();

		gitFileStruct.removeScriptEaseReferences();

		// // Go through the lists of lists in a GIT File. (e.g. Creature
		// // List, etc)
		// for (GffField gitFileField : gitFileStruct.getGffFields()) {
		// // Ignore AreaProperties field.
		// if (gitFileField.getLabel().equals("AreaProperties")) {
		// continue;
		// }
		//
		// // Individual structs (e.g. Creatures)
		// for (GffStruct individualFieldStruct : gitFileField.getList()) {
		// individualFieldStruct.removeScriptEaseReferences();
		// }
		// }
		// }
		// else if (fileType
		// .equalsIgnoreCase(GenericFileFormat.TYPE_DIALOGUE_BP)) {
		//
		// GffStruct dlgFileStruct = this.getTopLevelStruct();
		//
		// List<GffField> dlgFileFields;
		//
		// // The list of lists in a Git File. e.g. Creature List
		// dlgFileFields = dlgFileStruct.getGffFields();
		//
		// for (GffField dlgFilefield : dlgFileFields) {
		// final String dlgFieldlabel = this.getLabelArray().get(
		// (int) dlgFilefield.getLabelIndex());
		//
		// // Handle fields:
		// if (dlgFieldlabel.equals("EndConverAbort")
		// || dlgFieldlabel.equals("EndConversation")) {
		// final String scriptName = dlgFilefield.getString();
		//
		// if (scriptName.startsWith(ErfFile.SCRIPT_FILE_PREFIX)) {
		// this.setField(dlgFilefield, "");
		// }
		//
		// // Handle lists:
		// } else if (dlgFieldlabel.equals("EntryList")
		// || dlgFieldlabel.equals("ReplyList")
		// || dlgFieldlabel.equals("StartingList")) {
		// List<GffStruct> readList = dlgFilefield.getList();
		// for (GffStruct aStruct : readList) {
		// final List<GffField> listFields = aStruct
		// .getGffFields();
		// for (GffField listField : listFields) {
		// String listFieldLabel = this.getLabelArray().get(
		// (int) listField.getLabelIndex());
		//
		// if (listFieldLabel.equals("Script")
		// || listFieldLabel.equals("Active")) {
		// final String scriptName = listField.getString();
		//
		// if (scriptName
		// .startsWith(ErfFile.SCRIPT_FILE_PREFIX))
		// this.setField(listField, "");
		// }
		// // This is for the sync structs inside
		// // of reply and entry dialogues.
		// if (listFieldLabel.equals("EntriesList")
		// || listFieldLabel.equals("RepliesList")) {
		// List<GffStruct> syncList = listField.getList();
		//
		// for (GffStruct syncStruct : syncList) {
		// final List<GffField> syncFields = syncStruct
		// .getGffFields();
		//
		// for (GffField syncField : syncFields) {
		// String syncFieldLabel = this
		// .getLabelArray()
		// .get((int) syncField
		// .getLabelIndex());
		// if (syncFieldLabel.equals("Active")) {
		// final String scriptName = syncField
		// .getString();
		// if (scriptName
		// .startsWith(ErfFile.SCRIPT_FILE_PREFIX))
		// this.setField(syncField, "");
		// }
		// }
		// }
		// }
		// }
		// }
		// }
		// }
		// }
	}

	/**
	 * Updates all instances that are from the given source GFF.<br>
	 * <br>
	 * This method assumes it is being called on an Area GIT file, so if that
	 * ever becomes untrue, this method will need to be altered to match.
	 * 
	 * @param sourceGFF
	 *            The GFF that is the template for the instances to update.
	 * @param slot
	 *            The slot id to update.
	 * @param scriptResRef
	 *            The new data to live in the given slot.
	 */
	protected void updateAllInstances(GenericFileFormat sourceGFF, String slot,
			String scriptResRef) {
		if (!this.isInstanceUpdatable()) {
			throw new IllegalStateException(
					"Cannot update instances in anything but a GIT file. This is a "
							+ fileType + " file.");
		}

		final GffStruct gitFileStruct = this.getTopLevelStruct();
		final String sourceResRef = sourceGFF.getResRef();

		// Go through the lists of lists in a GIT File. (e.g. Creature List,
		// etc). The rest of the comments will use the creature list as an
		// example.
		for (GffField gitFileField : gitFileStruct.getGffFields()) {
			// Find the appropriate list.
			if (gitFileField.getLabel().equals(sourceGFF.getGITListLabel())) {
				continue;
			}

			// List of all (e.g.) creature structs in creature list.
			List<GffStruct> instances = gitFileField.getList();

			// Parses the individual creatures from the list.
			for (GffStruct instance : instances) {

				List<GffField> instanceFields = instance.getGffFields();

				boolean shouldUpdate = false;
				GffField toUpdate = null;

				// Search the individual creature fields to see if this is an
				// instance to update and to find the field to edit.
				for (GffField instanceField : instanceFields) {
					// Checks if the field equals TemplateResRef. This means the
					// individualFieldStruct creature is the same as the
					// individualFieldStructField creature.

					// is this an instance from our source?
					if (instanceField.getLabel().equals("TemplateResRef")
							&& instanceField.getStringData().equals(
									sourceResRef)) {
						shouldUpdate = true;
					}
					// is this the slot or droid that we're looking for?
					else if (instanceField.getLabel().equals(slot)) {
						toUpdate = instanceField;
					}
				}

				if (shouldUpdate) {
					if (toUpdate != null) {
						toUpdate.setData(scriptResRef);
					} else {
						System.err.println("Could not find and update slot "
								+ slot + " on instances of " + sourceResRef
								+ " in the area " + this.getName());
					}
				}
			}
		}
	}

	/**
	 * Determines whether this GFF may have
	 * {@link #updateAllInstances(GenericFileFormat, String, String)} called
	 * upon it.
	 * 
	 * @return <code>true</code> if this GFF supports updating instances.
	 */
	protected boolean isInstanceUpdatable() {
		String fileType = this.getFileType().trim();
		return fileType
				.equalsIgnoreCase(GenericFileFormat.TYPE_GAME_INSTANCE_FILE);
	}
}

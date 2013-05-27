package io.genericfileformat;

import io.ErfFile;
import io.NWNConversation;
import io.NWNDialogueLine;
import io.NWNObject;
import io.TlkManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scriptease.gui.WindowFactory;
import scriptease.model.semodel.librarymodel.GameTypeManager;
import scriptease.translator.io.model.Resource;
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
 * @author kschenk
 * 
 */
public class GenericFileFormat {
	private static final String GFF_VERSION = "V3.2";

	// Note: the BP stands for Blueprint, not Boston Pizza
	protected static final String TYPE_SOUND_BP = "UTS";
	protected static final String TYPE_WAYPOINT_BP = "UTW";
	protected static final String TYPE_TRIGGER_BP = "UTT";
	protected static final String TYPE_CREATURE_BP = "UTC";
	protected static final String TYPE_ENCOUNTER_BP = "UTE";
	protected static final String TYPE_MERCHANT_BP = "UTM";
	protected static final String TYPE_PLACEABLE_BP = "UTP";
	protected static final String TYPE_ITEM_BP = "UTI";
	protected static final String TYPE_DOOR_BP = "UTD";
	protected static final String TYPE_DIALOGUE_BP = "DLG";
	public static final String TYPE_JOURNAL_BP = "JRL";
	protected static final String TYPE_MODULE_INFO = "IFO";
	protected static final String TYPE_AREA_GAME_INSTANCE_FILE = "GIT";
	protected static final String TYPE_AREA_FILE = "ARE";
	public static final String TYPE_MODULE = "module";

	/**
	 * Separator used when indexing occurs. The left hand side of the separator
	 * is the resref, and to the right is the index information. It is a
	 * character that cannot naturally occur within a resref.
	 */
	// this must never be set to an alphanumeric character or "_" or "."
	// This gets used in regex searches, so try to avoid regular expression
	// special characters, too. Anything else should be fine. - remiller
	public static final String RESREF_SEPARATOR = "#";

	// these are the specific fields for conversations that are used in dialogue
	// line indexing
	protected final static String DIALOGUE_PLAYER_REPLY_LIST = "ReplyList";
	protected final static String DIALOGUE_NPC_ENTRY_LIST = "EntryList";

	// header data
	private final String fileType;
	// end header data

	// file data
	protected final List<GffStruct> structArray;
	protected final List<GffField> fieldArray;
	protected final List<String> labelArray;
	protected List<Long> fieldIndicesArray;
	protected List<List<Long>> listIndicesArray;
	// end file data

	// added by us for convenience. This isn't actually stored within a GFF on
	// disk.
	private final String resRef;

	/**
	 * Length of the Labels in the GFF, from GFF documentation section 3.5
	 */
	private static final int LABEL_BYTE_LENGTH = 16;

	/**
	 * Internationalization table for GFFField data.
	 */
	private static final TlkManager stringTable = TlkManager.getInstance();

	/**
	 * Creates a new GFF with the passed in ResRef and File Type. Note that this
	 * constructor does not add anything to the actual fields. Thus, it can
	 * cause major issues if you just create a new GFF with this and add it to
	 * the module.
	 * 
	 * @param resRef
	 * @param fileType
	 *            A string. The types can be found as static fields starting
	 *            with TYPE_* in GenericFileFormat.
	 */
	protected GenericFileFormat(String resRef, String fileType) {
		this.resRef = resRef;
		this.fileType = fileType;

		this.structArray = new ArrayList<GffStruct>();
		this.fieldArray = new ArrayList<GffField>();
		this.labelArray = new ArrayList<String>();

		this.fieldIndicesArray = new ArrayList<Long>();
		this.listIndicesArray = new ArrayList<List<Long>>();
	}

	/**
	 * Reads in a GenericFileFormat from file.
	 * 
	 * @param resRef
	 * @param reader
	 * @param filePosition
	 * @throws IOException
	 */
	public GenericFileFormat(String resRef, ScriptEaseFileAccess reader,
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

		// read GFF Header Data, as from GFF doc 3.2
		reader.seek(filePosition);

		this.fileType = reader.readString(4);
		version = reader.readString(4);
		structOffset = reader.readUnsignedInt(true) + filePosition;
		structCount = reader.readUnsignedInt(true);
		fieldOffset = reader.readUnsignedInt(true) + filePosition;
		fieldCount = reader.readUnsignedInt(true);
		labelOffset = reader.readUnsignedInt(true) + filePosition;
		labelCount = reader.readUnsignedInt(true);
		fieldDataOffset = reader.readUnsignedInt(true) + filePosition;
		// 4 bytes of fieldDataCount, which we don't use and re-calculate in
		// writeHeader(). - remiller
		reader.skipBytes(4);
		fieldIndicesOffset = reader.readUnsignedInt(true) + filePosition;
		fieldIndicesCount = reader.readUnsignedInt(true);
		listIndicesOffset = reader.readUnsignedInt(true) + filePosition;
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

		// need to read labels first to make debugging easier
		this.readLabels(reader, labelOffset, labelCount);
		this.readStructs(reader, structOffset, structCount);
		this.readFields(reader, fieldOffset, fieldCount, fieldDataOffset);

		this.readFieldIndices(reader, fieldIndicesOffset, fieldIndicesCount);
		this.readListIndices(reader, listIndicesOffset, listIndicesCount);
	}

	public String getFileType() {
		return this.fileType;
	}

	public String getResRef() {
		return this.resRef;
	}

	/**
	 * Retrieves the Dialogue struct (as in conversations docs 2.2) that the
	 * given Dialogue Sync Struct (2.3) is pointing to.
	 * 
	 * @param syncStruct
	 *            The sync struct to resolve.
	 * @param isPlayerLine
	 *            whether the sync struct is for a player line or an NPC line.
	 * @return The Dialogue Struct
	 */
	public GffStruct resolveSyncStruct(GffStruct syncStruct,
			boolean isPlayerLine) {
		final String listLabel;
		final int index;
		final List<GffStruct> dialogueStructList;

		if (isPlayerLine)
			listLabel = GenericFileFormat.DIALOGUE_PLAYER_REPLY_LIST;
		else
			listLabel = GenericFileFormat.DIALOGUE_NPC_ENTRY_LIST;

		dialogueStructList = this.getList(listLabel);

		index = new Integer(syncStruct.getString("Index"));

		if (index >= 0 && index < dialogueStructList.size())
			return dialogueStructList.get(index);
		else
			return null;
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
	public GffStruct getTopLevelStruct() {
		for (GffStruct struct : this.structArray)
			if (struct.isTopLevelStruct())
				return struct;
		return null;
	}

	/**
	 * Gets the value of the name field of the GFF. The actual field(s) that are
	 * relevant can change from type to type.
	 * 
	 * @param index
	 * 
	 * @param reader
	 *            the Stream to read from.
	 * @return
	 * @throws IOException
	 */
	private String getName(String index) {
		final String type = this.fileType.trim();
		String name;

		// creature blueprint
		if (type.equalsIgnoreCase(GenericFileFormat.TYPE_CREATURE_BP)) {
			final String lastName;

			name = this.getFieldByLabel("FirstName").getStringData();
			lastName = this.getFieldByLabel("LastName").getStringData();

			name += lastName == null ? "" : " " + lastName;
		}
		// door, placeable, item, sound, merchant/store blueprints
		else if (type.equalsIgnoreCase(GenericFileFormat.TYPE_DOOR_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_PLACEABLE_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_MERCHANT_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_SOUND_BP)) {
			name = this.getFieldByLabel("LocName").getStringData();
		}
		// item, encounter, trigger, waypoint blueprints
		else if (type.equalsIgnoreCase(GenericFileFormat.TYPE_ITEM_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_ENCOUNTER_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_TRIGGER_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_WAYPOINT_BP)) {
			name = this.getFieldByLabel("LocalizedName").getStringData();
		}
		// module blueprints
		else if (type.equalsIgnoreCase(GenericFileFormat.TYPE_MODULE_INFO)) {
			name = this.getFieldByLabel("Mod_Name").getStringData();
		}
		// areas
		else if (type.equalsIgnoreCase(GenericFileFormat.TYPE_AREA_FILE)) {
			name = this.getFieldByLabel("Name").getStringData();
		}
		// journal blueprints
		else if (type.equalsIgnoreCase(GenericFileFormat.TYPE_JOURNAL_BP)) {
			final GffStruct category = getJournalCategory(index);

			name = category.getFieldByLabel("Name").getStringData();
		}
		// other blueprints don't have a display name, just a resref.
		else {
			name = this.resRef;
		}

		return name;
	}

	private GffStruct getJournalCategory(String index) {
		final List<GffStruct> categories;
		final GffStruct category;

		categories = this.getFieldByLabel("Categories").getListData();

		category = categories.get(Integer.valueOf(index));
		return category;
	}

	/**
	 * Gets the tag from this GFF, if it has one. Otherwise, it returns an empty
	 * string.
	 * 
	 * @return The tag of this GFF, or an empty string.
	 */
	private String getTag(String index) {
		final String type = this.fileType.trim();
		final GffField field;

		if (type.equalsIgnoreCase(GenericFileFormat.TYPE_AREA_FILE)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_CREATURE_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_DOOR_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_ENCOUNTER_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_ITEM_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_MERCHANT_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_PLACEABLE_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_SOUND_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_TRIGGER_BP)
				|| type.equalsIgnoreCase(GenericFileFormat.TYPE_WAYPOINT_BP)) {
			field = this.getFieldByLabel("Tag");
		} else if (type.equalsIgnoreCase(GenericFileFormat.TYPE_MODULE_INFO)) {
			// BioWare, why you no consistent?
			field = this.getFieldByLabel("Mod_Tag");
		} else if (type.equalsIgnoreCase(GenericFileFormat.TYPE_JOURNAL_BP)) {
			final GffStruct category;
			final List<GffStruct> categories;

			categories = this.getFieldByLabel("Categories").getListData();

			category = categories.get(Integer.valueOf(index));

			field = category.getFieldByLabel("Tag");
		} else {
			field = null;
		}

		return field == null ? "" : field.getStringData();
	}

	/**
	 * Remove generated references when we read it in, so that when we write it
	 * out, the naming and references are correct.
	 */
	private void removeGeneratedReferences() {
		final GameTypeManager typeManager;
		final Collection<String> scriptSlots;

		typeManager = ErfFile.getTranslator().getGameTypeManager();
		scriptSlots = typeManager.getSlots(this.getScriptEaseType());

		// remove all generated script references
		for (String slotName : scriptSlots) {
			final GffField field;

			field = this.getFieldByLabel(slotName);

			// Stupid special case. I think they added it in a patch or
			// something, because some placeables don't have it, and it's not
			// mentioned in the official docs. But it's definitely there.
			if (field == null && this.getScriptEaseType().equals("placeable"))
				continue;
			else if (field == null) {
				throw new NullPointerException("Null Field found for " + this
						+ " for slot " + slotName);
			}

			final String reference;

			reference = this.getFieldByLabel(slotName).getStringData();

			if (ErfFile.isScriptEaseGenerated(reference)) {
				// TODO: get the default script from the patterns file or
				// something
				final String originalScript = "";

				this.setField(slotName, originalScript);
			}
		}
	}

	/**
	 * 
	 * @return The ScriptEase notion of this game object.
	 */
	public Resource getObjectRepresentation() {
		return this.getObjectRepresentation(null);
	}

	/**
	 * Extracts a ScriptEase representation of this GFF. <code>index</code> is a
	 * string that is used by GFFs that aren't directly game objects themselves,
	 * but are instead containers. These GFFs use the given string as indexing
	 * information to determine which of their contents to get a game object
	 * representation for.
	 * 
	 * @param index
	 *            Data used to resolve which internal part of this GFF to return
	 *            the game object for. May be <code>null</code> or
	 *            <code>""</code> if the GFF isn't a blueprint wrapper.
	 * 
	 * @return The ScriptEase version of this GFF object that matches the
	 *         indexing information.
	 */
	public Resource getObjectRepresentation(String index) {
		final Resource representation;
		final String name;
		final String fileType = this.getFileType().trim();
		String templateId = this.getResRef() + "." + fileType;

		if (!this.generatesObject()) {
			throw new UnsupportedOperationException(fileType
					+ " does not generate an object representation.");
		}

		if (index == null)
			index = "";

		// conversations
		if (fileType.equalsIgnoreCase(GenericFileFormat.TYPE_DIALOGUE_BP)) {
			if (index.isEmpty()) {
				return new NWNConversation(templateId, this);
			} else {
				return this.getDialogLine(Arrays.asList(index
						.split(NWNDialogueLine.INDEXER_SEPARATOR)));
			}
		}
		// other types
		else {
			name = this.getName(index);
			final String tag;
			final String type;

			tag = this.getTag(index);
			type = this.getScriptEaseType();

			representation = new NWNObject(templateId, type, name, tag);
		}

		// clean up
		this.removeGeneratedReferences();

		return representation;
	}

	/**
	 * Finds the dialogue line with the given indexing information.
	 * 
	 * @param indexes
	 *            The list of indexes
	 * @return
	 */
	private Resource getDialogLine(List<String> indexes) {
		final GffStruct syncStruct;
		final boolean isPlayerLine;
		final GffStruct struct;

		syncStruct = this.getDialogueLineStruct(indexes);

		if (syncStruct == null)
			return null;

		/*
		 * Because dialogue lines must go [NPC->PC->NPC->PC->...], an even
		 * number of indexes must be referencing a player line, odd is NPC.
		 */
		isPlayerLine = indexes.size() % 2 == 0;
		struct = this.resolveSyncStruct(syncStruct, isPlayerLine);

		if (struct != null)
			return new NWNDialogueLine(this, syncStruct, isPlayerLine, indexes,
					struct.getString("Text"));
		else
			return null;
	}

	private GffStruct getDialogueLineStruct(List<String> indexes) {
		boolean isPlayerLine;
		final List<GffStruct> startingList;
		String childListLabel;
		List<GffStruct> children;
		int index;
		GffStruct syncStruct;

		startingList = this.getTopLevelStruct().getFieldByLabel("StartingList")
				.getListData();

		// trace the index trail until we get to the appropriate struct.
		try {
			syncStruct = startingList.get(new Integer(indexes.get(0)));
		} catch (NumberFormatException e) {
			return null;
		}

		for (int i = 1; i < indexes.size(); i++) {
			index = new Integer(indexes.get(i));

			isPlayerLine = i % 2 == 0;

			if (isPlayerLine)
				childListLabel = "EntriesList";
			else
				childListLabel = "RepliesList";

			children = this.resolveSyncStruct(syncStruct, isPlayerLine)
					.getFieldByLabel(childListLabel).getListData();

			if (index >= 0 && index < children.size())
				syncStruct = children.get(index);
		}
		return syncStruct;
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
			type = NWNConversation.TYPE_DIALOGUE;
		} else if (typeString
				.equalsIgnoreCase(GenericFileFormat.TYPE_MODULE_INFO)) {
			type = TYPE_MODULE;
		} else if (typeString
				.equalsIgnoreCase(GenericFileFormat.TYPE_AREA_FILE)) {
			type = "area";
		}

		if (type == null) {
			System.err
					.println("NWN GFF: Could not convert NWN type \""
							+ typeString
							+ "\" to ScriptEase type. Defaulting type to first available");
			type = GameTypeManager.DEFAULT_VOID_TYPE;
		}
		return type;
	}

	/**
	 * Determines if this GFF is of a type that can generate a ScriptEase
	 * object.
	 * 
	 * @return if this GFF can be translated into something important to
	 *         ScriptEase
	 */
	public boolean generatesObject() {
		final String typeString = this.fileType.trim();
		final ArrayList<String> importantTypes = new ArrayList<String>();

		importantTypes.add(GenericFileFormat.TYPE_CREATURE_BP);
		importantTypes.add(GenericFileFormat.TYPE_DOOR_BP);
		importantTypes.add(GenericFileFormat.TYPE_ENCOUNTER_BP);
		importantTypes.add(GenericFileFormat.TYPE_ITEM_BP);
		importantTypes.add(GenericFileFormat.TYPE_MERCHANT_BP);
		importantTypes.add(GenericFileFormat.TYPE_PLACEABLE_BP);
		importantTypes.add(GenericFileFormat.TYPE_SOUND_BP);
		importantTypes.add(GenericFileFormat.TYPE_TRIGGER_BP);
		importantTypes.add(GenericFileFormat.TYPE_WAYPOINT_BP);
		importantTypes.add(GenericFileFormat.TYPE_DIALOGUE_BP);
		importantTypes.add(GenericFileFormat.TYPE_AREA_FILE);
		importantTypes.add(GenericFileFormat.TYPE_MODULE_INFO);

		return importantTypes.contains(typeString);
	}

	private final void readFields(ScriptEaseFileAccess reader,
			long fieldOffset, long fieldCount, long fieldDataOffset)
			throws IOException {
		for (int i = 0; i < (int) fieldCount; i++) {
			reader.seek(fieldOffset + (i * GffField.BYTE_LENGTH));
			this.fieldArray.add(new GffField(reader, fieldDataOffset));
		}
	}

	private final void readStructs(ScriptEaseFileAccess reader,
			long structOffset, long structCount) throws IOException {
		for (long i = 0; i < structCount; i++) {
			reader.seek(structOffset + (i * GffStruct.BYTE_LENGTH));
			this.structArray.add(new GffStruct(reader));
		}
	}

	private final void readLabels(ScriptEaseFileAccess reader,
			long labelOffset, long labelCount) throws IOException {
		reader.seek(labelOffset);

		for (long i = 0; i < labelCount; i++) {
			this.labelArray.add(reader
					.readString(GenericFileFormat.LABEL_BYTE_LENGTH));
		}
	}

	private final void readFieldIndices(ScriptEaseFileAccess reader,
			long fieldIndicesOffset, long fieldIndicesSize) throws IOException {
		reader.seek(fieldIndicesOffset);

		int bytesRead = 0;
		while (bytesRead < fieldIndicesSize) {
			this.fieldIndicesArray.add(reader.readUnsignedInt(true));
			bytesRead += 4;
		}
	}

	private final void readListIndices(ScriptEaseFileAccess reader,
			long listIndicesOffset, long listIndicesSize) throws IOException {
		reader.seek(listIndicesOffset);

		// for each list, load the elements of that list
		long bytesRead = 0;
		while (bytesRead < listIndicesSize) {
			final long numElements = reader.readUnsignedInt(true);
			final List<Long> indexList = new ArrayList<Long>((int) numElements);

			bytesRead += 4;

			for (long j = 0; j < numElements; j++) {
				indexList.add(reader.readUnsignedInt(true));
				bytesRead += 4;
			}

			// ... and add that list like a boss.
			this.listIndicesArray.add(indexList);
		}
	}

	/**
	 * Searches this GFF's field list for the field that matches the supplied
	 * label. This method has a special case for "OnClick". If that event isn't
	 * found, we just return null. This can cause catastrophic problems, so make
	 * sure to check for "null" if you aren't searching for a specific field.
	 * The reason for this heresy is that NWN only introduced the field in
	 * placeables in 1.67. Any module created before then doesn't have the field
	 * in those placeables. "But we require NWN 1.69 anyways!" That's true, but
	 * you could have an old module, save it in 1.69, and they still wouldn't
	 * have the fields. Which kills the ScriptEase. Instead, this workaround
	 * lets us continue with business as usual.
	 * 
	 * @param fieldList
	 *            the list to search in.
	 * @param label
	 *            The label to search by.
	 * @return The field that matches the given label
	 */
	private static GffField getFieldByLabel(Collection<GffField> fieldList,
			String label) {
		String searchLabel;
		for (GffField field : fieldList) {
			searchLabel = field.getLabel();

			if (searchLabel.equalsIgnoreCase(label)) {
				return field;
			}
		}

		// This is awful, but as with many things, NWN has a special case.
		// "OnClick" can be optional in placeables. Fun, isn't it?
		if (label.equals("OnClick"))
			return null;

		// this can happen if it is called on the incorrect list, or with a
		// label that doens't exist.
		throw new IllegalStateException("Failed to locate field " + label);
	}

	// this is for convenience. It's ugly to always pass this stuff around
	private GffField getFieldByLabel(String label) {
		return GenericFileFormat.getFieldByLabel(this.fieldArray, label);
	}

	/**
	 * Gets the string data for the field with the given label in this GFF file.
	 * 
	 * @param label
	 *            The field's label.
	 * @return the data contained in that field.
	 */
	protected List<GffStruct> getList(String label) {
		return this.getFieldByLabel(label).getListData();
	}

	/**
	 * Gets the string data for the field with the givne label in this GFF file.
	 * 
	 * @param label
	 *            The field's label.
	 * @return the data contained in that field.
	 */
	protected String getString(String label) {
		return this.getFieldByLabel(label).getStringData();
	}

	/**
	 * Sets the field with the given label to the given data string.
	 * 
	 * @param fieldLabel
	 *            The field's label.
	 * @param newData
	 *            The new data value.
	 */
	protected void setField(String fieldLabel, String newData) {
		this.setField(null, fieldLabel, newData);
	}

	/**
	 * Sets the field with the given label to the given data string.
	 * 
	 * @param index
	 *            The indexing information into this GFF file. Used for
	 *            conversations and journals.
	 * @param fieldLabel
	 *            The field's label.
	 * @param newData
	 *            The new data value.
	 */
	public void setField(String index, String fieldLabel, String newData) {
		final String type = this.fileType.trim();
		final GffField field;

		if (type.equalsIgnoreCase(GenericFileFormat.TYPE_JOURNAL_BP)) {
			if (index == null)
				throw new IllegalArgumentException(
						"Journals require indexing information to assign fields.");

			field = this.getJournalCategory(index).getFieldByLabel(fieldLabel);
		} else if (type.equalsIgnoreCase(GenericFileFormat.TYPE_DIALOGUE_BP)
				&& index != null) {
			final GffStruct lineStruct;
			final List<String> indexes;

			indexes = Arrays.asList(index
					.split(NWNDialogueLine.INDEXER_SEPARATOR));
			lineStruct = this.getDialogueLineStruct(indexes);

			// even # of indexes means player line.
			final boolean isPlayerLine = indexes.size() % 2 == 0;

			final GffStruct resolvedSyncStruct;

			// We resolve the sync struct, meaning we find the dialogue struct
			// that the sync struct belongs to. We shouldn't be doing this when
			// we attach scripts to dialogue lines, but we were.
			resolvedSyncStruct = this.resolveSyncStruct(lineStruct,
					isPlayerLine);

			// If it's a cause, i.e. script on Script, use ResolvedSyncStruct.

			// If it's the effect, i.e. script on Active, use lineStruct.
			if (fieldLabel.equals("Active"))
				field = lineStruct.getFieldByLabel(fieldLabel);
			else
				field = resolvedSyncStruct.getFieldByLabel(fieldLabel);
		} else {
			field = this.getFieldByLabel(fieldLabel);

			if (field == null && this.getScriptEaseType().equals("placeable")) {
				final String name = this.getName("");

				final String message;

				message = "<html>Warning: The Placeable: " + name
						+ " does not have an OnClick field.<br>"
						+ "It may have been created before patch 1.67. The "
						+ "\"When " + name
						+ " is clicked\" cause will not work.<br>"
						+ "To fix this, open the NWN toolest, add any script "
						+ "to the slot, save the module, then remove the "
						+ "script from the slot and save again.</html>";

				WindowFactory.getInstance().showInformationDialog("Warning",
						message);

				return;
			}
		}

		if (field == null)
			throw new NullPointerException(
					"Null GffField given when setting field value.");

		field.setData(newData);
	}

	@Override
	public String toString() {
		String stringRep = "";

		stringRep += "GFF [\"" + this.resRef + "\" Type:" + this.fileType
				+ "]\n";

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
	 * @return the number of bytes written.
	 * @throws IOException
	 */
	public long write(ScriptEaseFileAccess writer, long filePosition)
			throws IOException {
		final int headerSize = 4 * 14; // 14 header entries, 4 bytes each
		final long structsOffset = headerSize;
		final long fieldsOffset;
		final long labelsOffset;
		final long fieldDataOffset;
		final long fieldIndicesArrayOffset;
		final long listIndicesArrayOffset;
		final long fileSize;

		// Write file's data before header. Each of these return values is the
		// size of the chunk it just wrote.
		fieldsOffset = structsOffset
				+ this.writeStructs(writer, filePosition + structsOffset);

		// NEeds to be called after writefielddatablock
		labelsOffset = fieldsOffset
				+ this.writeFields(writer, filePosition + fieldsOffset);

		fieldDataOffset = labelsOffset
				+ this.writeLabels(writer, filePosition + labelsOffset);

		// Needs to be called before writefields.
		fieldIndicesArrayOffset = fieldDataOffset
				+ this.writeFieldDataBlock(writer, filePosition
						+ fieldDataOffset);

		// We need to call writeFields again because DataOrDataOffsets changed
		// when we called writeFieldDataBlock.
		this.writeFields(writer, filePosition + fieldsOffset);

		listIndicesArrayOffset = fieldIndicesArrayOffset
				+ this.writeFieldIndices(writer, filePosition
						+ fieldIndicesArrayOffset);
		fileSize = listIndicesArrayOffset
				+ this.writeListIndices(writer, filePosition
						+ listIndicesArrayOffset);

		// write the now fully-known header data
		this.writeHeader(writer, filePosition, structsOffset, fieldsOffset,
				labelsOffset, fieldDataOffset, fieldIndicesArrayOffset,
				listIndicesArrayOffset, fileSize);

		return fileSize;
	}

	private long writeStructs(ScriptEaseFileAccess writer, long offset)
			throws IOException {
		writer.seek(offset);

		for (GffStruct struct : this.structArray) {
			struct.write(writer);
		}

		return writer.getFilePointer() - offset;
	}

	private long writeFields(ScriptEaseFileAccess writer, long offset)
			throws IOException {
		writer.seek(offset);

		for (GffField field : this.fieldArray) {
			field.write(writer);
		}

		return writer.getFilePointer() - offset;
	}

	private long writeLabels(ScriptEaseFileAccess writer, long offset)
			throws IOException {
		writer.seek(offset);

		for (String label : this.labelArray) {
			writer.writeString(label, GenericFileFormat.LABEL_BYTE_LENGTH);
		}

		return writer.getFilePointer() - offset;
	}

	private long writeFieldDataBlock(ScriptEaseFileAccess writer, long offset)
			throws IOException {
		int written = 0;

		// complex types that are not lists or structs get written to the field
		// data block. They do their own seek()-ing.
		for (GffField field : this.fieldArray) {
			if (field.isNormalComplexType()) {
				written += field.writeFieldData(writer, offset, written);
			}
		}

		return written;
	}

	private long writeFieldIndices(ScriptEaseFileAccess writer, long offset)
			throws IOException {
		writer.seek(offset);

		for (Long index : this.fieldIndicesArray) {
			writer.writeUnsignedInt(index, true);
		}

		return writer.getFilePointer() - offset;
	}

	private long writeListIndices(ScriptEaseFileAccess writer, long offset)
			throws IOException {
		writer.seek(offset);

		// each list is size followed by the list of indexes, as per
		// GFF docs 3.8
		for (List<Long> indexList : this.listIndicesArray) {
			writer.writeUnsignedInt(indexList.size(), true);

			for (Long index : indexList) {
				writer.writeUnsignedInt(index, true);
			}
		}

		return writer.getFilePointer() - offset;
	}

	/**
	 * Writes this GFF's header information to disk.
	 * 
	 * @param filePosition
	 *            The byte address of the GFF file within the given writer.
	 * @param listIndicesArrayOffset
	 * @param fieldIndicesArrayOffset
	 * @param fieldDataOffset
	 * @param labelsOffset
	 * @param fieldsOffset
	 * @param structsOffset
	 * 
	 * @throws IOException
	 */
	private void writeHeader(ScriptEaseFileAccess writer, long filePosition,
			long structOffset, long fieldsOffset, long labelsOffset,
			long fieldDataOffset, long fieldIndicesArrayOffset,
			long listIndicesArrayOffset, long fileSize) throws IOException {
		final long fieldDataCount;
		final long fieldIndicesCount;
		final long listIndicesCount;

		// these counts are calculated because they are byte lengths, not
		// logical size.
		fieldDataCount = fieldIndicesArrayOffset - fieldDataOffset;
		fieldIndicesCount = listIndicesArrayOffset - fieldIndicesArrayOffset;
		listIndicesCount = fileSize - listIndicesArrayOffset;

		writer.seek(filePosition);

		writer.writeString(this.fileType, 4);
		writer.writeString(GFF_VERSION, 4);
		writer.writeUnsignedInt(structOffset, true);
		writer.writeUnsignedInt(this.structArray.size(), true);
		writer.writeUnsignedInt(fieldsOffset, true);
		writer.writeUnsignedInt(this.fieldArray.size(), true);
		writer.writeUnsignedInt(labelsOffset, true);
		writer.writeUnsignedInt(this.labelArray.size(), true);
		writer.writeUnsignedInt(fieldDataOffset, true);
		writer.writeUnsignedInt(fieldDataCount, true);
		writer.writeUnsignedInt(fieldIndicesArrayOffset, true);
		writer.writeUnsignedInt(fieldIndicesCount, true);
		writer.writeUnsignedInt(listIndicesArrayOffset, true);
		writer.writeUnsignedInt(listIndicesCount, true);
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
		public static final long BYTE_LENGTH = 12;

		private final long typeNumber;
		private final long dataOrDataOffset;
		private final long fieldCount;

		/**
		 * Builds a new GFF struct that is read from the given reader, and whose
		 * fields are to be located at <code>fieldIndicesOffset</code>
		 * 
		 * @param reader
		 *            The reader to read from.
		 * @throws IOException
		 *             if the cat came back the very next day.
		 */
		private GffStruct(ScriptEaseFileAccess reader) throws IOException {
			this(reader.readUnsignedInt(true), reader.readUnsignedInt(true),
					reader.readUnsignedInt(true));
		}

		/**
		 * Builds a new GFF struct with the given values.
		 * 
		 * @param typeNumber
		 * @param dataOrDataOffset
		 * @param fieldCount
		 */
		protected GffStruct(long typeNumber, long dataOrDataOffset,
				long fieldCount) {
			this.typeNumber = typeNumber;
			this.dataOrDataOffset = dataOrDataOffset;
			this.fieldCount = fieldCount;
		}

		@Override
		public String toString() {
			return "GffStruct [" + this.typeNumber + ", "
					+ this.dataOrDataOffset + ", " + this.fieldCount + "]";
		}

		private GffField getFieldByLabel(String label) {
			return GenericFileFormat
					.getFieldByLabel(this.getGffFields(), label);
		}

		/**
		 * Writes this struct to disk at the location that the given writer is
		 * currently pointing to.
		 * 
		 * @param writer
		 * @throws IOException
		 */
		private void write(ScriptEaseFileAccess writer) throws IOException {
			writer.writeUnsignedInt(this.typeNumber, true);
			writer.writeUnsignedInt(this.dataOrDataOffset, true);
			writer.writeUnsignedInt(this.fieldCount, true);
		}

		/**
		 * Gets the fields that this struct logically contains.
		 * 
		 * @return this struct's fields.
		 */
		private List<GffField> getGffFields() {
			final int fieldCount = (int) this.fieldCount;
			final int dataOrDataOffset = (int) this.dataOrDataOffset;
			final List<GffField> fields = new ArrayList<GffField>(fieldCount);

			/*
			 * If Struct.FieldCount = 1, dataOrDataOffset is an index into the
			 * Field Array.
			 */
			if (fieldCount == 1) {
				final GffField field = GenericFileFormat.this.fieldArray
						.get(dataOrDataOffset);
				fields.add(field);
			}
			/*
			 * If Struct.FieldCount > 1, dataOrDataOffset is a byte offset into
			 * the Field Indices array, where there is an array of DWORDs having
			 * a number of elements equal to Struct.FieldCount. Each one of
			 * these DWORDs is an index into the Field Array.
			 */
			else {
				// div 4 because each index is DWORD (4 bytes)
				final long fieldIndicesStart = dataOrDataOffset / 4;
				long index;
				for (long i = 0; i < fieldCount; i++) {
					// get the index from the Field Indices
					index = GenericFileFormat.this.fieldIndicesArray
							.get((int) (fieldIndicesStart + i));

					// resolve the index to an actual field.
					fields.add(GenericFileFormat.this.fieldArray
							.get((int) index));
				}
			}
			return fields;
		}

		/**
		 * Determines if this is the top level struct.
		 * 
		 * @return Whether this is the top level struct
		 */
		private boolean isTopLevelStruct() {
			// documentation states the top level struct always has a type
			// number of 0xFFFFFFFF, which is -1 when considered signed
			return this.typeNumber == 0xFFFFFFFF;
		}

		private void removeScriptEaseReferences() {
			final List<GffField> fields = this.getGffFields();
			final Collection<Slot> slots;
			final Collection<String> slotNames = new ArrayList<String>();

			
			// TODO THIS WILL NEED REFACTORING!!!!!!!
			slots = ErfFile.getTranslator().getLibrary().getEventSlotManager().getEventSlots();

			for (Slot slot : slots) {
				slotNames.add(slot.getKeyword());
			}
			String fieldValue;

			// go through each script field and fix the reference
			for (GffField field : fields) {
				if (field.isStructType()) {
					field.getGffStruct().removeScriptEaseReferences();
				} else if (field.isListType()) {
					for (GffStruct struct : field.getListData()) {
						struct.removeScriptEaseReferences();
					}
				}
				// only bother looking at script slot fields.
				else if (slotNames.contains(field.getLabel())) {
					fieldValue = field.getStringData();

					if (ErfFile.isScriptEaseGenerated(fieldValue)) {
						field.setData("");
					}
				}
			}
		}

		public boolean hasField(String label) {
			for (GffField field : this.getGffFields()) {
				if (field.getLabel().equals(label)) {
					return true;
				}
			}

			return false;
		}

		/**
		 * Gets the list stored in the field with the given label.
		 * 
		 * @param label
		 *            the field's name.
		 * @return The data stored in that field.
		 */
		public List<GffStruct> getList(String label) {
			return this.getFieldByLabel(label).getListData();
		}

		/**
		 * Gets the string stored in the field with the given label.
		 * 
		 * @param label
		 *            the field's name.
		 * @return The data stored in that field.
		 */
		public String getString(String label) {
			return this.getFieldByLabel(label).getStringData();
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

		public static final int BYTE_LENGTH = 12;

		private static final int MAX_RESREF_LENGTH = 16;

		public static final int TYPE_WORD = 2;
		public static final int TYPE_DWORD = 4;
		public static final int TYPE_INT = 5;
		public static final int TYPE_DWORD64 = 6;
		public static final int TYPE_INT64 = 7;
		public static final int TYPE_FLOAT = 8;
		public static final int TYPE_DOUBLE = 9;
		public static final int TYPE_CEXOSTRING = 10;
		public static final int TYPE_RESREF = 11;
		public static final int TYPE_CEXOLOCSTRING = 12;
		public static final int TYPE_VOID = 13;
		public static final int TYPE_STRUCT = 14;
		public static final int TYPE_LIST = 15;

		// max length is defined in GFF doc page 4
		private static final int EXO_STRING_MAX_LENGTH = 1024;

		private final long typeNumber;
		private final long labelIndex;
		private long dataOrDataOffset;

		private class CExoLocString {
			public long strRef;
			public final Map<Long, String> strings = new HashMap<Long, String>();

			public int getByteSize() {
				// start with 4 + 4 = 8 for the StringRef + StringCount
				int size = 8;

				for (Long id : this.strings.keySet()) {
					size += 8; // id and length storage
					size += this.strings.get(id).length();
				}

				return size;
			}
		}

		// Field data variables that store the field's data for "complex types"
		// (see isComplex() for more)
		private CExoLocString fieldDataLocString;
		private String fieldDataString;
		private double fieldDataDouble;
		// private long fieldDataWord32;
		private long fieldDataDWord64;
		private long fieldDataInt64;
		private byte[] fieldDataBytes;

		// end field data variables

		private GffField(ScriptEaseFileAccess reader, long fieldDataOffset)
				throws IOException {
			this(reader.readUnsignedInt(true), reader.readUnsignedInt(true),
					reader.readUnsignedInt(true));

			this.readData(reader, fieldDataOffset);
		}

		protected GffField(long typeNumber, long labelIndex,
				long dataOrDataOffset) {
			this.typeNumber = typeNumber;
			this.labelIndex = labelIndex;
			this.dataOrDataOffset = dataOrDataOffset;
		}

		/**
		 * Writes out the GffField data to the writer.
		 * 
		 * @param writer
		 * @throws IOException
		 */
		private void write(ScriptEaseFileAccess writer) throws IOException {
			writer.writeUnsignedInt(this.typeNumber, true);
			writer.writeUnsignedInt(this.labelIndex, true);
			writer.writeUnsignedInt(this.dataOrDataOffset, true);
		}

		@Override
		public String toString() {
			return "GffField [" + this.getName() + ", type: " + this.typeNumber
					+ ", dodOffset: " + this.dataOrDataOffset + "]";
		}

		/**
		 * @return Unsigned int in a long representing the type.
		 */
		private long getType() {
			return this.typeNumber;
		}

		/**
		 * @return the labelIndex
		 */
		public long getLabelIndex() {
			return this.labelIndex;
		}

		/**
		 * Finds the GFFStruct that this field points to.
		 * 
		 * @return the struct pointed to by this field.
		 */
		private GffStruct getGffStruct() {
			if (!this.isStructType()) {
				throw new IllegalStateException(
						"GffField does not contain a GffStruct.");
			}

			/*
			 * Normally, a Field's DataOrDataOffset value would be a byte offset
			 * into the Field Data Block, but for a Struct, it is an index into
			 * the Struct Array.
			 */
			return GenericFileFormat.this.structArray
					.get((int) this.dataOrDataOffset);
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
		private List<GffStruct> getListData() {
			if (!this.isListType()) {
				throw new IllegalStateException(
						"GffField does not contain a GffList.");
			}

			final List<GffStruct> structs = new ArrayList<GffStruct>();

			// this is the offset in bytes into the list array
			int countedOffset = 0;

			for (List<Long> list : GenericFileFormat.this.listIndicesArray) {
				if (countedOffset == this.dataOrDataOffset) {
					// Found it. Resolve the indices.
					for (long index : list) {
						final GffStruct struct = GenericFileFormat.this.structArray
								.get((int) index);
						structs.add(struct);
					}

					return structs;
				} else if (countedOffset > this.dataOrDataOffset) {
					break;
				}

				countedOffset += calculateListByteSize(list);
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
		 * Gets this field's label as stored in the label arrays indexed by
		 * {@link #getLabelIndex()}.
		 * 
		 * @return the field's label.
		 */
		private String getLabel() {
			return GenericFileFormat.this.labelArray.get((int) this
					.getLabelIndex());
		}

		/**
		 * Reads and stores the data of this field.
		 * 
		 * @param reader
		 * @return
		 * @throws IOException
		 *             if everything goes to hell.
		 */
		private void readData(ScriptEaseFileAccess reader, long fieldDataOffset)
				throws IOException {
			if (!this.isNormalComplexType()) {
				// if simple type, we already have the data in dataOrDataOffset
				// if list or struct, data is loaded into the structArray or
				// listIndicesArray
				return;
			}

			// get to the data
			reader.seek(fieldDataOffset + this.dataOrDataOffset);

			long length;

			switch ((int) this.getType()) {
			case GffField.TYPE_DWORD64:
				this.fieldDataDWord64 = reader.readUnsignedInt(true);
				break;
			case GffField.TYPE_INT64:
				this.fieldDataInt64 = reader.readLong(true);
				break;
			case GffField.TYPE_DOUBLE:
				// this.fieldDataDouble = reader.readDouble(true);
				// break;

				throw new UnsupportedOperationException(
						"I can't read a Double yet!");
			case GffField.TYPE_CEXOSTRING:
				length = reader.readUnsignedInt(true);

				this.fieldDataString = reader.readString((int) length);
				break;
			case GffField.TYPE_RESREF:
				length = reader.readByte();

				this.fieldDataString = reader.readString((int) length);

				break;
			case GffField.TYPE_CEXOLOCSTRING:
				// This is annoyingly complicated. See CExoLocString in GFF doc,
				// page 3, and 4.6 for details. - remiller

				// length here means length in bytes of this total structure
				// after the length int
				length = reader.readUnsignedInt(true);
				long stringRef = reader.readUnsignedInt(true);
				long stringCount = reader.readUnsignedInt(true);

				this.fieldDataLocString = new CExoLocString();
				this.fieldDataLocString.strRef = stringRef;

				// Store all the ones saved within this file. If it's in the
				// internationalization (TLK) file, then we'll look that up
				// separately.
				int stringLen;

				for (int i = 0; i < stringCount; i++) {
					long id = reader.readUnsignedInt(true);

					stringLen = reader.readInt(true);
					if (stringLen > 0) {
						String value = reader.readString(stringLen);

						this.fieldDataLocString.strings.put(id, value);
					}
				}

				break;
			case GffField.TYPE_VOID:
				// voids are arbitrary binary data. Hopefully that doesn't cause
				// us nightmares. - remiller
				long numBytes = reader.readUnsignedInt(true);
				this.fieldDataBytes = reader.readBytes((int) numBytes);
				break;
			default:
				this.dieUnknownType();
			}
		}

		public void setBlankCExoLocString() {
			// This is annoyingly complicated. See CExoLocString in GFF doc,
			// page 3, and 4.6 for details. - remiller
			if (this.getType() == GffField.TYPE_CEXOLOCSTRING) {
				this.fieldDataLocString = new CExoLocString();
				this.fieldDataLocString.strRef = -1;
			} else {
				System.err.println("Attempted to set blank CExoLocString for "
						+ "non CExoLocString field " + this
						+ ". You're doing it wrong.");
			}
		}

		private IllegalStateException dieUnknownType() {
			// This should never happen, unless we missed a type or we
			// somehow hit a snag while reading unsigned ints and
			// casting them?
			throw new IllegalStateException("I don't know what type this is: "
					+ this.getType() + " for field " + this.getLabel());
		}

		/**
		 * Sets this field's data to the value contained in the given string (to
		 * be converted to whatever type is expected).
		 * 
		 * @param value
		 *            The string containing the value to be used.
		 */
		protected void setData(String value) {
			String oldVal = null;

			if (!this.isComplexType()) { // just data
				this.dataOrDataOffset = Long.valueOf(value);
			} else if (this.isListType() || this.isStructType()) {
				throw new IllegalStateException("GffField " + this.getName()
						+ " cannot be set to value" + value
						+ " because is a Struct (" + this.isStructType()
						+ ") or List (" + this.isListType() + ") type.");
			} else {
				switch ((int) this.getType()) {
				case GffField.TYPE_DWORD64:
					this.fieldDataDWord64 = Long.valueOf(value);
					break;
				case GffField.TYPE_INT64:
					this.fieldDataInt64 = Long.valueOf(value);
					break;
				case GffField.TYPE_DOUBLE:
					this.fieldDataDouble = Double.valueOf(value);
					break;
				case GffField.TYPE_CEXOSTRING:
				case GffField.TYPE_RESREF:
					oldVal = this.fieldDataString;
					this.fieldDataString = value;
					break;
				case GffField.TYPE_CEXOLOCSTRING:
					if (this.fieldDataLocString.strRef == -1) {
						oldVal = this.fieldDataLocString.strings.put(0L, value);
					} else {
						throw new UnsupportedOperationException(
								"Cannot set data on CExoLocStrings that exist in the TLK file.");
					}

					break;
				case GffField.TYPE_VOID:
					this.fieldDataBytes = value.getBytes();
					break;
				default:
					this.dieUnknownType();
				}

				/*
				 * It annoys me that I have to do this, but it's required since
				 * we need to write fields before their data sections, and the
				 * only way to know the offsets otherwise is to actually write
				 * them. - remiller
				 */
				// if our size changed, update all of the following neighbours'
				// offsets.
				if (oldVal != null) {
					long offsetDelta = 0;
					boolean found = false;

					offsetDelta = value.length() - oldVal.length();

					// we can use the enclosing GFF's field array because
					// structs store their fields there too.
					for (GffField field : GenericFileFormat.this.fieldArray) {
						// find myself (but not in a become-a-monk way)
						if (field == this) {
							found = true;
						} else {
							if (found && field.isNormalComplexType()) {
								// adding the delta will work if its positive of
								// negative.
								field.dataOrDataOffset += offsetDelta;
							}
						}
					}
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
		private String getStringData() {
			if (!this.isComplexType()) { // just data
				return Long.toString(this.dataOrDataOffset);
			} else if (this.isListType() || this.isStructType()) {
				throw new IllegalStateException(
						"GffField "
								+ this.getName()
								+ " does not contain a String. It contains a struct or a list.");
			} else {
				switch ((int) this.getType()) {
				case GffField.TYPE_DWORD64:
					return Long.toString(this.fieldDataDWord64);
				case GffField.TYPE_INT64:
					return Long.toString(this.fieldDataInt64);
				case GffField.TYPE_DOUBLE:
					return Double.toString(this.fieldDataDouble);
				case GffField.TYPE_CEXOSTRING:
				case GffField.TYPE_RESREF:
					return this.fieldDataString;
				case GffField.TYPE_CEXOLOCSTRING:
					String value;
					// we prioritize strings stored in the file over external
					// ones, like the toolset does (as described in GFF
					// documentation).

					// 0 for the English version. We're not interested in
					// supporting internationalization yet.
					value = this.fieldDataLocString.strings.get(0L);

					if (value == null) {
						value = GenericFileFormat.stringTable
								.lookup(this.fieldDataLocString.strRef);
					}

					return value;
				case GffField.TYPE_VOID:
					return new String(this.fieldDataBytes);
				default:
					this.dieUnknownType();
					return null;
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
		 * @param fieldDataBlockOffset
		 *            the offset to the Field Data Block.
		 * @param dataOffset
		 *            the offset for this field's data within the field data
		 *            block.
		 * @return the number of bytes written
		 * @throws IOException
		 *             if monkeys start wearing rubber pants.
		 */
		private long writeFieldData(ScriptEaseFileAccess writer,
				long fieldDataBlockOffset, long dataOffset) throws IOException {
			String strValue;
			final long startByte;

			/*
			 * Non-complex types are inherently handled by writing the field
			 * itself, since that's where they are stored, so we just deal with
			 * complex types here. Similar deal with structs and lists - they're
			 * stored separately.
			 */
			if (!this.isNormalComplexType()) {
				throw new IllegalStateException(
						"Can't write simple types, lists, or structs in GFF files with writeFieldData(...).");
			}

			this.dataOrDataOffset = dataOffset;

			// get to the data location
			writer.seek(fieldDataBlockOffset + this.dataOrDataOffset);

			startByte = writer.getFilePointer();

			switch ((int) this.getType()) {
			case GffField.TYPE_DWORD64:
				writer.writeUnsignedLong(this.fieldDataDWord64, true);
				break;
			case GffField.TYPE_INT64:
				writer.writeLong(this.fieldDataInt64, true);
				break;
			case GffField.TYPE_DOUBLE:
				// writer.writeDouble(this.fieldDataDouble, true);
				// break;

				throw new IllegalStateException("I can't write a Double yet!");
			case GffField.TYPE_CEXOSTRING:
				strValue = this.fieldDataString;

				if (strValue == null) {
					System.err.println("Encountered null FieldDataString in "
							+ this);
				}

				if (strValue.length() > EXO_STRING_MAX_LENGTH)
					strValue.substring(0, EXO_STRING_MAX_LENGTH);

				writer.writeUnsignedInt(strValue.length(), true);

				writer.writeString(strValue, strValue.length());

				break;
			case GffField.TYPE_RESREF:
				String newData = this.fieldDataString.toLowerCase();

				if (newData.length() > GffField.MAX_RESREF_LENGTH)
					newData = newData.substring(0, GffField.MAX_RESREF_LENGTH);

				writer.writeByte(newData.length());

				writer.writeString(newData, newData.length());

				break;
			case GffField.TYPE_CEXOLOCSTRING:
				final Map<Long, String> strings = this.fieldDataLocString.strings;
				final long strRef = this.fieldDataLocString.strRef;

				// byteSize does not include itself in the size calculation
				int byteSize = this.fieldDataLocString.getByteSize();

				writer.writeUnsignedInt(byteSize, true);
				writer.writeUnsignedInt(strRef, true);
				writer.writeUnsignedInt(strings.size(), true);

				// Write the value to the ERF file, even if it's already in the
				// TLK file. The toolset does this when you run a Build on the
				// module, so it can't be that bad. - remiller
				for (long id : strings.keySet()) {
					writer.writeUnsignedInt(id, true);
					strValue = strings.get(id);

					if (strValue.length() > EXO_STRING_MAX_LENGTH)
						strValue.substring(0, EXO_STRING_MAX_LENGTH);

					writer.writeUnsignedInt(strValue.length(), true);
					writer.writeString(strValue);
				}

				break;
			case GffField.TYPE_VOID:
				writer.writeUnsignedInt(this.fieldDataBytes.length, true);
				writer.writeBytes(this.fieldDataBytes);

				break;
			default:
				this.dieUnknownType();
			}

			return writer.getFilePointer() - startByte;
		}

		/**
		 * Determines whether the type is a complex type (meaning the field data
		 * is located in the field data block of the GFF, or elsewhere for
		 * structs/lists) or a simple type. This check is based on table 3.4b in
		 * the GFF documentation.
		 * 
		 * @return Whether this field is a complex type or not.
		 * @see #isNormalComplexType()
		 */
		private boolean isComplexType() {
			long type = this.getType();

			return (type > GffField.TYPE_INT) && (type != GffField.TYPE_FLOAT);
		}

		/**
		 * Determines if the type is a normal complex type (meaning the field
		 * data is located in the field data block of the GFF). That is, a
		 * Regular Complex Type is a Complex Type but not a struct or a list.<br>
		 * <br>
		 * This check is based on table 3.4b in the GFF documentation. Any thing
		 * listed as <code>Complex? yes</code> without asterisks is considered
		 * Normal Complex.
		 * 
		 * @return whether this field is a normal complex type or not.
		 * @see #isComplexType()
		 */
		private boolean isNormalComplexType() {
			return this.isComplexType() && !this.isListType()
					&& !this.isStructType();
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

			return (type == GffField.TYPE_STRUCT);
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

			return (type == GffField.TYPE_LIST);
		}
	}

	public void removeScriptEaseReferences() {
		GffStruct gitFileStruct = this.getTopLevelStruct();

		gitFileStruct.removeScriptEaseReferences();
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
	public void updateAllInstances(GenericFileFormat sourceGFF, String slot,
			String scriptResRef) {
		if (!this.isInstanceUpdatable()) {
			throw new IllegalStateException(
					"Cannot update instances in anything but a GIT file. This is a "
							+ this.getFileType() + " file.");
		}

		final GffStruct gitFileStruct = this.getTopLevelStruct();
		final String sourceResRef = sourceGFF.getResRef();

		// Go through the lists of lists in a GIT File. (e.g. Creature List,
		// etc). The rest of the comments will use the creature list as an
		// example.
		for (GffField gitFileField : gitFileStruct.getGffFields()) {
			// Find the appropriate list.
			if (!gitFileField.getLabel().equals(sourceGFF.getGITListLabel())) {
				continue;
			}

			// List of all (e.g.) creature structs in creature list.
			List<GffStruct> instances = gitFileField.getListData();

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
								+ " in the area " + this.getName(""));
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
	public boolean isInstanceUpdatable() {
		String fileType = this.getFileType().trim();
		return fileType
				.equalsIgnoreCase(GenericFileFormat.TYPE_AREA_GAME_INSTANCE_FILE);
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
}

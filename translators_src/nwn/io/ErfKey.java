package io;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import scriptease.translator.io.tools.ScriptEaseFileAccess;

/**
 * ErfKey is a simple class that represents the ErfKey struct as described in
 * BioWare's Erf file documentation (page 3)
 * 
 * @author jtduncan
 * @author remiller
 * 
 */
public class ErfKey {
	private static final int UNUSED_BYTES = 2;

	/**
	 * The maximum length of any resref in characters.
	 */
	protected static final int RESREF_MAX_LENGTH = 16;

	/**
	 * The size of any ErfKey in bytes
	 */
	// we know that this is listed as 24 bytes in the docs, but 32 seems to
	// actually be the correct number. Go figure.
	protected static final short BYTE_LENGTH = 32;

	// These types come from the Key/BIF documentation, table 1.3.1
	protected static final short SCRIPT_SOURCE_TYPE = 2009;
	protected static final short SCRIPT_COMPILED_TYPE = 2010;
	protected static final short AREA_FILE_TYPE = 2012;
	protected static final short MODULE_IFO_TYPE = 2014;
	protected static final short CREATURE_INSTANCE_TYPE = 2015;
	protected static final short AREA_GAME_INSTANCE_FILE_TYPE = 2023;
	protected static final short ITEM_BLUEPRINT_TYPE = 2025;
	protected static final short CREATURE_BLUEPRINT_TYPE = 2027;
	protected static final short CONVERSATION_FILE_TYPE = 2029;
	protected static final short TILE_OR_BLUEPRINT_PALETTE_TYPE = 2030;
	protected static final short TRIGGER_BLUEPRINT_TYPE = 2032;
	protected static final short SOUND_BLUEPRINT_TYPE = 2035;
	protected static final short GENERAL_GFF_FILE_TYPE = 2037;
	protected static final short FACTION_FILE_TYPE = 2038;
	protected static final short ENCOUNTER_BLUEPRINT_TYPE = 2040;
	protected static final short DOOR_BLUEPRINT_TYPE = 2042;
	protected static final short PLACEABLE_BLUEPRINT_TYPE = 2044;
	protected static final short GAME_INSTANCE_COMMENTS_TYPE = 2046;
	protected static final short GUI_LAYOUT_FILE_TYPE = 2047;
	protected static final short STORE_BLUEPRINT_TYPE = 2051;
	protected static final short JOURNAL_FILE_TYPE = 2056;
	protected static final short WAYPOINT_BLUEPRINT_TYPE = 2058;
	protected static final short PLOT_INSTANCE_TYPE = 2065;

	private static final Map<Short, String> extensionMap = new HashMap<Short, String>();

	static {
		ErfKey.extensionMap.put((short) 0xFFFF, "N/A");
		ErfKey.extensionMap.put((short) 1, "bmp");
		ErfKey.extensionMap.put((short) 3, "tga");
		ErfKey.extensionMap.put((short) 4, "wav");
		ErfKey.extensionMap.put((short) 6, "plt");
		ErfKey.extensionMap.put((short) 7, "ini");
		ErfKey.extensionMap.put((short) 10, "txt");
		ErfKey.extensionMap.put((short) 2002, "mdl");
		ErfKey.extensionMap.put(SCRIPT_SOURCE_TYPE, "nss");
		ErfKey.extensionMap.put(SCRIPT_COMPILED_TYPE, "ncs");
		ErfKey.extensionMap.put(AREA_FILE_TYPE, "are");
		ErfKey.extensionMap.put((short) 2013, "set");
		ErfKey.extensionMap.put(MODULE_IFO_TYPE, "ifo");
		ErfKey.extensionMap.put(CREATURE_INSTANCE_TYPE, "bic");
		ErfKey.extensionMap.put((short) 2016, "wok");
		ErfKey.extensionMap.put((short) 2017, "2da");
		ErfKey.extensionMap.put((short) 2022, "txi");
		ErfKey.extensionMap.put(AREA_GAME_INSTANCE_FILE_TYPE, "git");
		ErfKey.extensionMap.put(ITEM_BLUEPRINT_TYPE, "uti");
		ErfKey.extensionMap.put(CREATURE_BLUEPRINT_TYPE, "utc");
		ErfKey.extensionMap.put(CONVERSATION_FILE_TYPE, "dlg");
		ErfKey.extensionMap.put(TILE_OR_BLUEPRINT_PALETTE_TYPE, "itp");
		ErfKey.extensionMap.put(TRIGGER_BLUEPRINT_TYPE, "utt");
		ErfKey.extensionMap.put((short) 2033, "dds");
		ErfKey.extensionMap.put(SOUND_BLUEPRINT_TYPE, "uts");
		ErfKey.extensionMap.put((short) 2036, "ltr");
		ErfKey.extensionMap.put(GENERAL_GFF_FILE_TYPE, "gff");
		ErfKey.extensionMap.put(FACTION_FILE_TYPE, "fac");
		ErfKey.extensionMap.put(ENCOUNTER_BLUEPRINT_TYPE, "ute");
		ErfKey.extensionMap.put(DOOR_BLUEPRINT_TYPE, "utd");
		ErfKey.extensionMap.put(PLACEABLE_BLUEPRINT_TYPE, "utp");
		ErfKey.extensionMap.put((short) 2045, "dft");
		ErfKey.extensionMap.put(GAME_INSTANCE_COMMENTS_TYPE, "gic");
		ErfKey.extensionMap.put(GUI_LAYOUT_FILE_TYPE, "gui");
		ErfKey.extensionMap.put(STORE_BLUEPRINT_TYPE, "utm");
		ErfKey.extensionMap.put((short) 2052, "dwk");
		ErfKey.extensionMap.put((short) 2053, "pwk");
		ErfKey.extensionMap.put(JOURNAL_FILE_TYPE, "jrl");
		ErfKey.extensionMap.put(WAYPOINT_BLUEPRINT_TYPE, "utw");
		ErfKey.extensionMap.put((short) 2060, "ssf");
		ErfKey.extensionMap.put((short) 2064, "ndb");
		ErfKey.extensionMap.put(PLOT_INSTANCE_TYPE, "ptm");
		ErfKey.extensionMap.put((short) 2066, "ptt");
	}

	private final String resRef;
	private final short resType;

	/**
	 * Reads an ErfKey from the file.
	 * 
	 * @param stream
	 *            The stream from which the ErfKey is read.
	 * @throws IOException
	 */
	public ErfKey(ScriptEaseFileAccess stream) throws IOException {
		this(stream.readString(ErfKey.RESREF_MAX_LENGTH), stream.readInt(true),
				stream.readShort(true));
		stream.skipBytes(ErfKey.UNUSED_BYTES); // Unused space.
	}

	/**
	 * This exists as an adapter for {@link #ErfKey(ScriptEaseFileAccess)}. It
	 * drops the id on purpose, because we compute it at write time, and even
	 * BioWare says it's redundant data.
	 */
	private ErfKey(String resRef, int id, short type) {
		this(resRef, type);
	}

	/**
	 * Creates a new ErfKey.
	 * 
	 * @param resRef
	 *            The unique name of the resource.
	 * @param resourceType
	 *            The type of the resource from table 1.3.1 in the Key/BIF
	 *            documentation
	 */
	public ErfKey(String resRef, short resourceType) {
		this.resRef = resRef;
		this.resType = resourceType;
	}

	/**
	 * Writes an ErfKey to the file.
	 * 
	 * @param stream
	 *            The stream that will perform the writing.
	 * @param resourceID
	 *            The resource ID number. This is equivalent to
	 *            <code>( ErfKeyFileLocation - OffSetToKeyList ) / entryCount</code>
	 *            or to its index in the resources list.
	 * @throws IOException
	 */
	public void write(ScriptEaseFileAccess stream, int resourceID)
			throws IOException {
		stream.writeString(this.resRef, ErfKey.RESREF_MAX_LENGTH);
		stream.writeInt(resourceID, true);
		stream.writeShort(this.resType, true);
		stream.writeNullBytes(ErfKey.UNUSED_BYTES); // Unused space
	}

	protected short getResType() {
		return this.resType;
	}

	public String getResRef() {
		return this.resRef;
	}

	/**
	 * Determines whether the key represents a GFF file. This test is based off
	 * of the Key/Bif documentation, table 1.3.1
	 * 
	 * @return True if the key represents a GFF file
	 */
	public boolean isGFF() {
		return (this.resType == MODULE_IFO_TYPE)
				|| (this.resType == CREATURE_INSTANCE_TYPE)
				|| (this.resType == AREA_GAME_INSTANCE_FILE_TYPE)
				|| (this.resType == ITEM_BLUEPRINT_TYPE)
				|| (this.resType == CREATURE_BLUEPRINT_TYPE)
				|| (this.resType == CONVERSATION_FILE_TYPE)
				|| (this.resType == TILE_OR_BLUEPRINT_PALETTE_TYPE)
				|| (this.resType == TRIGGER_BLUEPRINT_TYPE)
				|| (this.resType == SOUND_BLUEPRINT_TYPE)
				|| (this.resType == GENERAL_GFF_FILE_TYPE)
				|| (this.resType == FACTION_FILE_TYPE)
				|| (this.resType == ENCOUNTER_BLUEPRINT_TYPE)
				|| (this.resType == DOOR_BLUEPRINT_TYPE)
				|| (this.resType == PLACEABLE_BLUEPRINT_TYPE)
				|| (this.resType == GAME_INSTANCE_COMMENTS_TYPE)
				|| (this.resType == GUI_LAYOUT_FILE_TYPE)
				|| (this.resType == STORE_BLUEPRINT_TYPE)
				|| (this.resType == JOURNAL_FILE_TYPE)
				|| (this.resType == WAYPOINT_BLUEPRINT_TYPE)
				|| (this.resType == PLOT_INSTANCE_TYPE)
				|| (this.resType == AREA_FILE_TYPE);
	}

	@Override
	public String toString() {
		return "ErfKey [" + this.resRef + ", type: " + this.resType + "]";
	}

	protected String getExtension() {
		return ErfKey.extensionMap.get(this.getResType());
	}
}
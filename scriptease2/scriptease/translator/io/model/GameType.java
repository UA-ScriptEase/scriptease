package scriptease.translator.io.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * Represents a valid game type possible in the game's scripting language. Each
 * GameType has a displayName which is the name the user will view, a label
 * which is the unique identifier for the GameType, a codeSymbol which is the
 * translation of the type into the game's scripting language, and a set of
 * valid slots for which this GameType can be used.
 * 
 * It also has an optional regex (reg).
 * 
 * @author mfchurch
 */
public class GameType {
	public static final String DEFAULT_UNKNOWN_TYPE = "unknown";

	public static final String DEFAULT_VOID_TYPE = "void";
	public static final String DEFAULT_BOOL_TYPE = "question";
	public static final String DEFAULT_LIST_WIDGET = "Li";

	/**
	 * Enumeration of the possible widgets that may be used to display/edit a
	 * value for a particular type.
	 * 
	 * @author remiller
	 */
	public static enum GUIType {
		JTEXTFIELD, JSPINNER, JCOMBOBOX;
	}

	private String displayName;
	private String keyword;
	private String regEx;
	private String widgetName;
	private Collection<String> slots;
	private GUIType guiEditorName;
	private Map<String, String> enums;
	private Collection<AbstractFragment> format;
	private String codeSymbol;
	private Map<String, String> escapes;

	/**
	 * Builds a new Game Type that is completely empty.
	 */
	public GameType() {
		this("", "unknown", "", new ArrayList<AbstractFragment>(),
				new ArrayList<String>(), "", "", new HashMap<String, String>(),
				null, null);
	}

	/**
	 * Build a new Game Type representation.
	 * 
	 * @param name
	 *            The name of this game type.
	 * @param keyword
	 *            The unique keyword for this game type.
	 * @param codeSymbol
	 *            The string to be used to represent this type in code.
	 * @param fragments
	 *            Collection of fragments to be used
	 * @param slots
	 *            The collection of applicable game slots
	 * @param enums
	 *            An optional list of enumeration value strings.
	 * @param regEx
	 *            An optional regular expression that all values for bindings of
	 *            this type must match.
	 * @param escapes
	 *            An optional map of (key) characters which need to be escaped
	 *            and their escape characters (values)
	 * @param gui
	 *            An optional GUI widget specification string to be used . Must
	 *            be one of: <code>JComboBox</code><code>JSpinner</code>
	 *            <code>JTextField</code>
	 */
	public GameType(String name, String keyword, String codeSymbol,
			Collection<AbstractFragment> fragments, Collection<String> slots,
			String enums, String regEx, Map<String, String> escapes,
			GUIType gui, String widgetName) {

		// Sanity check. If we're not requiring a selection from a list of
		// precisely nothing, that's Very Bad. - remiller
		if (gui != null && gui.equals(GUIType.JCOMBOBOX)
				&& (enums == null || enums.trim().equalsIgnoreCase("")))
			throw new IllegalArgumentException(
					"Empty enumeration for game type using Combo Box editor.");

		this.displayName = name;
		this.keyword = keyword;
		this.regEx = regEx;
		this.slots = new ArrayList<String>(slots);
		this.guiEditorName = gui;
		this.format = new ArrayList<AbstractFragment>(fragments);
		this.enums = this.convertEnumStringToMap(enums);
		this.codeSymbol = codeSymbol;
		this.escapes = new HashMap<String, String>(escapes);
		this.widgetName = widgetName;
	}

	/**
	 * Converts enums of the format "VALUE1<NAME1>|VALUE2<NAME2>|VALUE3|..." or
	 * even just "VALUE1|VALUE2|..." into a map where value=NAME and key=VALUE
	 * 
	 * @param enums
	 */
	private Map<String, String> convertEnumStringToMap(String enums) {
		final Map<String, String> enumsMap;

		enumsMap = new HashMap<String, String>();

		if (enums.length() == 0)
			return enumsMap;

		String[] theEnums = enums.split("[|]");
		for (int i = 0; i < theEnums.length; i++) {
			String name = null;
			String value = null;

			String anEnum = theEnums[i];
			String[] front = anEnum.split("<");
			value = front[0];
			if (front.length > 1) {
				String between = front[1];
				String[] back = between.split(">");
				name = back[0];
			}

			// if no name was specified, use the value
			if (name != null)
				enumsMap.put(value, name);
			else
				enumsMap.put(value, value);
		}
		return enumsMap;
	}

	/**
	 * Converts the provided Map (where key=NAME, and value=VALUE) into a string
	 * formatted "VALUE1<NAME1>|VALUE2<NAME2>|..." or even just
	 * "VALUE1|VALUE2|..."
	 * 
	 * @param enums
	 * @return
	 */
	private String convertEnumMapToString(Map<String, String> enums) {
		String enumString = "";
		for (Entry<String, String> entry : enums.entrySet()) {
			final String value = entry.getKey();
			final String name = entry.getValue();
			String enumEntry;

			if (value.equals(""))
				continue;

			enumEntry = value;

			// Don't bother putting the name if it's the same as the value
			if (!name.equals(value))
				enumEntry += "<" + name + ">";

			enumString += enumEntry + "|";
		}

		// remove the ending separator
		if (enumString.endsWith("|"))
			enumString = enumString.substring(0, enumString.length() - 1);

		return enumString;
	}

	/**
	 * Returns a string which represents the GameType's enumerable types in the
	 * format "VALUE1<NAME1>|VALUE2<NAME2>|VALUE3|..." or even just
	 * "VALUE1|VALUE2|..."
	 * 
	 * @return
	 */
	public String getEnumString() {
		return this.convertEnumMapToString(this.enums);
	}

	/**
	 * Returns the enumerated values of a type known by the model. <br>
	 * <br>
	 * An example of these would be if a translator gives special names to
	 * booleans to make more sense to the user. So the enumerated map would look
	 * like <code>{ (Active, true), (Inactive, false) }</code>
	 * 
	 * @return
	 */
	public Map<String, String> getEnumMap() {
		return new HashMap<String, String>(this.enums);
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public void setFormat(Collection<AbstractFragment> format) {
		this.format = format;
	}

	public void setCodeSymbol(String codeSymbol) {
		this.codeSymbol = codeSymbol;
	}

	public String getCodeSymbol() {
		return this.codeSymbol;
	}

	/**
	 * Returns the format for the type as a collection of
	 * {@link AbstractFragment}s.
	 * 
	 * @return
	 */
	public Collection<AbstractFragment> getFormat() {
		return this.format;
	}

	public String getKeyword() {
		return this.keyword;
	}

	public String getReg() {
		return this.regEx;
	}

	/**
	 * Returns the name of the widget that should be displayed in the
	 * {@link TypeWidget} associated with a type known by the model.
	 * 
	 * @return
	 */
	public String getWidgetName() {
		return this.widgetName;
	}

	public void setWidgetName(String widgetName) {
		this.widgetName = widgetName;
	}

	public void setReg(String reg) {
		this.regEx = reg;
	}

	public Boolean hasReg() {
		if (this.regEx != null && !this.regEx.isEmpty())
			return true;
		return false;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Returns the GUI widget that is to edit a type known by the model.
	 * 
	 * @return the GUI widget that will edit a component of this type, or
	 *         <code>null</code> if there is no widget specified.
	 */
	public GUIType getGui() {
		return this.guiEditorName;
	}

	/**
	 * Sets the GUI widget to be used for editing a binding of this type. Can be
	 * <code>null</code>.
	 * 
	 * @param newGuiId
	 *            Name of the GUI widget used to edit a binding of this type.
	 *            Must be one of <code>null</code>, <code>"JSpinner"</code>,
	 *            <code>"JComboBox"</code> or <code>"JTextField"</code>
	 */
	public void setGui(GUIType newGuiId) {
		this.guiEditorName = newGuiId;
	}

	/**
	 * Returns a copy of the map of (key) characters which need to be escaped
	 * and their escape characters (values).
	 * 
	 * @return
	 */
	public Map<String, String> getEscapes() {
		return new HashMap<String, String>(escapes);
	}

	public void addSlot(String slot) {
		this.slots.add(slot);
	}

	/**
	 * Returns a copy of the Type's valid slots if the Type does not have any
	 * slots, return an empty list
	 * 
	 * @return Collection<String> of the Type's valid slots.
	 */
	public Collection<String> getSlots() {
		if (this.slots == null) {
			return new ArrayList<String>();
		}
		return new ArrayList<String>(this.slots);
	}

	public Boolean hasSlot(String slot) {
		if (this.slots != null) {
			for (String value : this.slots) {
				if (value.equals(slot)) {
					return true;
				}
			}
			return false;
		}
		System.err.println("Tried to access slot " + slot + " on type "
				+ this.displayName + " which does not have slots");
		return false;
	}

	/**
	 * Checks if the two objects are equal by comparing their label fields,
	 * since labels are unique identifiers for the GameType
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GameType) {
			if (obj == this) {
				return true;
			}
			final GameType other = (GameType) obj;
			return other.keyword.equals(this.keyword);
		}
		return false;
	}

	/**
	 * Returns the label as a String representation of the GameType
	 */
	@Override
	public String toString() {
		return "GameType [" + this.displayName + "]";
	}

	/**
	 * Returns whether the type has a gui component
	 * 
	 * @return
	 */
	public boolean hasGUI() {
		return this.guiEditorName != null;
	}

	/**
	 * Returns whether the type is enumerated
	 * 
	 * @return
	 */
	public boolean hasEnum() {
		return this.enums != null && !this.enums.isEmpty();
	}
}

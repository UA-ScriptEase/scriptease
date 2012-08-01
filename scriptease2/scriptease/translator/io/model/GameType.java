package scriptease.translator.io.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import scriptease.translator.codegenerator.code.fragments.Fragment;

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

	/**
	 * Enumeration of the possible widgets that may be used to display/edit a
	 * value for a particular type.
	 * 
	 * @author remiller
	 */
	public enum TypeValueWidgets {
		JTEXTFIELD, JSPINNER, JCOMBOBOX;
	}

	private String displayName;
	private String keyword;
	private String regEx;
	private Collection<String> slots;
	private TypeValueWidgets guiEditorName;
	private Map<String, String> enums;
	private Collection<Fragment> format;
	private String codeSymbol;

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
	 * @param gui
	 *            An optional GUI widget specification string to be used . Must
	 *            be one of: <code>JComboBox</code><code>JSpinner</code>
	 *            <code>JTextField</code>
	 */
	public GameType(String name, String keyword, String codeSymbol,
			Collection<Fragment> fragments, Collection<String> slots,
			String enums, String regEx, TypeValueWidgets gui) {

		// Sanity check. If we're not requiring a selection from a list of
		// precisely nothing, that's Very Bad. - remiller
		if (gui != null && gui.equals(TypeValueWidgets.JCOMBOBOX)
				&& (enums == null || enums.trim().equalsIgnoreCase("")))
			throw new IllegalArgumentException(
					"Empty enumeration for game type using Combo Box editor.");

		this.displayName = name;
		this.keyword = keyword;
		this.regEx = regEx;
		this.slots = new ArrayList<String>();
		this.slots.addAll(slots);
		this.guiEditorName = gui;
		this.format = new ArrayList<Fragment>();
		this.enums = this.convertEnumStringToMap(enums);
		this.format.addAll(fragments);
		this.codeSymbol = codeSymbol;
	}

	/**
	 * Converts enums of the format "VALUE1<NAME1>|VALUE2<NAME2>|VALUE3|..." or
	 * even just "VALUE1|VALUE2|..." into a map where value=NAME and key=VALUE
	 * 
	 * @param enums
	 */
	private Map<String, String> convertEnumStringToMap(String enums) {
		Map<String, String> enumsMap = new HashMap<String, String>();
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
			
			if(value.equals(""))
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
		return convertEnumMapToString(this.enums);
	}

	/**
	 * Returns a copy of the GameType's enumerations where key=NAME and
	 * value=VALUE
	 * 
	 * @return
	 */
	public Map<String, String> getEnumMap() {
		return new HashMap<String, String>(this.enums);
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public void setFormat(Collection<Fragment> format) {
		this.format = format;
	}

	public void setCodeSymbol(String codeSymbol) {
		this.codeSymbol = codeSymbol;
	}

	public String getCodeSymbol() {
		return this.codeSymbol;
	}

	public Collection<Fragment> getFormat() {
		return this.format;
	}

	public String getKeyword() {
		return keyword;
	}

	public String getReg() {
		return regEx;
	}

	public void setReg(String reg) {
		this.regEx = reg;
	}

	public Boolean hasReg() {
		if (regEx != null && !regEx.isEmpty())
			return true;
		return false;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Gets the GUI widget used to edit a binding of this type. Can be
	 * <code>null</code>.
	 * 
	 * @return GUI widget used to edit a binding of this type. Can be
	 *         <code>null</code>.
	 */
	public TypeValueWidgets getGui() {
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
	public void setGui(TypeValueWidgets newGuiId) {
		this.guiEditorName = newGuiId;
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
		if (this.slots != null) {
			return new ArrayList<String>(this.slots);
		} else
			return new ArrayList<String>();
	}

	public Boolean hasSlot(String slot) {
		if (slots != null) {
			for (String value : slots) {
				if (value.equals(slot)) {
					return true;
				}
			}
			return false;
		}
		System.err.println("Tried to access slot " + slot + " on type "
				+ displayName + " which does not have slots");
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
			GameType other = (GameType) obj;
			return other.keyword.equals(keyword);
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
		if (guiEditorName != null)
			return true;
		return false;
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

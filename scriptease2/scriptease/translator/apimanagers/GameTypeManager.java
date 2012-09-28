package scriptease.translator.apimanagers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.io.model.GameType;
import scriptease.translator.io.model.GameType.TypeValueWidgets;

/**
 * GameTypeManager maintains a single access point for all the GameTypes using a
 * modified Flyweight design pattern, and singleton.
 * 
 * There must always be at least one type in the gameType set for proper
 * functionality.
 * 
 * It is populated by the first call to buildManager, which should only be done
 * once at initialization.
 * 
 * @author mfchurch
 */
public class GameTypeManager {
	public static final String DEFAULT_VOID_TYPE = "void";
	public static final String DEFAULT_BOOL_TYPE = "question";

	private final Map<String, GameType> gameTypes;
	private final TypeConverter typeConverter;

	public GameTypeManager() {
		this.gameTypes = new HashMap<String, GameType>();
		this.typeConverter = new TypeConverter();
	}

	public Collection<GameType> getGameTypes() {
		return new ArrayList<GameType>(this.gameTypes.values());
	}

	public TypeConverter getTypeConverter() {
		return this.typeConverter;
	}

	public void addGameType(GameType type) {
		this.gameTypes.put(type.getKeyword(), type);
	}

	public Collection<AbstractFragment> getFormat(String keyword) {
		final GameType type = this.gameTypes.get(keyword);
		final Collection<AbstractFragment> format = new ArrayList<AbstractFragment>();
		if (type != null)
			format.addAll(type.getFormat());
		return format;
	}

	/**
	 * Gets a collection containing all of the GameTypeManager's keywords.
	 * 
	 * @return A collection of the GameTypeManager's keyword
	 */
	public Collection<String> getKeywords() {
		return new ArrayList<String>(this.gameTypes.keySet());
	}

	public void clear() {
		this.gameTypes.clear();
	}

	public String getReg(String keyword) {
		final GameType type = this.gameTypes.get(keyword);
		if (type != null) {
			return type.getReg();
		} else
			return "";
	}

	public Map<String, String> getEnumMap(String keyword) {
		final GameType type = this.gameTypes.get(keyword);
		if (type != null) {
			return type.getEnumMap();
		} else
			return new HashMap<String, String>();
	}

	public String getDisplayText(String keyword) {
		final GameType type = this.gameTypes.get(keyword);
		if (type != null) {
			return type.getDisplayName();
		} else
			return "";
	}

	public boolean hasReg(String keyword) {
		final GameType type = this.gameTypes.get(keyword);
		if (type != null) {
			return type.hasReg();
		} else
			return false;
	}

	public Collection<String> getSlots(String keyword) {
		final GameType type = this.gameTypes.get(keyword);
		if (type != null) {
			return type.getSlots();
		} else
			return new ArrayList<String>();
	}

	public String getCodeSymbol(String keyword) {
		final GameType type = this.gameTypes.get(keyword);
		if (type != null) {
			return type.getCodeSymbol();
		} else
			return "";
	}

	/**
	 * Gets the GUI widget that is to be used for editing this type.
	 * 
	 * @param keyword
	 *            the type whose GUI is to be determined.
	 * @return the GUI widget that will edit a component of this type, or
	 *         <code>null</code> if there is no widget specified.
	 */
	public TypeValueWidgets getGui(String keyword) {
		final GameType type = this.gameTypes.get(keyword);

		return type != null ? type.getGui() : null;
	}

	public void addGameTypes(Collection<GameType> types) {
		for (GameType type : types)
			this.addGameType(type);
	}

	@Override
	public String toString() {
		return "GameTypeManager [" + this.gameTypes.keySet() + "]";
	}

	public boolean hasGUI(String keyword) {
		final GameType type = this.gameTypes.get(keyword);
		if (type != null) {
			return type.hasGUI();
		} else
			return false;
	}

	public boolean hasEnum(String keyword) {
		final GameType type = this.gameTypes.get(keyword);
		if (type != null) {
			return type.hasEnum();
		} else
			return false;
	}
}

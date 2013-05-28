package scriptease.model.semodel.librarymodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.io.model.GameType;
import scriptease.translator.io.model.GameType.GUIType;

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
 * GameTypeManagers have a one to one relationship with LibraryModels. This lets
 * us use multiple type managers for a single story.
 * 
 * @author mfchurch
 * @author kschenk
 */
class GameTypeManager {
	private final Map<String, GameType> gameTypes;
	private final TypeConverter typeConverter;

	protected GameTypeManager() {
		this.gameTypes = new HashMap<String, GameType>();
		this.typeConverter = new TypeConverter();
	}

	/**
	 * Returns all of the {@link GameType}s stored by the
	 * {@link GameTypeManager}.
	 * 
	 * @return
	 */
	protected Collection<GameType> getGameTypes() {
		return new ArrayList<GameType>(this.gameTypes.values());
	}

	protected TypeConverter getTypeConverter() {
		return this.typeConverter;
	}

	/**
	 * Returns the format of the type keywords.
	 * 
	 * @param keyword
	 * @return
	 */
	protected Collection<AbstractFragment> getTypeFormat(String keyword) {
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
	protected Collection<String> getTypeKeywords() {
		return new ArrayList<String>(this.gameTypes.keySet());
	}

	/**
	 * Returns the regex of the type.
	 * 
	 * @param keyword
	 * @return
	 */
	protected String getTypeRegex(String keyword) {
		final GameType type = this.gameTypes.get(keyword);
		if (type != null) {
			return type.getReg();
		} else
			return "";
	}

	/**
	 * Returns the enumerated values of the type.
	 * 
	 * @param keyword
	 * @return
	 */
	protected Map<String, String> getTypeEnumeratedValues(String keyword) {
		final GameType type = this.gameTypes.get(keyword);
		if (type != null) {
			return type.getEnumMap();
		} else
			return new HashMap<String, String>();
	}

	protected String getDisplayText(String keyword) {
		final GameType type = this.gameTypes.get(keyword);
		if (type != null) {
			return type.getDisplayName();
		} else
			return "";
	}

	protected Collection<String> getSlots(String keyword) {
		final GameType type = this.gameTypes.get(keyword);
		if (type != null) {
			return type.getSlots();
		} else
			return new ArrayList<String>();
	}

	protected String getCodeSymbol(String keyword) {
		final GameType type = this.gameTypes.get(keyword);
		if (type != null) {
			return type.getCodeSymbol();
		} else
			return "";
	}

	protected Map<String, String> getEscapes(String keyword) {
		final GameType type = this.gameTypes.get(keyword);
		if (type != null) {
			return type.getEscapes();
		} else {
			return new HashMap<String, String>(0);
		}
	}

	/**
	 * Gets the GUI widget that is to be used for editing this type.
	 * 
	 * @param keyword
	 *            the type whose GUI is to be determined.
	 * @return the GUI widget that will edit a component of this type, or
	 *         <code>null</code> if there is no widget specified.
	 */
	protected GUIType getGui(String keyword) {
		final GameType type = this.gameTypes.get(keyword);

		if (type == null)
			return null;
		else
			return type.getGui();
	}

	protected void addGameTypes(Collection<GameType> types) {
		for (GameType type : types)
			this.gameTypes.put(type.getKeyword(), type);
	}

	/**
	 * Gets the widget name that is to be used for displaying the type widget.
	 * 
	 * @param keyword
	 *            the type whose widget name is to be determined.
	 */
	protected String getWidgetName(String keyword) {
		final GameType type = this.gameTypes.get(keyword);
		if (type != null)
			return type.getWidgetName();
		else
			return null;
	}

	@Override
	public String toString() {
		return "GameTypeManager [" + this.gameTypes.keySet() + "]";
	}
}

package scriptease.translator.io.tools;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.translator.io.model.GameConstant;

/**
 * Factory used to facilitate the creation of GameConstant types.
 * 
 * @author mfchurch Implements Factory Design Pattern
 */
public class GameConstantFactory {
	private static GameConstantFactory instance;

	public static GameConstantFactory getInstance() {
		if (GameConstantFactory.instance != null)
			return GameConstantFactory.instance;
		GameConstantFactory.instance = new GameConstantFactory();
		return GameConstantFactory.instance;
	}

	/**
	 * Shortcut for getConstant with a single type
	 * 
	 * @param type
	 * @param resolutionCode
	 * @return
	 */
	public GameConstant getConstant(String type, String resolutionCode) {
		ArrayList<String> types = new ArrayList<String>(1);
		types.add(type);
		return this.getConstant(types, resolutionCode);
	}

	public GameConstant getConstant(final Collection<String> types,
			final String resolutionCode) {
		if (resolutionCode == null)
			throw new IllegalArgumentException(
					"Cannot create a GameConstant with a null resolutionCode");

		return new SimpleGameConstant(types, resolutionCode);
	}

	public GameConstant getTypedBlankConstant(final String type) {
		return this.getConstant(type, "");
	}
}

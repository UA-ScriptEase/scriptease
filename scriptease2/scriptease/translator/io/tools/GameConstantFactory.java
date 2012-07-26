package scriptease.translator.io.tools;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.model.IdentifiableGameConstant;

/**
 * Factory used to facilitate the creation of GameConstant types.
 * 
 * @author mfchurch Implements Factory Design Pattern
 */
public class GameConstantFactory {
	private static GameConstantFactory instance;

	public static GameConstantFactory getInstance() {
		if (instance != null)
			return instance;
		else {
			instance = new GameConstantFactory();
			return instance;
		}
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
		return this.getConstant(types, resolutionCode, null);
	}

	public GameConstant getConstant(final Collection<String> types,
			final String resolutionCode, final String resref) {
		if (resolutionCode == null)
			throw new IllegalArgumentException(
					"Cannot create a GameConstant with a null resolutionCode");

		final GameConstant newConstant;
		if (resref != null && !resref.isEmpty()) {
			newConstant = new IdentifiableGameConstant() {
				@Override
				public String getResolutionText() {
					return resolutionCode;
				}

				@Override
				public String getName() {
					return getTemplateID();
				}

				@Override
				public Collection<String> getTypes() {
					return types;
				}

				@Override
				public String toString() {
					return getName();
				}

				@Override
				public String getTemplateID() {
					return resref;
				}

				@Override
				public String getTag() {
					return resref;
				}
			};
		} else {
			newConstant = new SimpleGameConstant(types, resolutionCode);
		}
		return newConstant;

	}

	public GameConstant getTypedBlankConstant(final String type) {
		return this.getConstant(type, "");
	}
}

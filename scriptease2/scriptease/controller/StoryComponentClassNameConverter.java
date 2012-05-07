package scriptease.controller;

import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryItemSequence;

/**
 * Collection of methods for coverting between StoryComponent class names and
 * their class files (used in FileIO). This class should be maintained to
 * properly reflect all StoryComponent classes in ScriptEase.
 * 
 * @author mfchurch
 * 
 */
public class StoryComponentClassNameConverter {

	public static Class<? extends StoryComponent> convertToModelClass(
			String name) {

		if (name.equals("ScriptIt")) {
			return ScriptIt.class;
		} else if (name.equals("KnowIt")) {
			return KnowIt.class;
		} else if (name.equals("StoryComponentContainer")) {
			return StoryComponentContainer.class;
		} else if (name.equals("AskIt")) {
			return AskIt.class;
		} else if (name.equals("StoryItemSequence")) {
			return StoryItemSequence.class;
		} else
			throw new IllegalStateException("Unable to convert between Name ["
					+ name + "] and a valid model class");
	}

	public static String getClassName(Class<? extends StoryComponent> aClass) {
		return aClass.getSimpleName();
	}
}

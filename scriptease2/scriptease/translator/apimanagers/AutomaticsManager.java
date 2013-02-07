package scriptease.translator.apimanagers;

import java.util.Collection;

import scriptease.model.LibraryModel;
import scriptease.model.StoryModel;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.translator.Translator;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.Resource;

/**
 * AutomaticsManager provides functionality for resolving the binding of module
 * specific automatic parameters.
 * 
 * @author mfchurch
 * 
 */
public class AutomaticsManager {
	// Special types that can automatically bound if provided by the GameModule
	// implementation
	public static final String MODULE_TYPE = "module";
	public static final String AUTOMATIC_LABEL = "automatic";

	public AutomaticsManager() {
	}

	/**
	 * Adds all the automatic ScriptIts for the given translator to the given model
	 * 
	 * @param translator
	 * @param model
	 */
	public void resolveAndAddAutomatics(final GameModule gameModule, Translator translator, final StoryModel model) {
		final Collection<LibraryModel> libraries = translator.getLibraries();
		for (LibraryModel library : libraries) {
			final Collection<ScriptIt> automatics = library
					.getAutomatics();
			for (ScriptIt automatic : automatics) {
					ScriptIt copy = automatic.clone();
					resolveAutomaticParameters(copy, gameModule);
					model.getRoot().addStoryChild(copy);
			}
		}
	}

	/**
	 * Resolves the given automatic StoryComponent's parameters for the given
	 * gameModule
	 * 
	 * @param automatic
	 * @param gameModule
	 */
	private void resolveAutomaticParameters(ScriptIt automatic,
			GameModule gameModule) {
		final Collection<KnowIt> parameters = automatic.getParameters();
		for (KnowIt parameter : parameters) {
			final Collection<String> types = parameter.getTypes();
			for (String type : types) {
				// TODO iterate all special types supported
				if (type.equalsIgnoreCase(MODULE_TYPE)) {
					final Resource module = gameModule.getModule();
					parameter.setBinding(module);
				}
			}
		}
	}
}

package scriptease.controller.io.converter.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import scriptease.ScriptEase;
import scriptease.controller.io.XMLNode;
import scriptease.gui.WindowFactory;
import scriptease.model.complex.StoryPoint;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.io.model.GameModule;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class StoryModelConverter implements Converter {
	public static StoryModel currentStory = null;

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(StoryModel.class);
	}

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final StoryModel model = (StoryModel) source;
		final Collection<String> libraryNames = new ArrayList<String>();
		final String modulePath;

		modulePath = model.getModule().getLocation().getAbsolutePath();

		for (LibraryModel library : model.getOptionalLibraries()) {
			libraryNames.add(library.getTitle());
		}

		XMLNode.TITLE.write(writer, model.getTitle());
		XMLNode.AUTHOR.write(writer, model.getAuthor());
		XMLNode.VERSION.write(writer, model.getCompatibleVersion());
		XMLNode.TRANSLATOR.write(writer, model.getTranslator().getName());
		XMLNode.OPTIONAL_LIBRARIES.writeChildren(writer, libraryNames);
		XMLNode.GAME_MODULE.write(writer, modulePath);
		XMLNode.START_STORY_POINT.write(writer, context, model.getRoot());
		XMLNode.DIALOGUES.write(writer, context, model.getDialogueRoots());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final String SE_VERSION = ScriptEase.getInstance().getVersion();

		final StoryModel model;

		final String title;
		final String author;
		final String version;
		final String translatorName;
		final Collection<String> libraryNames;
		final String modulePath;

		final Collection<LibraryModel> optionalLibraries;
		final Collection<DialogueLine> lines;

		final Translator translator;
		final GameModule module;
		final StoryPoint newRoot;

		title = XMLNode.TITLE.read(reader);
		author = XMLNode.AUTHOR.read(reader);
		version = XMLNode.VERSION.read(reader);
		translatorName = XMLNode.TRANSLATOR.read(reader);
		libraryNames = XMLNode.OPTIONAL_LIBRARIES.readChildren(reader);
		modulePath = XMLNode.GAME_MODULE.read(reader);

		optionalLibraries = new ArrayList<LibraryModel>();

		translator = TranslatorManager.getInstance().getTranslator(
				translatorName);

		// Make sure the .ses file and the current ScriptEase version are
		// compatible
		if (!version.equals(SE_VERSION)
				&& !SE_VERSION.equals(ScriptEase.NO_VERSION_INFORMATION)) {
			WindowFactory
					.getInstance()
					.showProblemDialog(
							"Incompatible ScriptEase version (2." + SE_VERSION
									+ ")",
							"The story file being loaded is incompatible "
									+ "with the current ScriptEase version. \n\n"
									+ "Your story file was created in ScriptEase 2."
									+ version
									+ ".\nThe current ScriptEase version you are using is 2."
									+ SE_VERSION
									+ ".\nYou will need to use an earlier compatible "
									+ "ScriptEase version.");

			return null;
		} else if (translator == null)
			throw new IllegalStateException("Translator could not be found.");

		// Load the Translator
		TranslatorManager.getInstance().setActiveTranslator(translator);

		for (String libraryName : libraryNames) {
			final LibraryModel library = translator.findLibrary(libraryName);

			if (library == null) {
				System.err.println("Could not find optional library "
						+ libraryName + " for " + translator.getName());

				WindowFactory.getInstance().showWarningDialog(
						"Library Not Found",
						"The library with the name " + libraryName
								+ " was not found in the "
								+ translator.getName()
								+ " translator's optional library directory. "
								+ "\nPlease add the library and reload "
								+ "the story, or save the story to "
								+ "remove the library from it.");
			} else {
				System.out.println("Loaded library " + libraryName);
				optionalLibraries.add(library);
			}
		}

		// Try to open the Pattern Model
		module = translator.loadModule(new File(modulePath));

		if (module == null) {
			if (!SEModelManager.getInstance().usingTranslator(translator))
				translator.unloadTranslator();

			TranslatorManager.getInstance().setActiveTranslator(null);
			throw new IllegalStateException("Game module could not be loaded.");
		}

		model = new StoryModel(module, title, author, version, translator,
				optionalLibraries);

		currentStory = model;

		// Story points rely on the current story being set, so we need to load
		// them later.
		newRoot = XMLNode.START_STORY_POINT.read(reader, context, model,
				StoryPoint.class);

		if (newRoot == null)
			throw new IllegalStateException("Model root could not be loaded.");

		model.setRoot(newRoot);

		lines = XMLNode.DIALOGUES.readChildren(reader, context, model,
				DialogueLine.class);

		model.addDialogueRoots(lines);

		// reset these to free the memory.
		currentStory = null;

		return model;
	}
}

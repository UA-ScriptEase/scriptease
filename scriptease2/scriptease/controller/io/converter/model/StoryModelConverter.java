package scriptease.controller.io.converter.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import scriptease.ScriptEase;
import scriptease.controller.io.XMLAttribute;
import scriptease.controller.io.XMLNode;
import scriptease.gui.WindowFactory;
import scriptease.model.complex.StoryPoint;
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

/**
 * Converts story models to and from XML.
 * 
 * @author previous devs
 * @author kschenk
 * 
 */
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

		LibraryModelConverter.currentLibrary = model.getTranslator()
				.getLibrary();

		final Collection<String> libraryNames = new ArrayList<String>();
		final String modulePath;

		modulePath = model.getModule().getLocation().getAbsolutePath();

		for (LibraryModel library : model.getOptionalLibraries()) {
			libraryNames.add(library.getTitle());
		}

		XMLAttribute.NAME.write(writer, model.getTitle());
		XMLAttribute.AUTHOR.write(writer, model.getAuthor());
		XMLAttribute.DESCRIPTION.write(writer, model.getDescription());
		XMLNode.VERSION.writeString(writer, model.getCompatibleVersion());
		XMLNode.TRANSLATOR.writeString(writer, model.getTranslator().getName());
		XMLNode.OPTIONAL_LIBRARIES.writeChildren(writer, libraryNames);
		XMLNode.GAME_MODULE.writeString(writer, modulePath);
		XMLNode.DIALOGUES
				.writeObject(writer, context, model.getDialogueRoots());
		XMLNode.START_STORY_POINT.writeObject(writer, context, model.getRoot());

		LibraryModelConverter.currentLibrary = null;
	}

	@Override
	public Object unmarshal(final HierarchicalStreamReader reader,
			final UnmarshallingContext context) {
		final String READ_STORY = "Reading Story...";
		final String SE_VERSION = ScriptEase.getInstance().getVersion();

		final StoryModel model;

		final String title;
		final String author;
		final String version;
		final String description;
		final String translatorName;
		final Collection<String> libraryNames;
		final String modulePath;

		final Collection<LibraryModel> optionalLibraries;
		final Collection<DialogueLine> lines;

		final Translator translator;
		final GameModule module;

		title = XMLAttribute.NAME.read(reader);
		author = XMLAttribute.AUTHOR.read(reader);
		description = XMLAttribute.DESCRIPTION.read(reader);
		version = XMLNode.VERSION.readString(reader);
		translatorName = XMLNode.TRANSLATOR.readString(reader);
		libraryNames = XMLNode.OPTIONAL_LIBRARIES.readStringCollection(reader);
		modulePath = XMLNode.GAME_MODULE.readString(reader);

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
							"Incompatible ScriptEase version (" + SE_VERSION
									+ ")",
							"The story file being loaded is incompatible "
									+ "with the current ScriptEase version. \n\n"
									+ "Your story file was created in ScriptEase "
									+ version
									+ ".\nThe current ScriptEase version you are using is "
									+ SE_VERSION
									+ ".\nYou will need to use an earlier compatible "
									+ "ScriptEase version.");

			return null;
		} else if (translator == null)
			throw new IllegalStateException("Translator could not be found.");

		LibraryModelConverter.currentLibrary = translator.getLibrary();

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
			TranslatorManager.getInstance().setActiveTranslator(null);
			throw new IllegalStateException("Game module could not be loaded.");
		}

		model = new StoryModel(module, title, author, description, version,
				translator, optionalLibraries);

		// Story points rely on the current story being set, so we need to load
		// them after assigning the story to the static variable.
		currentStory = model;

		lines = XMLNode.DIALOGUES.readCollection(reader, context,
				DialogueLine.class);

		model.addDialogueRoots(lines);

		WindowFactory.showProgressBar(READ_STORY, new Runnable() {
			@Override
			public void run() {
				model.setRoot(XMLNode.START_STORY_POINT.readObject(reader,
						context, StoryPoint.class));
			}
		});

		// reset these to free the memory.
		currentStory = null;
		LibraryModelConverter.currentLibrary = null;

		return model;
	}
}

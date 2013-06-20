package scriptease.controller.io.converter.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import scriptease.ScriptEase;
import scriptease.controller.io.FileIO;
import scriptease.gui.WindowFactory;
import scriptease.model.complex.StoryPoint;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.io.model.GameModule;

import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class StoryModelConverter implements Converter {
	private static final String TAG_TITLE = "Title";
	private static final String TAG_AUTHOR = "Author";
	private static final String TAG_VERSION = "Version";
	private static final String TAG_OPTIONAL_LIBRARIES = "OptionalLibraries";
	private static final String TAG_OPTIONAL_LIBRARY = "OptionalLibrary";
	private static final String TAG_STORY_START_POINT = "StartStoryPoint";
	private static final String TAG_TRANSLATOR = "Translator";
	private static final String TAG_GAME_MODULE = "GameModule";

	public static GameModule currentModule = null;

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) {
		return type.equals(StoryModel.class);
	}

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		final StoryModel model = (StoryModel) source;

		writer.startNode(TAG_TITLE);
		writer.setValue(model.getTitle());
		writer.endNode();

		writer.startNode(TAG_AUTHOR);
		writer.setValue(model.getAuthor());
		writer.endNode();

		writer.startNode(TAG_VERSION);
		writer.setValue(model.getCompatibleVersion());
		writer.endNode();

		writer.startNode(TAG_TRANSLATOR);
		writer.setValue(model.getTranslator().getName());
		writer.endNode();

		writer.startNode(TAG_OPTIONAL_LIBRARIES);
		for (LibraryModel library : model.getOptionalLibraries()) {
			writer.startNode(TAG_OPTIONAL_LIBRARY);
			writer.setValue(library.getTitle());
			writer.endNode();
		}
		writer.endNode();

		writer.startNode(TAG_GAME_MODULE);
		writer.setValue(model.getModule().getLocation().getAbsolutePath());
		writer.endNode();

		// write out the story's pattern instances
		writer.startNode(TAG_STORY_START_POINT);
		context.convertAnother(model.getRoot());
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		final StoryModel model;
		final String title;
		final String author;
		final String version;
		final Translator translator;
		final GameModule module;
		final StoryPoint newRoot;
		final Collection<String> libraryNames;
		final Collection<LibraryModel> optionalLibraries;

		title = FileIO.readValue(reader, TAG_TITLE);
		author = FileIO.readValue(reader, TAG_AUTHOR);

		version = FileIO.readValue(reader, TAG_VERSION);

		// Make sure the .ses file and the current ScriptEase version is
		// compatible
		final String ScriptEaseVersion = ScriptEase.getInstance().getVersion();

		if (!version.equals(ScriptEaseVersion)
				&& !ScriptEaseVersion.equals(ScriptEase.NO_VERSION_INFORMATION)) {
			WindowFactory
					.getInstance()
					.showProblemDialog(
							"Incompatible ScriptEase version (2."
									+ ScriptEaseVersion + ")",
							"The story file being loaded is incompatible "
									+ "with the current ScriptEase version. \n\n"
									+ "Your story file was created in ScriptEase 2."
									+ version
									+ ".\nThe current ScriptEase version you are using is 2."
									+ ScriptEaseVersion
									+ ".\nYou will need to use an earlier compatible "
									+ "ScriptEase version.");

			return null;
		}

		translator = TranslatorManager.getInstance().getTranslator(
				FileIO.readValue(reader, TAG_TRANSLATOR));

		if (translator == null)
			throw new IllegalStateException("Translator could not be found.");

		// Load the Translator
		TranslatorManager.getInstance().setActiveTranslator(translator);
		System.out.println(translator + " loaded");

		libraryNames = FileIO.readValues(reader, TAG_OPTIONAL_LIBRARIES,
				TAG_OPTIONAL_LIBRARY);

		optionalLibraries = new ArrayList<LibraryModel>();

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

		final String modulePath = FileIO.readValue(reader, TAG_GAME_MODULE);
		// Try to open the Pattern Model
		module = translator.loadModule(new File(modulePath));

		if (module == null) {
			if (!SEModelManager.getInstance().usingTranslator(translator))
				translator.unloadTranslator();
			TranslatorManager.getInstance().setActiveTranslator(null);

			throw new XStreamException("Game module could not be loaded.");
		}
		System.out.println(module + " loaded");

		currentModule = module;

		model = new StoryModel(module, title, author, version, translator,
				optionalLibraries);

		reader.moveDown();

		newRoot = (StoryPoint) context.convertAnother(model, StoryPoint.class);

		if (newRoot == null)
			throw new IllegalStateException("Model root could not be loaded.");

		model.setRoot(newRoot);
		System.out.println(model + " loaded");

		reader.moveUp();
		// reset these to free the memory.
		currentModule = null;
		return model;
	}
}

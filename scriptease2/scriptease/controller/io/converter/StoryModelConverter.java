 package scriptease.controller.io.converter;

import java.io.File;

import scriptease.controller.io.FileIO;
import scriptease.gui.quests.QuestNode;
import scriptease.model.StoryModel;
import scriptease.model.PatternModelPool;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.io.model.GameModule;

import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class StoryModelConverter implements Converter{
	private static final String TAG_TITLE = "Title";
	private static final String TAG_AUTHOR = "Author";
	private static final String TAG_ROOT = "StoryRoot";
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

		writer.startNode(TAG_TRANSLATOR);
		writer.setValue(model.getTranslator().getName());
		writer.endNode();

		writer.startNode(TAG_GAME_MODULE);
		writer.setValue(model.getModule().getLocation().getAbsolutePath());
		writer.endNode();

		// write out the story's pattern instances
		writer.startNode(TAG_ROOT);
		context.convertAnother(model.getRoot());
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		StoryModel model = null;
		final String title;
		final String author;
		final Translator translator;
		final GameModule module;
		final QuestNode newRoot;

		title = FileIO.readValue(reader, TAG_TITLE);

		author = FileIO.readValue(reader, TAG_AUTHOR);

		translator = TranslatorManager.getInstance().getTranslator(
				FileIO.readValue(reader, TAG_TRANSLATOR));
		if (translator == null)
			throw new IllegalStateException(
					"Translator could not be found.");

		// Try to open the Pattern Model
		try {
			// Load the Translator
			TranslatorManager.getInstance().setActiveTranslator(translator);
			System.out.println(translator + " loaded");

			module = translator.loadModule(new File(FileIO.readValue(
					reader, TAG_GAME_MODULE)));

			if (module == null)
				throw new XStreamException(
						"Game module could not be loaded.");

			System.out.println(module + " loaded");
			currentModule = module;

			model = new StoryModel(module, title, author, translator);

			reader.moveDown();
			
			newRoot = (QuestNode) context.convertAnother(
					model, QuestNode.class);
			if (newRoot == null)
				throw new IllegalStateException(
						"Model root could not be loaded.");

			model.setRoot(newRoot);

			System.out.println(model + " loaded");

			reader.moveUp();
		}
		// If the Pattern Model failed to load
		catch (XStreamException e) {
			model = null;
			// Unload the translator if not being used.
			if (!PatternModelPool.getInstance().usingTranslator(translator))
				translator.unLoadTranslator();
			TranslatorManager.getInstance().setActiveTranslator(null);
			// Pass it back up
			throw e;
		}
		// reset these to free the memory.
		currentModule = null;
		return model;
	}
}

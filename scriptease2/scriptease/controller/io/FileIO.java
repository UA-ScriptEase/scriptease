package scriptease.controller.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import scriptease.controller.io.converter.APIDictionaryConverter;
import scriptease.controller.io.converter.AskItConverter;
import scriptease.controller.io.converter.CodeBlockConverter;
import scriptease.controller.io.converter.DescribeItConverter;
import scriptease.controller.io.converter.GameMapConverter;
import scriptease.controller.io.converter.GameTypeConverter;
import scriptease.controller.io.converter.KnowItBindingConverter;
import scriptease.controller.io.converter.KnowItConverter;
import scriptease.controller.io.converter.LanguageDictionaryConverter;
import scriptease.controller.io.converter.LibraryModelConverter;
import scriptease.controller.io.converter.ScriptItConverter;
import scriptease.controller.io.converter.SlotConverter;
import scriptease.controller.io.converter.StoryComponentContainerConverter;
import scriptease.controller.io.converter.StoryItemSequenceConverter;
import scriptease.controller.io.converter.StoryModelConverter;
import scriptease.controller.io.converter.fragment.FormatIDFragmentConverter;
import scriptease.controller.io.converter.fragment.IndentedFragmentConverter;
import scriptease.controller.io.converter.fragment.LineFragmentConverter;
import scriptease.controller.io.converter.fragment.LiteralFragmentConverter;
import scriptease.controller.io.converter.fragment.MapRefFragmentConverter;
import scriptease.controller.io.converter.fragment.ReferenceFragmentConverter;
import scriptease.controller.io.converter.fragment.ScopeFragmentConverter;
import scriptease.controller.io.converter.fragment.SeriesFragmentConverter;
import scriptease.controller.io.converter.fragment.SimpleFragmentConverter;
import scriptease.controller.io.converter.graphnode.KnowItNodeConverter;
import scriptease.controller.io.converter.graphnode.TextNodeConverter;
import scriptease.gui.WindowManager;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.graph.nodes.KnowItNode;
import scriptease.gui.graph.nodes.TextNode;
import scriptease.gui.quests.QuestNode;
import scriptease.gui.quests.QuestNodeConverter;
import scriptease.gui.quests.QuestPoint;
import scriptease.gui.quests.QuestPointConverter;
import scriptease.gui.quests.QuestPointNode;
import scriptease.gui.quests.QuestPointNodeConverter;
import scriptease.model.CodeBlock;
import scriptease.model.LibraryModel;
import scriptease.model.PatternModel;
import scriptease.model.StoryModel;
import scriptease.model.atomic.DescribeIt;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryItemSequence;
import scriptease.translator.APIDictionary;
import scriptease.translator.LanguageDictionary;
import scriptease.translator.codegenerator.code.fragments.FormatIDFragment;
import scriptease.translator.codegenerator.code.fragments.IndentedFragment;
import scriptease.translator.codegenerator.code.fragments.LineFragment;
import scriptease.translator.codegenerator.code.fragments.LiteralFragment;
import scriptease.translator.codegenerator.code.fragments.MapRefFragment;
import scriptease.translator.codegenerator.code.fragments.ReferenceFragment;
import scriptease.translator.codegenerator.code.fragments.ScopeFragment;
import scriptease.translator.codegenerator.code.fragments.SimpleFragment;
import scriptease.translator.codegenerator.code.fragments.series.SeriesFragment;
import scriptease.translator.codegenerator.code.fragments.series.UniqueSeriesFragment;
import scriptease.translator.io.model.GameMap;
import scriptease.translator.io.model.GameType;
import scriptease.translator.io.model.Slot;
import scriptease.util.FileOp;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * Reads and writes Stories, Libraries and Dictionaries via various sources on
 * disk using XStream. Singleton so that it can watch for infinite looping on
 * load.
 * 
 * @author remiller
 * @author mfchurch
 */
public class FileIO {
	public enum IoMode {
		API_DICTIONARY, LANGUAGE_DICTIONARY, STORY, NONE;
	}

	private static FileIO instance;

	public static FileIO getInstance() {
		if (instance == null)
			instance = new FileIO();
		return instance;
	}

	private IoMode mode;

	/**
	 * Reads a Story from disk.
	 * 
	 * @param location
	 *            The file to read from
	 * @return The read-in Story.
	 */
	public StoryModel readStory(File location) {
		return (StoryModel) this.readData(location);
	}

	/**
	 * Reads a Patterns Library from disk.
	 * 
	 * @param location
	 *            The file to read from
	 * @return The read-in Library.
	 */
	public LibraryModel readLibrary(File location) {
		return (LibraryModel) this.readData(location);
	}

	/**
	 * Writes an API Dictionary to disk at the given location.
	 * 
	 * @param dictionary
	 *            The dictionary to write.
	 * @param location
	 *            The file path to write to.
	 */
	public void writeAPIDictionary(APIDictionary dictionary, File location) {
		this.mode = IoMode.API_DICTIONARY;
		this.writeData(dictionary, location);
		this.mode = IoMode.NONE;
	}

	/**
	 * Read the API dictionary file from the given location.
	 * 
	 * @param location
	 *            The file path to read from.
	 * @return The API dictionary as read from disk.
	 */
	public APIDictionary readAPIDictionary(File location) {
		if (this.mode == IoMode.API_DICTIONARY)
			throw new IllegalStateException(
					"Loop detected in APIDictionary Loading");

		this.mode = IoMode.API_DICTIONARY;
		APIDictionary apiDictionary = (APIDictionary) this.readData(location);
		this.mode = IoMode.NONE;

		return apiDictionary;
	}

	/**
	 * Read the language dictionary file from the given location.
	 * 
	 * @param location
	 *            The file path to read from.
	 * @return The language dictionary as read from disk.
	 */
	public LanguageDictionary readLanguageDictionary(File location) {
		if (this.mode == IoMode.LANGUAGE_DICTIONARY)
			throw new IllegalStateException(
					"Loop detected in LanguageDictionary Loading");

		this.mode = IoMode.LANGUAGE_DICTIONARY;
		LanguageDictionary languageDict = (LanguageDictionary) this
				.readData(location);
		this.mode = IoMode.NONE;

		return languageDict;
	}

	/**
	 * Writes the given PatternModel as XML to the given location;
	 * 
	 * @param model
	 *            The pattern model to save to disk.
	 * @param location
	 *            the location to save to.
	 */
	public void writePatternModel(PatternModel model, File location) {
		this.writeData(model, location);
	}

	private void writeData(Object dataModel, File location) {
		final File backupLocation;
		FileOutputStream fileOut = null;
		final XStream stream = this.buildXStream();

		backupLocation = FileOp.replaceExtension(location,
				FileOp.getExtension(location) + "_backup");

		// Create/empty the file we're saving to,
		try {
			if ((backupLocation.exists() && !backupLocation.delete())
					|| !location.renameTo(backupLocation))
				System.err.println("Failed to create a backup file for "
						+ backupLocation + "!");

			location.createNewFile();
		} catch (IOException e) {
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(
					Thread.currentThread(), e);
		}

		// save the ScriptEase patterns.
		try {
			fileOut = new FileOutputStream(location);
			stream.toXML(dataModel, fileOut);
		} catch (IOException e) {
			System.err.println("Patterns save problem: Could not access "
					+ location.getAbsolutePath());

			boolean retry = WindowManager.getInstance().showRetryProblemDialog(
					"Save Patterns File",
					"ScriptEase was unable to access "
							+ location.getAbsolutePath());

			if (retry)
				this.writeData(dataModel, location);
		} finally {
			try {
				if (fileOut != null)
					fileOut.close();
			} catch (IOException e2) {
				System.err
						.println("Failed to close an output file connection.");
			}
		}
	}

	private Object readData(File location) {
		InputStream fileIn = null;
		Object dataModel = null;
		final XStream stream = this.buildXStream();
		boolean retry;

		try {
			System.err.println("Creating input stream for file: " + location);
			fileIn = new FileInputStream(location);
			System.err.println("Input Stream: " + fileIn
					+ " created for file: " + location);

			dataModel = stream.fromXML(fileIn);
		} catch (IOException e) {
			System.err.println("Failed to read file "
					+ location.getAbsolutePath());
			e.printStackTrace();

			retry = WindowManager.getInstance().showRetryProblemDialog(
					"Problem Reading",
					"ScriptEase was unable to read from "
							+ location.getAbsolutePath());

			if (retry)
				this.readData(location);
		} catch (XStreamException e) {
			System.err
					.println("An error occured while loading "
							+ location.getAbsolutePath()
							+ ". It might be XStream complaining about a malformatted file, or some other error occured incidentally while loading.");
			e.printStackTrace();
		} finally {
			try {
				if (fileIn != null)
					fileIn.close();
			} catch (IOException e) {
				System.err.println("Failed to close an input file connection.");
			}
		}

		return dataModel;
	}

	/**
	 * Configures the XStream to be using our custom converters and to
	 * understand our non-default tags.
	 * 
	 * @param stream
	 *            the stream to configure.
	 */
	private XStream buildXStream() {
		final XStream stream = new XStream();

		stream.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);

		// Alias these tags so that it does not care about the packages,
		// plus it makes the XML nicer.
		stream.alias("Story", StoryModel.class);
		stream.alias("Library", LibraryModel.class);
		stream.alias("Root", StoryComponentContainer.class);
		stream.alias("KnowIt", KnowIt.class);
		stream.alias("StoryComponentContainer", StoryComponentContainer.class);
		stream.alias("AskIt", AskIt.class);
		stream.alias("StoryItemSequence", StoryItemSequence.class);
		stream.alias("KnowItBinding", KnowItBinding.class);
		stream.alias("Type", GameType.class);
		stream.alias("Slot", Slot.class);
		stream.alias("Binding", KnowItBinding.class);
		stream.alias("Value", String.class);

		// Language Dictionary Fragments
		stream.alias("APIDictionary", APIDictionary.class);
		stream.alias("LanguageDictionary", LanguageDictionary.class);
		stream.alias("Map", GameMap.class);
		stream.alias("Format", FormatIDFragment.class);
		stream.alias("Indent", IndentedFragment.class);
		stream.alias("Line", LineFragment.class);
		stream.alias("Literal", LiteralFragment.class);
		stream.alias("MapRef", MapRefFragment.class);
		stream.alias("FormatRef", ReferenceFragment.class);
		stream.alias("Scope", ScopeFragment.class);
		stream.alias("Series", SeriesFragment.class);
		stream.alias("Series", UniqueSeriesFragment.class);
		stream.alias("Fragment", SimpleFragment.class);
		stream.alias("DescribeIt", DescribeIt.class);
		stream.alias("GraphNode", GraphNode.class);
		stream.alias("TextNode", TextNode.class);
		stream.alias("KnowItNode", KnowItNode.class);
		stream.alias("CodeBlock", CodeBlock.class);
		stream.alias("ScriptIt", ScriptIt.class);
		stream.alias("QuestNode", QuestNode.class);
		stream.alias("QuestPoint", QuestPoint.class);
		stream.alias("QuestPointNode", QuestPointNode.class);
		

		// the below are aliased for backwards compatibility

		/* <Insert backwards-compatible aliases here> */

		// now register all of the leaf-level converters
		stream.registerConverter(new StoryModelConverter());
		stream.registerConverter(new LibraryModelConverter());
		stream.registerConverter(new StoryComponentContainerConverter());
		stream.registerConverter(new KnowItConverter());
		stream.registerConverter(new AskItConverter());
		stream.registerConverter(new StoryItemSequenceConverter());
		stream.registerConverter(new KnowItBindingConverter());
		stream.registerConverter(new GameTypeConverter());
		stream.registerConverter(new SlotConverter());
		stream.registerConverter(new FormatIDFragmentConverter());
		stream.registerConverter(new IndentedFragmentConverter());
		stream.registerConverter(new LineFragmentConverter());
		stream.registerConverter(new LiteralFragmentConverter());
		stream.registerConverter(new MapRefFragmentConverter());
		stream.registerConverter(new ReferenceFragmentConverter());
		stream.registerConverter(new ScopeFragmentConverter());
		stream.registerConverter(new SeriesFragmentConverter());
		stream.registerConverter(new SimpleFragmentConverter());
		stream.registerConverter(new APIDictionaryConverter());
		stream.registerConverter(new LanguageDictionaryConverter());
		stream.registerConverter(new GameMapConverter());
		stream.registerConverter(new DescribeItConverter());
		stream.registerConverter(new TextNodeConverter());
		stream.registerConverter(new KnowItNodeConverter());
		stream.registerConverter(new CodeBlockConverter());
		stream.registerConverter(new ScriptItConverter());
		stream.registerConverter(new QuestPointConverter());
		stream.registerConverter(new QuestNodeConverter()); 
		stream.registerConverter(new QuestPointNodeConverter()); 

		return stream;
	}

	/**
	 * Reads a simple text value from the reader, provided that the XML element
	 * that we're reading from actually has the correct tag (ignoring case).
	 * 
	 * @param reader
	 *            the reader to read from.
	 * @param expectedTag
	 *            The tag that we expect to see.
	 * @return the value read in from the reader.
	 */
	public static String readValue(HierarchicalStreamReader reader,
			String expectedTag) {
		final String value;
		final String tag;

		reader.moveDown();
		tag = reader.getNodeName();

		if (!tag.equalsIgnoreCase(expectedTag))
			throw new ConversionException(
					"XML element was not as expected. Expected " + expectedTag
							+ ", but received " + tag);

		value = reader.getValue();
		reader.moveUp();

		return value;
	}

	/**
	 * Gets the current read/write mode for the pattern io class.
	 * 
	 * @return
	 */
	public IoMode getMode() {
		return this.mode;
	}
}

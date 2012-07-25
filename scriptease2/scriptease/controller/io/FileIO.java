package scriptease.controller.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.controller.io.converter.APIDictionaryConverter;
import scriptease.controller.io.converter.AskItConverter;
import scriptease.controller.io.converter.CodeBlockReferenceConverter;
import scriptease.controller.io.converter.CodeBlockSourceConverter;
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
import scriptease.model.CodeBlockReference;
import scriptease.model.CodeBlockSource;
import scriptease.model.LibraryModel;
import scriptease.model.PatternModel;
import scriptease.model.StoryComponent;
import scriptease.model.StoryModel;
import scriptease.model.atomic.DescribeIt;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
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
 * Reads and writes Stories, Libraries and Dictionaries from or to various
 * sources on disk using XStream. Singleton so that it can watch for infinite
 * looping on load.
 * 
 * @author remiller
 * @author mfchurch
 */
public class FileIO {
	/**
	 * The modes that are possible for the IO system to be in. These are useful
	 * for changing behaviour dependent on what's going on. This has been done
	 * as a global variable with a singleton for simplicity, but if we find that
	 * we've got a lot of special cases all over, we may want to switch to a
	 * Strategy pattern style solution.
	 * 
	 * @author remiller
	 */
	public enum IoMode {
		API_DICTIONARY, LANGUAGE_DICTIONARY, STORY, LIBRARY, NONE;
	}

	private static FileIO instance;

	public static FileIO getInstance() {
		if (instance == null)
			instance = new FileIO();
		return instance;
	}

	private FileIO() {
		this.mode = IoMode.NONE;
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
		final StoryModel story;
		final QuestPoint rootQP;

		story = (StoryModel) this.readData(location, IoMode.STORY);

		if (story != null) {
			// Why are quests so bloody messy? - remiller
			rootQP = ((QuestPointNode) story.getRoot().getStartPoint())
					.getQuestPoint();

			BindingFixer.fixBindings(rootQP);
		}

		return story;
	}

	/**
	 * Reads a Patterns Library from disk.
	 * 
	 * @param location
	 *            The file to read from
	 * @return The read-in Library.
	 */
	public LibraryModel readLibrary(File location) {
		final LibraryModel lib;
		lib = (LibraryModel) this.readData(location, IoMode.LIBRARY);

		BindingFixer.fixBindings(lib.getRoot());

		return lib;
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
		this.writeData(dictionary, location, IoMode.API_DICTIONARY);
	}

	/**
	 * Read the API dictionary file from the given location.
	 * 
	 * @param location
	 *            The file path to read from.
	 * @return The API dictionary as read from disk.
	 */
	public APIDictionary readAPIDictionary(File location) {
		/*
		 * Check against inifiniloops. This is usually caused by trying to get
		 * something from the API dictionary while the API dictionary isn't done
		 * loading yet.
		 * 
		 * - remiller
		 */
		if (this.mode == IoMode.API_DICTIONARY)
			throw new IllegalStateException(
					"Loop detected in APIDictionary Loading");

		APIDictionary apiDictionary = (APIDictionary) this.readData(location,
				IoMode.API_DICTIONARY);

		BindingFixer.fixBindings(apiDictionary.getLibrary().getRoot());

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

		LanguageDictionary languageDict = (LanguageDictionary) this.readData(
				location, IoMode.LANGUAGE_DICTIONARY);

		return languageDict;
	}

	/**
	 * Writes the given StoryModel as XML to the given location;
	 * 
	 * @param model
	 *            The model to save to disk.
	 * @param location
	 *            the location to save to.
	 */
	public void writeStoryModel(PatternModel model, File location) {
		this.writeData(model, location, IoMode.STORY);
	}

	/**
	 * Writes the given LibraryModel as XML to the given location;
	 * 
	 * @param model
	 *            The model to save to disk.
	 * @param location
	 *            the location to save to.
	 */
	public void writeLibraryModel(PatternModel model, File location) {
		this.writeData(model, location, IoMode.LIBRARY);
	}

	private void writeData(Object dataModel, File location, IoMode mode) {
		final IoMode prevMode = this.mode;
		this.mode = mode;

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
				this.writeData(dataModel, location, mode);
		} finally {
			try {
				if (fileOut != null)
					fileOut.close();
			} catch (IOException e2) {
				System.err
						.println("Failed to close an output file connection.");
			}

			this.mode = prevMode;
		}
	}

	private Object readData(File location, IoMode mode) {
		final IoMode prevMode = this.mode;
		this.mode = mode;

		InputStream fileIn = null;
		Object dataModel = null;
		final XStream stream = this.buildXStream();
		boolean retry;

		try {
			System.out.println("Creating input stream for file: " + location);
			fileIn = new FileInputStream(location);
			System.out.println("Input Stream: " + fileIn
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
				this.readData(location, mode);
		} catch (XStreamException e) {
			System.err
					.println("An error occured while loading "
							+ location.getAbsolutePath()
							+ ". It might be XStream complaining about a malformatted file, or some other error occured incidentally while loading.");
			e.printStackTrace();

			retry = WindowManager
					.getInstance()
					.showRetryProblemDialog(
							"Problem Reading",
							"I can't understand the file "
									+ location.getAbsolutePath()
									+ ".\n\nIt might be a malformatted file or from a previous version of ScriptEase.");

			if (retry)
				this.readData(location, mode);
		} finally {
			try {
				if (fileIn != null)
					fileIn.close();
			} catch (IOException e) {
				System.err.println("Failed to close an input file connection.");
			} finally {
				// put the io mode back
				this.mode = prevMode;
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
		stream.alias("CodeBlockSource", CodeBlockSource.class);
		stream.alias("CodeBlockReference", CodeBlockReference.class);
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
		stream.registerConverter(new CodeBlockSourceConverter());
		stream.registerConverter(new CodeBlockReferenceConverter());
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

	/*
	 * This is all part of a terrible hack. XStream writes out the full KnowIt
	 * for KnowItBindingReferences, which is normally fine, because it will
	 * internally reference XML elements that are supposed to be loaded as the
	 * same object. However, it can't do that with Implicit KnowIts because the
	 * original reference isn't saved to the file. When reloading, it creates a
	 * duplicate of the actual implicit it should be pointing to, which causes
	 * problems. The symptoms for this bug are to have a slot bound to an
	 * implicit,
	 * 
	 * There are a few solutions to this which are:
	 * 
	 * 1. Fix the references after everything is done loading. (Ugly hack - the
	 * current solution)
	 * 
	 * 2. Write the owner to the file so that the containing cause can be found,
	 * its implicits collected, and the binding fixed. (A different ugly hack)
	 * 
	 * 3. Have a referencing system similar to the CodeBlock one. I haven't
	 * thought deeply about this, but I'm pretty sure such a thing could be
	 * leveraged to fix this problem in a clean way. (The only nice way I can
	 * think of)
	 * 
	 * We can't write the implicits to the file, either, because they don't
	 * belong there.
	 * 
	 * I hate implicits so damned much. They have so many special rules.
	 * 
	 * - remiller
	 */
	private static class BindingFixer extends AbstractNoOpStoryVisitor {
		public static void fixBindings(StoryComponent root) {
			final BindingFixer fixer = new BindingFixer();

			// sniff out the broken links and fix them
			root.process(fixer);
		}

		private BindingFixer() {
		}

		@Override
		protected void defaultProcessComplex(ComplexStoryComponent complex) {
			super.defaultProcessComplex(complex);

			for (StoryComponent child : complex.getChildren())
				child.process(this);
		}

		@Override
		public void processKnowIt(KnowIt knowIt) {
			super.processKnowIt(knowIt);

			this.fixBinding(knowIt);
		}

		@Override
		public void processScriptIt(ScriptIt scriptIt) {
			super.processScriptIt(scriptIt);

			for (KnowIt param : scriptIt.getParameters()) {
				param.process(this);
			}
		}

		private void fixBinding(KnowIt knowIt) {
			KnowItBinding bindingWrapper;
			KnowIt referent;
			Collection<KnowIt> implicits;
			ScriptIt parentCause;

			bindingWrapper = knowIt.getBinding();
			parentCause = this.getCause(knowIt);

			if (!(bindingWrapper instanceof KnowItBindingReference)
					|| parentCause == null)
				return;

			referent = ((KnowItBindingReference) bindingWrapper).getValue();

			implicits = parentCause.getImplicits();

			// rebind to implicits that match.
			for (KnowIt inScope : implicits) {
				if (referent.getDisplayText().equals(inScope.getDisplayText())) {
					knowIt.setBinding(inScope);
					break;
				}
			}
		}

		private ScriptIt getCause(KnowIt reference) {
			StoryComponent parent = reference;
			ScriptIt scriptIt;

			while (parent != null) {
				if (parent instanceof ScriptIt) {
					scriptIt = (ScriptIt) parent;
					if (scriptIt.isCause())
						return scriptIt;
				}

				parent = parent.getOwner();
			}

			return null;
		}
	}
}

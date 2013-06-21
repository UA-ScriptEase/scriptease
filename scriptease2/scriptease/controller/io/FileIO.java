package scriptease.controller.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import scriptease.controller.StoryAdapter;
import scriptease.controller.io.converter.IdentityArrayListConverter;
import scriptease.controller.io.converter.fragment.FormatDefinitionFragmentConverter;
import scriptease.controller.io.converter.fragment.FormatReferenceFragmentConverter;
import scriptease.controller.io.converter.fragment.IndentedFragmentConverter;
import scriptease.controller.io.converter.fragment.LineFragmentConverter;
import scriptease.controller.io.converter.fragment.LiteralFragmentConverter;
import scriptease.controller.io.converter.fragment.MapRefFragmentConverter;
import scriptease.controller.io.converter.fragment.ScopeFragmentConverter;
import scriptease.controller.io.converter.fragment.SeriesFragmentConverter;
import scriptease.controller.io.converter.fragment.SimpleDataFragmentConverter;
import scriptease.controller.io.converter.model.DescribeItConverter;
import scriptease.controller.io.converter.model.DescribeItNodeConverter;
import scriptease.controller.io.converter.model.DialogueLineConverter;
import scriptease.controller.io.converter.model.GameMapConverter;
import scriptease.controller.io.converter.model.GameTypeConverter;
import scriptease.controller.io.converter.model.LanguageDictionaryConverter;
import scriptease.controller.io.converter.model.LibraryModelConverter;
import scriptease.controller.io.converter.model.SlotConverter;
import scriptease.controller.io.converter.model.StoryModelConverter;
import scriptease.controller.io.converter.storycomponent.AskItConverter;
import scriptease.controller.io.converter.storycomponent.CauseItConverter;
import scriptease.controller.io.converter.storycomponent.CodeBlockReferenceConverter;
import scriptease.controller.io.converter.storycomponent.CodeBlockSourceConverter;
import scriptease.controller.io.converter.storycomponent.ControlItConverter;
import scriptease.controller.io.converter.storycomponent.KnowItBindingConverter;
import scriptease.controller.io.converter.storycomponent.KnowItConverter;
import scriptease.controller.io.converter.storycomponent.NoteConverter;
import scriptease.controller.io.converter.storycomponent.ScriptItConverter;
import scriptease.controller.io.converter.storycomponent.StoryComponentContainerConverter;
import scriptease.controller.io.converter.storycomponent.StoryPointConverter;
import scriptease.gui.WindowFactory;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockReference;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.describeits.DescribeItNode;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryPoint;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.LanguageDictionary;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.code.fragments.FormatReferenceFragment;
import scriptease.translator.codegenerator.code.fragments.LiteralFragment;
import scriptease.translator.codegenerator.code.fragments.MapRefFragment;
import scriptease.translator.codegenerator.code.fragments.SimpleDataFragment;
import scriptease.translator.codegenerator.code.fragments.container.FormatDefinitionFragment;
import scriptease.translator.codegenerator.code.fragments.container.IndentFragment;
import scriptease.translator.codegenerator.code.fragments.container.LineFragment;
import scriptease.translator.codegenerator.code.fragments.container.ScopeFragment;
import scriptease.translator.codegenerator.code.fragments.container.SeriesFragment;
import scriptease.translator.io.model.GameMap;
import scriptease.translator.io.model.GameType;
import scriptease.translator.io.model.Slot;
import scriptease.util.FileOp;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

/**
 * Reads and writes Stories, Libraries and Dictionaries from or to various
 * sources on disk using XStream. Singleton so that it can watch for infinite
 * looping on load.
 * 
 * @author remiller
 * @author mfchurch
 * @author jyuen
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
		LIBRARY, LANGUAGE_DICTIONARY, STORY, NONE;
	}

	private static FileIO instance;

	public static FileIO getInstance() {
		if (FileIO.instance == null)
			FileIO.instance = new FileIO();
		return FileIO.instance;
	}

	private FileIO() {
		this.mode = IoMode.NONE;
	}

	private IoMode mode;

	/**
	 * Saves a CSV file to disk.
	 * 
	 * @param data
	 *            The data to generate in the CSV file.
	 * @param file
	 *            The file path to save the CSV in.
	 */
	public void saveCSV(Collection<? extends Collection<String>> data, File file) {
		final FileWriter out;
		String output;

		try {
			out = new FileWriter(file);

			for (Collection<String> row : data) {
				output = "";

				for (String value : row)
					output += value + ",";

				// Remove the last comma.
				output = output.substring(0, output.length() - 1);

				output += "\n";

				out.append(output);
			}

			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads a Story from disk.
	 * 
	 * @param location
	 *            The file to read from
	 * @return The read-in Story.
	 */
	public StoryModel readStory(File location) {
		final StoryModel story;
		final StoryPoint rootQP;

		story = (StoryModel) this.readData(location, IoMode.STORY);

		if (story != null) {
			rootQP = story.getRoot();
			BindingFixer.fixBindings(rootQP);
		}

		return story;
	}

	/**
	 * Reads a Library from the given location.
	 * 
	 * @param location
	 *            The file to read from
	 * @return The read-in Library.
	 */
	public LibraryModel readLibrary(Translator translator, File location) {
		final LibraryModel lib;

		lib = (LibraryModel) this.readData(location, IoMode.LIBRARY);

		lib.setTranslator(translator);

		BindingFixer.fixBindings(lib.getRoot());

		return lib;
	}

	/**
	 * Read the Library file from the given location.
	 * 
	 * @param location
	 *            The file path to read from.
	 * @return The LibraryModel as read from disk.
	 */
	public LibraryModel readDefaultLibrary(Translator translator, File location) {
		/*
		 * Check against inifiniloops. This is usually caused by trying to get
		 * something from the library while the library isn't done loading yet.
		 * 
		 * - remiller
		 */
		if (this.mode == IoMode.LIBRARY) {
			throw new IllegalStateException(
					"Loop detected in LibraryModel Loading");
		}
		return this.readLibrary(translator, location);
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
	public void writeStoryModel(SEModel model, File location) {
		this.writeData(model, location, IoMode.STORY);
	}

	/**
	 * Writes a LibraryModel to disk at the given location.
	 * 
	 * @param dictionary
	 *            The dictionary to write.
	 * @param location
	 *            The file path to write to.
	 */
	public void writeLibraryModel(final LibraryModel library,
			final File location) {
		WindowFactory.showProgressBar("Saving Library...", new Runnable() {
			@Override
			public void run() {
				FileIO.this.writeData(library, location, IoMode.LIBRARY);
			}
		});
	}

	private void writeData(Object dataModel, File location, IoMode mode) {
		final IoMode prevMode = this.mode;
		this.mode = mode;

		final File backupLocation;
		FileOutputStream fileOut = null;

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
			this.buildXStream().toXML(dataModel, fileOut);
		} catch (IOException e) {
			System.err.println("Patterns save problem: Could not access "
					+ location.getAbsolutePath());

			boolean retry = WindowFactory.getInstance().showRetryProblemDialog(
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
		boolean retry;

		try {
			System.out.println("Creating input stream for file: " + location);
			fileIn = new FileInputStream(location);
			System.out.println("Input Stream: " + fileIn
					+ " created for file: " + location);

			dataModel = this.buildXStream().fromXML(fileIn);
		} catch (IOException e) {
			System.err.println("Failed to read file "
					+ location.getAbsolutePath());
			e.printStackTrace();

			retry = WindowFactory.getInstance().showRetryProblemDialog(
					"Reading",
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

			retry = WindowFactory
					.getInstance()
					.showRetryProblemDialog(
							"Reading",
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
		stream.alias("KnowItBinding", KnowItBinding.class);
		stream.alias("Type", GameType.class);
		stream.alias("Slot", Slot.class);
		stream.alias("Binding", KnowItBinding.class);
		stream.alias("Value", String.class);
		stream.alias("DialogueLine", DialogueLine.class);

		// Language Dictionary Fragments
		stream.alias("LibraryModel", LibraryModel.class);
		stream.alias("LanguageDictionary", LanguageDictionary.class);
		stream.alias("Map", GameMap.class);
		stream.alias("Format", FormatDefinitionFragment.class);
		stream.alias("Indent", IndentFragment.class);
		stream.alias("Line", LineFragment.class);
		stream.alias("Literal", LiteralFragment.class);
		stream.alias("MapRef", MapRefFragment.class);
		stream.alias("FormatRef", FormatReferenceFragment.class);
		stream.alias("Scope", ScopeFragment.class);
		stream.alias("Series", SeriesFragment.class);
		stream.alias("Fragment", SimpleDataFragment.class);
		stream.alias("DescribeIt", DescribeIt.class);
		stream.alias("DescribeItNode", DescribeItNode.class);
		stream.alias("CodeBlock", CodeBlock.class);
		stream.alias("CodeBlockSource", CodeBlockSource.class);
		stream.alias("CodeBlockReference", CodeBlockReference.class);
		stream.alias("ScriptIt", ScriptIt.class);
		stream.alias("StoryPoint", StoryPoint.class);
		stream.alias("Note", Note.class);
		stream.alias("ControlIt", ControlIt.class);
		stream.alias("CauseIt", CauseIt.class);

		// the below are aliased for backwards compatibility

		/* <Insert backwards-compatible aliases here> */

		// now register all of the leaf-level converters
		stream.registerConverter(new StoryModelConverter());
		stream.registerConverter(new DialogueLineConverter());
		stream.registerConverter(new StoryComponentContainerConverter());
		stream.registerConverter(new KnowItConverter());
		stream.registerConverter(new AskItConverter());
		stream.registerConverter(new KnowItBindingConverter());
		stream.registerConverter(new GameTypeConverter());
		stream.registerConverter(new SlotConverter());
		stream.registerConverter(new FormatDefinitionFragmentConverter());
		stream.registerConverter(new IndentedFragmentConverter());
		stream.registerConverter(new LineFragmentConverter());
		stream.registerConverter(new LiteralFragmentConverter());
		stream.registerConverter(new MapRefFragmentConverter());
		stream.registerConverter(new FormatReferenceFragmentConverter());
		stream.registerConverter(new ScopeFragmentConverter());
		stream.registerConverter(new SeriesFragmentConverter());
		stream.registerConverter(new SimpleDataFragmentConverter());
		stream.registerConverter(new LibraryModelConverter());
		stream.registerConverter(new LanguageDictionaryConverter());
		stream.registerConverter(new GameMapConverter());
		stream.registerConverter(new CodeBlockSourceConverter());
		stream.registerConverter(new CodeBlockReferenceConverter());
		stream.registerConverter(new ScriptItConverter());
		stream.registerConverter(new CauseItConverter());
		stream.registerConverter(new StoryPointConverter());
		stream.registerConverter(new NoteConverter());
		stream.registerConverter(new DescribeItConverter());
		stream.registerConverter(new DescribeItNodeConverter());
		stream.registerConverter(new ControlItConverter());

		stream.registerConverter(new IdentityArrayListConverter(stream
				.getMapper()));

		return stream;
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
	private static class BindingFixer extends StoryAdapter {
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

		private CauseIt getCause(KnowIt reference) {
			StoryComponent parent = reference;

			while (parent != null) {
				if (parent instanceof CauseIt) {
					return (CauseIt) parent;
				}

				parent = parent.getOwner();
			}

			return null;
		}
	}
}

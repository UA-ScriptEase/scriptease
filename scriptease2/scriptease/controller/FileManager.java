package scriptease.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import scriptease.ScriptEase;
import scriptease.controller.io.FileIO;
import scriptease.controller.modelverifier.problem.StoryProblem;
import scriptease.controller.observer.FileManagerObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.gui.PanelFactory;
import scriptease.gui.StatusManager;
import scriptease.gui.WindowFactory;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.model.LibraryManager;
import scriptease.model.LibraryModel;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;
import scriptease.model.StoryModel;
import scriptease.translator.APIDictionary;
import scriptease.translator.GameCompilerException;
import scriptease.translator.Translator;
import scriptease.translator.Translator.DescriptionKeys;
import scriptease.translator.TranslatorManager;
import scriptease.translator.codegenerator.CodeGenerator;
import scriptease.translator.codegenerator.ScriptInfo;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.tools.ScriptEaseFileAccess;
import scriptease.util.BiHashMap;
import scriptease.util.FileOp;

/**
 * The file manager is responsible for knowing about and controlling access to
 * all of the GameModules and code paks open at any one time. <br>
 * <br>
 * It also takes care of the user preferences and config files, as well as
 * saving the instanced patterns. <br>
 * <br>
 * Queries to open a file or to see if a file exists should go through
 * FileManager.<br>
 * <br>
 * FileManager is a singleton.
 * 
 * @author remiller
 */
public final class FileManager {
	private static final String SAVE_AS = Il8nResources
			.getString("Save_Model_As");

	/**
	 * The file extension for ScriptEase Library Files
	 */
	public static final String FILE_EXTENSION_LIBRARY = "sel";
	/**
	 * The file extension for ScriptEase Story Files
	 */
	public static final String FILE_EXTENSION_STORY = "ses";

	public static final FileNameExtensionFilter STORY_FILTER = new FileNameExtensionFilter(
			"ScriptEase Story Files", FileManager.FILE_EXTENSION_STORY);

	public static final FileNameExtensionFilter LIBRARY_FILTER = new FileNameExtensionFilter(
			"ScriptEase Library Files", FileManager.FILE_EXTENSION_LIBRARY);

	/**
	 * Set of all openFiles currently open. Strictly speaking, the model itself
	 * isn't open, its module and patterns are.
	 */
	private final BiHashMap<File, StoryModel> openFiles;

	private final Set<File> tempFiles;

	private final Map<File, FileChannel> filesToChannels;

	private final List<WeakFileManagerObserverReference<FileManagerObserver>> observers;

	/**
	 * The sole instance of this class as per the singleton pattern.
	 */
	private static final FileManager instance = new FileManager();

	/**
	 * The maximum number of files to be considered 'recent'
	 */
	private static final int RECENT_FILE_MAX = 5;

	/**
	 * Creates a FileManager that loads in the configuration file and user
	 * preferences files.
	 */
	private FileManager() {
		this.tempFiles = new HashSet<File>();

		this.openFiles = new BiHashMap<File, StoryModel>();

		this.filesToChannels = new HashMap<File, FileChannel>();

		this.observers = new ArrayList<WeakFileManagerObserverReference<FileManagerObserver>>(
				FileManager.RECENT_FILE_MAX);
	}

	/**
	 * @return the Singleton instance
	 */
	public static final FileManager getInstance() {
		return FileManager.instance;
	}

	/**
	 * General method for opening a file. Creates a new file channel for the
	 * given file, which the FileManager remembers.
	 * 
	 * @param toOpen
	 *            The file location to open.
	 */
	public void open(File toOpen) {
		FileChannel opened;

		// open the given file and remember it
		try {
			// TODO: update this to the Java 7 standard, once java 7 exists
			opened = (new FileInputStream(toOpen)).getChannel();

			// TODO: lock this channel object
			this.filesToChannels.put(toOpen, opened);
		} catch (FileNotFoundException e) {
			Thread.currentThread().getUncaughtExceptionHandler()
					.uncaughtException(Thread.currentThread(), e);
		}
	}

	/**
	 * General method for closing a file. Closes the FileChannel that we already
	 * had open and causes FileManager to forget about it.<br>
	 * <br>
	 * Does nothing if the given file isn't one that was opened by FileManager
	 * with {@link FileManager#open(File)}, or if it is <code>null</code>.
	 * 
	 * @param toClose
	 *            The file location to close.
	 */
	public void close(File toClose) {
		FileChannel channel = this.filesToChannels.get(toClose);

		if (channel == null)
			return;

		try {
			channel.close();
		} catch (IOException e) {
			System.err.println("Failed to close file: "
					+ toClose.getAbsolutePath());
			Thread.currentThread().getUncaughtExceptionHandler()
					.uncaughtException(Thread.currentThread(), e);
		}

		this.filesToChannels.remove(toClose);
	}

	/**
	 * Saves the given model. Checks the map of open files to see if the model
	 * has already been saved somewhere. If so, it saves the given model there.
	 * If not, it calls <code>saveAs</code> to prompt the user for a new
	 * location.
	 * 
	 * @param model
	 *            The model to be saved.
	 * @see #saveAs(StoryModel)
	 */
	public void save(PatternModel model) {

		model.process(new ModelAdapter() {
			@Override
			public void processLibraryModel(LibraryModel libraryModel) {
				final Translator active;
				final APIDictionary apiDictionary;
				final File location;

				active = TranslatorManager.getInstance().getActiveTranslator();
				apiDictionary = active.getApiDictionary();
				location = active
						.getPathProperty(DescriptionKeys.API_DICTIONARY_PATH
								.toString());

				FileIO.getInstance()
						.writeAPIDictionary(apiDictionary, location);
			}

			@Override
			public void processStoryModel(StoryModel storyModel) {
				final File location = FileManager.this.openFiles
						.getKey(storyModel);
				final String saveMessage = "Saving story "
						+ storyModel.getTitle() + " to " + location;

				System.out.println(saveMessage);
				StatusManager.getInstance().setStatus(saveMessage + " ...");
				if (location == null) {
					FileManager.this.saveAs(storyModel);
					return;
				}

				FileManager.this.writeStoryModelFile(storyModel, location);
			}
		});
	}

	/**
	 * Prompts the user to choose a file location, then saves the given model to
	 * that location.<br>
	 * If the file does not have the proper extension, the proper extension is
	 * added.
	 * 
	 * @param model
	 *            The model to be saved.
	 * @see #save(PatternModel)
	 */
	public void saveAs(PatternModel model) {
		model.process(new ModelAdapter() {
			@Override
			public void processLibraryModel(LibraryModel libraryModel) {
				final WindowFactory windowManager = WindowFactory.getInstance();
				final Translator active;
				final APIDictionary apiDictionary;

				active = TranslatorManager.getInstance().getActiveTranslator();
				apiDictionary = active.getApiDictionary();

				File location = windowManager.showFileChooser(
						FileManager.SAVE_AS, libraryModel.getName(),
						FileManager.LIBRARY_FILTER);

				if (location == null) {
					return;
				}

				if (!FileOp.getExtension(location).equalsIgnoreCase(
						FileManager.FILE_EXTENSION_LIBRARY)) {
					location = FileOp.addExtension(location,
							FileManager.FILE_EXTENSION_LIBRARY);
				}

				if (location.exists()
						&& !windowManager.showConfirmOverwrite(location))
					return;

				FileIO.getInstance()
						.writeAPIDictionary(apiDictionary, location);
			}

			@Override
			public void processStoryModel(StoryModel storyModel) {
				final WindowFactory windowManager = WindowFactory.getInstance();

				File location = windowManager.showFileChooser(
						FileManager.SAVE_AS, storyModel.getName(),
						FileManager.STORY_FILTER);

				// make sure the user didn't cancel/close window.
				if (location == null) {
					return;
				}

				// ensure that the location has the proper extension.
				if (!FileOp.getExtension(location).equalsIgnoreCase(
						FileManager.FILE_EXTENSION_STORY)) {
					location = FileOp.addExtension(location,
							FileManager.FILE_EXTENSION_STORY);
				}

				// check if the location exists already.
				if (location.exists()
						&& !windowManager.showConfirmOverwrite(location))
					return;

				// save the model
				FileManager.this.writeStoryModelFile(storyModel, location);
			}
		});
	}

	/**
	 * Saves the given model to the given location.
	 * 
	 * @param model
	 *            The model to be saved.
	 * @param location
	 *            The location to save the model.
	 */
	private void writeStoryModelFile(StoryModel model, File location) {
		final StoryModel storedModel = this.openFiles.getValue(location);
		final WindowFactory windowManager = WindowFactory.getInstance();
		final FileIO writer = FileIO.getInstance();

		// make sure the file isn't already being used by another model.
		if (storedModel != null && storedModel != model) {
			// pop up an alert.
			boolean retry = windowManager
					.showRetryProblemDialog(
							"Overwriting File",
							"The file is already in use by another story.\nWould you like to select a new file?",
							"Yes");
			if (retry) {
				// force the user to choose a new location for the file.
				this.saveAs(model);
			}
			return;
		}

		// make sure the model doesn't reference a module that is already in use
		// by another model.
		if (FileManager.getInstance().isModuleInUse(model)) {
			windowManager
					.showProblemDialog(
							"Problem",
							"The file references resources already in use by another story.\nPlease ensure only one Story is open for each game module.");
			return;
		}

		// Write the Story's patterns to XML
		writer.writeStoryModel(model, location);

		// update the map of open files/models to reflect the new file location.
		// remove the entry corresponding to the model, not the location.
		this.openFiles.removeValue(model);
		this.openFiles.put(location, model);

		this.writeCode(model, true);

		// update the recent files list in the preferences file.
		this.updateRecentFiles(location);

		// update the recent files menu items in the GUI.
		this.notifyObservers(model, location);
	}

	private void writeCode(StoryModel model, boolean compile) {
		// generate the code
		final GameModule module = model.getModule();
		final Translator translator = model.getTranslator();
		final Collection<StoryProblem> problems = new ArrayList<StoryProblem>();
		final Collection<ScriptInfo> scriptInfos = CodeGenerator.generateCode(
				model, problems);

		module.addScripts(scriptInfos);
		module.addIncludeFiles(translator.getIncludes());

		this.saveScriptInOutput(scriptInfos, translator);

		// now save that code to the module
		compile &= problems.isEmpty() && translator.getCompiler() != null
				&& translator.getCompiler().exists();

		if (translator.getCompiler() == null
				|| !translator.getCompiler().exists())
			WindowFactory
					.getInstance()
					.showWarningDialog(
							"Compiler not found",
							"I couldn't find the compiler for "
									+ translator.getName()
									+ ".\n\nCheck that the compiler path in the \"translator.ini\" file"
									+ " in the translator directory is correct."
									+ "\nRestart ScriptEase after saving."
									+ "\n\nI saved the story without compiling.");

		try {
			if (compile)
				StatusManager.getInstance().setStatus(
						"Saving Story with Compiling...");

			module.save(compile);
			StatusManager.getInstance().setStatus(
					(compile ? "Compilation and " : "")
							+ "Story Save Successful!");
		} catch (IOException e) {
			// Nothing better to do at the moment except let ScriptEase
			// explode.
			// Eventually, this should undo the save. IE have a backup
			// file that we would ordinarily replace, but since we've failed
			// we just delete the file we're currently writing and leave the
			// original intact. - remiller
			StatusManager.getInstance().setStatus("Story Save Failed!");
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(
					Thread.currentThread(), new IOError(e));
		} catch (GameCompilerException e) {
			if (compile) {
				StatusManager.getInstance().setStatus("Compilation Failed.");
				if (WindowFactory
						.getInstance()
						.showRetryProblemDialog(
								"Compiling",
								"Game compilation failed. I can try to save the scripts without compiling them.\n"
										+ "You might be able to open them in another program to see what went wrong.",
								"Save (No Compile)")) {
					this.writeCode(model, false);
					return;
				}
			}
		}
	}

	/**
	 * Saves a copy of the scripts and include files in scriptease's output
	 * directory
	 * 
	 * @param scriptInfos
	 * @param translator
	 */
	private void saveScriptInOutput(Collection<ScriptInfo> scriptInfos,
			Translator translator) {
		String outputDir = ScriptEase.getInstance().getPreference(
				ScriptEase.OUTPUT_DIRECTORY_KEY)
				+ "/";
		String storyName = PatternModelManager.getInstance().getActiveModel()
				.getName();

		new File(outputDir + storyName).mkdirs();
		File outputFile = new File(outputDir + storyName + "/" + storyName
				+ ".output");

		// Write the script to the file
		ScriptEaseFileAccess seWriter;
		try {
			seWriter = new ScriptEaseFileAccess(outputFile, "rw");

			for (ScriptInfo script : scriptInfos) {
				seWriter.writeString(script == null ? "" : script.getCode());
			}

			seWriter.close();

			// Copy necessary include files
			for (File include : translator.getIncludes()) {
				File includeFile = new File(outputDir + storyName + "/"
						+ include.getName());
				FileOp.copyFile(include, includeFile);
			}
		} catch (Throwable e) {
			System.err.println("Exception - Unable to save to output folder!\n"
					+ e);
		}
	}

	/**
	 * Tests whether the given file is in use by another Story.
	 * 
	 * @return
	 */
	public boolean isModuleInUse(StoryModel toTest) {
		final File location = toTest.getModule().getLocation();
		File openLocation;

		for (StoryModel model : this.openFiles.getValues()) {
			openLocation = model.getModule().getLocation();

			if (openLocation.equals(location) && model != toTest) {
				return true;
			}
		}

		return false;
	}

	public void loadLibraryModel(File location) {
		final FileIO reader = FileIO.getInstance();
		PatternModel model = null;

		if (location == null || !location.exists())
			return;

		try {
			model = reader.readLibrary(location);
		} catch (Throwable e) {
			Thread.currentThread().getUncaughtExceptionHandler()
					.uncaughtException(Thread.currentThread(), e);
		}

		if (model != null)
			LibraryManager.getInstance().add((LibraryModel) model);
	}

	/**
	 * Opens the module that exists at the given location, and creates a new
	 * internal frame for it. Certain parts of this operation may fail, but as
	 * long as the module file exists it will throw no exceptions.<br>
	 * <br>
	 * If the patterns file cannot be located, then a new, empty pattern set is
	 * built.<br>
	 * <br>
	 * If the module at <code>location</code> is already open, then a new frame
	 * will be created using the existing model.
	 * 
	 * @param location
	 *            The location of the module file to open. Passing
	 *            <code>null</code> will have no effect.
	 */
	public void openStoryModel(final File location) {
		final StoryModel model;
		final FileIO reader;

		if (location == null || !location.exists()) {
			WindowFactory.getInstance().showProblemDialog(
					"File Not Found",
					"I could not locate the file \""
							+ location.getAbsolutePath() + "\".");
			return;
		}

		// if we've already got the model open, then we don't need to
		// load it again.
		if (this.openFiles.containsKey(location)) {
			model = this.openFiles.getValue(location);
			this.updateRecentFiles(location);
		} else {
			reader = FileIO.getInstance();
			model = reader.readStory(location);
			if (model == null)
				return;

			this.openFiles.put(location, model);

			// this needs to happen before we add it to the model pool
			// so that
			// the menu items update themselves correctly - remiller
			this.updateRecentFiles(location);

			PatternModelManager.getInstance().add(model, true);
		}
		this.notifyObservers(model, location);

		PanelFactory.getInstance().createTabForModel(model);
	}

	private void updateRecentFiles(File opened) {
		final ScriptEase se = ScriptEase.getInstance();
		final int recentFileCount = this.getRecentFileCount();

		List<String> recentFilePaths = new ArrayList<String>();

		recentFilePaths.add(opened.getAbsolutePath());

		// gather up all the old 'list' elements
		for (int i = 0; i < recentFileCount; i++) {
			final File recentFile = new File(
					se.getPreference(ScriptEase.RECENT_FILE_PREFIX + i));

			if (recentFile.exists() && !recentFile.equals(opened)
					&& (recentFile != opened))
				recentFilePaths.add(recentFile.getAbsolutePath());

			// clear out the old saved list entry
			se.setPreference(ScriptEase.RECENT_FILE_PREFIX + i, null);
		}

		// keep the size correct.
		if (recentFilePaths.size() > FileManager.RECENT_FILE_MAX)
			recentFilePaths = recentFilePaths.subList(0,
					FileManager.RECENT_FILE_MAX);

		// push out the new list.
		int i = 0;
		for (String recentPath : recentFilePaths) {
			se.setPreference(ScriptEase.RECENT_FILE_PREFIX + i, recentPath);
			i++;
		}
	}

	/**
	 * Determines if the given model can be closed without losing any desired
	 * unsaved changes. This requires user input when there are unsaved changes,
	 * because the user must decide if they must be kept or not.
	 * 
	 * @param model
	 *            The story model to test.
	 * 
	 * @return <code>true</code> only if the model can safely be closed without
	 *         losing any desired unsaved changes.
	 */
	public boolean hasUnsavedChanges(PatternModel model) {
		if (!UndoManager.getInstance().isSaved(model)) {
			int choice = WindowFactory.getInstance().showConfirmClose(model);

			if (choice == JOptionPane.CANCEL_OPTION)
				return false;
			else if (choice == JOptionPane.YES_OPTION) {
				this.save(model);
				return this.hasUnsavedChanges(model);
			}
		}

		// the only way we should get here is if the user had unsaved changes
		// and saved them, or didn't have any unsaved changes at all.
		return true;
	}

	/**
	 * Closes the given model's files. It is highly recommended to use
	 * {@link #hasUnsavedChanges(StoryModel)} before calls to
	 * {@link #close(StoryModel)} to verify with the user that there are no
	 * unsaved changed to lose.
	 * 
	 * @param model
	 *            The model whose files should be closed.
	 */
	public boolean close(PatternModel model) {
		if (model == null)
			return false;

		model.process(new ModelAdapter() {
			@Override
			public void processLibraryModel(LibraryModel libraryModel) {
				if (PanelFactory.getInstance()
						.getComponentsForModel(libraryModel).size() < 1) {
					PatternModelManager.getInstance().remove(libraryModel);
				}
			}

			@Override
			public void processStoryModel(StoryModel storyModel) {
				final GameModule module;

				module = storyModel.getModule();

				try {
					module.close();
				} catch (IOException e) {
					// I can't think of anything better to do with this sort
					// of error except let ScriptEase explode. - remiller
					Thread.getDefaultUncaughtExceptionHandler()
							.uncaughtException(Thread.currentThread(),
									new IOError(e));
				}

				if (PanelFactory.getInstance()
						.getComponentsForModel(storyModel).size() < 1) {
					PatternModelManager.getInstance().remove(storyModel);

					FileManager.this.openFiles.removeValue(storyModel);

				}
			}
		});

		return true;
	}

	/**
	 * Closes all open stories and prompts the user if they have unsaved
	 * changes.
	 * 
	 * @return returns <code>true</code> if the close operation was successful
	 *         for all models. Can return <code>false</code> if the user cancels
	 *         the close operation because they have unsaved changes.
	 */
	public boolean closeOpenStories() {
		// ensure that we won't be losing any unsaved changes. This has to be a
		// separate loop from the closing loop because if we cancel the close,
		// we need to be able to back out fully.
		for (PatternModel model : PatternModelManager.getInstance().getModels()) {
			if (!this.hasUnsavedChanges(model))
				return false;
		}

		// actually perform... The Closing. Cue dramatic music!
		for (PatternModel model : PatternModelManager.getInstance().getModels()) {
			if (!this.close(model))
				return false;
		}

		return true;
	}

	/**
	 * Gets the number of files that have been recently opened. This value is
	 * calculated from the current preferences, so modification of the current
	 * preferences between calls to getRecentFileCount() will result in
	 * different values. This number is never larger than
	 * {@link #RECENT_FILE_MAX}
	 * 
	 * @return The number of recently opened files.
	 */
	public int getRecentFileCount() {
		final ScriptEase se = ScriptEase.getInstance();
		int count = 0;
		String filePath = se.getPreference(ScriptEase.RECENT_FILE_PREFIX
				+ count);

		while (filePath != null) {
			count++;
			filePath = se.getPreference(ScriptEase.RECENT_FILE_PREFIX + count);
		}

		return count;
	}

	/**
	 * FileManager remembers a collection of the last N files opened which is up
	 * to {@link #RECENT_FILE_MAX} members long. <code>getRecentFile</code> the
	 * nth recently opened file. If nth is 3 and we have a memory 5 files deep,
	 * then the 3rd most recent file is returned.
	 * 
	 * @param nth
	 *            The index into the recent files collection. The most recent
	 *            file starts at 0 and older files are assigned numbers higher.
	 *            If this is a negative number then the most recent is returned.
	 *            Conversely, if nth is greater than the number of stored recent
	 *            files or RECENT_FILE_MAX, then nth is reset to the highest
	 *            allowable index.
	 * @return The <code>nth</code> (or as close as possible) recent file.
	 */
	public File getRecentFile(int nth) {
		if (nth < 0)
			nth = 0;
		else if ((nth > FileManager.RECENT_FILE_MAX)
				|| (nth >= this.getRecentFileCount()))
			nth = this.getRecentFileCount() - 1;

		return new File(ScriptEase.getInstance().getPreference(
				ScriptEase.RECENT_FILE_PREFIX + nth));
	}

	/**
	 * Creates a temporary directory that the FileManager knows about. This is
	 * the preferred method of creating temporary directories, since then we can
	 * ensure that the directories are deleted when the application dies, unlike
	 * {@link File#deleteOnExit()}, which only deletes on clean exits, not error
	 * crashes or other shutdowns.
	 * 
	 * @param name
	 *            The name of the directory to create.
	 * @return The File pointing to the directory created.
	 */
	public File createTempDirectory(String name) {
		File tempDir = null;

		String tempdir = System.getProperty("java.io.tmpdir");

		// The Internet claims it is prudent to ensure the file separator is
		// present at the end of the path - remiller
		if (!tempdir.endsWith(System.getProperty("file.separator"))) {
			tempdir += System.getProperty("file.separator");
		}

		try {
			tempDir = File.createTempFile(name, "");

			// delete the *file* and replace it with a *directory* of the same
			// name. This essentially a (common) hack around the lack of a
			// File.createTempDirectory method - remiller
			tempDir.delete();

			if (!tempDir.mkdir())
				throw new IOException("Failed to create temp directory: "
						+ tempDir.getAbsolutePath());

			this.tempFiles.add(tempDir);

			Runtime.getRuntime().addShutdownHook(
					this.createDeleteFileHook(tempDir));
		} catch (IOException e) {
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(
					Thread.currentThread(),
					new IOException("Failed to create temp directory \"" + name
							+ "\""));
		}

		return tempDir;
	}

/**
	 * Creates a temporary file that the FileManager knows about. This is
	 * the preferred method of creating temporary files, since then we can
	 * ensure that the files are deleted when the application dies, unlike
	 * {@link File#deleteOnExit()}, which only deletes on clean exits, not error
	 * crashes or other shutdowns.
	 * 
	 * @param prefix Three character prefix. Identical to <b>prefix</b> in {@link File#createTempFile(String, String, File)}
	 * @param directory See <b>directory</b> in {@link File#createTempFile(String, String, File)}
	 * @param sufffix Suffix to be used in the file name. May be null. See <b>suffix</b> in {@link File#createTempFile(String, String, File)
	 * @param maxLength The maximum file name length allowed
	 * @return The File pointing to the directory created.
	 */
	public File createTempFile(String prefix, String suffix, File directory,
			int maxLength) {
		File tempFile = null;
		String uniqueName = "";

		if (prefix == null)
			prefix = "";
		if (suffix == null)
			suffix = "";

		try {
			int charCount = prefix.length() + suffix.length();
			int i = 0;
			uniqueName = Integer.toString(i, 36);

			while (charCount <= maxLength) {
				tempFile = new File(directory, prefix + uniqueName + suffix);

				if (tempFile.exists()) {
					// if it already exists, then we have to try again.
					i++;
					uniqueName = Integer.toString(i, 36);
				} else {
					tempFile.createNewFile();
					break;
				}
			}

			if ((charCount > maxLength) && (!tempFile.exists()))
				throw new IOException("Failed to create temp file.");

			this.tempFiles.add(tempFile);

			Runtime.getRuntime().addShutdownHook(
					this.createDeleteFileHook(tempFile));
		} catch (IOException e) {
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(
					Thread.currentThread(),
					new IOException("Failed to create temp file."));
		}

		return tempFile;
	}

	/**
	 * Creates a Thread that is intended to be a Shutdown Hook as in
	 * {@link Runtime#addShutdownHook()}. The created thread will attempt to
	 * delete <code>deleted</code>.
	 * 
	 * @param deleted
	 *            The file to be deleted.
	 * 
	 * @return A Shutdown Hook thread that will delete a single file.
	 */
	private Thread createDeleteFileHook(final File deleted) {
		Thread shutdownHook;

		shutdownHook = new Thread(new Runnable() {
			private static final int MAX_ATTEMPTS = 10;

			@Override
			public void run() {
				int attempts = 0;

				// check to see if the work is already done.
				if (!deleted.exists())
					return;

				while (!FileManager.this.recursiveDelete(deleted)
						&& attempts < MAX_ATTEMPTS) {
					attempts++;
				}

				if ((attempts >= MAX_ATTEMPTS) || deleted.exists())
					System.err.println("Failed to delete temp directory: "
							+ deleted + " on exit!");
			}
		}, "DeleteTempFile");

		return shutdownHook;
	}

	public void deleteTempFiles() {
		Collection<File> deleted = new ArrayList<File>(this.tempFiles.size());
		for (File temp : this.tempFiles) {
			if (this.deleteTempFile(temp))
				deleted.add(temp);
		}
		this.tempFiles.removeAll(deleted);
	}

	/**
	 * Since Java will only delete empty directories, this exists to delete the
	 * contents first, if <code>file</code> is a directory.
	 * 
	 * @param file
	 *            The file to delete.
	 * @return True if the delete was successful as per {@link File#delete()}.
	 */
	private boolean recursiveDelete(File file) {
		boolean success = true;

		if (file.isDirectory()) {
			for (File subFile : file.listFiles()) {
				success = success && this.recursiveDelete(subFile);
			}

			return success && file.delete();
		} else {
			if (this.tempFiles.contains(file)) {
				return this.deleteTempFile(file);
			} else {
				return file.delete();
			}
		}
	}

	/**
	 * Deletes the temporary file that was created by the FileManager.
	 * 
	 * @param temp
	 *            The temporary file to be deleted. This must be a temp file
	 *            created by the FileManager.
	 * @throws IllegalStateException
	 *             if this method is called with a file that was not created by
	 *             the FileManager or was not a temp file.
	 */
	public boolean deleteTempFile(File temp) {
		boolean deleted = false;

		if (this.tempFiles.contains(temp)) {
			if (temp.isDirectory())
				deleted = this.recursiveDelete(temp);
			else
				deleted = temp.delete();

		} else {
			// the FileManager didn't create this file, and therefore we can't
			// be sure that it's a temp file.
			throw new IllegalStateException(
					"Tried to delete a temp file that the FileManager didn't know about: "
							+ temp.getAbsolutePath());
		}

		return deleted;
	}

	public void addObserver(FileManagerObserver observer) {
		final Collection<WeakFileManagerObserverReference<FileManagerObserver>> observersCopy;

		observersCopy = new ArrayList<WeakFileManagerObserverReference<FileManagerObserver>>(
				this.observers);

		for (WeakFileManagerObserverReference<FileManagerObserver> observerRef : observersCopy) {
			FileManagerObserver storyComponentObserver = observerRef.get();
			if (storyComponentObserver != null
					&& storyComponentObserver == observer)
				return;
			else if (storyComponentObserver == null)
				this.observers.remove(observerRef);
		}

		this.observers
				.add(new WeakFileManagerObserverReference<FileManagerObserver>(
						observer));
	}

	public void removeObserver(FileManagerObserver observer) {
		for (WeakFileManagerObserverReference<FileManagerObserver> reference : this.observers) {
			if (reference.get() == observer) {
				this.observers.remove(reference);
				return;
			}
		}
	}

	private void notifyObservers(StoryModel model, File location) {
		Collection<WeakFileManagerObserverReference<FileManagerObserver>> observersCopy = new ArrayList<WeakFileManagerObserverReference<FileManagerObserver>>(
				this.observers);

		for (WeakFileManagerObserverReference<FileManagerObserver> observerRef : observersCopy) {
			FileManagerObserver observer = observerRef.get();
			if (observer != null)
				observer.fileReferenced(model, location);
			else
				this.observers.remove(observerRef);
		}
	}

	/**
	 * WeakReference wrapper used to track how many WeakReferences of each type
	 * are generated. This class provides no functionality, but it does make it
	 * easier for us to see where memory leaks may be occurring.
	 * 
	 * @author kschenk
	 * 
	 * @param <T>
	 */
	private class WeakFileManagerObserverReference<T> extends WeakReference<T> {
		public WeakFileManagerObserverReference(T referent) {
			super(referent);
		}
	}
}

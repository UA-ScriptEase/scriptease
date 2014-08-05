package scriptease.translator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileNameExtensionFilter;

import scriptease.ScriptEase;
import scriptease.controller.FileManager;
import scriptease.controller.ModelVisitor;
import scriptease.gui.WindowFactory;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.model.semodel.librarymodel.TypeConverter;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.GameType;
import scriptease.translator.io.model.Slot;
import scriptease.util.FileOp;
import scriptease.util.ListOp;
import scriptease.util.StringOp;

/**
 * a ScriptEase Game Translator. Translators are defined by a
 * <code>translator.ini</code> file located in a direct sub-folder of the
 * "translators" directory.<br>
 * <br>
 * For a Translator (and therefore its <code>translator.ini</code> file) to be
 * valid, it <b>must</b> contain the following information:
 * <ul>
 * <li>A name</li>
 * <li>A path to the <i>Library</i> file.</li>
 * <li>A path to the <i>Language Dictionary</i> file.</li>
 * <li>A path to the <code>.class</code> file which is the Translator's
 * implementation of {@link scriptease.translator.io.model.GameModule}.</li>
 * </ul>
 * Translators may also include other optional information. Attempting to get
 * any non-required property may result in a <code>null</code> return value.
 * Some common examples are:
 * <ul>
 * <li>A comma separated list of legal Game Module file extensions.</li>
 * <li>A path to a directory that contains all necessary include files.</li>
 * <li>A path to the compiler</li>
 * <li>A path to the game directory</li>
 * <li>An icon to be associated with the translator</li>
 * </ul>
 * but any property defined in a <code>translator.ini</code> can be acquired
 * through {@link #getProperty(String)}. The above examples have convenience
 * methods for getting that specific data.<br>
 * <br>
 * Because translators are so heavily defined by their translator.ini files,
 * they are backed by a {@link Properties} object, to enforce the formatting and
 * to facilitate I/O. <br>
 * <br>
 * Translators are named after the folder that the <code>translator.ini</code>
 * file exists in.
 * 
 * @author graves
 * @author remiller
 * @author mfchurch
 * @author kschenk
 * @author jyuen
 */
public class Translator extends SEModel {
	/**
	 * The expected file name of the Translator Description file.
	 */
	public static final String TRANSLATOR_DESCRIPTION_FILE_NAME = "translator.ini";
	private static final String CLASS_FILE_EXTENSION = ".class";

	/**
	 * Enumerates all required and some common optional keys for Translator
	 * properties.
	 * 
	 * @author remiller
	 * @author jyuen
	 */
	public enum DescriptionKeys {
		// Mandatory keys
		NAME, API_DICTIONARY_PATH, LANGUAGE_DICTIONARY_PATH, GAME_MODULE_PATH, VERSION,
		// Suggested keys
		SUPPORTED_FILE_EXTENSIONS, ICON_PATH, COMPILER_PATH, SUPPORTS_TESTING, GAME_DIRECTORY, OPTIONAL_LIBRARIES_PATH;

		public static final String FALSE = "false";
		private static final String DIRECTORY = "directory";
	}

	private static final String LANGUAGE_DICT_SCHEMA_LOCATION = "scriptease/resources/schema/LanguageDictionarySchema.xsd";
	private static final String LIBRARY_SCHEMA_LOCATION = "scriptease/resources/schema/LibrarySchema.xsd";
	private static final String CODE_ELEMENT_SCHEMA_LOCATION = "scriptease/resources/schema/CodeElementSchema.xsd";

	private final Properties properties;

	// data loaded from the translator.ini file
	private final Class<? extends GameModule> gameModuleClass;
	private final LanguageDictionary languageDictionary;
	private final Collection<LibraryModel> libraries;

	private final Collection<String> legalExtensions;

	// either the location of the jar, or the location of the description file.
	private final File location;

	/**
	 * Builds a new Translator from the given translator Jar or description
	 * file.
	 * 
	 * @param descriptionFile
	 *            The location of the description file for this translator.
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	protected Translator(File descriptionFile) throws IOException {
		super("", "", "");

		if (descriptionFile == null
				|| !descriptionFile.getName().equalsIgnoreCase(
						Translator.TRANSLATOR_DESCRIPTION_FILE_NAME)) {
			throw new IllegalArgumentException(
					"Tried to load a translator from a file that was not a translator.ini file.");
		}

		this.properties = new Properties();
		this.legalExtensions = new ArrayList<String>();
		this.libraries = new ArrayList<LibraryModel>();

		this.location = descriptionFile;

		final String extensionsString;
		final Reader descriptionReader;
		final String gameModuleClassLocation;

		final String loadingLibraryText = "Loading Library...";
		final String loadingOptionalText = "Loading Optional Libraries...";

		final ClassLoader loader;

		// load up the properties
		descriptionReader = new BufferedReader(new FileReader(descriptionFile));

		this.properties.load(descriptionReader);

		descriptionReader.close();

		// load legal file extensions
		extensionsString = this
				.getProperty(DescriptionKeys.SUPPORTED_FILE_EXTENSIONS);
		if (extensionsString != null) {
			for (String extension : Arrays.asList(extensionsString.split(","))) {
				this.legalExtensions.add(extension.trim());
			}
		}

		loader = this.initClassloader(this.location);

		gameModuleClassLocation = toBinaryName(this
				.getProperty(DescriptionKeys.GAME_MODULE_PATH.toString()));

		// load the Game Module implementation (but don't create an instance)
		Class<?> gmClass = null;
		try {
			gmClass = loader.loadClass(gameModuleClassLocation);
		} catch (ClassNotFoundException e) {
			System.err.println("The game module class could not be loaded: "
					+ e.getMessage());
			e.printStackTrace();
		}

		if (gmClass != null && GameModule.class.isAssignableFrom(gmClass)) {
			this.gameModuleClass = (Class<? extends GameModule>) gmClass;
		} else {
			this.gameModuleClass = null;
			System.err.println(gmClass == null ? "null" : gmClass.getName()
					+ " is not an instance of GameModule");
		}

		this.languageDictionary = FileManager.getInstance()
				.openLanguageDictionary(this);

		if (this.languageDictionary == null)
			throw new IllegalStateException(
					"Unable to load the LanguageDictionary.");

		WindowFactory.showProgressBar(loadingLibraryText, new Runnable() {
			@Override
			public void run() {
				libraries.add(FileManager.getInstance().openDefaultLibrary(
						Translator.this));
			}
		});

		if (this.libraries.isEmpty())
			// If it's empty, something crazy is going on.
			throw new IllegalStateException("Unable to load the Library.");

		WindowFactory.showProgressBar(loadingOptionalText, new Runnable() {
			@Override
			public void run() {
				libraries.addAll(FileManager.getInstance()
						.openOptionalLibraries(Translator.this));
			}

		});
	}

	/**
	 * Transforms a given file path to the binary name as expected in
	 * {@link ClassLoader#loadClass(String)}.
	 * 
	 * @param classPath
	 *            the path to convert.
	 * @return the binary name for the class at the path given.
	 */
	private String toBinaryName(String classPath) {
		String binaryName = classPath;

		// strip off the .class if provided.
		if (binaryName.endsWith(CLASS_FILE_EXTENSION))
			binaryName = binaryName.substring(0, binaryName.length()
					- CLASS_FILE_EXTENSION.length());

		binaryName = binaryName.replaceAll("/", ".");

		return binaryName;
	}

	/**
	 * Builds a URLClassLoader that is aware of the translator directory and all
	 * contained Jar files. It is to be used to load all Game Module classes.
	 * 
	 * @param location
	 *            the translator directory to look into.
	 * @return The configured classloader
	 */
	private ClassLoader initClassloader(File location) {
		final FileFilter jarFilter;
		final Collection<File> fileSourceLocations;
		final Collection<URL> urlSourceLocations;

		jarFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().toLowerCase().endsWith(".jar");
			}
		};

		fileSourceLocations = FileOp.findFiles(location.getParentFile(),
				jarFilter);
		fileSourceLocations.add(location.getParentFile());
		urlSourceLocations = new ArrayList<URL>();

		URL url;
		for (File srcLocation : fileSourceLocations) {
			try {
				url = srcLocation.toURI().toURL();
			} catch (MalformedURLException e) {
				// this is a stupid exception. - remiller
				continue;
			}

			urlSourceLocations.add(url);
		}

		return new URLClassLoader(urlSourceLocations.toArray(new URL[0]));
	}

	/**
	 * Finds and returns the optional libraries.
	 * 
	 * @return
	 */
	public Collection<LibraryModel> getOptionalLibraries() {
		final Collection<LibraryModel> tail = ListOp.tail(libraries);

		if (tail != null)
			return tail;
		else
			return new ArrayList<LibraryModel>();
	}

	/**
	 * Adds a new optional library to the translator.
	 * 
	 * @param library
	 */
	public void addOptionalLibrary(LibraryModel library) {
		this.libraries.add(library);
	}

	/**
	 * Finds a library in the translator by name.
	 * 
	 * @param name
	 *            The name to find for the library name. If this is null, null
	 *            will be returned.
	 * @return
	 */
	public LibraryModel findLibrary(String name) {
		if (name == null)
			return null;

		for (LibraryModel library : this.getLibraries()) {
			if (library.getTitle().equals(name))
				return library;
		}

		return null;
	}

	/**
	 * Gets a property specific to a particular Translator. Properties that are
	 * required by all Translators should be acquired instead through
	 * {@link #getProperty(DescriptionKeys)} or
	 * {@link #getPathProperty(DescriptionKeys)}. For getting properties that
	 * store a file path, use {@link #getPathProperty(String)} or
	 * {@link #getPathProperty(DescriptionKeys)} instead.
	 * 
	 * @param propertyName
	 *            the key of the property to get from the translator.ini file.
	 * @return the value of the property, or <code>null</code> if that property
	 *         is not found.
	 * @see #getPathProperty(String)
	 * @see #getLanguageDictionary()
	 * @see #getCompiler()
	 * @see #getName()
	 */
	public String getProperty(String propertyName) {
		final String property = this.properties.getProperty(propertyName);
		if (property == null)
			System.err.println("Property " + propertyName
					+ " was requested and not found in the " + this.location
					+ " translator.");
		return property;
	}

	/**
	 * Gets a property specific to a particular Translator that has been
	 * identified as a common or required property. For getting properties that
	 * store a file path, use {@link #getPathProperty(String)} or
	 * {@link #getPathProperty(DescriptionKeys)} instead. For getting a custom
	 * translator property not defined in {@link DescriptionKeys}, use
	 * {@link #getProperty(String)}.
	 * 
	 * @param propertyName
	 *            the key of the property to get from the translator.ini file.
	 * @return the value of the property, or <code>null</code> if that property
	 *         is not found.
	 * @see #getPathProperty(DescriptionKeys)
	 * @see #getLanguageDictionary()
	 * @see #getCompiler()
	 * @see #getName()
	 */
	public String getProperty(DescriptionKeys propertyKey) {
		return this.getProperty(propertyKey.toString());
	}

	/**
	 * Gets a property specific to a particular Translator that is referencing a
	 * file path. The file path is then cleaned up to be relative to the
	 * translator's directory, or is left alone if it is an absolute path.
	 * Returns a File that represents the now-correct directory, or
	 * <code>null</code> if no such property exists.<br>
	 * <br>
	 * For required or well-known properties, use
	 * {@link #getPathProperty(DescriptionKeys)}
	 * 
	 * @param propertyName
	 *            The name of the property, case-sensitive, from the
	 *            translator.ini file.
	 * @return A File that stores the correct path from the translator to the
	 *         file noted under the given property or <code>null</code> if
	 *         <code>propertyName</code> is not a property supported by the
	 *         translator.
	 * @see #getProperty(String)
	 */
	public File getPathProperty(String propertyName) {
		final String path = this.getProperty(propertyName);
		File file;

		file = (path == null) ? null : new File(path);

		if (file != null && !file.isAbsolute())
			file = new File(this.location.getParentFile(), path);

		return file;
	}

	/**
	 * Gets a property specific to a particular Translator that has been
	 * identified as a common or required property and is also referencing a
	 * file path. The file path is then cleaned up to be relative to the
	 * translator's directory, or is left alone if it is an absolute path.
	 * Returns a File that represents the now-correct directory, or
	 * <code>null</code> if no such property exists.<br>
	 * <br>
	 * For custom path properties, use {@link #getPathProperty(String)}
	 * 
	 * @param propertyKey
	 *            The key from {@link DescriptionKeys} to look up.
	 * @return A File that stores the correct path from the translator to the
	 *         file noted under the given property or <code>null</code> if
	 *         <code>propertyName</code> is not a property supported by the
	 *         translator.
	 * @see #getProperty(String)
	 */
	public File getPathProperty(DescriptionKeys propertyKey) {
		return this.getPathProperty(propertyKey.toString());
	}

	/**
	 * Sets a user-alterable preference to the given value. The value is not
	 * tested to see if is a valid value for the given key. Passing a value of
	 * <code>null</code> will delete that preference entirely (key and value).<br>
	 * <br>
	 * If the key is not a legitimate key, then a new entry will be created for
	 * non-<code>null</code> values.
	 * 
	 * @param preferenceKey
	 *            The unique string that denotes the preference.
	 * @param preferenceValue
	 *            The value to set the preference to. If this is
	 *            <code>null</code>, then the preference will be removed
	 *            entirely.
	 */
	public void setPreference(DescriptionKeys preferenceKey,
			String preferenceValue) {
		if (preferenceValue == null)
			this.properties.remove(preferenceKey.toString());
		else
			this.properties.setProperty(preferenceKey.toString(),
					preferenceValue);
	}

	/**
	 * Saves the translator preferences to disk (i.e. the translator.ini file)
	 */
	public void saveTranslatorPreferences() {
		FileOutputStream out;

		try {
			out = new FileOutputStream(this.location);

			this.properties.store(out, "Translator Preferences File");
			out.close();
		} catch (IOException e) {
			System.err
					.println("Error saving translator preferences. Aborting.");
			e.printStackTrace();
		}
	}

	/**
	 * Validates a translator by checking that the given translator has a value
	 * for all the required paths, that each path exists, and, if possible, that
	 * each file is valid (as per XML validation, for example).
	 * 
	 * @return A message if the translator is invalid or else return a empty
	 *         string.
	 */
	public String isValid() {
		final String NAME_NOT_FOUND = "The translator at " + this.getLocation()
				+ " is missing its name definition in translator.ini.";
		final String VERSION_NOT_FOUND = "The translator at "
				+ this.getLocation()
				+ " is missing its version definition in translator.ini.";
		final String LIBRARY_NOT_FOUND = "The Library Path definition in translator.ini for translator "
				+ this.getLocation()
				+ " is missing or the dictionary file does not exist there.";
		final String LANGUAGE_DICT_NOT_FOUND = "The Language Dictionary Path definition in translator.ini for translator "
				+ this.getLocation()
				+ " is missing or the dictionary file does not exist there.";
		final String GAME_MODULE_NOT_FOUND = "The translator at "
				+ this.getLocation()
				+ " is missing its Game Module Path definition in translator.ini "
				+ "or the Game Module implementation does not exist there.";
		final String INCOMPATIBLE_VERSION = "The translator at "
				+ this.getLocation()
				+ " does not have a compatible ScriptEase version number.";
		final String UNLOADABLE_GAME_MODULE = "The " + this.getTitle()
				+ " translator's game module is unloadable.";
		final String LIBRARY_VALIDATION_ERR = "The " + this.getTitle()
				+ " translator's Library does not pass validation.";
		final String LANGUAGE_DICT_VALIDATION_ERR = "The " + this.getTitle()
				+ " translator's language dictionary does not pass validation.";

		final File libraryPath;
		final File languageDictPath;
		final File gameModulePath;
		final GameModule testGameModule;
		final String translatorDirName;
		final String version;

		libraryPath = this.getPathProperty(DescriptionKeys.API_DICTIONARY_PATH);
		languageDictPath = this
				.getPathProperty(DescriptionKeys.LANGUAGE_DICTIONARY_PATH);
		gameModulePath = this.getPathProperty(DescriptionKeys.GAME_MODULE_PATH);
		version = this.getProperty(DescriptionKeys.VERSION);
		translatorDirName = this.getLocation().getParentFile().getName();

		if (!translatorDirName.equals(translatorDirName.toLowerCase())) {
			System.err
					.println("Translators must be in lower case directories.");
			return "Translators have to be in lower case directories.";
		}

		/*
		 * The "translator.ini" file *must* contain a name and references to a
		 * library, a languageDictionary, GameModule, and version
		 * implementation. All references must exist in the file system. Any
		 * other references are optional and will not be checked. See javadoc
		 * and/or wiki for details.
		 */
		if (this.getTitle() == null) {
			System.err.println(NAME_NOT_FOUND);
			return NAME_NOT_FOUND;

		} else if (version == null) {
			System.err.println(VERSION_NOT_FOUND);
			return VERSION_NOT_FOUND;

		} else if (!version.equals(ScriptEase.getInstance().getVersion())
				&& !ScriptEase.getInstance().getVersion()
						.equals(ScriptEase.NO_VERSION_INFORMATION)) {
			System.err.println(INCOMPATIBLE_VERSION);
			return INCOMPATIBLE_VERSION;

		} else if (libraryPath == null || !libraryPath.exists()) {
			System.err.println(LIBRARY_NOT_FOUND);
			return LIBRARY_NOT_FOUND;

		} else if (languageDictPath == null || !languageDictPath.exists()) {
			System.err.println(LANGUAGE_DICT_NOT_FOUND);
			return LANGUAGE_DICT_NOT_FOUND;

		} else if (gameModulePath == null || !gameModulePath.exists()) {
			System.err.println(GAME_MODULE_NOT_FOUND);
			return GAME_MODULE_NOT_FOUND;
		}

		// Now check that the referenced files are valid, including ensuring
		// that we can load the .class file for their implementation of
		// GameModule
		testGameModule = this.createGameModuleInstance();
		if (testGameModule == null) {
			System.err.println(UNLOADABLE_GAME_MODULE);
			return UNLOADABLE_GAME_MODULE;
		}

		// this is a bit of a hack, but is necessary to get FileOp to also
		// extract the referenced file(s) into the temp directory that it stores
		// all of these things. - remiller
		System.out.println("Extracting XML Schema dependencies for "
				+ this.getTitle() + " translator...");
		FileOp.getFileResource(Translator.CODE_ELEMENT_SCHEMA_LOCATION);

		try {
			if (!FileOp.validateXML(libraryPath,
					FileOp.getFileResource(Translator.LIBRARY_SCHEMA_LOCATION))) {
				System.err.println(LIBRARY_VALIDATION_ERR);
				return LIBRARY_VALIDATION_ERR;

			} else if (!FileOp.validateXML(languageDictPath, FileOp
					.getFileResource(Translator.LANGUAGE_DICT_SCHEMA_LOCATION))) {
				System.err.println(LANGUAGE_DICT_VALIDATION_ERR);
				return LANGUAGE_DICT_VALIDATION_ERR;
			}
		} catch (FileNotFoundException e) {
			return "Translator schema file was not found.";
		}

		return "";
	}

	/**
	 * Gets the location of this translator's translator.ini definition file.
	 * 
	 * @return the location of the translator definition
	 */
	public File getLocation() {
		return this.location;
	}

	/**
	 * Gets the Language Dictionary for this translator.
	 * 
	 * @return the languageDictionary
	 */
	public LanguageDictionary getLanguageDictionary() {
		return this.languageDictionary;
	}

	/**
	 * Attempts to get the compiler location. If the translator does not specify
	 * a compiler location, <code>null</code> is returned.<br>
	 * <br>
	 * This is a common non-required property of translators.
	 * 
	 * @return The location to the game's compiler, or <code>null</code> if no
	 *         compiler is defined.
	 */
	public File getCompiler() {
		return this.getPathProperty(DescriptionKeys.COMPILER_PATH);
	}

	/**
	 * Attempts to get the translator's icon. If the translator does not specify
	 * an icon file, or if the icon file cannot be read, <code>null</code> is
	 * returned.<br>
	 * <br>
	 * This is a common non-required property of translators.
	 * 
	 * @return The location to the translator's icon, or <code>null</code> if no
	 *         icon is defined or readable.
	 */
	public Icon getIcon() {
		final File iconLocation;

		iconLocation = this.getPathProperty(DescriptionKeys.ICON_PATH);

		return iconLocation == null ? null : new ImageIcon(
				iconLocation.getAbsolutePath());
	}

	/**
	 * Determines if the Translator's GameModule supports outside running of
	 * modules. <br>
	 * <br>
	 * This is a common non-required property of translators.
	 * 
	 * @return The location to the translator's icon, or <code>null</code> if no
	 *         icon is defined or readable.
	 */
	public boolean getSupportsTesting() {
		final String testingSupportStr;
		final boolean supportsTesting;

		testingSupportStr = this.getProperty(DescriptionKeys.SUPPORTS_TESTING);

		supportsTesting = testingSupportStr == null ? false : Boolean
				.parseBoolean(testingSupportStr);

		return supportsTesting;
	}

	/**
	 * Returns true if the module loads directories instead of files with
	 * extensions.
	 * 
	 * @return
	 */
	public boolean moduleLoadsDirectories() {
		return this.legalExtensions.size() == 1
				&& this.legalExtensions.contains(DescriptionKeys.DIRECTORY);
	}

	/**
	 * Creates a new file filter for the translator. This will throw an
	 * exception if the translator only allows modules. It is recommended to use
	 * {@link #moduleLoadsDirectories()} to check before using this method.
	 * 
	 * @return
	 */
	public javax.swing.filechooser.FileFilter createModuleFileFilter() {
		final javax.swing.filechooser.FileFilter filter;

		if (this.moduleLoadsDirectories()) {
			throw new IllegalArgumentException("Cannot filter files for this"
					+ " translator because it accepts directories.");
		} else if (this.legalExtensions.size() > 0)
			filter = new FileNameExtensionFilter(this.getTitle()
					+ " Game Files",
					this.legalExtensions
							.toArray(new String[this.legalExtensions.size()]));
		else
			filter = null;

		return filter;
	}

	private GameModule createGameModuleInstance() {
		GameModule module = null;

		try {
			module = this.gameModuleClass.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
			module = null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			module = null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			module = null;
		}

		return module;
	}

	/**
	 * Loads a saved GameModule object back into memory.
	 * 
	 * @param location
	 *            The File to load the saved GameModule from.
	 * @return The loaded GameModule object or <code>null</code> if there was a
	 *         problem during loading.
	 */
	public GameModule loadModule(File location) {
		final String LOADING_MODULE = "Loading Module...";
		final GameModule module = this.createGameModuleInstance();
		final GameModuleLoader loader = new GameModuleLoader();

		if (!location.exists()) {
			// We need to do this separately, because otherwise "open" creates a
			// new, empty file for the module.
			System.err.println("Module not found at [" + location + "]");
		} else {
			module.setLocation(location);
			WindowFactory.showProgressBar(LOADING_MODULE, new Runnable() {
				@Override
				public void run() {
					loader.load(module);
				}
			});
		}

		// If the module could not be found, ask the user for its location
		if (!loader.loadedSuccessfully()) {
			WindowFactory.getInstance().showProblemDialog(
					"Problem loading Game Module",
					"I couldn't find a Game File at \"" + location + "\"."
							+ loader.getErrorMessage());
			final File newLocation = this.requestNewLocation();

			return this.loadModule(newLocation);
		}

		System.out.println(module + " loaded");

		return module;
	}

	private File requestNewLocation() {
		final File newLocation;

		if (!this.moduleLoadsDirectories())
			// Otherwise pass in a null filter (defaults to accept all)
			newLocation = WindowFactory.getInstance().showFileChooser("Select",
					"", this.createModuleFileFilter(), this.getLocation());
		else
			newLocation = WindowFactory.getInstance().showDirectoryChooser(
					"Select", "", this.getLocation());

		return newLocation;
	}

	/**
	 * Returns only the library associated with the translator. This is obtained
	 * from the Library Manager.
	 * 
	 * Note that this method does not return default libraries that should be
	 * common across all translators.
	 * 
	 * @return
	 */
	public LibraryModel getLibrary() {
		return ListOp.head(this.libraries);
	}

	/**
	 * Returns all libraries, including the default and optional libraries.
	 * 
	 * @return
	 */
	public Collection<LibraryModel> getLibraries() {
		return this.libraries;
	}

	/**
	 * Goes through all libraries to find the describeIt associated with the
	 * passed in StoryComponent.
	 * 
	 * @param knowIt
	 * @return
	 */
	public DescribeIt getDescribeIt(StoryComponent component) {
		for (LibraryModel library : this.getLibraries()) {
			final DescribeIt describeIt = library.getDescribeIt(component);

			if (describeIt != null)
				return describeIt;
		}

		return null;
	}

	@Override
	public String toString() {
		return "Translator [" + this.getTitle() + "]";
	}

	/**
	 * This class deals with module loading, and error handling. I had to make
	 * this to let us pass in a runnable to the progress bar. It's a bit of a
	 * hack, although it does separate module error handling nicely.
	 * 
	 * @author kschenk
	 * 
	 */
	private class GameModuleLoader {
		private boolean loadSuccess = false;
		private String errorMessage = "";

		/**
		 * Returns true if the module loaded successfully.
		 * 
		 * @return
		 */
		private boolean loadedSuccessfully() {
			return this.loadSuccess;
		}

		/**
		 * Returns an error message if one was generated.
		 * 
		 * @return
		 */
		private String getErrorMessage() {
			return this.errorMessage;
		}

		/**
		 * Attempts to load the module.
		 * 
		 * @param module
		 */
		private void load(GameModule module) {
			try {
				// read the module to memory.
				module.load(false);
				loadSuccess = true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				if (module.getLocation().exists()) {
					System.err.println("Module in use at "
							+ module.getLocation());
					this.errorMessage = " \nThe module is currently in use by another program."
							+ "\n\nPlease either choose a different module for "
							+ "the story or close it in the other program.";
				} else {
					System.err.println("Module not found at "
							+ module.getLocation());
					this.errorMessage = " \nThe module file does not exist. "
							+ "\n\nPlease either choose a different module for "
							+ "the story or add it to the module directory.";
				}
				// This should only actually be called if module is read only.
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Possibly corrupt module at "
						+ module.getLocation());
				this.errorMessage = "\n\n It might be corrupt. "
						+ "\n\nPlease either choose a different module for the "
						+ "story or fix the corrupt module.";
			}
		}
	}

	@Override
	public Collection<GameType> getTypes() {
		final Collection<GameType> types = new ArrayList<GameType>();

		for (LibraryModel library : this.getLibraries()) {
			types.addAll(library.getTypes());
		}

		return types;
	}

	@Override
	public void setTitle(String title) {
		this.setPreference(DescriptionKeys.NAME, title);
		this.saveTranslatorPreferences();
	}

	/**
	 * Gets the name of this translator. The name is defined as the name of the
	 * folder containing the translator.
	 * 
	 * @return the translator's name
	 */
	@Override
	public String getTitle() {
		return this.getProperty(DescriptionKeys.NAME);
	}

	public Collection<CodeBlockSource> findSimilarTargets(ScriptIt owner, int id) {
		final Collection<CodeBlockSource> srcs = new ArrayList<CodeBlockSource>();
		for (LibraryModel library : this.getLibraries()) {
			final CodeBlockSource src = library.findCodeBlockSource(owner, id);
			if (src != null)
				srcs.add(src);
		}
		return srcs;
	}

	/**
	 * Goes through the libraries to find an appropriate type converter. Returns
	 * null if none exist.
	 * 
	 * @param knowIt
	 * @return
	 */
	public ScriptIt getTypeConverter(KnowIt knowIt) {
		ScriptIt scriptIt = null;

		for (LibraryModel library : this.getLibraries()) {
			final TypeConverter converter = library.getTypeConverter();
			scriptIt = converter.convert(knowIt);

			if (scriptIt != null)
				break;
		}

		return scriptIt;
	}

	@Override
	public GameType getType(String keyword) {
		for (GameType type : this.getTypes()) {
			if (type.getName().equals(keyword))
				return type;
		}

		return null;
	}

	@Override
	public Translator getTranslator() {
		return this;
	}

	@Override
	public String getSlotDefaultFormat() {
		for (LibraryModel library : this.getLibraries()) {
			final String defaultFormat = library.getSlotDefaultFormat();

			if (StringOp.exists(defaultFormat))
				return defaultFormat;
		}

		return "";
	}

	private Collection<Slot> getSlots() {
		final Collection<Slot> slots = new ArrayList<Slot>();

		for (LibraryModel library : this.getLibraries()) {
			slots.addAll(library.getSlots());
		}

		return slots;
	}

	@Override
	public Slot getSlot(String name) {
		for (Slot slot : this.getSlots()) {
			if (slot.getDisplayName().equals(name))
				return slot;
		}
		return null;
	}

	@Override
	public void process(ModelVisitor visitor) {
		visitor.processTranslator(this);
	}
}

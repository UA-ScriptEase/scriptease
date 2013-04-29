package scriptease.translator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
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

import scriptease.controller.FileManager;
import scriptease.controller.io.FileIO;
import scriptease.gui.StatusManager;
import scriptease.gui.WindowFactory;
import scriptease.model.LibraryModel;
import scriptease.translator.apimanagers.EventSlotManager;
import scriptease.translator.apimanagers.GameTypeManager;
import scriptease.translator.io.model.GameModule;
import scriptease.util.FileOp;

/**
 * a ScriptEase Game Translator. Translators are defined by a
 * <code>translator.ini</code> file located in a direct sub-folder of the
 * "translators" directory.<br>
 * <br>
 * For a Translator (and therefore its <code>translator.ini</code> file) to be
 * valid, it <b>must</b> contain the following information:
 * <ul>
 * <li>A name</li>
 * <li>A path to the <i>API Dictionary</i> file.</li>
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
 */
public class Translator {
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
	 */
	public enum DescriptionKeys {
		// Mandatory keys
		NAME, API_DICTIONARY_PATH, LANGUAGE_DICTIONARY_PATH, GAME_MODULE_PATH,
		// Suggested keys
		SUPPORTED_FILE_EXTENSIONS, INCLUDES_PATH, ICON_PATH, COMPILER_PATH, SUPPORTS_TESTING, GAME_DIRECTORY, OPTIONAL_LIBRARIES_PATH;

		public static final String FALSE = "false";
		private static final String DIRECTORY = "directory";
	}

	private static final String LANGUAGE_DICT_SCHEMA_LOCATION = "scriptease/resources/schema/LanguageDictionarySchema.xsd";
	private static final String API_DICT_SCHEMA_LOCATION = "scriptease/resources/schema/ApiDictionarySchema.xsd";
	private static final String CODE_ELEMENT_SCHEMA_LOCATION = "scriptease/resources/schema/CodeElementSchema.xsd";

	private final Properties properties;

	// data loaded from the translator.ini file
	private final Class<? extends GameModule> gameModuleClass;
	private APIDictionary apiDictionary;
	private LanguageDictionary languageDictionary;

	private final Collection<String> legalExtensions;

	// special class loader that knows to look in the translators for their
	// GameModule implementation and required java libaries.
	private final ClassLoader loader;

	// either the location of the jar, or the location of the description file.
	private final File location;

	private Collection<LibraryModel> optionalLibraries;

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
		if (descriptionFile == null
				|| !descriptionFile.getName().equalsIgnoreCase(
						Translator.TRANSLATOR_DESCRIPTION_FILE_NAME)) {
			throw new IllegalArgumentException(
					"Tried to load a translator from a file that was not a translator.ini file.");
		}

		this.properties = new Properties();
		this.legalExtensions = new ArrayList<String>();

		this.location = descriptionFile;

		final String extensionsString;
		final Reader descriptionReader;
		final String gameModuleClassLocation;

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

		this.loader = this.initClassloader(this.location);

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
	 * Gets a collection of the files found in the activeTranslators include
	 * directory. This can be empty if the translator does not specify an
	 * includes directory, or the the translator's includes directory is empty.
	 * Any file name that starts with .svn is ignored.
	 */
	public Collection<File> getIncludes() {
		final File includeDir = this
				.getPathProperty(DescriptionKeys.INCLUDES_PATH);
		final FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				// Don't include .svn files (double checking)
				return !file.getName().startsWith(".svn");
			}
		};

		return includeDir != null ? Arrays.asList(includeDir.listFiles(filter))
				: new ArrayList<File>();
	}

	/**
	 * Finds and returns the optional libraries.
	 * 
	 * @return
	 */
	public Collection<LibraryModel> getOptionalLibraries() {
		if (this.optionalLibraries == null) {
			final File optionalLibraryDir;

			this.optionalLibraries = new ArrayList<LibraryModel>();
			optionalLibraryDir = this
					.getPathProperty(DescriptionKeys.OPTIONAL_LIBRARIES_PATH);

			final FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.getName().endsWith(
							FileManager.FILE_EXTENSION_LIBRARY);
				}
			};

			if (optionalLibraryDir != null && optionalLibraryDir.exists()) {
				final Collection<File> optionalFiles;

				optionalFiles = FileOp.findFiles(optionalLibraryDir, filter);

				for (File file : optionalFiles) {
					final LibraryModel optionalLibrary;

					optionalLibrary = FileIO.getInstance().readLibrary(file);

					optionalLibrary.setTranslator(this);

					this.optionalLibraries.add(optionalLibrary);
				}
			}
		}

		return this.optionalLibraries;
	}

	public LibraryModel findLibrary(String name) {
		final LibraryModel defaultLibrary = this.getLibrary();

		if (defaultLibrary.getName().equals(name))
			return defaultLibrary;

		for (LibraryModel library : this.getOptionalLibraries()) {
			if (library.getName().equals(name))
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
	 * @see #getIncludes()
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
	 * @see #getIncludes()
	 */
	public File getPathProperty(DescriptionKeys propertyKey) {
		return this.getPathProperty(propertyKey.toString());
	}

	/**
	 * Gets the object responsible for mapping type keywords to their
	 * game-specific values.
	 * 
	 * @return the Type Manager
	 */
	public GameTypeManager getGameTypeManager() {
		return this.getApiDictionary().getGameTypeManager();
	}

	/**
	 * Gets the object responsible for mapping Event Slot keywords to their
	 * game-specific values.
	 * 
	 * @return the Slot Manager
	 */
	public EventSlotManager getSlotManager() {
		return this.getApiDictionary().getEventSlotManager();
	}

	/**
	 * Validates a translator by checking that the given translator has a value
	 * for all the required paths, that each path exists, and, if possible, that
	 * each file is valid (as per XML validation, for example).
	 * 
	 * @return Whether the translator is valid or not.
	 */
	public boolean isValid() {
		final File apiDictPath;
		final File languageDictPath;
		final File gameModulePath;
		final GameModule testGameModule;
		final String translatorDirName = this.getLocation().getParentFile()
				.getName();

		if (!translatorDirName.equals(translatorDirName.toLowerCase())) {
			System.err
					.println("Translators must be in lower case directories.");
			return false;
		}

		apiDictPath = this.getPathProperty(DescriptionKeys.API_DICTIONARY_PATH);
		languageDictPath = this
				.getPathProperty(DescriptionKeys.LANGUAGE_DICTIONARY_PATH);
		gameModulePath = this.getPathProperty(DescriptionKeys.GAME_MODULE_PATH);

		/*
		 * The "translator.ini" file *must* contain a name and references to an
		 * apiDictionary, a languageDictionary, and a GameModule implementation.
		 * All references must exist in the file system. Any other references
		 * are optional and will not be checked. See javadoc and/or wiki for
		 * details.
		 */
		if (this.getName() == null) {
			System.err.println("The translator at " + this.getLocation()
					+ " is missing its name definition in translator.ini.");
			return false;
		} else if (apiDictPath == null || !apiDictPath.exists()) {
			System.err
					.println("The API Dictionary Path definition in translator.ini for translator "
							+ this.getLocation()
							+ " is missing or the dictionary file does not exist there.");
			return false;
		} else if (languageDictPath == null || !languageDictPath.exists()) {
			System.err
					.println("The Language Dictionary Path definition in translator.ini for translator "
							+ this.getLocation()
							+ " is missing or the dictionary file does not exist there.");
			return false;
		} else if (gameModulePath == null || !gameModulePath.exists()) {
			System.err
					.println("The translator at "
							+ this.getLocation()
							+ " is missing its Game Module Path definition in translator.ini "
							+ "or the Game Module implementation does not exist there.");
			return false;
		}

		// Now check that the referenced files are valid, including ensuring
		// that we can load the .class file for their implementation of
		// GameModule
		testGameModule = this.createGameModuleInstance();
		if (testGameModule == null) {
			System.err.println("The " + this.getName()
					+ " translator's game module is unloadable.");
			return false;
		}

		// this is a bit of a hack, but is necessary to get FileOp to also
		// extract the referenced file(s) into the temp directory that it stores
		// all of these things. - remiller
		System.out.println("Extracting XML Schema dependencies for "
				+ this.getName() + " translator...");
		FileOp.getFileResource(Translator.CODE_ELEMENT_SCHEMA_LOCATION);

		try {
			if (!FileOp
					.validateXML(
							apiDictPath,
							FileOp.getFileResource(Translator.API_DICT_SCHEMA_LOCATION))) {
				System.err
						.println("The "
								+ this.getName()
								+ " translator's API dictionary does not pass validation.");
				return false;
			} else if (!FileOp.validateXML(languageDictPath, FileOp
					.getFileResource(Translator.LANGUAGE_DICT_SCHEMA_LOCATION))) {
				System.err
						.println("The "
								+ this.getName()
								+ " translator's language dictionary does not pass validation.");
				return false;
			}
		} catch (FileNotFoundException e) {
			return false;
		}

		return true;
	}

	/**
	 * Gets the name of this translator. The name is defined as the name of the
	 * folder containing the translator.
	 * 
	 * @return the translator's name
	 */
	public String getName() {
		return this.getProperty(DescriptionKeys.NAME);
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
	 * Returns whether the API dictionary is loaded.
	 * 
	 * @return <code>true</code> if the API dictionary is in memory.
	 */
	public boolean apiDictionaryIsLoaded() {
		return this.apiDictionary != null;
	}

	/**
	 * Gets the API Dictionary for this translator. The APIDictionary is lazy
	 * loaded because translator objects are created when ScriptEase is run,
	 * whereas APIDictionarys are only created when we load something that needs
	 * it.
	 * 
	 * @return the apiDictionary
	 */
	public APIDictionary getApiDictionary() {
		if (this.apiDictionary == null)
			this.loadAPIDictionary();

		return this.apiDictionary;
	}

	private void loadAPIDictionary() {
		final FileIO xmlReader;
		final File apiFile;

		xmlReader = FileIO.getInstance();
		// load the apiDictionary
		apiFile = this.getPathProperty(DescriptionKeys.API_DICTIONARY_PATH);

		this.apiDictionary = xmlReader.readAPIDictionary(this, apiFile);

		if (this.apiDictionary == null)
			throw new IllegalStateException("Unable to load the APIDictionary.");
	}

	public void unloadTranslator() {
		this.apiDictionary = null;
		this.languageDictionary = null;
	}

	/**
	 * Gets the Language Dictionary for this translator. If there is no Language
	 * Dictionary assigned to the translator yet, this method will attempt to
	 * open one.
	 * 
	 * @return the languageDictionary
	 */
	public LanguageDictionary getLanguageDictionary() {
		if (this.languageDictionary == null) {
			final FileIO xmlReader;
			final File languageFile;
			xmlReader = FileIO.getInstance();
			// load the languageDictionary
			languageFile = this
					.getPathProperty(DescriptionKeys.LANGUAGE_DICTIONARY_PATH);

			this.languageDictionary = xmlReader
					.readLanguageDictionary(languageFile);
		}

		if (this.languageDictionary != null)
			return this.languageDictionary;
		else
			throw new IllegalStateException(
					"Unable to load the LanguageDictionary.");
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
			filter = new FileNameExtensionFilter(
					this.getName() + " Game Files",
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
		StatusManager.getInstance().setStatus("Loading Module ...");
		GameModule module = this.createGameModuleInstance();

		if (!location.exists()) {
			// We need to do this separately, because otherwise "open" creates a
			// new, empty file for the module.
			System.err.println("Module not found at [" + location + "]");

			WindowFactory.getInstance().showProblemDialog(
					"Problem loading Game Module",
					"I couldn't find a Game Module at \"" + location + "\"."
							+ " \n\nPlease tell me a new location to use.");

			module = null;
		} else
			try {
				module.setLocation(location);
				// read the module to memory.
				module.load(false);
			} catch (FileNotFoundException e) {
				// This should only actually be called if module is read only.
				System.err.println("Module not found at [" + location + "]");
				e.printStackTrace();

				WindowFactory.getInstance().showProblemDialog(
						"Problem loading Game Module",
						"I couldn't find a Game Module at \"" + location
								+ "\". It may no longer exist or be in use."
								+ " \n\nPlease tell me a new location to use.");

				module = null;
			} catch (IOException e) {
				e.printStackTrace();
				WindowFactory
						.getInstance()
						.showProblemDialog(
								"Problem loading GameModule",
								"I can't read the Game Module at \""
										+ location
										+ "\".\n\n It might be corrupt. Please give me another file to try instead.");
				module = null;
			}

		// If the module could not be found, ask the user for its location
		if (module == null) {
			File newLocation = this.requestNewLocation();

			if (newLocation == null)
				module = null;
			else
				module = this.loadModule(newLocation);
		}

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
		return this.getApiDictionary().getLibrary();
	}

	@Override
	public String toString() {
		return "Translator [" + this.getName() + "]";
	}
}

package scriptease.translator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileNameExtensionFilter;

import scriptease.controller.apimanagers.EventSlotManager;
import scriptease.controller.apimanagers.GameTypeManager;
import scriptease.controller.io.FileIO;
import scriptease.gui.SEFrame;
import scriptease.gui.WindowManager;
import scriptease.model.LibraryManager;
import scriptease.model.LibraryModel;
import scriptease.translator.codegenerator.GameObjectPicker;
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
 */
public class Translator {
	/**
	 * The expected file name of the Translator Description file.
	 */
	public static final String TRANSLATOR_DESCRIPTION_FILE_NAME = "translator.ini";

	/**
	 * Enumerates all required and some common optional keys for Translator
	 * properties.
	 * 
	 * @author remiller
	 */
	public enum DescriptionKeys {
		// Mandatory keys
		NAME,
		API_DICTIONARY_PATH,
		LANGUAGE_DICTIONARY_PATH,
		GAME_MODULE_PATH,
		// Suggested keys
		SUPPORTED_FILE_EXTENSIONS,
		INCLUDES_PATH,
		ICON_PATH,
		COMPILER_PATH,
		CUSTOM_PICKER_PATH,
		SUPPORTS_TESTING,
		GAME_DIRECTORY;
	}

	private final Properties properties;

	// data loaded from the translator.ini file
	private final Class<? extends GameModule> gameModuleClass;
	private APIDictionary apiDictionary;
	private LanguageDictionary languageDictionary;

	private final Collection<String> legalExtensions;
	private GameObjectPicker customGameObjectPicker;

	final GameModuleClassLoader loader;

	// either the location of the jar, or the location of the description file.
	private final File location;

	private static final String LANGUAGE_DICT_SCHEMA_LOCATION = "scriptease/resources/schema/LanguageDictionarySchema.xsd";
	private static final String API_DICT_SCHEMA_LOCATION = "scriptease/resources/schema/ApiDictionarySchema.xsd";
	private static final String CODE_ELEMENT_SCHEMA_LOCATION = "scriptease/resources/schema/CodeElementSchema.xsd";

	/**
	 * Builds a new Translator from the given translator Jar or description
	 * file.
	 * 
	 * @param descriptionFile
	 *            The location of the description file for this translator.
	 * @throws IOException
	 */
	protected Translator(File descriptionFile) throws IOException {
		final String extensionsString;
		File gameModuleClassFile;
		final Reader descriptionReader;

		if (descriptionFile == null
				|| !descriptionFile.getName().equalsIgnoreCase(
						TRANSLATOR_DESCRIPTION_FILE_NAME)) {
			throw new IllegalArgumentException(
					"Tried to load a translator from a file that was not a translator.ini file.");
		}

		this.properties = new Properties();
		this.legalExtensions = new ArrayList<String>();

		this.location = descriptionFile;

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

		// load the Game Module implementation (but don't create an instance)
		loader = new GameModuleClassLoader(this.getClass().getClassLoader(),
				this);
		gameModuleClassFile = this
				.getPathProperty(DescriptionKeys.GAME_MODULE_PATH);
		// new
		// File(this.getProperty(DescriptionKeys.GAME_MODULE_PATH.toString()));

		System.err.println("Game Module Class File "
				+ (gameModuleClassFile.exists() ? "discovered"
						: "could not be found") + " at "
				+ gameModuleClassFile.getAbsolutePath());

		this.gameModuleClass = loader.loadGameModuleClass(gameModuleClassFile);
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
		System.out.println("Extracting XML Schema dependencies...");
		FileOp.getFileResource(CODE_ELEMENT_SCHEMA_LOCATION);
		System.out.println("Done");

		try {
			if (!FileOp.validateXML(apiDictPath,
					FileOp.getFileResource(API_DICT_SCHEMA_LOCATION))) {
				System.err
						.println("The "
								+ this.getName()
								+ " translator's API dictionary does not pass validation.");
				return false;
			} else if (!FileOp.validateXML(languageDictPath,
					FileOp.getFileResource(LANGUAGE_DICT_SCHEMA_LOCATION))) {
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
	public boolean loadedAPIDictionary() {
		return this.apiDictionary != null;
	}

	/**
	 * Gets the API Dictionary for this translator.
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

	public void unLoadTranslator() {
		this.apiDictionary = null;
		this.languageDictionary = null;
	}

	/**
	 * Gets the Language Dictionary for this translator. returns null if not yet
	 * initialized
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
	 * Gets the Game Module extensions that this translator supports.
	 * 
	 * @return the legalExtensions
	 */
	public String[] getLegalExtensions() {
		return new ArrayList<String>(this.legalExtensions)
				.toArray(new String[this.legalExtensions.size()]);
	}

	/**
	 * Determines if this translator can open the given file as a GameModule.
	 * 
	 * @return <code>true</code> if the translator supports reading the given
	 *         file.
	 */
	public boolean supportsModuleFile(File location) {
		return this.legalExtensions.contains(FileOp.getExtension(location));
	}

	private GameModule createGameModuleInstance() {
		GameModule module = null;

		try {
			module = this.gameModuleClass.newInstance();
		} catch (InstantiationException e) {
			// WindowManager.getInstance().showProblemDialog(
			// "Problem loading GameModule.",
			// "Instantiation Exception while loading:\nResource: "
			// + location + "\nDetails: ");
			e.printStackTrace();
			module = null;
		} catch (IllegalAccessException e) {
			// WindowManager.getInstance().showProblemDialog(
			// "Problem loading GameModule.",
			// "Illegal Access Exception while loading:\nResource: "
			// + location + "\nDetails: ");
			e.printStackTrace();
			module = null;
		} catch (NullPointerException e) {
			// WindowManager.getInstance().showProblemDialog(
			// "Problem loading GameModule.",
			// "Null Pointer Exception while loading:\nResource: "
			// + location + "\nDetails: ");
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
		SEFrame.getInstance().setStatus("Loading Module ...");
		GameModule module = this.createGameModuleInstance();
		try {
			module.setLocation(location);
			// read the module to memory.
			module.load(false);
		} catch (FileNotFoundException e) {
			System.err.println("Module not found at [" + location + "]");
			WindowManager.getInstance().showProblemDialog(
					"Problem loading Game Module",
					"I couldn't find a Game Module at \"" + location
							+ "\". \n\nPlease tell me a new location to use.");
			module = null;
		} catch (IOException e) {
			e.printStackTrace();
			WindowManager
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
				module = loadModule(newLocation);
		}

		return module;
	}

	private File requestNewLocation() {
		final File newLocation;

		FileNameExtensionFilter filter = null;
		// Build the filter based on the translator selected
		filter = new FileNameExtensionFilter(this.getName() + " Game Files",
				this.getLegalExtensions());
		// Otherwise pass in a null filter (defaults to accept all)
		newLocation = WindowManager.getInstance().showFileChooser("Select",
				filter, this.getLocation());

		return newLocation;
	}

	/**
	 * Loads a java class file which holds the code for a custom picker
	 * interface.
	 * 
	 * @param location
	 * @return The java-based picker described by the translator author
	 */
	@SuppressWarnings("unchecked")
	private GameObjectPicker loadCustomGameObjectPicker(File location) {
		// Get the name of the .class file that implements the objPicker
		// interface, and load it.
		GameObjectPicker objPicker = null;

		try {
			// must use Binary Name. Ex: "translators.NWN.data.ErfFile"
			// this.customPicker
			Class<GameObjectPicker> pickerClass = (Class<GameObjectPicker>) loader
					.loadClass(loader.getBinaryNameForClassFile(location));
			objPicker = (pickerClass).newInstance();
		} catch (ClassNotFoundException e) {
			System.err.println("The file: " + location.getAbsolutePath()
					+ " does not appear to be a valid picker.");
			e.printStackTrace();
		} catch (InstantiationException e) {
			System.err.println("The picker at: " + location.getAbsolutePath()
					+ " could not be instantiated.");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.err
					.println("ScriptEase 2 does not have permission to access the file at: "
							+ location.getAbsolutePath()
							+ ", picker could not be loaded.");
			e.printStackTrace();
		}

		return objPicker;
	}

	public GameObjectPicker getCustomGameObjectPicker() {
		final File customPickerPath;

		customPickerPath = this
				.getPathProperty(DescriptionKeys.CUSTOM_PICKER_PATH);

		// Look for a custom picker class.
		if (this.customGameObjectPicker == null && customPickerPath != null)
			this.customGameObjectPicker = loadCustomGameObjectPicker(customPickerPath);

		return this.customGameObjectPicker;
	}

	/**
	 * Returns the libraries associated with the translator, including the
	 * default libraries that are marked by having a null translator. These are
	 * obtained from the Library Manager.
	 * 
	 * @return
	 */
	public Collection<LibraryModel> getLibraries() {
		final Collection<LibraryModel> libraries;

		libraries = new ArrayList<LibraryModel>();

		for (LibraryModel library : LibraryManager.getInstance().getLibraries())
			if (library.getTranslator() == this
					|| library.getTranslator() == null)
				libraries.add(library);

		return libraries;
	}

	/**
	 * Returns only the libraries associated with the translator. These are
	 * obtained from the Library Manager.
	 * 
	 * Note that this method does not return default libraries that should be
	 * common across all translators.
	 * 
	 * @return
	 */
	public Collection<LibraryModel> getLibrariesSpecificToTranslator() {
		final Collection<LibraryModel> libraries;

		libraries = new ArrayList<LibraryModel>();

		for (LibraryModel library : LibraryManager.getInstance().getLibraries())
			if (library.getTranslator() == this)
				libraries.add(library);

		return libraries;
	}

	/**
	 * Private implementation of ClassLoader used to load user-defined
	 * implementations of the GameModule interface. Each instance of this will
	 * then be called on to load all of the classes requested by the GameModule
	 * implementation as well.<br>
	 * <br>
	 * There should be one of these per translator.
	 * 
	 * @author graves
	 * @author remiller
	 */
	private class GameModuleClassLoader extends ClassLoader {
		private final Translator translator;

		/**
		 * 
		 * @param parent
		 *            The loader to defer to first for loading classes.
		 * @param translator
		 *            The translator that this loader is loading for.
		 */
		public GameModuleClassLoader(ClassLoader parent, Translator translator) {
			super(parent);

			this.translator = translator;
		}

		/**
		 * Attempts to resolve the given binary name as a file path, then
		 * attempts to load that file as an object.
		 */
		@Override
		protected Class<?> findClass(String binaryName)
				throws ClassNotFoundException {
			// Try to load the class manually.
			final File pathToClass = this.getClassFileForBinaryName(binaryName);
			byte classData[] = new byte[0];
			final FileInputStream fileIn;
			Class<?> result = null;

			try {
				if (pathToClass == null || !pathToClass.exists())
					throw new FileNotFoundException();

				fileIn = new FileInputStream(pathToClass);
				byte[] buffer = new byte[2048];
				int n;

				while ((n = fileIn.read(buffer)) >= 0) {
					classData = Arrays.copyOf(classData, classData.length + n);
					System.arraycopy(buffer, 0, classData,
							classData.length - n, n);
				}

				result = defineClass(null, classData, 0, classData.length);

				if (result == null)
					throw new ClassFormatError();
			} catch (SecurityException e) {
				throw new ClassNotFoundException(
						"Read access was denied when trying to open: "
								+ pathToClass.getAbsolutePath());
			} catch (FileNotFoundException e) {
				throw new ClassNotFoundException("No file exists at: "
						+ pathToClass);
			} catch (IOException e) {
				throw new ClassNotFoundException(
						"An I/O error occurred when trying to read: "
								+ pathToClass.getAbsolutePath());
			} catch (ClassFormatError e) {
				throw new ClassNotFoundException(
						"A valid .class file does not exist at: "
								+ pathToClass.getAbsolutePath());
			} catch (ClassCastException e) {
				throw new ClassNotFoundException("The class found at: "
						+ pathToClass.getAbsolutePath()
						+ " is not an implementation of GameModule.");
			}

			return result;
		}

		/**
		 * Attempts to resolve the given File to a .class file, then attempts to
		 * load it using a custom class loader.
		 * 
		 * @param gameModuleFile
		 * @return A new instance of the subclass of GameModule found at the
		 *         <code>gameModulePath</code> location. If an exception is
		 *         thrown, null is returned instead.
		 */
		@SuppressWarnings("unchecked")
		public Class<? extends GameModule> loadGameModuleClass(
				File gameModuleFile) {
			String className;

			// Get the name of the .class file that implements the GameModule
			// interface, and load it.
			try {
				// must use Fully Qualified Binary Name.
				className = this.getBinaryNameForClassFile(gameModuleFile);

				if (className == null)
					throw new ClassNotFoundException(
							"Failed to parse class name.");

				Class<? extends GameModule> loadedClass = (Class<? extends GameModule>) this
						.loadClass(className);

				return loadedClass;
			} catch (ClassNotFoundException e) {
				handleFailedClassLoad(e,
						"The Game Module implementation class could not be loaded");
			}
			// catch (InstantiationException e) {
			// handleFailedClassLoad(e,
			// "The Game Module implementation class could not be instantiated");
			// }
			// catch (IllegalAccessException e) {
			// handleFailedClassLoad(e,
			// "The Game Module implementation class could not be accessed");
			// }
			catch (ClassCastException e) {
				handleFailedClassLoad(e,
						"The Game Module implementation class is not a subclass of GameModule");
			}

			return null;
		}

		private void handleFailedClassLoad(Throwable e, String dialogMessage) {
			System.err.println("The game module class could not be loaded: "
					+ dialogMessage);
			e.printStackTrace();
			// WindowManager.getInstance().showProblemDialog(
			// "Game Module implementation class loading failed.\n",
			// dialogMessage + ": \n");
		}

		/**
		 * Converts the given binary name into the path of the .class file.
		 * 
		 * @param binaryName
		 * @return A new File object pointing to the location of the .class file
		 *         specified by the binary name.
		 */
		private File getClassFileForBinaryName(String binaryName) {
			final File result;
			binaryName = "translators."
					+ this.translator.getLocation().getParentFile().getName()
					+ "." + binaryName;

			result = new File(binaryName.replace('.', File.separatorChar)
					+ ".class");

			return result.exists() ? result : null;
		}

		/**
		 * Converts the absolute path of a file object into the binary name for
		 * that .class file.
		 * 
		 * @param file
		 * @return The binary name for the given file. Example:
		 *         "translators.NWN.data.ErfFile"
		 */
		private String getBinaryNameForClassFile(File file) {
			String result = null;
			int namespaceStartIndex = file.getAbsolutePath().indexOf(
					"translators");

			if (namespaceStartIndex != -1) {
				result = (FileOp.removeExtension(file)).getAbsolutePath()
						.substring(namespaceStartIndex);
				result = result.replace(File.separatorChar, '.');

				// pull off the translators.<translatorDir> part. We stick this
				// back in findClass. I know it seems silly, but there are
				// intricacies in how it looks for classes where this is a clean
				// solution. - remiller
				result = result.substring(result.indexOf(".") + 1);
				result = result.substring(result.indexOf(".") + 1);
			}

			return result;
		}
	}

	@Override
	public String toString() {
		return "Translator [" + this.getName() + "]";
	}
}

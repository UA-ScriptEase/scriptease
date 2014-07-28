package scriptease;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import scriptease.controller.FileManager;
import scriptease.controller.exceptionhandler.ScriptEaseExceptionHandler;
import scriptease.gui.WindowFactory;
import scriptease.gui.internationalization.Il8nResources;
import scriptease.translator.TranslatorManager;
import scriptease.util.FileOp;

/**
 * Main ScriptEase class. It is responsible for the running and exiting
 * behaviour of the entire application. It is also responsible for being the
 * access point for ScriptEase user preferences and configuration properties.<br>
 * <br>
 * This class implements the Singleton Pattern.
 * 
 * @author remiller
 * @author mfchurch
 */
public final class ScriptEase implements Runnable {
	// Set to true to hide stuff we don't want people to see
	private static final boolean SE250RELEASE = true;

	// property and configuration keys
	public static final String LOOK_AND_FEEL_KEY = "UseJavaUI";
	public static final String TITLE = "ScriptEase II";
	public static final String NO_VERSION_INFORMATION = "(No version information available)";

	private final String version;
	private final String specificVersion;

	/**
	 * This is an enumeration of all of the possible keys to use to get
	 * information from the Configuration File. Each key is case sensitive.
	 * 
	 * @author remiller
	 */
	public enum ConfigurationKeys {
		TranslatorsDirectory, BugServer;
	}

	public static final String RECENT_FILE_PREFIX = "recentFile";
	public static final String DEBUG_KEY = "debug";
	public static final String OUTPUT_DIRECTORY_KEY = "outputDirectory";
	public static final String FONT_SIZE_KEY = "FontSize";
	public static final String UNDO_STACK_SIZE_KEY = "MaxUndoSteps";

	/**
	 * If <code>true</code> then some extra features useful to debugging will be
	 * enabled.
	 */
	public static boolean DEBUG_MODE;

	/**
	 * location of the config file. See {@link #configuration}
	 */
	private static final String CONFIG_FILE_LOCATION = "scriptease/resources/scriptease.ini";

	/**
	 * location of the files used as defaults for the user preferences. See
	 * {@link #userPreferences}
	 */
	private static final String DEFAULT_PREFS_LOCATION = "scriptease/resources/default_preferences.ini";

	/**
	 * location of the files used as defaults for the user preferences. See
	 * {@link #userPreferences}
	 */
	private static final String USER_PREFS_LOCATION = "user_preferences.ini";

	/**
	 * Stores ScriptEase's run and build information
	 */
	private final Properties configuration;

	/**
	 * Stores defaults for user-alterable properties.
	 */
	private final Properties defaultPrefs;

	/**
	 * Stores the user-alterable properties
	 */
	private final Properties userPreferences;

	private static final ScriptEase instance = new ScriptEase();

	/**
	 * Default, generic error code that means
	 * "Something bad happened somewhere". <br>
	 * <br>
	 * Avoid using this error code if a more specific error code exists. If a
	 * more specific error code does <i>not</i> exist, then consider creating
	 * it.
	 */
	public static final int ERROR_CODE_UNSPECIFIED = -1;
	// TODO: make this a hidden file. Left undone since this is OS-dependent and
	// will be easier to do in Java 1.7
	/**
	 * Directory that houses all of the user-specific data. This data will live
	 * in a directory that is unique to the user like their home directory on
	 * linux, or My Documents on Windows.
	 */
	private static final File SCRIPTEASE_USER_DATA_DIR = new File(
			System.getProperty("user.home"), "ScriptEase");

	/**
	 * This is private because of the singleton pattern. Use getInstance()
	 * instead.
	 * 
	 * @throws IOException
	 */
	private ScriptEase() {
		final Manifest mf;

		Thread.setDefaultUncaughtExceptionHandler(ScriptEaseExceptionHandler
				.getInstance());

		// TODO: actually use the user's locale
		Il8nResources.init(Locale.getDefault());

		this.configuration = new Properties();
		this.defaultPrefs = new Properties();
		// load user prefs with the defaults predefined
		this.userPreferences = new Properties(this.defaultPrefs);

		mf = FileOp.getScriptEaseManifest();

		if (mf != null) {
			this.version = mf.getMainAttributes().getValue(
					Attributes.Name.IMPLEMENTATION_VERSION);
			this.specificVersion = mf.getMainAttributes().getValue(
					new Name("Implementation-Hash"));
		} else {
			this.specificVersion = this.version = ScriptEase.NO_VERSION_INFORMATION;
		}

		// now we set up ScriptEase as per the config files.
		this.configure();
	}

	/**
	 * Gets the sole instance of ScriptEase as per the Singleton design pattern.
	 * 
	 * @return The sole instance of ScriptEase.
	 */
	public static ScriptEase getInstance() {
		return ScriptEase.instance;
	}

	public static boolean is250Release() {
		return SE250RELEASE;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ScriptEase se = ScriptEase.getInstance();

		se.unpackRequiredLibs();

		WindowFactory.showProgressBar("Loading ScriptEase...", new Runnable() {
			@Override
			public void run() {
				// pre-load the translatorManager to give functionality to the
				// program
				TranslatorManager.getInstance();
			}
		});

		SwingUtilities.invokeLater(se);
	}

	/**
	 * Unpacks the required libraries into the running directory. This only
	 * occurs if we're running from a Jar. <br>
	 * <br>
	 * We need to do this if we're running from a Jar because the current Java
	 * version is the son of a flaming goat and can't handle classpath entries
	 * that are referencing jars inside a jar. Way to go, Sun.
	 */
	private void unpackRequiredLibs() {
		final CodeSource src = ScriptEase.class.getProtectionDomain()
				.getCodeSource();
		final URL jarUrl;
		BufferedOutputStream dest;
		final int bufferSize = 2048;
		final String libLocation = "scriptease/resources/lib";
		File requiredLib = null;
		final ZipInputStream zipInput;
		ZipEntry entry;

		if (src == null)
			return;

		jarUrl = src.getLocation();

		try {
			zipInput = new ZipInputStream(jarUrl.openStream());

			while ((entry = zipInput.getNextEntry()) != null) {
				String entryName = entry.getName();

				// only write out the actual files not the directory
				if (entryName.startsWith(libLocation) && !entry.isDirectory()
						&& !entryName.equals(libLocation)) {
					requiredLib = new File("lib",
							entryName.substring(libLocation.length()));

					System.err.println("Extracting \"" + entry + "\" to \""
							+ requiredLib + "\"");

					requiredLib.getParentFile().mkdirs();
					requiredLib.createNewFile();

					int count;
					byte data[] = new byte[bufferSize];

					// write the file to disk
					dest = new BufferedOutputStream(new FileOutputStream(
							requiredLib), bufferSize);
					while ((count = zipInput.read(data, 0, bufferSize)) >= 0) {
						dest.write(data, 0, count);
					}

					dest.flush();
					dest.close();
				}
			}

			zipInput.close();
		} catch (Throwable e) {
			JOptionPane.showMessageDialog(null,
					"Failed to unpack required library " + requiredLib,
					"ScriptEase Startup Error", JOptionPane.ERROR_MESSAGE);

			// remove the last one we wrote because it might be corrupt.
			if (requiredLib != null && requiredLib.exists())
				requiredLib.delete();

			Toolkit.getDefaultToolkit().beep();
			e.printStackTrace();

			this.exit(ScriptEase.ERROR_CODE_UNSPECIFIED);
		}
	}

	@Override
	public void run() {
		WindowFactory.getInstance().buildAndShowMainFrame();
	}

	/**
	 * Loads the configuration files and performs any changes needed to
	 * configure ScriptEase to conform to those loaded preferences.
	 */
	private void configure() {
		// everything in this method requires that the preference and
		// configuration files are loaded first. -remiller
		this.loadConfigurations();

		// check if we are debugging
		this.checkDebugging();

		// now we can configure
		this.chooseLookAndFeel();

		this.setUIConstants();
	}

	private void setUIConstants() {
		UIManager.getDefaults().put("TabbedPane.contentBorderInsets",
				new Insets(0, 0, 0, 0));
		UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", true);
		UIManager.put("ProgressBar.selectionForeground", Color.black);
		UIManager.put("ProgressBar.selectionBackground", Color.black);
	}

	private void checkDebugging() {
		ScriptEase.DEBUG_MODE = Boolean.parseBoolean(this
				.getPreference(ScriptEase.DEBUG_KEY));
	}

	private void loadConfigurations() {
		// load config
		this.loadConfiguration(this.configuration,
				FileOp.getFileResource(ScriptEase.CONFIG_FILE_LOCATION));

		this.ensureConfigDirectories();

		// load default user prefs
		this.loadConfiguration(this.defaultPrefs,
				FileOp.getFileResource(ScriptEase.DEFAULT_PREFS_LOCATION));

		// load saved user prefs
		File userFile = FileOp.getFileResource(ScriptEase.USER_PREFS_LOCATION);
		if ((userFile == null || !userFile.exists())) {
			System.err
					.println("User preferences file not found, attempting to create it...");

			userFile = new File(".", ScriptEase.USER_PREFS_LOCATION);

			try {
				userFile.createNewFile();
			} catch (IOException e) {
				// I can't think of anything better to do here except let
				// ScriptEase explode. - remiller
				Thread.getDefaultUncaughtExceptionHandler().uncaughtException(
						Thread.currentThread(), new IOError(e));
			}

			if (userFile.exists())
				System.err.println("User preferences file created!");
		}
		/*
		 * TODO Eventually this property will get removed. We need to fix up all
		 * of our comparison methods. Google information about "timsort" and
		 * "java 1.7" issues.
		 * 
		 * NOTE! This is sometimes not respected by Java, in which case
		 * exceptions get thrown around anyways, because why not? So we'll have
		 * to fix all of our compare methods, although I have no idea how.
		 * 
		 * Ticket: 31529443
		 */
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

		this.loadConfiguration(this.userPreferences, userFile);
	}

	/**
	 * Ensures that all directories (and only directories) required by the
	 * config file exist.
	 */
	private void ensureConfigDirectories() {
		final List<ConfigurationKeys> requiredLocationKeys = new ArrayList<ConfigurationKeys>();
		File file;
		String path;

		/*
		 * This is the list of keys for the required directories. Add new
		 * directory dependencies here as needed. We have to explicitly list
		 * just the directories because it's impossible to tell the difference
		 * between files and directories by the path alone. - remiller
		 */
		requiredLocationKeys.add(ConfigurationKeys.TranslatorsDirectory);

		for (ConfigurationKeys key : requiredLocationKeys) {
			path = this.getConfiguration(key);

			if (path == null) {
				String message = "Configuration file is expected to contain entry for key "
						+ key + " but not such entry was found!";

				System.err.println(message + " Eject eject eject!");
				throw new Error(new FileNotFoundException(message));
			}

			file = new File(path);

			// make translators live relative to the current running directory,
			// rather than the default of user.dir. Everything else should
			// go in the user data directory.
			if (!file.isAbsolute()) {
				if (key.equals(ConfigurationKeys.TranslatorsDirectory))
					file = new File(".", file.getPath());
				else
					file = new File(ScriptEase.SCRIPTEASE_USER_DATA_DIR,
							file.getPath());
			}

			if (!file.exists() && !file.mkdirs())
				throw new Error(new FileNotFoundException(
						"Failed to create required directory "
								+ file.getAbsolutePath()));
		}
	}

	private void loadConfiguration(Properties config, File location) {
		final InputStream loadStream;

		try {
			loadStream = new FileInputStream(location);

			config.load(loadStream);

			loadStream.close();
		} catch (IOException e) {
			// I can't think of anything better to do here except let ScriptEase
			// explode. - remiller
			Thread.getDefaultUncaughtExceptionHandler().uncaughtException(
					Thread.currentThread(), new IOError(e));
		}
	}

	/**
	 * Determines and returns a configuration property.
	 * 
	 * @param configKey
	 *            The unique enumerated string to use to look up the property.
	 * @return The value assigned to the property labeled with configKey
	 */
	public String getConfiguration(ConfigurationKeys configKey) {
		return this.configuration.getProperty(configKey.toString());
	}

	/**
	 * Determines and returns a user preference.
	 * 
	 * @param preferenceKey
	 *            The unique string use to to look up the preference.
	 * @return The value assigned to the given key, or <code>null</code> if the
	 *         property is not found.
	 */
	public String getPreference(String preferenceKey) {
		return this.userPreferences.getProperty(preferenceKey);
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
	public void setPreference(String preferenceKey, String preferenceValue) {
		if (preferenceValue == null)
			this.userPreferences.remove(preferenceKey);
		else
			this.userPreferences.setProperty(preferenceKey, preferenceValue);
	}

	/**
	 * Tries to set the look and feel to the platform specific L&F
	 */
	private void chooseLookAndFeel() {
		final boolean useJavaUI = Boolean.parseBoolean(this
				.getPreference(ScriptEase.LOOK_AND_FEEL_KEY));
		if (useJavaUI)
			return;

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			// If you want to experiment with making SEII look like garbage,
			// uncomment this line!
			// UIManager.setLookAndFeel(UIManager.getInstalledLookAndFeels()[1].getClassName());
		} catch (ClassNotFoundException e) {
			this.handleUIFailure();
			return;
		} catch (InstantiationException e) {
			this.handleUIFailure();
			return;
		} catch (IllegalAccessException e) {
			this.handleUIFailure();
			return;
		} catch (UnsupportedLookAndFeelException e) {
			this.handleUIFailure();
			return;
		}
	}

	private void handleUIFailure() {
		boolean retry = WindowFactory.getInstance().showRetryProblemDialog(
				"Load UI", null);

		if (retry)
			this.chooseLookAndFeel();
	}

	/**
	 * Exits the software normally. Checks for any unsaved work and asks to save
	 * it.
	 */
	public void exit() {
		final FileManager fileMan = FileManager.getInstance();

		if (!fileMan.closeOpenStories())
			return;

		fileMan.deleteTempFiles();

		this.saveUserPrefs();

		System.exit(0);
	}

	/**
	 * Exits the program in an error state, ie total crash. <BR>
	 * <BR>
	 * Does not perform the extra nice things that {@link #exit()} does.
	 * 
	 * @param errorCode
	 *            Error code constant from ScriptEase.java.
	 */
	public void exit(int errorCode) {
		System.exit(errorCode);
	}

	/**
	 * Saves the user preferences to disk
	 */
	public void saveUserPrefs() {
		FileOutputStream out;
		File userPrefsFile = new File(ScriptEase.USER_PREFS_LOCATION);

		try {
			if (!userPrefsFile.exists()) {
				System.err
						.println("User preferences file not found, attempting to create it...");
				if (userPrefsFile.createNewFile())
					System.err.println("User preferences file created!");
				else
					throw new IOException(
							"Failed to create user preferences file.");
			}

			out = new FileOutputStream(userPrefsFile);

			this.userPreferences.store(out, "ScriptEase User Preferences File");

			out.close();
		} catch (IOException e) {
			System.err.println("Error saving user preferences. Aborting.");
			e.printStackTrace();
		}
	}

	/**
	 * Gets the ScriptEase version as determined by the Jar's Manifest file. If
	 * not running from the jar, this may not be useful data.
	 * 
	 * @return The version of ScriptEase.
	 */
	public String getVersion() {
		return this.version;
	}

	/**
	 * Gets the ScriptEase version control revision as determined by the Jar's
	 * Manifest file. If not running from the jar, this may not be useful data.
	 * 
	 * @return The specific revision of ScriptEase.
	 */
	public String getCommitHash() {
		return this.specificVersion;
	}
}

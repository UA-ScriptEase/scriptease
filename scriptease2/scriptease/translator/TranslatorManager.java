package scriptease.translator;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import scriptease.ScriptEase;
import scriptease.ScriptEase.ConfigurationKeys;
import scriptease.controller.observer.PatternModelEvent;
import scriptease.controller.observer.PatternModelObserver;
import scriptease.controller.observer.TranslatorObserver;
import scriptease.gui.StatusManager;
import scriptease.gui.WindowFactory;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;
import scriptease.translator.apimanagers.GameTypeManager;

/**
 * Singleton class that manages the available translators for different games.
 * Translators themselves are configuration files that point to the location of:<br>
 * <br>
 * <ul>
 * <li>The "translator.ini" file itself.</li>
 * <li>An <code>APIXMLInterpreter</code> XML file.</li>
 * <li>A <code>FormatXMLInterpreter</code> XML file.</li>
 * <li>A .class file containing an implementation of the <code>GameModule</code>
 * interface.</li>
 * </ul>
 * 
 * @author graves
 */
public class TranslatorManager implements PatternModelObserver {
	private static final String NO_TRANSLATORS_PROBLEM = "ScriptEase could not locate any valid game translators in its \"translators\" directory. "
			+ "\n\nYou will not be able to open Story files or perform any other game-specific operations.";

	private Set<Translator> translatorPool;

	private Translator activeTranslator;

	private static TranslatorManager instance = new TranslatorManager();

	private final List<WeakReference<TranslatorObserver>> observers;

	/**
	 * Returns the singleton instance of the TranslatorManager.
	 * 
	 * @return the sole instance of the TranslatorManager.
	 */
	public static TranslatorManager getInstance() {
		return TranslatorManager.instance;
	}

	/**
	 * Private constructor of the singleton TranslatorManager. Scans for
	 * available translators, then attempts to load the last active translator
	 * from the user preferences file. If unsuccessful, it loads the first
	 * translator as the default.
	 */
	private TranslatorManager() {
		this.translatorPool = new HashSet<Translator>();
		this.observers = new ArrayList<WeakReference<TranslatorObserver>>();

		// scan for translators in the translators folder
		this.fillTranslatorPool();
		PatternModelManager.getInstance().addPatternModelObserver(this);
	}

	/**
	 * Searches the "translators" directory and attempts to load all translators
	 * found inside.
	 */
	private void fillTranslatorPool() {
		final File translatorsDirectory;
		final FileFilter iniFilter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.getName().equalsIgnoreCase(
						Translator.TRANSLATOR_DESCRIPTION_FILE_NAME)
						&& file.isFile();
			}
		};
		File[] contents;

		// Search for "translator.ini" files in direct sub-directories of our
		// "translators" folder.
		translatorsDirectory = new File(ScriptEase.getInstance()
				.getConfiguration(ConfigurationKeys.TranslatorsDirectory));

		System.out.println("Looking for translators in "
				+ translatorsDirectory.getAbsolutePath());

		if (translatorsDirectory.isDirectory()) {
			for (File translator : translatorsDirectory.listFiles()) {
				if (!translator.isDirectory())
					continue;

				contents = translator.listFiles(iniFilter);

				// We use only the first "translator.ini"
				if (contents != null && contents.length > 0)
					this.addTranslator(contents[0]);
			}
		}

		if (this.translatorPool.isEmpty())
			WindowFactory.getInstance().showInformationDialog("No Translators",
					TranslatorManager.NO_TRANSLATORS_PROBLEM);
	}

	/**
	 * Adds the given translator File to the list of translator Files if it is
	 * not null and not already in the list.
	 * 
	 * @param newTranslatorLocation
	 * @return Whether the translator was added or not.
	 */
	private boolean addTranslator(File newTranslatorLocation) {
		Translator newTranslator = null;

		try {
			newTranslator = new Translator(newTranslatorLocation);
		} catch (IOException e) {
			boolean retry;
			System.err.println("Failed to load translator: "
					+ newTranslatorLocation.getAbsolutePath());

			retry = WindowFactory.getInstance().showRetryProblemDialog(
					"Failed to load translator.",

					"An I/O exception occurred while attempting to load the "
							+ newTranslatorLocation.getName() + " translator.");

			if (retry)
				return this.addTranslator(newTranslatorLocation);
		} catch (IllegalArgumentException e) {
			System.err.println("Tried to load translator: "
					+ newTranslatorLocation.getAbsolutePath()
					+ " but that's not a translator at all.");
			return false;
		}

		if (!newTranslator.isValid()) {
			System.err
					.println("Translator failed validation ("
							+ newTranslatorLocation.getAbsolutePath()
							+ "). Moving on.");

			WindowFactory
					.getInstance()
					.showProblemDialog(
							"Invalid translator",
							"The "
									+ newTranslator.getName()
									+ " translator is not valid and will not be loaded.");

			newTranslator = null;
		}

		return newTranslator != null && this.translatorPool.add(newTranslator);
	}

	/**
	 * Gets all translators that ScriptEase knows about.
	 * 
	 * @return all translators
	 */
	public Collection<Translator> getTranslators() {
		return new ArrayList<Translator>(this.translatorPool);
	}

	/**
	 * Gets the translator with the given name, ignoring case.
	 * 
	 * @param name
	 *            The case-insensitive name of the translator to use.
	 * 
	 * @return The translator that matches the given name, or <code>null</code>
	 */
	public Translator getTranslator(String name) {
		for (Translator translator : this.translatorPool) {
			if (translator.getName().equalsIgnoreCase(name))
				return translator;
		}

		return null;
	}

	/**
	 * Shortcut for getting the translator from the active Story Model.
	 * 
	 * @return the active translator. Can be <code>null</code> when there is no
	 *         active translator.
	 */
	public Translator getActiveTranslator() {
		return this.activeTranslator;
	}

	/**
	 * Shortcut method to get the active API Dictionary of the current active
	 * translator.
	 * 
	 * @return
	 */
	public APIDictionary getActiveAPIDictionary() {
		return this.activeTranslator.getApiDictionary();
	}

	/**
	 * Shortcut method to get the active Game Type Manager of the current active
	 * translator.
	 * 
	 * @return
	 */
	public GameTypeManager getActiveGameTypeManager() {
		return this.activeTranslator.getGameTypeManager();
	}

	/**
	 * Registers an observer to be notified when the TranslatorManager loads a
	 * translator
	 * 
	 * @param observer
	 *            the observer to register
	 */
	public void addTranslatorObserver(TranslatorObserver observer) {
		Collection<WeakReference<TranslatorObserver>> observersCopy = new ArrayList<WeakReference<TranslatorObserver>>(
				this.observers);

		for (WeakReference<TranslatorObserver> observerRef : observersCopy) {
			if (observerRef == null)
				continue;

			final TranslatorObserver translatorObserver = observerRef.get();
			if (translatorObserver != null && translatorObserver == observer)
				return;
			else if (translatorObserver == null)
				this.observers.remove(observerRef);
		}

		this.observers.add(new WeakReference<TranslatorObserver>(observer));
	}

	/**
	 * Unregisters an observer to be notified when the TrasnlatorManager loads a
	 * translator
	 * 
	 * @param observer
	 *            the observer to register
	 */
	public void removeTranslatorObserver(TranslatorObserver observer) {
		for (WeakReference<TranslatorObserver> reference : this.observers) {
			if (reference.get() == observer) {
				this.observers.remove(reference);
				return;
			}
		}
	}

	private void notifyObservers() {
		Collection<WeakReference<TranslatorObserver>> observersCopy = new ArrayList<WeakReference<TranslatorObserver>>(
				this.observers);

		for (WeakReference<TranslatorObserver> observerRef : observersCopy) {
			TranslatorObserver graphNodeObserver = observerRef.get();
			if (graphNodeObserver != null)
				graphNodeObserver.translatorLoaded(this.activeTranslator);
			else
				this.observers.remove(observerRef);
		}
	}

	/**
	 * Determines the appropriate Translators for loading a particular Game
	 * Module to load based on the given file path. It does this by checking the
	 * translatorPool's translator.ini extensions and comparing them to the
	 * extension of the given file. If no match is found, logs an error and
	 * returns null.
	 * 
	 * @param module
	 *            The file whose extension will be tested.
	 * @return all translators that could open the given module
	 */
	public Collection<Translator> getTranslatorsForGameModuleFile(
			File moduleFile) {
		final Collection<Translator> supporters = new ArrayList<Translator>();

		for (Translator translator : this.translatorPool) {
			if (translator.supportsModuleFile(moduleFile))
				supporters.add(translator);
		}

		if (supporters.isEmpty())
			System.err
					.println("Could not determine an appropriate translator for the game file: "
							+ moduleFile.getAbsolutePath());

		return supporters;
	}

	/**
	 * Gets the Translator that is defined on disk at <code>location</code>. If
	 * no translator exists there, then <code>null</code> is returned.
	 * 
	 * @param location
	 *            the location to search for.
	 * @return the translator located at <code>location</code>, or
	 *         <code>null</code> if no such translator exists.
	 */
	public Translator getTranslatorByLocation(File location) {
		for (Translator translator : this.translatorPool) {
			if (translator.getLocation().equals(location))
				return translator;
		}

		return null;
	}

	/**
	 * Returns the type keywords active for the current translator.
	 * 
	 * @return
	 */
	public Collection<String> getActiveTypeKeywords() {
		return this.getActiveTranslator().getGameTypeManager().getKeywords();
	}

	public void setActiveTranslator(Translator translator) {
		if (this.activeTranslator == translator)
			return;
		this.activeTranslator = translator;
		if (translator != null)
			StatusManager.getInstance().setStatus(
					translator.getName() + " translator loaded");

		this.notifyObservers();
	}

	@Override
	public void modelChanged(PatternModelEvent event) {
		final short eventType = event.getEventType();
		final PatternModel model = event.getPatternModel();
		Translator translator = (model == null ? null : model.getTranslator());

		if (eventType == PatternModelEvent.PATTERN_MODEL_ACTIVATED) {
			if (this.activeTranslator != translator) {
				this.setActiveTranslator(translator);
			}
		} else if (eventType == PatternModelEvent.PATTERN_MODEL_REMOVED) {
			if (!PatternModelManager.getInstance().usingTranslator(translator)) {
				this.setActiveTranslator(null);
			}
		}
	}
}

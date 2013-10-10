package scriptease.translator;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.ScriptEase;
import scriptease.ScriptEase.ConfigurationKeys;
import scriptease.controller.observer.ObserverManager;
import scriptease.controller.observer.SEModelEvent;
import scriptease.controller.observer.SEModelObserver;
import scriptease.controller.observer.TranslatorObserver;
import scriptease.gui.StatusManager;
import scriptease.gui.WindowFactory;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * Singleton class that manages the available translators for different games.
 * Translators themselves are configuration files that point to the location of:<br>
 * <br>
 * <ul>
 * <li>The "translator.ini" file itself.</li>
 * <li>A <code>LibraryInterpreter</code> XML file.</li>
 * <li>A <code>FormatXMLInterpreter</code> XML file.</li>
 * <li>A .class file containing an implementation of the <code>GameModule</code>
 * interface.</li>
 * </ul>
 * 
 * @author graves
 * @author jyuen
 */
public class TranslatorManager {

	private Set<Translator> translatorPool;

	private Translator activeTranslator;

	private static TranslatorManager instance = new TranslatorManager();

	private final ObserverManager<TranslatorObserver> observerManager;

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
		this.observerManager = new ObserverManager<TranslatorObserver>();

		// scan for translators in the translators folder
		this.fillTranslatorPool();

		final SEModelObserver observer;

		observer = new SEModelObserver() {
			@Override
			public void modelChanged(SEModelEvent event) {
				final SEModel model = event.getPatternModel();

				if (model != null) {
					final TranslatorManager manager = TranslatorManager.this;
					final SEModelEvent.Type eventType = event.getEventType();
					final Translator translator = model.getTranslator();
					final Translator active = manager.activeTranslator;

					if (eventType == SEModelEvent.Type.ACTIVATED) {
						// A model is activated with a different translator
						if (translator != active) {
							manager.setActiveTranslator(translator);
						}
					} else if (eventType == SEModelEvent.Type.REMOVED) {
						final SEModelManager modelManager;

						modelManager = SEModelManager.getInstance();
						// A model is removed and it's translators aren't used
						if (!modelManager.usingTranslator(translator)
								&& !modelManager.hasActiveModel()) {
							manager.setActiveTranslator(null);
						}
					}
				}
			}
		};
		SEModelManager.getInstance().addSEModelObserver(this, observer);

		System.out.println("Finished loading " + this.translatorPool.size()
				+ " translators.");
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

				if (contents != null && contents.length > 0) {
					// We use only the first "translator.ini"
					final File translatorInfo = contents[0];
					this.addTranslator(translatorInfo);
				}
			}
		}
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

		final String validationMessage = newTranslator.isValid();
		if (!validationMessage.equals("")) {
			System.err
					.println("Translator failed validation ("
							+ newTranslatorLocation.getAbsolutePath()
							+ "). Moving on.");

			WindowFactory
					.getInstance()
					.showProblemDialog(
							"Invalid translator",
							"There's a validation problem with the "
									+ newTranslator.getName()
									+ " translator, so I can't use it. \n\nProblem:\n"
									+ validationMessage
									+ "\n\n"
									+ "You will not be able to open Story files or perform any "
									+ "other game-specific operations related to this translator.");

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
	 * Shortcut method to get the active default library of the current active
	 * translator.
	 * 
	 * @return
	 */
	public LibraryModel getActiveDefaultLibrary() {
		if (this.activeTranslator == null)
			return null;
		return this.activeTranslator.getLibrary();
	}

	/**
	 * Registers an observer to be notified when the TranslatorManager loads a
	 * translator
	 * 
	 * @param observer
	 *            the observer to register
	 */
	public void addTranslatorObserver(Object object, TranslatorObserver observer) {
		this.observerManager.addObserver(object, observer);
	}

	/**
	 * Unregisters an observer to be notified when the TrasnlatorManager loads a
	 * translator
	 * 
	 * @param observer
	 *            the observer to register
	 */
	public void removeTranslatorObserver(TranslatorObserver observer) {
		this.observerManager.removeObserver(observer);
	}

	private void notifyObservers() {
		for (TranslatorObserver observer : this.observerManager.getObservers())
			observer.translatorLoaded(this.activeTranslator);
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
			if (!translator.moduleLoadsDirectories()
					&& translator.createModuleFileFilter().accept(moduleFile))
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

	public void setActiveTranslator(Translator translator) {
		if (this.activeTranslator == translator)
			return;
		this.activeTranslator = translator;

		final String message = translator + " activated.";

		StatusManager.getInstance().setTemp(message);

		if (translator != null) {
			// We have this here so we don't run into issues later if the
			translator.getLibrary();
			translator.getOptionalLibraries();
		}

		this.notifyObservers();
	}
}

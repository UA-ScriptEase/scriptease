package scriptease.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import scriptease.controller.observer.ObserverManager;
import scriptease.controller.observer.SEModelEvent;
import scriptease.controller.observer.SEModelObserver;
import scriptease.controller.observer.TranslatorObserver;
import scriptease.controller.observer.library.LibraryEvent;
import scriptease.controller.observer.library.LibraryManagerEvent;
import scriptease.controller.observer.library.LibraryManagerObserver;
import scriptease.controller.observer.library.LibraryObserver;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;

/**
 * Manages all of the Libraries in ScriptEase.
 * 
 * Needs to observe:
 * <ol>
 * <li>StoryModelPool so it can unload translators from the library when they
 * are no longer needed.</li>
 * 
 * <li>TranslatorManager so it can add new translators to the library, and</li>
 * 
 * <li>It's child LibraryModels, so it can notify the view in the event of an
 * element being added or removed.</li>
 * </ol>
 * 
 * Implements the Singleton Design Pattern
 * 
 * @author mfchurch
 */
public class LibraryManager implements LibraryObserver {

	final private static LibraryManager instance = new LibraryManager();
	private static final String SCRIPTEASE_LIBRARY = "ScriptEase";
	private final LibraryModel scriptEaseLibrary;
	private Collection<LibraryModel> libraries;
	private final Map<Translator, LibraryModel> loadedTranslators;

	private final ObserverManager<LibraryManagerObserver> observerManager;

	private LibraryManager() {
		this.libraries = new CopyOnWriteArraySet<LibraryModel>();
		this.loadedTranslators = new HashMap<Translator, LibraryModel>();
		this.observerManager = new ObserverManager<LibraryManagerObserver>();

		final SEModelObserver modelObserver;
		final TranslatorObserver translatorObserver;

		this.scriptEaseLibrary = new LibraryModel(SCRIPTEASE_LIBRARY,
				SCRIPTEASE_LIBRARY);

		this.scriptEaseLibrary.add(new AskIt());
		this.scriptEaseLibrary.add(new Note());

		this.add(this.scriptEaseLibrary);

		modelObserver = new SEModelObserver() {
			/*
			 * On StoryModelPoolEvent, If the StoryModelPool no longer uses the
			 * translator, removes the translator from loadedTranslators.
			 */
			@Override
			public void modelChanged(SEModelEvent event) {
				if (event.getEventType() == SEModelEvent.Type.REMOVED)
					for (Translator translator : LibraryManager.this.loadedTranslators
							.keySet()) {
						if (!SEModelManager.getInstance().usingTranslator(
								translator)) {
							// this.remove(loadedTranslators.get(translator));
							LibraryManager.this.loadedTranslators
									.remove(translator);
							break;
						}
					}
			}
		};
		translatorObserver = new TranslatorObserver() {

			/**
			 * On Translator load, if the translator has not previously been
			 * loading into the Library, do so.
			 */
			@Override
			public void translatorLoaded(Translator newTranslator) {
				if (newTranslator != null
						&& !LibraryManager.this.loadedTranslators
								.containsKey(newTranslator)) {
					final LibraryModel translatorLibrary = newTranslator
							.getApiDictionary().getLibrary();
					LibraryManager.this.add(translatorLibrary);
					LibraryManager.this.loadedTranslators.put(newTranslator,
							translatorLibrary);
				}
			}
		};

		TranslatorManager.getInstance().addTranslatorObserver(this,
				translatorObserver);
		SEModelManager.getInstance().addSEModelObserver(this, modelObserver);
	}

	public void add(LibraryModel library) {
		this.libraries.add(library);
		library.addLibraryChangeListener(this);
		this.notifyChange(new LibraryManagerEvent(library,
				LibraryManagerEvent.LIBRARYMODEL_ADDED, null));
	}

	public void remove(LibraryModel library) {
		this.libraries.remove(library);
		library.removeLibraryChangeListener(this);
		this.notifyChange(new LibraryManagerEvent(library,
				LibraryManagerEvent.LIBRARYMODEL_REMOVED, null));
	}

	public static LibraryManager getInstance() {
		return LibraryManager.instance;
	}

	/**
	 * Gets a collection of references to all of the libraries, including user
	 * libs and API dictionaries.
	 * 
	 * @return a collection of the libraries
	 */
	public Collection<LibraryModel> getLibraries() {
		// TODO Check where this gets called
		return new ArrayList<LibraryModel>(this.libraries);
	}

	/**
	 * Get the list of Libraries which are not Translator Libraries
	 * 
	 * @return
	 */
	public Collection<LibraryModel> getUserLibraries() {
		// TODO This may not work as intended
		Collection<LibraryModel> userLibraries = new ArrayList<LibraryModel>();
		for (LibraryModel library : this.libraries) {
			if (!this.loadedTranslators.containsValue(library)
					&& library != scriptEaseLibrary)
				userLibraries.add(library);
		}
		return userLibraries;
	}

	public LibraryModel getScriptEaseLibrary() {
		return this.scriptEaseLibrary;
	}

	/**
	 * Adds the given observer as a observer of all LibraryManager's libraries
	 * 
	 * @param observer
	 */
	public void addLibraryManagerObserver(Object object,
			LibraryManagerObserver observer) {
		this.observerManager.addObserver(object, observer);
	}

	/**
	 * Notifies the LibraryManager's Observers that a LibraryModel has been
	 * added or removed.
	 */
	private void notifyChange(LibraryManagerEvent event) {
		for (LibraryManagerObserver observer : this.observerManager
				.getObservers())
			observer.modelChanged(event);
	}

	/**
	 * Forwards LibaryModel events to the LibraryManager observers
	 */
	@Override
	public void modelChanged(LibraryModel changed, LibraryEvent event) {
		this.notifyChange(new LibraryManagerEvent(changed,
				LibraryManagerEvent.LIBRARYMODEL_CHANGED, event));
	}
}

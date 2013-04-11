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
	private Collection<LibraryModel> libraries;
	private final Map<Translator, LibraryModel> loadedTranslators;
	private final StoryComponentContainer masterRoot;

	private final ObserverManager<LibraryManagerObserver> observerManager;

	private LibraryManager() {
		this.libraries = new CopyOnWriteArraySet<LibraryModel>();
		this.loadedTranslators = new HashMap<Translator, LibraryModel>();
		this.masterRoot = new StoryComponentContainer("Library");

		this.observerManager = new ObserverManager<LibraryManagerObserver>();

		this.masterRoot.registerChildType(ScriptIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.masterRoot.registerChildType(AskIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.masterRoot.registerChildType(KnowIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.masterRoot.registerChildType(StoryComponentContainer.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.masterRoot.registerChildType(Note.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.masterRoot.registerChildType(ControlIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);

		this.buildDefaultLibrary();

		final SEModelObserver modelObserver;
		final TranslatorObserver translatorObserver;

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
				} /*else {
					for (LibraryModel model : LibraryManager.this.loadedTranslators
							.values()) {
						LibraryManager.this.remove(model);
					}
					LibraryManager.this.loadedTranslators.clear();
				}*/
			}
		};

		TranslatorManager.getInstance().addTranslatorObserver(this,
				translatorObserver);
		SEModelManager.getInstance().addPatternModelObserver(this,
				modelObserver);
	}

	/**
	 * Builds and adds the default ScriptEaseLibrary
	 */
	private void buildDefaultLibrary() {
		final LibraryModel scriptEaseLibrary;
		final AskIt conditional;
		final Note note;

		scriptEaseLibrary = new LibraryModel(LibraryManager.SCRIPTEASE_LIBRARY,
				LibraryManager.SCRIPTEASE_LIBRARY);

		conditional = new AskIt();
		note = new Note();

		scriptEaseLibrary.add(conditional);
		scriptEaseLibrary.add(note);

		this.add(scriptEaseLibrary);
	}

	public void add(LibraryModel library) {
		this.libraries.add(library);

		this.masterRoot.addStoryChild(library.getRoot());

		library.addLibraryChangeListener(this);
		this.notifyChange(new LibraryManagerEvent(library,
				LibraryManagerEvent.LIBRARYMODEL_ADDED, null));
	}

	public void remove(LibraryModel library) {
		this.libraries.remove(library);

		this.masterRoot.removeStoryChild(library.getRoot());

		library.removeLibraryChangeListener(this);
		this.notifyChange(new LibraryManagerEvent(library,
				LibraryManagerEvent.LIBRARYMODEL_REMOVED, null));
	}

	public static LibraryManager getInstance() {
		return LibraryManager.instance;
	}

	/**
	 * Checks if the LibraryManager has any Libraries
	 * 
	 * @return
	 */
	public boolean hasLibraries() {
		return !this.libraries.isEmpty();
	}

	/**
	 * Get's the name of the Libraries where the given StoryComponent is
	 * located. Returns null if the component does not belong to any Libraries
	 * 
	 * @param component
	 * @return
	 */
	public String getLibraryNameFromComponent(StoryComponent component) {
		String name = "";
		for (LibraryModel library : this.libraries) {
			if (library.containsStoryComponent(component))
				name = library.getName() + ", ";
		}
		if (name.isEmpty())
			return null;
		else
			return name.substring(0, name.length() - 2);
	}

	/**
	 * Gets a collection of references to all of the libraries, including user
	 * libs and API dictionaries. For a tree of all StoryComponents from all
	 * libraries use {@link #getLibraryMasterRoot()}.
	 * 
	 * @return a collection of the libraries
	 * @see #getLibraryMasterRoot()
	 */
	public Collection<LibraryModel> getLibraries() {
		return new ArrayList<LibraryModel>(this.libraries);
	}

	/**
	 * Gets the root that has as children the contents of all available
	 * libraries.
	 * 
	 * @return
	 */
	public StoryComponentContainer getLibraryMasterRoot() {
		if (this.masterRoot.getChildCount() <= 0) {
			for (LibraryModel model : this.libraries) {
				if (model != null)
					this.masterRoot.addStoryChild(model.getRoot());
			}
		}

		return this.masterRoot;
	}

	/**
	 * Get the list of Libraries which are not Translator Libraries
	 * 
	 * @return
	 */
	public Collection<LibraryModel> getUserLibraries() {
		Collection<LibraryModel> libraries = this.getLibraries();
		Collection<LibraryModel> userLibraries = new ArrayList<LibraryModel>();
		for (LibraryModel library : libraries) {
			if (!this.loadedTranslators.containsValue(library)
					&& !library.getName().equals(
							LibraryManager.SCRIPTEASE_LIBRARY))
				userLibraries.add(library);
		}
		return userLibraries;
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
	 * Removes the given observer as a observer of all LibraryManager's
	 * libraries
	 * 
	 * @param observer
	 */
	public void removeLibraryChangeListener(LibraryManagerObserver observer) {
		this.observerManager.removeObserver(observer);
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

	/**
	 * Returns if the LibraryManager contains the given LibraryModel
	 * 
	 * @param model
	 * @return
	 */
	public boolean contains(LibraryModel model) {
		return this.libraries.contains(model);
	}
}

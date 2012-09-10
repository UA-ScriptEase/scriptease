package scriptease.model;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import scriptease.controller.observer.PatternModelEvent;
import scriptease.controller.observer.PatternModelObserver;
import scriptease.controller.observer.TranslatorObserver;
import scriptease.controller.observer.library.LibraryEvent;
import scriptease.controller.observer.library.LibraryManagerEvent;
import scriptease.controller.observer.library.LibraryManagerObserver;
import scriptease.controller.observer.library.LibraryObserver;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.translator.Translator;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.GameTypeManager;

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
public class LibraryManager implements TranslatorObserver, LibraryObserver,
		PatternModelObserver {

	final private static LibraryManager instance = new LibraryManager();
	private static final String SCRIPTEASE_LIBRARY = "ScriptEase";
	private Collection<LibraryModel> libraries;
	private final Map<Translator, LibraryModel> loadedTranslators;
	private final StoryComponentContainer masterRoot;

	private final Collection<WeakLibraryManagerObserverReference<LibraryManagerObserver>> observers;

	private LibraryManager() {
		this.libraries = new CopyOnWriteArraySet<LibraryModel>();
		this.loadedTranslators = new HashMap<Translator, LibraryModel>();
		this.masterRoot = new StoryComponentContainer("Library");

		this.observers = new CopyOnWriteArraySet<WeakLibraryManagerObserverReference<LibraryManagerObserver>>();

		this.masterRoot.registerChildType(ScriptIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.masterRoot.registerChildType(AskIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.masterRoot.registerChildType(KnowIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.masterRoot.registerChildType(StoryComponentContainer.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);

		this.buildDefaultLibrary();

		TranslatorManager.getInstance().addTranslatorObserver(this);
		PatternModelManager.getInstance().addPatternModelObserver(this);
	}

	/**
	 * Builds and adds the default ScriptEaseLibrary
	 */
	private void buildDefaultLibrary() {
		final LibraryModel scriptEaseLibrary = new LibraryModel(
				LibraryManager.SCRIPTEASE_LIBRARY, LibraryManager.SCRIPTEASE_LIBRARY);
		List<String> types = new ArrayList<String>(1);
		// Add an empty askIt
		types.add(GameTypeManager.DEFAULT_BOOL_TYPE);
		AskIt conditional = new AskIt(new KnowIt("question", types));
		scriptEaseLibrary.add(conditional);

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
					&& !library.getName().equals(LibraryManager.SCRIPTEASE_LIBRARY))
				userLibraries.add(library);
		}
		return userLibraries;
	}

	/**
	 * Adds the given observer as a observer of all LibraryManager's libraries
	 * 
	 * @param observer
	 */
	public void addLibraryManagerObserver(LibraryManagerObserver observer) {
		final Collection<WeakLibraryManagerObserverReference<LibraryManagerObserver>> observersCopy;

		observersCopy = new ArrayList<WeakLibraryManagerObserverReference<LibraryManagerObserver>>(
				this.observers);

		for (WeakLibraryManagerObserverReference<LibraryManagerObserver> observerRef : observersCopy) {
			LibraryManagerObserver storyComponentObserver = observerRef.get();
			if (storyComponentObserver != null
					&& storyComponentObserver == observer)
				return;
			else if (storyComponentObserver == null)
				this.observers.remove(observerRef);
		}

		this.observers
				.add(new WeakLibraryManagerObserverReference<LibraryManagerObserver>(
						observer));
	}

	/**
	 * Removes the given observer as a observer of all LibraryManager's
	 * libraries
	 * 
	 * @param observer
	 */
	public void removeLibraryChangeListener(LibraryManagerObserver observer) {
		for (WeakLibraryManagerObserverReference<LibraryManagerObserver> reference : this.observers) {
			if (reference.get() == observer) {
				this.observers.remove(reference);
				return;
			}
		}
	}

	/**
	 * Notifies the LibraryManager's Observers that a LibraryModel has been
	 * added or removed.
	 */
	private void notifyChange(LibraryManagerEvent event) {
		Collection<WeakLibraryManagerObserverReference<LibraryManagerObserver>> observersCopy = new ArrayList<WeakLibraryManagerObserverReference<LibraryManagerObserver>>(
				this.observers);

		for (WeakLibraryManagerObserverReference<LibraryManagerObserver> observerRef : observersCopy) {
			LibraryManagerObserver libraryManagerObserver = observerRef.get();
			if (libraryManagerObserver != null)
				libraryManagerObserver.modelChanged(event);
			else
				this.observers.remove(observerRef);
		}
	}

	/**
	 * On Translator load, if the translator has not previously been loading
	 * into the Library, do so.
	 */
	@Override
	public void translatorLoaded(Translator newTranslator) {
		if (newTranslator != null
				&& !this.loadedTranslators.containsKey(newTranslator)) {
			final LibraryModel translatorLibrary = newTranslator
					.getApiDictionary().getLibrary();
			this.add(translatorLibrary);
			this.loadedTranslators.put(newTranslator, translatorLibrary);
		} else {
			for (LibraryModel model : this.loadedTranslators.values()) {
				this.remove(model);
			}
			this.loadedTranslators.clear();
		}
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

	/**
	 * On StoryModelPoolEvent, If the StoryModelPool no longer uses the
	 * translator, removes the translator from loadedTranslators.
	 */
	@Override
	public void modelChanged(PatternModelEvent event) {
		if (event.getEventType() == PatternModelEvent.PATTERN_MODEL_REMOVED)
			for (Translator translator : this.loadedTranslators.keySet()) {
				if (!PatternModelManager.getInstance().usingTranslator(
						translator)) {
					// this.remove(loadedTranslators.get(translator));
					this.loadedTranslators.remove(translator);
					break;
				}
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
	private class WeakLibraryManagerObserverReference<T> extends
			WeakReference<T> {
		public WeakLibraryManagerObserverReference(T referent) {
			super(referent);
		}
	}
}

package scriptease.model.semodel;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.ModelVisitor;
import scriptease.controller.observer.ObserverManager;
import scriptease.controller.observer.StoryModelObserver;
import scriptease.gui.WindowFactory;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryPoint;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.Resource;

/**
 * A StoryModel is the binding object that associates a GameModule with a
 * StoryComponent tree so the tree can access its possible bindings.
 * 
 * @author remiller
 * @author mfchurch
 * @author kschenk
 */
public final class StoryModel extends SEModel {
	private final GameModule module;
	private final Translator translator;
	private final Collection<LibraryModel> optionalLibraries;
	private final ObserverManager<StoryModelObserver> observerManager;
	private StoryPoint startPoint;

	/**
	 * Full constructor for {@code ScriptEaseStoryModule}. Builds a
	 * {@code ScriptEaseStoryModule} with the supplied root model node, module
	 * and author.
	 * 
	 * @param module
	 *            The {@link GameModule} associated with the model objects. The
	 *            StoryModel's model objects can access only the game data from
	 *            this <code>module</code>. If <code>module</code> is null, this
	 *            model is considered to be a code library.
	 * @param title
	 *            The name of the Story.
	 * @param author
	 *            The author's name.
	 * @param translator
	 *            The Translator to use to interpret this story.
	 */
	public StoryModel(GameModule module, String title, String author,
			Translator translator, Collection<LibraryModel> optionalLibraries) {
		super(title, author);

		// TODO Where this gets called, need to add libraries ^

		this.startPoint = new StoryPoint("Start");
		this.module = module;
		this.translator = translator;
		this.optionalLibraries = optionalLibraries;
		this.observerManager = new ObserverManager<StoryModelObserver>();

		// Adds all of the automatic causes to the start point.
		// Note that automatics can only be defined in a library model for now
		for (LibraryModel library : this.getLibraries()) {
			for (Resource resource : module.getAutomaticHandlers()) {
				for (ScriptIt automatic : library.getAutomatics()) {

					final ScriptIt copy = automatic.clone();

					for (KnowIt parameter : copy.getParameters()) {
						if (!parameter.getTypes().containsAll(
								resource.getTypes()))
							throw new IllegalArgumentException(
									"Found invalid types for automatics");
						parameter.setBinding(resource);

						this.startPoint.addStoryChild(copy);
					}
				}
			}
		}
	}

	public void setStartPoint(StoryPoint startPoint) {
		if (startPoint == null)
			throw new IllegalArgumentException(
					"Cannot give StoryModel a null tree root.");
		this.startPoint = startPoint;
	}

	public StoryPoint getRoot() {
		return this.startPoint;
	}

	public void addLibrary(LibraryModel library) {
		if (!this.optionalLibraries.contains(library)) {
			this.optionalLibraries.add(library);

			for (StoryModelObserver observer : this.observerManager
					.getObservers()) {
				observer.libraryAdded(library);
			}
		} else
			WindowFactory.getInstance().showWarningDialog(
					"Library Already Exists",
					"The Library, " + library.getName()
							+ ", has already been added to the model.");
	}

	/**
	 * Gets the module used for bindings in this Story.
	 * 
	 * @return the module associated with this Story.
	 */
	public GameModule getModule() {
		return this.module;
	}

	@Override
	public Translator getTranslator() {
		return this.translator;
	}

	/**
	 * Returns all of the libraries associated with the story model, including
	 * the ScriptEase common library, the translator's default library, and all
	 * loaded optional libraries.
	 * 
	 * @return
	 */
	public Collection<LibraryModel> getLibraries() {
		final Collection<LibraryModel> libraries = new ArrayList<LibraryModel>();

		libraries.add(LibraryModel.getCommonLibrary());
		libraries.add(this.translator.getLibrary());
		libraries.addAll(this.optionalLibraries);

		return libraries;
	}

	public Collection<LibraryModel> getOptionalLibraries() {
		return this.optionalLibraries;
	}

	/**
	 * Adds a {@link StoryModelObserver} that remains for the lifetime of the
	 * model.
	 * 
	 * @param observer
	 */
	public void addStoryModelObserver(StoryModelObserver observer) {
		this.observerManager.addObserver(this, observer);
	}

	/**
	 * Returns the type keywords for all of the libraries containd in this
	 * story.
	 * 
	 * @return
	 */
	public Collection<String> getTypeKeywords() {
		final Collection<String> keywords = new ArrayList<String>();

		for (LibraryModel library : this.getLibraries()) {
			keywords.addAll(library.getTypeKeywords());
		}

		return keywords;
	}

	/**
	 * Searches for the type format in all of the libraries contained in this
	 * story.
	 * 
	 * @param keyword
	 * @return
	 */
	public Collection<AbstractFragment> getTypeFormat(String keyword) {
		final Collection<AbstractFragment> format;

		format = new ArrayList<AbstractFragment>();

		for (LibraryModel library : this.getLibraries()) {
			format.addAll(library.getTypeFormat(keyword));

			if (!format.isEmpty())
				break;
		}

		return format;
	}

	@Override
	public String toString() {
		return "StoryModel [" + this.getName() + "]";
	}

	@Override
	public void process(ModelVisitor processController) {
		processController.processStoryModel(this);
	}
}

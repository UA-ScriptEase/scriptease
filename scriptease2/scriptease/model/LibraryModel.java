package scriptease.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.controller.BindingAdapter;
import scriptease.controller.ModelVisitor;
import scriptease.controller.StoryAdapter;
import scriptease.controller.observer.library.LibraryEvent;
import scriptease.controller.observer.library.LibraryObserver;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryItemSequence;
import scriptease.translator.Translator;
import scriptease.translator.apimanagers.DescribeItManager;

/**
 * A collection of abstract patterns that are somehow related, either because
 * they are intended for a genre, a specific game, or are specifically promoted
 * by the user.<br>
 * <br>
 * All libraries share a reference to the translator that will be used to view
 * them, which can be <code>null</code> if no translator is set. This translator
 * will be the same at the Active Story's translator, or iif there is no Story
 * open, it can be any single translator.
 * 
 * @author remiller
 * @author mfchurch
 */
public class LibraryModel extends SEModel implements StoryComponentObserver {
	private static final String AUTOMATIC_LABEL = "automatic";
	private static final String COMMON_LIBRARY_NAME = "ScriptEase";

	private final Collection<LibraryObserver> listeners;
	// We save this as a variable since add is called many times.
	private final StoryAdapter categoryAdder;

	private Translator translator;
	private StoryComponentContainer effectsCategory;
	private StoryComponentContainer causesCategory;
	private StoryComponentContainer descriptionsCategory;
	private StoryComponentContainer controllersCategory;
	private StoryComponentContainer noteContainer;
	private StoryComponentContainer modelRoot;

	private static final LibraryModel COMMON_LIBRARY = new LibraryModel(
			COMMON_LIBRARY_NAME, COMMON_LIBRARY_NAME) {
		{
			this.add(new AskIt());
			this.add(new Note());
		}
	};

	/**
	 * The common library contains components common to all translators, such as
	 * notes.
	 * 
	 * @return
	 */
	public static LibraryModel getCommonLibrary() {
		return COMMON_LIBRARY;
	}

	/**
	 * Builds a new Library model with a blank author and title, and null
	 * translator.
	 */
	public LibraryModel() {
		this("", "", null);
	}

	/**
	 * Builds a new Library model with a blank author and title, and the passed
	 * translator.
	 * 
	 * @param translator
	 *            The translator that the library belongs to.
	 */
	public LibraryModel(Translator translator) {
		this("", "", translator);
	}

	/**
	 * Builds a new Library model with the supplied author and title, and a null
	 * translator.
	 * 
	 * @param title
	 *            the name of the library.
	 * @param author
	 */
	public LibraryModel(String title, String author) {
		this(title, author, null);
	}

	/**
	 * Builds a new Library model with the supplied author, title, and
	 * translator.
	 * 
	 * @param title
	 *            the name of the library.
	 * @param author
	 *            the author of the library.
	 * @param translator
	 *            The translator that this library belongs to.
	 */
	public LibraryModel(String title, String author, Translator translator) {
		super(title, author);

		this.translator = translator;

		this.modelRoot = new StoryComponentContainer(title);

		Collection<StoryComponentContainer> categories = new ArrayList<StoryComponentContainer>();

		this.buildCategories();

		categories.add(this.effectsCategory);
		categories.add(this.causesCategory);
		categories.add(this.descriptionsCategory);
		categories.add(this.controllersCategory);
		categories.add(this.noteContainer);

		this.getRoot().registerChildType(StoryComponentContainer.class,
				categories.size());
		for (StoryComponentContainer category : categories) {
			this.getRoot().addStoryChild(category);
		}

		this.registerCategoryChildTypes();
		this.listeners = new ArrayList<LibraryObserver>();

		this.categoryAdder = new StoryAdapter() {
			final LibraryModel model = LibraryModel.this;

			@Override
			public void processScriptIt(ScriptIt scriptIt) {
				final boolean success;
				if (scriptIt.isCause()) {
					success = this.model.causesCategory.addStoryChild(scriptIt);
					if (success)
						scriptIt.addStoryComponentObserver(this.model);
				} else {
					success = this.model.effectsCategory
							.addStoryChild(scriptIt);
					if (success)
						scriptIt.addStoryComponentObserver(this.model);
				}
			}

			@Override
			public void processAskIt(AskIt askIt) {
				final boolean success = this.model.controllersCategory
						.addStoryChild(askIt);
				if (success)
					askIt.addStoryComponentObserver(this.model);
			}

			@Override
			public void processControlIt(ControlIt controlIt) {
				final boolean success = this.model.controllersCategory
						.addStoryChild(controlIt);
				if (success)
					controlIt.addStoryComponentObserver(this.model);
			}

			@Override
			public void processNote(Note note) {
				final boolean success = this.model.noteContainer
						.addStoryChild(note);
				if (success)
					note.addStoryComponentObserver(this.model);
			}

			@Override
			public void processKnowIt(KnowIt knowIt) {
				final boolean success = this.model.descriptionsCategory
						.addStoryChild(knowIt);
				if (success) {
					knowIt.addStoryComponentObserver(this.model);

					final KnowItBinding binding = knowIt.getBinding();
					binding.process(new BindingAdapter() {
						@Override
						public void processFunction(
								KnowItBindingFunction function) {
							final ScriptIt doIt = function.getValue();
							LibraryModel.this.add(doIt);
						}
					});
				}
			}

			@Override
			public void processStoryComponentContainer(
					StoryComponentContainer container) {
				final boolean success = this.model.controllersCategory
						.addStoryChild(container);
				if (success)
					container.addStoryComponentObserver(this.model);
			}
		};
	}

	private void registerCategoryChildTypes() {
		this.effectsCategory.clearAllowableChildren();
		this.causesCategory.clearAllowableChildren();
		this.descriptionsCategory.clearAllowableChildren();
		this.controllersCategory.clearAllowableChildren();
		this.noteContainer.clearAllowableChildren();

		final int max = ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE;

		this.effectsCategory.registerChildType(ScriptIt.class, max);

		this.causesCategory.registerChildType(ScriptIt.class, max);

		this.descriptionsCategory.registerChildType(KnowIt.class, max);

		this.controllersCategory.registerChildType(
				StoryComponentContainer.class, max);
		this.controllersCategory.registerChildType(ScriptIt.class, max);
		this.controllersCategory.registerChildType(AskIt.class, max);
		this.controllersCategory
				.registerChildType(StoryItemSequence.class, max);
		this.controllersCategory.registerChildType(KnowIt.class, max);
		this.controllersCategory.registerChildType(ControlIt.class, max);

		this.noteContainer.registerChildType(Note.class, 1);
	}

	private void registerObservers() {
		this.effectsCategory.addStoryComponentObserver(this);
		this.causesCategory.addStoryComponentObserver(this);
		this.descriptionsCategory.addStoryComponentObserver(this);
		this.controllersCategory.addStoryComponentObserver(this);
		this.noteContainer.addStoryComponentObserver(this);
	}

	@Override
	public Translator getTranslator() {
		return this.translator;
	}

	public void setTranslator(Translator translator) {
		this.translator = translator;
	}

	public StoryComponentContainer getEffectsCategory() {
		return this.effectsCategory;
	}

	public StoryComponentContainer getCausesCategory() {
		return this.causesCategory;
	}

	public StoryComponentContainer getDescriptionsCategory() {
		return this.descriptionsCategory;
	}

	public StoryComponentContainer getControllersCategory() {
		return this.controllersCategory;
	}

	public StoryComponentContainer getNoteContainer() {
		return this.noteContainer;
	}

	/**
	 * Retrieves the automatics scriptIts from the causes
	 * 
	 * @return
	 */
	public Collection<ScriptIt> getAutomatics() {
		final Collection<ScriptIt> automatics = new ArrayList<ScriptIt>();

		for (StoryComponent cause : this.getCausesCategory().getChildren()) {
			if (cause instanceof ScriptIt) {
				for (String label : cause.getLabels()) {
					if (label.equalsIgnoreCase(AUTOMATIC_LABEL)) {
						automatics.add((ScriptIt) cause);
					}
				}
			}
		}
		return automatics;
	}

	/**
	 * Sort the root by the library categories
	 */
	public void setRoot(StoryComponentContainer root) {
		if (root == null)
			throw new IllegalArgumentException(
					"Cannot give LibraryModel a null tree root.");

		this.modelRoot = root;

		// distribute the categories properly.
		try {
			StoryComponentContainer containerChild;

			for (StoryComponent child : root.getChildren()) {
				containerChild = (StoryComponentContainer) child;

				if (child.getDisplayText().equalsIgnoreCase("EFFECTS"))
					this.effectsCategory = containerChild;
				else if (child.getDisplayText().equalsIgnoreCase("CAUSES"))
					this.causesCategory = containerChild;
				else if (child.getDisplayText()
						.equalsIgnoreCase("DESCRIPTIONS"))
					this.descriptionsCategory = containerChild;
				else if (child.getDisplayText().equalsIgnoreCase("CONTROLLERS"))
					this.controllersCategory = containerChild;
				else if (child.getDisplayText().equalsIgnoreCase("NOTE"))
					this.noteContainer = containerChild;
				else
					System.out.println("Unimplemented Child Type: "
							+ child.getDisplayText());
			}

			this.registerCategoryChildTypes();
			this.registerObservers();
		} catch (ClassCastException e) {
			System.err.println(this.getName() + " Library "
					+ " incorrectly formatted");
		}
	}

	public StoryComponentContainer getRoot() {
		return this.modelRoot;
	}

	/**
	 * Adds the given StoryComponent to the appropriate category. Also sets the
	 * component's library to the library.
	 * 
	 * @param component
	 */
	public void add(StoryComponent component) {
		component.process(this.categoryAdder);
		component.setLibrary(this);
	}

	/**
	 * Adds a KnowIt representing a DescribeIt to the LibraryModel. Adds the
	 * DescribeIt to the DescribeItManager if it is not already in there.
	 * 
	 * This should only ever be called when we are creating an entirely new
	 * DescribeIt.
	 * 
	 * @param describeIt
	 */
	public void add(DescribeIt describeIt) {
		final DescribeItManager describeItManager;

		describeItManager = this.translator.getApiDictionary()
				.getDescribeItManager();

		final KnowIt knowIt;

		knowIt = describeItManager.createKnowItForDescribeIt(describeIt);

		this.add(knowIt);

		describeItManager.addDescribeIt(describeIt, knowIt);
	}

	public void remove(StoryComponent component) {
		component.process(new StoryAdapter() {
			@Override
			public void processScriptIt(ScriptIt scriptIt) {
				final boolean success;
				if (scriptIt.isCause()) {
					success = LibraryModel.this.causesCategory
							.removeStoryChild(scriptIt);
					if (success)
						scriptIt.removeStoryComponentObserver(LibraryModel.this);
				} else {
					success = LibraryModel.this.effectsCategory
							.removeStoryChild(scriptIt);
					if (success)
						scriptIt.removeStoryComponentObserver(LibraryModel.this);
				}
			}

			@Override
			public void processAskIt(AskIt askIt) {
				final boolean success = LibraryModel.this.controllersCategory
						.removeStoryChild(askIt);
				if (success)
					askIt.removeStoryComponentObserver(LibraryModel.this);
			}

			public void processNote(Note note) {
				final boolean success = LibraryModel.this.noteContainer
						.removeStoryChild(note);
				if (success)
					note.removeStoryComponentObserver(LibraryModel.this);
			};

			@Override
			public void processKnowIt(KnowIt knowIt) {
				final boolean success = LibraryModel.this.descriptionsCategory
						.removeStoryChild(knowIt);
				if (success) {
					knowIt.removeStoryComponentObserver(LibraryModel.this);
				}
			}

			@Override
			public void processStoryComponentContainer(
					StoryComponentContainer container) {
				final boolean success = LibraryModel.this.controllersCategory
						.removeStoryChild(container);
				if (success)
					container.removeStoryComponentObserver(LibraryModel.this);
			}
		});
	}

	public void addAll(Collection<? extends StoryComponent> components) {
		for (StoryComponent component : components) {
			this.add(component);
		}
	}

	public void addLibraryChangeListener(LibraryObserver observer) {
		this.listeners.add(observer);
	}

	public void removeLibraryChangeListener(LibraryObserver observer) {
		this.listeners.remove(observer);
	}

	private void notifyChange(StoryComponent source, LibraryEvent.Type type) {
		for (LibraryObserver observer : this.listeners) {
			observer.modelChanged(this, new LibraryEvent(source, type));
		}
	}

	/**
	 * Forward the event to the LibraryModel observers, and save the library to
	 * file since it has changed
	 */
	@Override
	public void componentChanged(StoryComponentEvent event) {
		final StoryComponent source = event.getSource();

		if (event.getType() == StoryComponentChangeEnum.CHANGE_CHILD_REMOVED) {
			this.notifyChange(source, LibraryEvent.Type.REMOVAL);
		} else if (event.getType() == StoryComponentChangeEnum.CHANGE_CHILD_ADDED) {
			this.notifyChange(source, LibraryEvent.Type.ADDITION);
		} else {
			this.notifyChange(source, LibraryEvent.Type.CHANGE);
		}
	}

	/**
	 * Get's all the LibraryModel's StoryComponents
	 * 
	 * @return
	 */
	public List<StoryComponent> getAllStoryComponents() {
		final List<StoryComponent> components;

		components = new ArrayList<StoryComponent>();

		components.addAll(this.effectsCategory.getChildren());
		components.addAll(this.causesCategory.getChildren());
		components.addAll(this.descriptionsCategory.getChildren());
		components.addAll(this.controllersCategory.getChildren());
		components.addAll(this.noteContainer.getChildren());

		return components;
	}

	/**
	 * Returns true if the LibraryModel contains the given component (using ==
	 * instead of .equals)
	 * 
	 * @param component
	 * @return
	 */
	public boolean containsStoryComponent(StoryComponent sc) {
		Collection<StoryComponent> components = this.getAllStoryComponents();
		for (StoryComponent component : components) {
			if (component == sc)
				return true;
		}
		return false;
	}

	/**
	 * Initializes the categories, their acceptable child types, and observers
	 */
	private void buildCategories() {
		this.effectsCategory = new StoryComponentContainer("Effects");
		this.causesCategory = new StoryComponentContainer("Causes");
		this.descriptionsCategory = new StoryComponentContainer("Descriptions");
		this.controllersCategory = new StoryComponentContainer("Controllers");
		this.noteContainer = new StoryComponentContainer("Note");
		this.registerObservers();

	}

	@Override
	public String toString() {
		return this.getName();
	}

	@Override
	public void process(ModelVisitor processController) {
		processController.processLibraryModel(this);
	}

	@Override
	public Collection<LibraryModel> getLibraries() {
		final Collection<LibraryModel> libraries = new ArrayList<LibraryModel>();

		libraries.add(LibraryModel.getCommonLibrary());
		libraries.add(this.translator.getApiDictionary().getLibrary());

		return libraries;
	}
}

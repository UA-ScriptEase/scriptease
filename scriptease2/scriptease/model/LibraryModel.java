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
	 * Adds the given StoryComponent to the appropriate category
	 * 
	 * @param component
	 */
	public void add(StoryComponent component) {
		component.process(new CategoryAdder());
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

	private void notifyChange(short eventType, StoryComponentEvent event) {
		for (LibraryObserver observer : this.listeners) {
			observer.modelChanged(this,
					new LibraryEvent(this, eventType, event));
		}
	}

	/**
	 * Forward the event to the LibraryModel observers, and save the library to
	 * file since it has changed
	 */
	@Override
	public void componentChanged(StoryComponentEvent event) {
		if (event.getType() == StoryComponentChangeEnum.CHANGE_CHILD_REMOVED) {
			this.notifyChange(LibraryEvent.STORYCOMPONENT_REMOVED, event);
		} else if (event.getType() == StoryComponentChangeEnum.CHANGE_CHILD_ADDED) {
			this.notifyChange(LibraryEvent.STORYCOMPONENT_ADDED, event);
		} else {
			this.notifyChange(LibraryEvent.STORYCOMPONENT_CHANGED, event);
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

	/**
	 * Adds StoryComponents to their proper category
	 * 
	 * @author mfchurch
	 * 
	 */
	private class CategoryAdder extends StoryAdapter {
		@Override
		public void processScriptIt(ScriptIt scriptIt) {
			final boolean success;
			if (scriptIt.isCause()) {
				success = LibraryModel.this.causesCategory
						.addStoryChild(scriptIt);
				if (success)
					scriptIt.addStoryComponentObserver(LibraryModel.this);
			} else {
				success = LibraryModel.this.effectsCategory
						.addStoryChild(scriptIt);
				if (success)
					scriptIt.addStoryComponentObserver(LibraryModel.this);
			}
		}

		@Override
		public void processAskIt(AskIt askIt) {
			final boolean success = LibraryModel.this.controllersCategory
					.addStoryChild(askIt);
			if (success)
				askIt.addStoryComponentObserver(LibraryModel.this);
		}

		@Override
		public void processControlIt(ControlIt controlIt) {
			final boolean success = LibraryModel.this.controllersCategory
					.addStoryChild(controlIt);
			if (success)
				controlIt.addStoryComponentObserver(LibraryModel.this);
		}

		@Override
		public void processNote(Note note) {
			final boolean success = LibraryModel.this.noteContainer
					.addStoryChild(note);
			if (success)
				note.addStoryComponentObserver(LibraryModel.this);
		}

		@Override
		public void processKnowIt(KnowIt knowIt) {
			final boolean success = LibraryModel.this.descriptionsCategory
					.addStoryChild(knowIt);
			if (success) {
				knowIt.addStoryComponentObserver(LibraryModel.this);

				final KnowItBinding binding = knowIt.getBinding();
				binding.process(new BindingAdapter() {
					@Override
					public void processFunction(KnowItBindingFunction function) {
						final ScriptIt doIt = function.getValue();
						LibraryModel.this.add(doIt);
					}
				});
			}
		}

		@Override
		public void processStoryComponentContainer(
				StoryComponentContainer container) {
			final boolean success = LibraryModel.this.controllersCategory
					.addStoryChild(container);
			if (success)
				container.addStoryComponentObserver(LibraryModel.this);
		}
	}

	@Override
	public String toString() {
		return this.getName();
	}

	public ScriptIt retrieveScriptIt(String displayName) {
		ScriptItRetriever retriever = new ScriptItRetriever();
		return retriever.retrieveScriptIt(displayName);
	}

	/**
	 * Finds a ScriptIt in the LibraryModel with the given displayName
	 * 
	 * @author mfchurch
	 * 
	 */
	private class ScriptItRetriever extends StoryAdapter {
		private String displayName;
		private ScriptIt scriptIt;

		public ScriptIt retrieveScriptIt(String displayName) {
			this.displayName = displayName;
			StoryComponentContainer causesCategory = LibraryModel.this
					.getCausesCategory();
			for (StoryComponent cause : causesCategory.getChildren()) {
				if (this.scriptIt != null)
					return this.scriptIt;
				else
					cause.process(this);
			}
			StoryComponentContainer effectsCategory = LibraryModel.this
					.getEffectsCategory();
			for (StoryComponent effect : effectsCategory.getChildren()) {
				if (this.scriptIt != null)
					return this.scriptIt;
				else
					effect.process(this);
			}
			StoryComponentContainer descriptionsCategory = LibraryModel.this
					.getDescriptionsCategory();
			for (StoryComponent effect : descriptionsCategory.getChildren()) {
				if (this.scriptIt != null)
					return this.scriptIt;
				else
					effect.process(this);
			}
			StoryComponentContainer controllersCategory = LibraryModel.this
					.getControllersCategory();
			for (StoryComponent effect : controllersCategory.getChildren()) {
				if (this.scriptIt != null)
					return this.scriptIt;
				else
					effect.process(this);
			}
			return this.scriptIt;
		}

		@Override
		public void processAskIt(AskIt questionIt) {
			this.defaultProcessComplex(questionIt);
			questionIt.getCondition().process(this);
		}

		@Override
		public void processScriptIt(ScriptIt scriptIt) {
			for (KnowIt parameter : scriptIt.getParameters())
				parameter.process(this);
			if (scriptIt.getDisplayText().equals(this.displayName))
				this.scriptIt = scriptIt;
			this.defaultProcessComplex(scriptIt);
		}

		@Override
		protected void defaultProcessComplex(ComplexStoryComponent complex) {
			complex.processChildren(this);
		}

		@Override
		public void processKnowIt(KnowIt knowIt) {
			knowIt.getBinding().process(new BindingAdapter() {
				@Override
				public void processFunction(KnowItBindingFunction function) {
					final ScriptIt scriptIt = function.getValue();
					scriptIt.process(ScriptItRetriever.this);
				}
			});
		}
	}

	@Override
	public void process(ModelVisitor processController) {
		processController.processLibraryModel(this);
	}

	@Override
	public Collection<LibraryModel> getLibraries() {
		return this.translator.getLibraries();
	}
}

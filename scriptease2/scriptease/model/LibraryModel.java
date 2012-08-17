package scriptease.model;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.AbstractNoOpBindingVisitor;
import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.controller.FileManager;
import scriptease.controller.ModelVisitor;
import scriptease.controller.observer.LibraryEvent;
import scriptease.controller.observer.LibraryObserver;
import scriptease.controller.observer.StoryComponentEvent;
import scriptease.controller.observer.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.StoryComponentObserver;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingDescribeIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryItemSequence;
import scriptease.translator.Translator;

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
public class LibraryModel extends PatternModel implements
		StoryComponentObserver {
	private Translator translator;
	private final Collection<LibraryObserver> listeners;
	private StoryComponentContainer effectsCategory;
	private StoryComponentContainer causesCategory;
	private StoryComponentContainer descriptionsCategory;
	private StoryComponentContainer folderCategory;
	private boolean autosaving;
	private StoryComponentContainer modelRoot;

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

		categories.add(effectsCategory);
		categories.add(causesCategory);
		categories.add(descriptionsCategory);
		categories.add(folderCategory);

		this.getRoot().registerChildType(StoryComponentContainer.class,
				categories.size());
		for (StoryComponentContainer category : categories) {
			this.getRoot().addStoryChild(category);
		}

		this.registerCategoryChildTypes();
		this.autosaving = false;
		this.listeners = new ArrayList<LibraryObserver>();
	}

	private void registerCategoryChildTypes() {
		effectsCategory.clearAllowableChildren();
		causesCategory.clearAllowableChildren();
		descriptionsCategory.clearAllowableChildren();
		folderCategory.clearAllowableChildren();

		effectsCategory.registerChildType(ScriptIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		effectsCategory.registerChildType(AskIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		causesCategory.registerChildType(ScriptIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		descriptionsCategory.registerChildType(KnowIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		folderCategory.registerChildType(StoryComponentContainer.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		folderCategory.registerChildType(ScriptIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		folderCategory.registerChildType(AskIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		folderCategory.registerChildType(StoryItemSequence.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		folderCategory.registerChildType(KnowIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
	}

	private void registerObservers() {
		effectsCategory.addStoryComponentObserver(this);
		causesCategory.addStoryComponentObserver(this);
		descriptionsCategory.addStoryComponentObserver(this);
		folderCategory.addStoryComponentObserver(this);
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

	public StoryComponentContainer getFoldersCategory() {
		return this.folderCategory;
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
				else if (child.getDisplayText().equalsIgnoreCase("FOLDERS"))
					this.folderCategory = containerChild;
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
			/*
			 * System.out.println("Removed '" + event.getSource() + "' from " +
			 * toString());
			 */// Debug Code
			notifyChange(LibraryEvent.STORYCOMPONENT_REMOVED, event);
		} else if (event.getType() == StoryComponentChangeEnum.CHANGE_CHILD_ADDED) {
			/*
			 * System.out.println("Added '" + event.getSource() + "' to " +
			 * toString());
			 */// Debug Code
			notifyChange(LibraryEvent.STORYCOMPONENT_ADDED, event);
		} else {
			notifyChange(LibraryEvent.STORYCOMPONENT_CHANGED, event);
		}

		// Optionally save the modified model to disk
		if (this.autosaving)
			FileManager.getInstance().save(this);
	}

	/**
	 * Returns all story components representable by a story component panel.
	 * 
	 * @return
	 */
	public Collection<StoryComponent> getMainStoryComponents() {
		final Collection<StoryComponent> components;
		
		components = new ArrayList<StoryComponent>();
		
		components.addAll(this.effectsCategory.getChildren());
		components.addAll(this.causesCategory.getChildren());
		components.addAll(this.descriptionsCategory.getChildren());
		components.addAll(this.folderCategory.getChildren());
		return components;
	}

	/**
	 * Get's all the LibraryModel's StoryComponents
	 * 
	 * @return
	 */
	public Collection<StoryComponent> getAllStoryComponents() {
		final Collection<StoryComponent> components;
		
		components = new ArrayList<StoryComponent>();
		
		components.addAll(this.effectsCategory.getChildren());
		components.addAll(this.causesCategory.getChildren());
		components.addAll(this.descriptionsCategory.getChildren());
		components.addAll(this.folderCategory.getChildren());

		// Get the implicit and parameters of the binding scriptIts
		final Collection<StoryComponent> implicitsAndParameters = new ArrayList<StoryComponent>();
		for (StoryComponent component : components) {
			component.process(new AbstractNoOpStoryVisitor() {
				@Override
				public void processScriptIt(ScriptIt scriptIt) {
					Collection<KnowIt> implicits = scriptIt.getImplicits();
					for (KnowIt implicit : implicits) {
						if (!implicitsAndParameters.contains(implicit)) {
							implicitsAndParameters.add(implicit);
							implicit.process(this);
						}
					}
					Collection<KnowIt> parameters = scriptIt.getParameters();
					for (KnowIt parameter : parameters) {
						if (!implicitsAndParameters.contains(parameter)) {
							implicitsAndParameters.add(parameter);
							parameter.process(this);
						}
					}
				}

				@Override
				public void processKnowIt(KnowIt knowIt) {
					knowIt.getBinding().process(
							new AbstractNoOpBindingVisitor() {
								@Override
								public void processFunction(
										KnowItBindingFunction function) {
									ScriptIt scriptIt = function.getValue();
									if (!implicitsAndParameters
											.contains(scriptIt)) {
										implicitsAndParameters.add(scriptIt);
										processScriptIt(scriptIt);
									}
								}
							});
				}
			});
		}
		components.addAll(implicitsAndParameters);
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
		Collection<StoryComponent> components = getAllStoryComponents();
		for (StoryComponent component : components) {
			if (component == sc)
				return true;
		}
		return false;
	}

	public boolean isAutosaving() {
		return this.autosaving;
	}

	public void setAutosaving(boolean autosave) {
		this.autosaving = autosave;
	}

	/**
	 * Initializes the categories, their acceptable child types, and observers
	 */
	private void buildCategories() {
		effectsCategory = new StoryComponentContainer("Effects");
		causesCategory = new StoryComponentContainer("Causes");
		descriptionsCategory = new StoryComponentContainer("Descriptions");
		folderCategory = new StoryComponentContainer("Folders");
		this.registerObservers();

	}

	/**
	 * Adds StoryComponents to their proper category
	 * 
	 * @author mfchurch
	 * 
	 */
	private class CategoryAdder extends AbstractNoOpStoryVisitor {
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
			final boolean success = LibraryModel.this.effectsCategory
					.addStoryChild(askIt);
			if (success)
				askIt.addStoryComponentObserver(LibraryModel.this);
		}

		@Override
		public void processKnowIt(KnowIt knowIt) {
			final boolean success = LibraryModel.this.descriptionsCategory
					.addStoryChild(knowIt);
			if (success) {
				knowIt.addStoryComponentObserver(LibraryModel.this);

				final KnowItBinding binding = knowIt.getBinding();
				binding.process(new AbstractNoOpBindingVisitor() {
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
			final boolean success = LibraryModel.this.folderCategory
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
	 * DoItRetriever finds a DoIt in the LibraryModel with the given displayName
	 * 
	 * @author mfchurch
	 * 
	 */
	private class ScriptItRetriever extends AbstractNoOpStoryVisitor {
		private String displayName;
		private ScriptIt scriptIt;

		public ScriptIt retrieveScriptIt(String displayName) {
			this.displayName = displayName;
			StoryComponentContainer causesCategory = LibraryModel.this
					.getCausesCategory();
			for (StoryComponent cause : causesCategory.getChildren()) {
				if (scriptIt != null)
					return scriptIt;
				else
					cause.process(this);
			}
			StoryComponentContainer effectsCategory = LibraryModel.this
					.getEffectsCategory();
			for (StoryComponent effect : effectsCategory.getChildren()) {
				if (scriptIt != null)
					return scriptIt;
				else
					effect.process(this);
			}
			StoryComponentContainer descriptionsCategory = LibraryModel.this
					.getDescriptionsCategory();
			for (StoryComponent effect : descriptionsCategory.getChildren()) {
				if (scriptIt != null)
					return scriptIt;
				else
					effect.process(this);
			}
			StoryComponentContainer folderCategory = LibraryModel.this
					.getFoldersCategory();
			for (StoryComponent effect : folderCategory.getChildren()) {
				if (scriptIt != null)
					return scriptIt;
				else
					effect.process(this);
			}
			return scriptIt;
		}

		@Override
		public void processAskIt(AskIt questionIt) {
			defaultProcessComplex(questionIt);
			questionIt.getCondition().process(this);
		}

		@Override
		public void processScriptIt(ScriptIt scriptIt) {
			for (KnowIt parameter : scriptIt.getParameters())
				parameter.process(this);
			if (scriptIt.getDisplayText().equals(displayName))
				this.scriptIt = scriptIt;
			defaultProcessComplex(scriptIt);
		}

		@Override
		protected void defaultProcessComplex(ComplexStoryComponent complex) {
			complex.processChildren(this);
		}

		@Override
		public void processKnowIt(KnowIt knowIt) {
			knowIt.getBinding().process(new AbstractNoOpBindingVisitor() {
				@Override
				public void processFunction(KnowItBindingFunction function) {
					final ScriptIt scriptIt = function.getValue();
					scriptIt.process(ScriptItRetriever.this);
				}

				@Override
				public void processDescribeIt(KnowItBindingDescribeIt described) {
					Collection<ScriptIt> scriptIts = described.getValue()
							.getScriptIts();
					for (final ScriptIt doIt : scriptIts) {
						doIt.process(ScriptItRetriever.this);
					}
				}
			});
		}
	}

	@Override
	public void process(ModelVisitor processController) {
		processController.processLibraryModel(this);
	}
}

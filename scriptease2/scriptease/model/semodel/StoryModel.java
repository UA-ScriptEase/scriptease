package scriptease.model.semodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import scriptease.controller.BindingAdapter;
import scriptease.controller.ModelVisitor;
import scriptease.controller.StoryAdapter;
import scriptease.controller.observer.ObserverManager;
import scriptease.controller.observer.SEModelEvent;
import scriptease.controller.observer.StoryModelObserver;
import scriptease.gui.storycomponentpanel.StoryComponentPanelTree;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryGroup;
import scriptease.model.complex.StoryNode;
import scriptease.model.complex.StoryPoint;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.GameType;
import scriptease.translator.io.model.Resource;
import scriptease.translator.io.model.Slot;
import scriptease.util.StringOp;

/**
 * A StoryModel is the binding object that associates a GameModule with a
 * StoryComponent tree so the tree can access its possible bindings.
 * 
 * @author remiller
 * @author mfchurch
 * @author kschenk
 * @author jyuen
 */
public final class StoryModel extends SEModel {
	private GameModule module;
	private final Translator translator;
	private final String compatibleVersion;
	private final Collection<LibraryModel> optionalLibraries;
	private final ObserverManager<StoryModelObserver> observerManager;
	private final Collection<DialogueLine> dialogueRoots;
	private StoryComponentPanelTree storyComponentPanelTree;

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
	public StoryModel(GameModule module, String title, String author, String description,
			String compatibleVersion, Translator translator,
			Collection<LibraryModel> optionalLibraries) {
		super(title, author, description);

		this.setRoot(new StoryPoint("Start"));

		this.module = module;
		this.translator = translator;
		this.compatibleVersion = compatibleVersion;
		this.optionalLibraries = optionalLibraries;
		this.observerManager = new ObserverManager<StoryModelObserver>();
		this.dialogueRoots = new ArrayList<DialogueLine>();
		this.storyComponentPanelTree = new StoryComponentPanelTree(
				this.startPoint);
	}

	public Collection<DialogueLine> getDialogueRoots() {
		return this.dialogueRoots;
	}

	/**
	 * Adds a new {@link Dialogue} to the list contained in this model.
	 * 
	 * @param dialogue
	 * @return The newly added root
	 */
	public DialogueLine createAndAddDialogueRoot() {
		final DialogueLine newRoot = DialogueLine.createDialogueRoot(this);

		this.addDialogueRoot(newRoot);

		return newRoot;
	}

	/**
	 * Adds a specific dialogue line
	 * 
	 * @param line
	 * @return
	 */
	public boolean addDialogueRoot(DialogueLine line) {
		final boolean added = this.dialogueRoots.add(line);

		for (StoryModelObserver observer : this.observerManager.getObservers()) {
			observer.dialogueRootAdded(line);
		}

		return added;
	}

	public void addDialogueRoots(Collection<DialogueLine> lines) {
		for (DialogueLine line : lines)
			this.addDialogueRoot(line);
	}

	public boolean removeDialogueRoot(DialogueLine line) {
		final boolean removed = this.dialogueRoots.remove(line);

		for (StoryModelObserver observer : this.observerManager.getObservers()) {
			observer.dialogueRootRemoved(line);
		}

		return removed;
	}

	public void notifyDialogueChildAdded(DialogueLine child, DialogueLine parent) {
		for (StoryModelObserver observer : this.observerManager.getObservers()) {
			observer.dialogueChildAdded(child, parent);
		}
	}

	public void notifyDialogueChildRemoved(DialogueLine child,
			DialogueLine parent) {
		for (StoryModelObserver observer : this.observerManager.getObservers()) {
			observer.dialogueChildRemoved(child, parent);
		}
	}

	public void notifyBehaviourEdited(Behaviour behaviour) {
		for (StoryModelObserver observer : this.observerManager.getObservers()) {
			observer.behaviourEdited(behaviour);
		}
	}

	/**
	 * Sets the root of the model to the passed in {@link StoryPoint}. This is a
	 * simple setter method that does not fire off any observers.
	 * 
	 * @param startPoint
	 */
	public void setRoot(StoryPoint startPoint) {
		if (startPoint == null)
			throw new IllegalArgumentException(
					"Cannot give StoryModel a null tree root.");
		this.startPoint = startPoint;

		// Remove all existing story children.
		this.startPoint.removeStoryChildren(this.startPoint.getChildren());

		this.startPoint
				.addStoryChild(new Note(
						"You can't put story components in here. Add a new story point instead!"));
	}

	public StoryPoint getRoot() {
		return this.startPoint;
	}

	/**
	 * Removes the provided library from this story model.
	 * 
	 * @param library
	 */
	public void removeLibrary(LibraryModel library) {
		if (this.optionalLibraries.contains(library)) {
			this.optionalLibraries.remove(library);

			for (StoryModelObserver observer : this.observerManager
					.getObservers()) {
				observer.libraryRemoved(library);
			}
		} else {
			throw new IllegalStateException("Tried to remove library ("
					+ library.getTitle() + ") but it isn't even added!");
		}
	}

	/**
	 * Adds the provided library to this story model.
	 * 
	 * @param library
	 */
	public void addLibrary(LibraryModel library) {
		if (!this.optionalLibraries.contains(library)) {
			this.optionalLibraries.add(library);

			// Get the Is Active Description from the default library.
			KnowIt isActiveDescription = null;
			outer: for (LibraryModel existingLibrary : this.getLibraries()) {
				for (StoryComponent description : existingLibrary
						.getDescriptionsCategory().getChildren()) {

					if (description instanceof KnowIt
							&& description.getDisplayText().contains(
									"Is Active")) {
						isActiveDescription = (KnowIt) description;
						break outer;
					}
				}
			}

			// Add the active description and question to optional library
			// causes. We have to do this here instead of the apidictionary
			// because optional libraries do not have the is active description.
			if (isActiveDescription != null) {
				for (StoryComponent cause : library.getCausesCategory()
						.getChildren()) {

					final CauseIt causeIt = (CauseIt) cause;
					final KnowIt knowIt = isActiveDescription.clone();
					final AskIt askIt = new AskIt();

					if (causeIt.getChildren().isEmpty()) {
						causeIt.addStoryChild(knowIt);
						causeIt.addStoryChild(askIt);
						askIt.getCondition().setBinding(knowIt);
					}
				}
			}

			for (StoryModelObserver observer : this.observerManager
					.getObservers()) {
				observer.libraryAdded(library);
			}
		} else
			throw new IllegalStateException("Tried to add library ("
					+ library.getTitle() + ") but it already exists.");
	}

	/**
	 * Gets the module used for bindings in this Story.
	 * 
	 * @return the module associated with this Story.
	 */
	public GameModule getModule() {
		return this.module;
	}

	/**
	 * Gets the compatible ScriptEase version for this Story.
	 * 
	 * @return the compatible ScriptEase version
	 */
	public String getCompatibleVersion() {
		return this.compatibleVersion;
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

	public void addStoryModelObserver(Object object, StoryModelObserver observer) {
		this.observerManager.addObserver(object, observer);
	}

	@Override
	public Collection<GameType> getTypes() {
		final Collection<GameType> keywords = new ArrayList<GameType>();

		for (LibraryModel library : this.getLibraries()) {
			keywords.addAll(library.getGameTypes());
		}

		return keywords;
	}

	@Override
	public GameType getType(String keyword) {
		for (LibraryModel library : this.getLibraries()) {
			final GameType type = library.getType(keyword);

			if (StringOp.exists(type.getName()))
				return type;
		}

		return null;
	}

	/**
	 * Gets a collection of the files found in this model's include directory.
	 * This can be empty if the translator does not specify an includes
	 * directory, or the the translator's includes directory is empty.
	 * 
	 */
	public Collection<File> getIncludes() {
		final Collection<File> includes = new ArrayList<File>();

		for (LibraryModel library : this.getLibraries()) {
			includes.addAll(library.getIncludeFiles());
		}

		return includes;
	}

	public StoryComponentPanelTree getStoryComponentPanelTree() {
		return this.storyComponentPanelTree;
	}

	/**
	 * Generates a collection of story components that should be automatically
	 * added to the root of the model. This should likely only be called right
	 * before writing the code. The automatics generated will have the proper
	 * bindings on their parameters, and are ready to be written out.
	 * 
	 * @param model
	 * @return
	 */
	public Collection<StoryComponent> generateAutomaticCauses() {
		final Collection<StoryComponent> automatics;
		final Map<String, Collection<Resource>> automaticHandlers;

		automatics = new ArrayList<StoryComponent>();
		automaticHandlers = this.module.getAutomaticHandlers();

		for (LibraryModel library : this.getLibraries()) {
			for (String automaticLabel : automaticHandlers.keySet()) {
				for (Resource resource : automaticHandlers.get(automaticLabel)) {

					if (automaticLabel.equals("gameobjectautomatic")
							&& !this.getBoundResources().contains(resource))
						// gameobjectautomatic only adds the automatic if
						// we are using the resource somewhere in the model.
						continue;

					final Collection<String> resourceTypes;

					resourceTypes = resource.getTypes();

					for (CauseIt automatic : library
							.getAutomatics(automaticLabel)) {

						final CauseIt copy = automatic.clone();
						final Collection<KnowIt> parameters = copy
								.getParameters();

						for (KnowIt parameter : parameters) {
							final Collection<String> parameterTypes;

							parameterTypes = parameter.getTypes();

							if (!parameterTypes.containsAll(resourceTypes))
								throw new IllegalArgumentException(
										"Found invalid types for automatics");

							parameter.setBinding(resource);
						}

						automatics.add(copy);
					}
				}
			}
		}

		// Generate the auto - succeed for root story point.
		final LibraryModel defaultLibrary = this.translator.getLibrary();

		for (StoryComponent cause : defaultLibrary.getCausesCategory()
				.getChildren()) {
			if (cause.getDisplayText().toLowerCase().contains("is activated")) {
				final CauseIt storyPointEnabled;

				storyPointEnabled = ((CauseIt) cause).clone();

				storyPointEnabled.removeStoryChildren(storyPointEnabled
						.getChildren());

				for (KnowIt parameter : storyPointEnabled.getParameters())
					parameter.setBinding(new KnowItBindingStoryPoint(
							SEModelManager.getInstance().getActiveRoot()));

				for (StoryComponent effect : defaultLibrary
						.getEffectsCategory().getChildren()) {
					if (effect.getDisplayText().toLowerCase()
							.contains("succeed")) {
						final ScriptIt succeedStoryPoint;

						succeedStoryPoint = ((ScriptIt) effect).clone();

						for (KnowIt parameter : succeedStoryPoint
								.getParameters())
							parameter.setBinding(new KnowItBindingStoryPoint(
									SEModelManager.getInstance()
											.getActiveRoot()));

						storyPointEnabled.addStoryChild(succeedStoryPoint);
					}
				}

				automatics.add(storyPointEnabled);
				break;
			}
		}

		return automatics;
	}

	/**
	 * Finds all resources that are bound to know its
	 * 
	 * @return
	 */
	private Collection<Resource> getBoundResources() {
		final Collection<Resource> resources = new HashSet<Resource>();

		final StoryAdapter adapter;

		adapter = new StoryAdapter() {

			@Override
			protected void defaultProcessComplex(ComplexStoryComponent complex) {
				for (StoryComponent child : complex.getChildren()) {
					child.process(this);
				}
			}

			@Override
			public void processStoryGroup(StoryGroup storyGroup) {
				this.defaultProcessComplex(storyGroup);

				for (StoryNode successor : storyGroup.getSuccessors())
					successor.process(this);
			}

			@Override
			public void processStoryPoint(StoryPoint storyPoint) {
				this.defaultProcessComplex(storyPoint);

				for (StoryNode successor : storyPoint.getSuccessors())
					successor.process(this);
			}

			@Override
			public void processControlIt(ControlIt controlIt) {
				controlIt.processParameters(this);
				this.defaultProcessComplex(controlIt);
			}

			@Override
			public void processCauseIt(CauseIt causeIt) {
				causeIt.processSubjects(this);
				causeIt.processParameters(this);
				this.defaultProcessComplex(causeIt);
			}

			@Override
			public void processScriptIt(ScriptIt scriptIt) {
				scriptIt.processParameters(this);
				this.defaultProcessComplex(scriptIt);
			}

			@Override
			public void processKnowIt(final KnowIt knowIt) {
				knowIt.getBinding().process(new BindingAdapter() {

					@Override
					public void processReference(
							KnowItBindingReference reference) {
						reference.getValue().getBinding().process(this);
					}

					@Override
					public void processFunction(KnowItBindingFunction function) {
						function.getValue().process(new StoryAdapter() {

							@Override
							public void processScriptIt(ScriptIt scriptIt) {
								for (KnowIt param : scriptIt.getParameters()) {
									if (param.getBinding() instanceof KnowItBindingResource) {
										final KnowItBindingResource resource = (KnowItBindingResource) param
												.getBinding();

										if (resource
												.isIdentifiableGameConstant())
											resources.add(resource.getValue());
									}
								}
							}
						});
					};

					@Override
					public void processResource(KnowItBindingResource resource) {
						if (resource.isIdentifiableGameConstant())
							resources.add(resource.getValue());
					};

				});
			}

			@Override
			public void processAskIt(AskIt askIt) {
				askIt.getCondition().process(this);
				this.defaultProcessComplex(askIt);
			}
		};

		SEModelManager.getInstance().getActiveRoot().process(adapter);

		return resources;
	}

	@Override
	public String getSlotDefaultFormat() {
		String defaultFormat = "";

		for (LibraryModel library : this.getLibraries()) {
			defaultFormat = library.getSlotDefaultFormat();

			if (StringOp.exists(defaultFormat))
				break;
		}

		return defaultFormat;
	}

	@Override
	public Slot getSlot(String name) {
		Slot slot = null;

		for (LibraryModel library : this.getLibraries()) {
			slot = library.getSlot(name);

			if (slot != null)
				break;
		}

		return slot;
	}

	@Override
	public void setTitle(String title) {
		super.setTitle(title);

		SEModelManager.getInstance().notifyChange(this,
				SEModelEvent.Type.TITLECHANGED);
	}

	@Override
	public String toString() {
		return "StoryModel [" + this.getTitle() + "]";
	}

	@Override
	public void process(ModelVisitor processController) {
		processController.processStoryModel(this);
	}
}

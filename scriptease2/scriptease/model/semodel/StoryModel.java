package scriptease.model.semodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scriptease.controller.ModelVisitor;
import scriptease.controller.observer.ObserverManager;
import scriptease.controller.observer.StoryModelObserver;
import scriptease.gui.WindowFactory;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingAutomatic;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.StoryPoint;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.model.semodel.dialogue.DialogueLine.Speaker;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.io.model.GameModule;
import scriptease.translator.io.model.GameType;
import scriptease.translator.io.model.GameType.GUIType;
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
			String compatibleVersion, Translator translator,
			Collection<LibraryModel> optionalLibraries) {
		super(title, author);

		this.startPoint = new StoryPoint("Start");
		this.module = module;
		this.translator = translator;
		this.compatibleVersion = compatibleVersion;
		this.optionalLibraries = optionalLibraries;
		this.observerManager = new ObserverManager<StoryModelObserver>();

		// TODO load dialogues here.
		this.dialogueRoots = new ArrayList<DialogueLine>();
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
		final DialogueLine newRoot = new DialogueLine(this, Speaker.PC);

		this.dialogueRoots.add(newRoot);

		return newRoot;
	}

	/**
	 * Adds a specific dialogue line
	 * 
	 * @param line
	 * @return
	 */
	public boolean addDialogueRoot(DialogueLine line) {
		return this.dialogueRoots.add(line);
	}

	public boolean addDialogueRoots(Collection<DialogueLine> lines) {
		return this.dialogueRoots.addAll(lines);
	}

	public boolean removeDialogueRoot(DialogueLine line) {
		return this.dialogueRoots.remove(line);
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
		System.out.println(startPoint + " loaded in " + this);
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
					"The Library, " + library.getTitle()
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

	@Override
	public Collection<GameType> getTypes() {
		final Collection<GameType> keywords = new ArrayList<GameType>();

		for (LibraryModel library : this.getLibraries()) {
			keywords.addAll(library.getLibraryTypes());
		}

		return keywords;
	}

	@Override
	public GameType getType(String keyword) {
		for (LibraryModel library : this.getLibraries()) {
			final GameType type = library.getType(keyword);

			if (type != null)
				return type;
		}

		return null;
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

	/**
	 * Attaches the automatic bindings for any causes in the given story points,
	 * if an automatic binding is required for the specific cause as outlined in
	 * the API dictionary.
	 * 
	 * @param storyPoints
	 *            The collection of story points that automatic bindings should
	 *            be attached to.
	 */
	public void setAutomaticBindings(Collection<StoryPoint> storyPoints) {
		for (StoryPoint storyPoint : storyPoints) {
			for (StoryComponent component : storyPoint.getChildren()) {
				if (component instanceof CauseIt) {
					final CauseIt cause = (CauseIt) component;
					for (KnowIt parameter : cause.getParameters()) {

						final KnowItBinding binding = parameter.getBinding();
						if (binding instanceof KnowItBindingAutomatic) {
							final List<Resource> automatics = new ArrayList<Resource>();
							automatics.addAll(module.getAutomaticHandlers());

							parameter.setBinding(automatics.get(0));
						}
					}
				}
			}
		}
	}

	/**
	 * Generates a collection of story components that should be automatically
	 * added to the model. This should likely only be called right before
	 * writing the code. The automatics generated will have the proper bindings
	 * on their parameters, and are ready to be written out.
	 * 
	 * @param model
	 * @return
	 */
	public Collection<StoryComponent> generateAutomaticCauses() {
		final Collection<StoryComponent> automatics;
		final Collection<Resource> automaticHandlers;

		automatics = new ArrayList<StoryComponent>();
		automaticHandlers = this.module.getAutomaticHandlers();

		for (LibraryModel library : this.getLibraries()) {
			for (Resource resource : automaticHandlers) {
				final Collection<String> resourceTypes = resource.getTypes();

				for (CauseIt automatic : library.getAutomatics()) {
					final CauseIt copy = automatic.clone();
					final Collection<KnowIt> parameters = copy.getParameters();

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

		return automatics;
	}

	@Override
	public String getTypeRegex(String keyword) {
		String typeRegex = "";

		for (LibraryModel library : this.getLibraries()) {
			typeRegex = library.getTypeRegex(keyword);

			if (StringOp.exists(typeRegex))
				break;
		}

		return typeRegex;
	}

	@Override
	public Map<String, String> getTypeEnumeratedValues(String keyword) {
		final Map<String, String> enums = new HashMap<String, String>();

		for (LibraryModel library : this.getLibraries()) {
			enums.putAll(library.getTypeEnumeratedValues(keyword));

			if (!enums.isEmpty())
				break;
		}

		return enums;
	}

	@Override
	public String getTypeDisplayText(String keyword) {
		String displayText = "";

		for (LibraryModel library : this.getLibraries()) {
			displayText = library.getTypeDisplayText(keyword);

			if (StringOp.exists(displayText))
				break;
		}

		return displayText;
	}

	@Override
	public Collection<String> getTypeSlots(String keyword) {
		final Collection<String> slots;

		slots = new ArrayList<String>();

		for (LibraryModel library : this.getLibraries()) {
			slots.addAll(library.getTypeSlots(keyword));

			if (!slots.isEmpty())
				break;
		}

		return slots;
	}

	@Override
	public String getTypeCodeSymbol(String keyword) {
		String codeSymbol = "";

		for (LibraryModel library : this.getLibraries()) {
			codeSymbol = library.getTypeCodeSymbol(keyword);

			if (StringOp.exists(codeSymbol))
				break;
		}

		return codeSymbol;
	}

	@Override
	public Map<String, String> getTypeEscapes(String keyword) {
		final Map<String, String> escapes = new HashMap<String, String>();

		for (LibraryModel library : this.getLibraries()) {
			escapes.putAll(library.getTypeEscapes(keyword));

			if (!escapes.isEmpty())
				break;

		}

		return escapes;
	}

	@Override
	public GUIType getTypeGUI(String keyword) {
		GUIType gui = null;

		for (LibraryModel library : this.getLibraries()) {
			gui = library.getTypeGUI(keyword);

			if (gui != null)
				break;
		}

		return gui;
	}

	@Override
	public String getTypeWidgetName(String keyword) {
		String widgetName = "";

		for (LibraryModel library : this.getLibraries()) {
			widgetName = library.getTypeWidgetName(keyword);

			if (StringOp.exists(widgetName))
				break;
		}

		return widgetName;
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
	public String toString() {
		return "StoryModel [" + this.getTitle() + "]";
	}

	@Override
	public void process(ModelVisitor processController) {
		processController.processStoryModel(this);
	}
}

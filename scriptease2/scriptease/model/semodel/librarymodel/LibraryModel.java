package scriptease.model.semodel.librarymodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scriptease.controller.BindingAdapter;
import scriptease.controller.BindingVisitor;
import scriptease.controller.ModelVisitor;
import scriptease.controller.StoryAdapter;
import scriptease.controller.observer.ObserverManager;
import scriptease.controller.observer.library.LibraryEvent;
import scriptease.controller.observer.library.LibraryObserver;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.model.CodeBlock;
import scriptease.model.CodeBlockSource;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.describeits.DescribeItNode;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.semodel.SEModel;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.io.model.GameType;
import scriptease.translator.io.model.GameType.GUIType;
import scriptease.translator.io.model.Slot;
import scriptease.util.FileOp;
import scriptease.util.StringOp;

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
 * @author kschenk
 */
public class LibraryModel extends SEModel implements StoryComponentObserver {
	private static final String AUTOMATIC_LABEL = "automatic";
	private static final String COMMON_LIBRARY_NAME = "ScriptEase";

	private final ObserverManager<LibraryObserver> observerManager;

	private final Collection<String> includeFilePaths;
	// We save this as a variable since add is called many times.
	private final StoryAdapter categoryAdder;

	private final DescribeItManager describeItManager;
	private final EventSlotManager slotManager;
	private final Map<String, GameType> gameTypes;
	private final TypeConverter typeConverter;

	private File location;
	private Translator translator;
	private StoryComponentContainer effectsCategory;
	private StoryComponentContainer causesCategory;
	private StoryComponentContainer descriptionsCategory;
	private StoryComponentContainer controllersCategory;
	private StoryComponentContainer noteContainer;
	private StoryComponentContainer modelRoot;

	private int nextID;

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
		this.gameTypes = new HashMap<String, GameType>();
		this.typeConverter = new TypeConverter();

		this.translator = translator;
		this.modelRoot = new StoryComponentContainer(title);
		this.slotManager = new EventSlotManager();
		this.describeItManager = new DescribeItManager();
		this.includeFilePaths = new ArrayList<String>();

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
		this.observerManager = new ObserverManager<LibraryObserver>();

		this.categoryAdder = new StoryAdapter() {
			final LibraryModel model = LibraryModel.this;

			@Override
			public void processScriptIt(ScriptIt scriptIt) {
				final boolean success = this.model.effectsCategory
						.addStoryChild(scriptIt);
				if (success)
					scriptIt.addStoryComponentObserver(this.model);
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
			public void processCauseIt(CauseIt causeIt) {
				final boolean success = this.model.causesCategory
						.addStoryChild(causeIt);
				if (success)
					causeIt.addStoryComponentObserver(this.model);
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
		this.causesCategory.registerChildType(CauseIt.class, max);
		this.descriptionsCategory.registerChildType(KnowIt.class, max);
		this.controllersCategory.registerChildType(
				StoryComponentContainer.class, max);
		this.controllersCategory.registerChildType(ScriptIt.class, max);
		this.controllersCategory.registerChildType(AskIt.class, max);
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
	 * Sets the location of the .sel or .xml Library file.
	 * 
	 * @param location
	 */
	public void setLocation(File location) {
		this.location = location;
	}

	/**
	 * Returns the location of the .sel or .xml Library file.
	 * 
	 * @return
	 */
	public File getLocation() {
		return this.location;
	}

	/**
	 * Sets the include file paths to the passed in collection.
	 * 
	 * @param paths
	 */
	public void setIncludeFilePaths(Collection<String> paths) {
		this.includeFilePaths.clear();
		this.includeFilePaths.addAll(paths);
	}

	/**
	 * Returns the include file paths. For the files, use
	 * {@link #getIncludeFiles()} instead.
	 * 
	 * @return
	 */
	public Collection<String> getIncludeFilePaths() {
		return this.includeFilePaths;
	}

	/**
	 * Returns the include files used by the LibraryModel. For the paths, use
	 * {@link #getIncludeFilePaths()} instead.
	 * 
	 * @return
	 */
	public Collection<File> getIncludeFiles() {
		final Collection<File> includeFiles = new ArrayList<File>();

		if (FileOp.exists(this.location)) {
			for (String includeFilePath : this.includeFilePaths) {
				final File includeFile;

				includeFile = new File(this.location.getParentFile(),
						includeFilePath);

				if (FileOp.exists(includeFile))
					includeFiles.add(includeFile);
			}
		}

		return includeFiles;
	}

	/**
	 * Retrieves the automatics causeIts
	 * 
	 * @return
	 */
	public Collection<CauseIt> getAutomatics() {
		final Collection<CauseIt> automatics = new ArrayList<CauseIt>();

		for (StoryComponent cause : this.getCausesCategory().getChildren()) {
			if (cause instanceof CauseIt) {
				for (String label : cause.getLabels()) {
					if (label.equalsIgnoreCase(AUTOMATIC_LABEL)) {
						automatics.add((CauseIt) cause);
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
			System.err.println(this.getTitle() + " Library "
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

		if (component instanceof ScriptIt) {
			final List<CodeBlock> codeBlocks;

			codeBlocks = new ArrayList<CodeBlock>(
					((ScriptIt) component).getCodeBlocks());

			for (CodeBlock codeBlock : codeBlocks) {
				this.nextID = Math.max(codeBlock.getId() + 1, this.nextID);
			}
		}

		this.notifyChange(component, LibraryEvent.Type.ADDITION);
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
		final KnowIt knowIt;

		knowIt = this.createKnowItForDescribeIt(describeIt);

		this.add(knowIt);
		this.addDescribeIt(describeIt, knowIt);
	}

	public void remove(StoryComponent component) {
		component.process(new StoryAdapter() {
			@Override
			public void processScriptIt(ScriptIt scriptIt) {
				final boolean success = LibraryModel.this.effectsCategory
						.removeStoryChild(scriptIt);
				if (success)
					scriptIt.removeStoryComponentObserver(LibraryModel.this);

			}

			@Override
			public void processCauseIt(CauseIt causeIt) {
				final boolean success = LibraryModel.this.causesCategory
						.removeStoryChild(causeIt);
				if (success)
					causeIt.removeStoryComponentObserver(LibraryModel.this);
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

		this.notifyChange(component, LibraryEvent.Type.REMOVAL);
	}

	public void addAll(Collection<? extends StoryComponent> components) {
		for (StoryComponent component : components) {
			this.add(component);
		}
	}

	public void addLibraryChangeListener(LibraryObserver observer) {
		this.observerManager.addObserver(this, observer);
	}

	public void addLibraryChangeListener(Object object, LibraryObserver observer) {
		this.observerManager.addObserver(object, observer);
	}

	public void removeLibraryChangeListener(LibraryObserver observer) {
		this.observerManager.removeObserver(observer);
	}

	private void notifyChange(StoryComponent source, LibraryEvent.Type type) {
		for (LibraryObserver observer : this.observerManager.getObservers()) {
			observer.modelChanged(this, new LibraryEvent(source, type));
		}
	}

	/**
	 * Forward the event to the LibraryModel observers, and save the library to
	 * file since it has changed
	 */
	@Override
	public void componentChanged(StoryComponentEvent event) {
		this.notifyChange(event.getSource(), LibraryEvent.Type.CHANGE);
	}

	/**
	 * Get's all the LibraryModel's StoryComponents
	 * 
	 * @return
	 */
	public List<StoryComponent> getAllStoryComponents() {
		final List<StoryComponent> components = new ArrayList<StoryComponent>();

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
		return this.getTitle();
	}

	@Override
	public void process(ModelVisitor processController) {
		processController.processLibraryModel(this);
	}

	/**
	 * Finds a CodeBlockSource by its ID number.
	 * 
	 * @param targetId
	 *            The ID number of the CodeBlockSource to locate.
	 * 
	 * @return The CodeBlockSource that has the given id.
	 */
	public CodeBlockSource getCodeBlockByID(int targetId) {
		final CodeBlockFinder finder = new CodeBlockFinder();
		CodeBlockSource found;

		found = finder.findByID(targetId);

		return found;
	}

	private class CodeBlockFinder extends StoryAdapter {
		private CodeBlockSource found = null;
		private int targetId;

		/**
		 * Finds a CodeBlockSource by ID.
		 * 
		 * @param targetId
		 *            The ID to search by.
		 * @param dictionary
		 *            The dictionary to search in.
		 * 
		 * @return The source with the given id.
		 */
		public CodeBlockSource findByID(int targetId) {
			this.targetId = targetId;

			// let's start snooping about. Quick, someone play Pink Panther or
			// Mission Impossible! - remiller
			LibraryModel.this.getRoot().process(this);

			// not in the library. Try the slots next?
			if (this.found == null) {
				final Collection<KnowIt> knowIts = new ArrayList<KnowIt>();

				for (Slot slot : LibraryModel.this.getSlots()) {
					// gotta collect 'em together first.
					knowIts.addAll(slot.getImplicits());
					knowIts.addAll(slot.getParameters());

					for (KnowIt knowIt : knowIts) {
						knowIt.process(this);

						if (this.found != null) {
							return this.found;
						}
					}
					// keep looking
					knowIts.clear();
				}
			}

			return this.found;
		}

		@Override
		public void processScriptIt(ScriptIt scriptIt) {
			if (this.found != null)
				return;

			super.processScriptIt(scriptIt);

			for (CodeBlock block : scriptIt.getCodeBlocks()) {
				if (block.getId() == this.targetId
						&& block instanceof CodeBlockSource) {
					this.found = (CodeBlockSource) block;
					return;
				}
			}
		}

		@Override
		public void processKnowIt(KnowIt knowIt) {
			if (this.found != null)
				return;

			super.processKnowIt(knowIt);

			final CodeBlockFinder searcher = this;
			final BindingVisitor bindingSearcher;

			bindingSearcher = new BindingAdapter() {
				@Override
				public void processFunction(KnowItBindingFunction function) {
					function.getValue().process(searcher);
				}

				@Override
				public void processReference(KnowItBindingReference reference) {
					reference.getValue().process(searcher);
				}

				@Override
				public void processStoryPoint(KnowItBindingStoryPoint storyPoint) {
					storyPoint.getValue().process(searcher);
				}
			};

			knowIt.getBinding().process(bindingSearcher);
		}

		@Override
		protected void defaultProcessComplex(ComplexStoryComponent complex) {
			super.defaultProcessComplex(complex);

			for (StoryComponent child : complex.getChildren()) {
				child.process(this);

				// Found it. All craft, pull up!
				if (this.found != null)
					return;
			}
		}
	}

	/**
	 * Retrieves the next code block unique ID for this library.
	 * 
	 * @return The next available unique id for a code block.
	 */
	public int getNextCodeBlockID() {
		return this.nextID++;
	}

	/**
	 * Returns the DescribeIts from the library. Since we can't reference
	 * scriptIt's from another library, this will only return DescribeIts from
	 * this library and not the default library.
	 * 
	 * @return
	 */
	public Collection<DescribeIt> getDescribeIts() {
		return this.describeItManager.getDescribeIts();
	}

	/**
	 * Adds a describe it and it's attached story component.
	 * 
	 * @param describeIt
	 * @param component
	 */
	public void addDescribeIt(DescribeIt describeIt, StoryComponent component) {
		this.describeItManager.addDescribeIt(describeIt, component);
	}

	/**
	 * Removes a DescribeIt.
	 * 
	 * @param describeIt
	 */
	public void removeDescribeIt(DescribeIt describeIt) {
		this.describeItManager.removeDescribeIt(describeIt);
	}

	/**
	 * Returns the DescribeIt associated with the component.
	 * 
	 * @param component
	 * @return
	 */
	public DescribeIt getDescribeIt(StoryComponent component) {
		return this.describeItManager.getDescribeIt(component);
	}

	/**
	 * Returns true if the node is an end node in the path.
	 * 
	 * @param node
	 * @return
	 */
	public boolean isDescribeItEndNode(DescribeItNode node) {
		return this.describeItManager.isDescribeItEndNode(node);
	}

	/**
	 * Generates a blank KnowIt for the DescribeIt type that has the shortest
	 * path selected by default.
	 * 
	 * @param describeIt
	 * @return
	 */
	public KnowIt createKnowItForDescribeIt(DescribeIt describeIt) {
		return this.describeItManager.createKnowItForDescribeIt(describeIt);
	}

	/**
	 * Searches through the describe its to find a script it with the same
	 * display text as the one passed in. Since this is based off display text,
	 * results may not be accurate. Be warned.
	 * 
	 * @param scriptIt
	 * @return
	 */
	public DescribeIt findDescribeItWithScriptIt(ScriptIt scriptIt) {
		return this.describeItManager.findDescribeItWithScriptIt(scriptIt);
	}

	/**
	 * Returns all of the {@link GameType}s stored by the Library.
	 * 
	 * @return
	 */
	public Collection<GameType> getGameTypes() {
		return new ArrayList<GameType>(this.gameTypes.values());
	}

	/**
	 * Adds the game types to the library.
	 * 
	 * @param types
	 */
	public void addGameTypes(Collection<GameType> types) {
		for (GameType type : types)
			this.gameTypes.put(type.getKeyword(), type);
	}

	/**
	 * Returns the {@link TypeConverter}.
	 * 
	 * @return
	 */
	public TypeConverter getTypeConverter() {
		return this.typeConverter;
	}

	/**
	 * Returns the format for the type as a collection of
	 * {@link AbstractFragment}s.
	 * 
	 * @param keyword
	 * @return
	 */
	public Collection<AbstractFragment> getTypeFormat(String keyword) {
		final GameType type = this.gameTypes.get(keyword);
		final Collection<AbstractFragment> format = new ArrayList<AbstractFragment>();
		if (type != null)
			format.addAll(type.getFormat());
		return format;
	}

	/**
	 * Returns the default library for this library's translator. Returns null
	 * if there is no translator.
	 * 
	 * @return
	 */
	private LibraryModel getTranslatorDefaultLibrary() {
		final Translator translator = this.getTranslator();

		if (translator != null)
			return translator.getLibrary();
		else
			return null;

	}

	@Override
	public Collection<GameType> getTypes() {
		final LibraryModel defaultLibrary = this.getTranslatorDefaultLibrary();
		final Collection<GameType> keywords = new ArrayList<GameType>();

		if (defaultLibrary != null && this != defaultLibrary) {
			keywords.addAll(defaultLibrary.getLibraryTypes());
		}

		keywords.addAll(this.getLibraryTypes());

		return keywords;
	}

	/**
	 * Returns a collection of keywords associated with types stored by this
	 * LibraryModel only.
	 */
	public Collection<GameType> getLibraryTypes() {
		return new ArrayList<GameType>(this.gameTypes.values());
	}

	@Override
	public GameType getType(String keyword) {
		final LibraryModel defaultLibrary = this.getTranslatorDefaultLibrary();
		final GameType type = this.gameTypes.get(keyword);

		if (type == null && defaultLibrary != null && this != defaultLibrary) {
			return defaultLibrary.getType(keyword);
		} else
			return type;
	}

	@Override
	public String getTypeRegex(String keyword) {
		final LibraryModel defaultLibrary = this.getTranslatorDefaultLibrary();
		final GameType type = this.gameTypes.get(keyword);

		String regex;
		if (type != null) {
			regex = type.getReg();
		} else
			regex = "";

		if (!StringOp.exists(regex) && defaultLibrary != null
				&& this != defaultLibrary) {
			regex = defaultLibrary.getTypeRegex(keyword);
		}

		return regex;
	}

	@Override
	public Map<String, String> getTypeEnumeratedValues(String keyword) {
		final LibraryModel defaultLibrary = this.getTranslatorDefaultLibrary();
		final Map<String, String> enums = new HashMap<String, String>();
		final GameType type = this.gameTypes.get(keyword);

		if (type != null)
			enums.putAll(type.getEnumMap());

		if (enums.isEmpty() && defaultLibrary != null && this != defaultLibrary) {
			enums.putAll(defaultLibrary.getTypeEnumeratedValues(keyword));
		}

		return enums;
	}

	@Override
	public String getTypeDisplayText(String keyword) {
		final LibraryModel defaultLibrary = this.getTranslatorDefaultLibrary();
		final GameType type = this.gameTypes.get(keyword);

		String displayText;
		if (type != null) {
			displayText = type.getDisplayName();
		} else
			displayText = "";

		if (!StringOp.exists(displayText) && defaultLibrary != null
				&& this != defaultLibrary) {
			displayText = defaultLibrary.getTypeDisplayText(keyword);
		}

		return displayText;
	}

	@Override
	public Collection<String> getTypeSlots(String keyword) {
		final LibraryModel defaultLibrary = this.getTranslatorDefaultLibrary();

		final Collection<String> slots = new ArrayList<String>();
		final GameType type = this.gameTypes.get(keyword);

		if (type != null)
			slots.addAll(type.getSlots());

		if (slots.isEmpty() && defaultLibrary != null && this != defaultLibrary) {
			slots.addAll(defaultLibrary.getTypeSlots(keyword));
		}

		return slots;
	}

	@Override
	public String getTypeCodeSymbol(String keyword) {
		final LibraryModel defaultLibrary = this.getTranslatorDefaultLibrary();
		final GameType type = this.gameTypes.get(keyword);

		String codeSymbol;
		if (type != null) {
			codeSymbol = type.getCodeSymbol();
		} else
			codeSymbol = "";

		if (!StringOp.exists(codeSymbol) && defaultLibrary != null
				&& this != defaultLibrary) {
			codeSymbol = defaultLibrary.getTypeCodeSymbol(keyword);
		}

		return codeSymbol;
	}

	@Override
	public Map<String, String> getTypeEscapes(String keyword) {
		final LibraryModel defaultLibrary = this.getTranslatorDefaultLibrary();
		final Map<String, String> escapes = new HashMap<String, String>();
		final GameType type = this.gameTypes.get(keyword);

		if (type != null)
			escapes.putAll(type.getEscapes());

		if (escapes.isEmpty() && defaultLibrary != null
				&& this != defaultLibrary) {
			escapes.putAll(defaultLibrary.getTypeEscapes(keyword));
		}

		return escapes;
	}

	@Override
	public GUIType getTypeGUI(String keyword) {
		final LibraryModel defaultLibrary = this.getTranslatorDefaultLibrary();
		final GameType type = this.gameTypes.get(keyword);

		GUIType gui;
		if (type != null)
			gui = type.getGui();
		else
			gui = null;

		if (gui == null && defaultLibrary != null && this != defaultLibrary) {
			gui = defaultLibrary.getTypeGUI(keyword);
		}

		return gui;
	}

	@Override
	public String getTypeWidgetName(String keyword) {
		final LibraryModel defaultLibrary = this.getTranslatorDefaultLibrary();
		final GameType type = this.gameTypes.get(keyword);

		String widgetName;
		if (type != null)
			widgetName = type.getWidgetName();
		else
			widgetName = null;

		if (!StringOp.exists(widgetName) && defaultLibrary != null
				&& this != defaultLibrary) {
			widgetName = defaultLibrary.getTypeWidgetName(keyword);
		}

		return widgetName;
	}

	/**
	 * Sets the default format of the slot manager.
	 * 
	 * @param defaultKeyword
	 */
	public void setSlotDefaultFormat(String defaultKeyword) {
		this.slotManager.setDefaultFormatKeyword(defaultKeyword);
	}

	@Override
	public String getSlotDefaultFormat() {
		final LibraryModel defaultLibrary = this.getTranslatorDefaultLibrary();
		String format = this.slotManager.getDefaultFormatKeyword();

		if (!StringOp.exists(format) && defaultLibrary != null
				&& this != defaultLibrary) {
			format = defaultLibrary.getSlotDefaultFormat();
		}

		return format;
	}

	/**
	 * Returns the parameters known for the slot.
	 * 
	 * @param slot
	 * @return
	 */
	public Collection<KnowIt> getSlotParameters(String slot) {
		return this.getSlot(slot).getParameters();
	}

	/**
	 * Returns the implicits for a slot.
	 * 
	 * @param slot
	 * @return
	 */
	public Collection<KnowIt> getSlotImplicits(String slot) {
		return this.getSlot(slot).getImplicits();
	}

	/**
	 * Returns the condition for a slot.
	 * 
	 * @param slot
	 * @return
	 */
	public String getSlotCondition(String slot) {
		return this.getSlot(slot).getCondition();
	}

	@Override
	public Slot getSlot(String name) {
		final LibraryModel defaultLibrary = this.getTranslatorDefaultLibrary();
		Slot slot = this.slotManager.getEventSlot(name);

		if (slot == null && defaultLibrary != null && this != defaultLibrary) {
			slot = defaultLibrary.getSlot(name);
		}

		return slot;
	}

	/**
	 * Returns all of the slots known by the library model. Does not return any
	 * of the default library model's slots.
	 * 
	 * @return
	 */
	public Collection<Slot> getSlots() {
		return this.slotManager.getEventSlots();
	}

	public void addSlots(Collection<Slot> slots) {
		this.slotManager.addEventSlots(slots, this);
	}
}

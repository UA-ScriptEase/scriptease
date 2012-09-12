package scriptease.model;

import scriptease.controller.ModelVisitor;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.Translator;
import scriptease.translator.io.model.GameModule;

/**
 * A StoryModel is the binding object that associates a GameModule with a
 * StoryComponent tree so the tree can access its possible bindings.
 * 
 * @author remiller
 * @author mfchurch
 * @author kschenk
 */
public final class StoryModel extends PatternModel {
	private final GameModule module;
	private final Translator translator;
//	private final ModelVerifier modelVerifier;
	private StoryPoint startPoint;

	/**
	 * Builds a StoryModel with the supplied module and translator, and defaults
	 * for all other properties
	 * 
	 * @param module
	 *            The {@link GameModule} associated with the model objects. The
	 *            StoryModel's model objects can access only the game data from
	 *            this module.
	 * @param translator
	 *            The Translator to use to interpret this story.
	 */
	public StoryModel(GameModule module, Translator translator) {
		this(module, "", "", translator);
	}

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
			Translator translator) {
		super(title, author);

		// Temporary code to make a new quest model with a start and end node.
		this.startPoint = new StoryPoint("Start");
		this.module = module;
		this.translator = translator;
//		this.modelVerifier = new ModelVerifier(this.modelRoot);
//		this.createModelRules();
	}
	
	public void setStartPoint(StoryPoint startPoint){
		if(startPoint == null)
			throw new IllegalArgumentException("Cannot give StoryModel a null tree root.");
		this.startPoint = startPoint;
	}
	
	public StoryPoint getRoot(){
		return this.startPoint;
	}

	/**
	 * Creation and addition of rules that need to be maintained in the model.
	 */
//	private void createModelRules() {
//		if (modelVerifier == null)
//			throw new IllegalStateException(
//					"Model Verification was not initialized correctly");
//
//		Collection<StoryComponentChangeEnum> uniqueNameEvents = new ArrayList<StoryComponentChangeEnum>(
//				1);
//		uniqueNameEvents.add(StoryComponentChangeEnum.CHANGE_TEXT_NAME);
//		uniqueNameEvents.add(StoryComponentChangeEnum.CHANGE_CHILD_ADDED);
//
//		Collection<StoryComponentChangeEnum> referenceEvents = new ArrayList<StoryComponentChangeEnum>(
//				1);
//		referenceEvents.add(StoryComponentChangeEnum.CHANGE_CHILD_ADDED);
//		referenceEvents.add(StoryComponentChangeEnum.CHANGE_CHILD_REMOVED);
//		referenceEvents.add(StoryComponentChangeEnum.CHANGE_KNOW_IT_BOUND);
//
//		this.modelVerifier.addRule(new UniqueNameRule(), uniqueNameEvents);
//		this.modelVerifier.addRule(new ReferenceRule(), referenceEvents);
//	}

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

	@Override
	public String toString() {
		return "StoryModel [" + this.getName() + "]";
	}

	@Override
	public void process(ModelVisitor processController) {
		processController.processStoryModel(this);
	}
}

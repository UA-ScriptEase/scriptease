package scriptease.model.complex.behaviours;

import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.model.complex.ScriptIt;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.io.model.SimpleResource;
import scriptease.util.ListOp;

/**
 * A Behaviour represents a series of Tasks {@link Task}. A Behaviour can be
 * independent or collaborative, as defined by its tasks. An independent
 * behaviour is one that is executed by only one subject, while a collaborative
 * behaviour has a respondant to the subject in execution.
 * 
 * For example, an independent behaviour could be a subject walking around
 * randomly by him/herself. A collaborative behaviour could be a tavern patron
 * interacting with the bartender to perform a order drink behaviour.
 * 
 * @author jyuen
 */
public class Behaviour extends ScriptIt {
	public static final String INDEPENDENT_DISPLAY_TEXT = "<Initiator> does action with priority <Priority>";
	public static final String PRIORITY_TEXT = "Priority";

	private Task startTask;
	private Type type;

	public enum Type {
		INDEPENDENT, COLLABORATIVE
	}

	public Behaviour(LibraryModel library) {
		this(library, Type.INDEPENDENT, 0);
	}

	/**
	 * Creates a new behaviour.
	 * 
	 * @param displayText
	 *            the name for the behaviour
	 * @param type
	 *            the type of the behaviour - Independent or Collaborative
	 * @param startTask
	 *            the start task for this behaviour
	 * @param priority
	 *            the priority of this behaviour - higher priority means higher
	 *            order of execution.
	 */
	public Behaviour(LibraryModel library, Behaviour.Type type, int priority) {
		super(library, INDEPENDENT_DISPLAY_TEXT);

		this.type = type;

		this.registerChildType(Task.class, MAX_NUM_OF_ONE_TYPE);
		this.registerChildType(IndependentTask.class, MAX_NUM_OF_ONE_TYPE);
		this.registerChildType(CollaborativeTask.class, MAX_NUM_OF_ONE_TYPE);
	}

	// ******************* GETTERS AND SETTERS **********************//

	/**
	 * @return the priority
	 */
	public Float getPriority() {
		return Float.parseFloat(((SimpleResource) this.getParameter("Priority")
				.getBinding().getValue()).getCodeText());
	}

	@Override
	public boolean addStoryChild(StoryComponent newChild) {
		// TODO Auto-generated method stub
		return super.addStoryChild(newChild);
	}

	public boolean addStoryChildBefore(StoryComponent newChild,
			StoryComponent sibling) {
		return super.addStoryChildBefore(newChild, sibling);
	};

	/**
	 * @param priority
	 *            the priority to set
	 */
	public void setPriority(Float priority) {
		this.getParameter("Priority").setBinding(
				new KnowItBindingResource(SimpleResource.buildSimpleResource(
						"Number", priority + "")));
	}

	/**
	 * @return the startTask
	 */
	public Task getStartTask() {
		return startTask;
	}

	/**
	 * @param startTask
	 *            the startTask to set
	 */
	public void setStartTask(Task startTask) {
		if (startTask == this.startTask)
			return;

		// remove old start task child
		this.clearStoryChildren();

		if (startTask == null) {
			if (type == Type.INDEPENDENT) {
				this.startTask = new IndependentTask(this.getLibrary());
			} else {
				this.startTask = new CollaborativeTask(this.getLibrary());
			}
		} else
			this.startTask = startTask;

		this.addStoryChild(this.startTask);
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Changes the behaviour to a Collaborative or Independent behaviour. Note
	 * that this removes everything in the Behaviour.
	 * 
	 * @param type
	 *            the type to set
	 */
	public void setType(Type type) {
		if (this.type == type)
			return;

		this.type = type;

		this.resetBehaviour();

		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CHANGE_BEHAVIOUR_TYPE));
	}

	public void resetBehaviour() {
		this.setStartTask(null);

		if (this.codeBlocks.isEmpty())
			return;

		final CodeBlock main = this.getMainCodeBlock();
		final LibraryModel lib = this.getLibrary();
		// TODO TRANSLATOR DEPENDENCY ALERT FOR TYPES!
		final KnowIt initiator = new KnowIt(lib, "Initiator", "Creature");
		final KnowIt priority = new KnowIt(lib, "Priority", "Number");

		main.clearParameters();

		priority.setBinding(new SimpleResource(priority.getTypes(), Float
				.toString(this.getPriority())));

		main.addParameter(initiator);

		if (this.type == Type.COLLABORATIVE) {

			this.setDisplayText("<Initiator> interacts with <Responder> with priority <Priority>");

			// TODO CREATURE TYPE ISTRANSLATOR DEPENDENT!!!!
			main.addParameter(new KnowIt(lib, "Responder", "Creature"));
		} else if (this.type == Type.INDEPENDENT) {
			this.setDisplayText(INDEPENDENT_DISPLAY_TEXT);

		} else
			throw new IllegalStateException("Invalid type for Behaviour "
					+ type + " set.");

		main.addParameter(priority);
	}

	@Override
	public Behaviour clone() {
		final Behaviour component = (Behaviour) super.clone();

		component.type = this.type;
		component.setStartTask((Task) ListOp.head(component.getChildren()));

		return component;
	}

	@Override
	public void process(StoryVisitor visitor) {
		visitor.processBehaviour(this);
	}

	@Override
	public void revalidateKnowItBindings() {
		for (StoryComponent child : this.getChildren()) {
			child.revalidateKnowItBindings();
		}
	}
}

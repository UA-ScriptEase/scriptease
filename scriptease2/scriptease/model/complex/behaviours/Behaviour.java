package scriptease.model.complex.behaviours;

import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ComplexStoryComponent;

/**
 * A Behaviour represents a series of Tasks {@link Task}. A Behaviour can be
 * independent {@link IndependentBehaviour} or collaborative
 * {@link CollaborativeBehaviour}. An independent behaviour is one that is
 * executed by only one subject, while a collaborative behaviour has a
 * respondant to the subject in execution.
 * 
 * For example, an independent behaviour could be a subject walking around
 * randomly by him/herself. A collaborative behaviour could be a tavern patron
 * interacting with the bartender to perform a order drink behaviour.
 * 
 * @author jyuen
 */
public class Behaviour extends ComplexStoryComponent {

	private Task startTask;
	private Type type;

	private int priority;
	
	public enum Type {
		INDEPENDENT, COLLABORATIVE
	}

	public Behaviour(String name) {
		this(name, null, null, 0);
	}
	
	/**
	 * Behaviour prototype:
	 * 
	 * what the model looks like now :
	 *   - two kinds of tasks: collaborative & independent, that know their own effects
	 *   - a tool to build this behaviour
	 * 
	 * what we need:
	 *   - a way to represent behaviours in the dictionary
	 *   - a way to represent behaviours in JList
	 * 
	 */
	public Behaviour(String name, Behaviour.Type type, Task startTask, int priority) {
		super(name);

		this.startTask = startTask;
		this.priority = priority;

		if (startTask instanceof IndependentTask)
			this.type = Behaviour.Type.INDEPENDENT;
		else if (startTask instanceof CollaborativeTask)
			this.type = Behaviour.Type.COLLABORATIVE;
	}

	// ******************* GETTERS AND SETTERS **********************//

	/**
	 * @return the priority
	 */
	public Integer getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(Integer priority) {
		this.priority = priority;
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
		this.startTask = startTask;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(Type type) {
		this.type = type;
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

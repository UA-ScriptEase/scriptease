package scriptease.model.complex.behaviours;

import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;

/**
 * A collaborative task is a subclass of Task with a initiator subject and a
 * collaborator subject.
 * 
 * @author jyuen
 * 
 */
public class CollaborativeTask extends Task {

	private StoryComponentContainer initiatorEffectsContainer;
	private StoryComponentContainer responderEffectsContainer;

	private String initiatorName;
	private String responderName;

	/**
	 * Constructor to create a CollaborativeTask.
	 * 
	 * @param initiatorName
	 * @param responderName
	 */
	public CollaborativeTask(String initiatorName, String responderName) {
		super(initiatorName + ":" + responderName);

		this.initiatorEffectsContainer = new StoryComponentContainer(
				"Initiator Effects");
		this.initiatorEffectsContainer.registerChildType(ScriptIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);

		this.responderEffectsContainer = new StoryComponentContainer(
				"Collaborator Effects");
		this.responderEffectsContainer.registerChildType(ScriptIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);

		this.initiatorName = initiatorName;
		this.responderName = responderName;

		this.registerChildType(StoryComponentContainer.class, 2);
		this.addStoryChild(initiatorEffectsContainer);
		this.addStoryChild(responderEffectsContainer);
	}

	/**
	 * @return the initiatorEffectsContainer
	 */
	public StoryComponentContainer getInitiatorEffectsContainer() {
		return initiatorEffectsContainer;
	}

	/**
	 * @param initiatorEffectsContainer
	 *            the initiatorEffectsContainer to set
	 */
	public void setInitiatorEffectsContainer(
			StoryComponentContainer initiatorEffectsContainer) {
		this.initiatorEffectsContainer = initiatorEffectsContainer;
	}

	/**
	 * @return the responderEffectsContainer
	 */
	public StoryComponentContainer getResponderEffectsContainer() {
		return responderEffectsContainer;
	}

	/**
	 * @param responderEffectsContainer
	 *            the responderEffectsContainer to set
	 */
	public void setResponderEffectsContainer(
			StoryComponentContainer responderEffectsContainer) {
		this.responderEffectsContainer = responderEffectsContainer;
	}

	/**
	 * Sets the initiators name
	 * 
	 * @param initiatorName
	 */
	public void setInitiatorName(String initiatorName) {
		this.initiatorName = initiatorName;

		this.setDisplayText(this.initiatorName + ":" + this.responderName);
	}

	/**
	 * Sets the responders name
	 * 
	 * @param responderName
	 */
	public void setResponderName(String responderName) {
		this.responderName = responderName;

		this.setDisplayText(this.initiatorName + ":" + this.responderName);
	}

	/**
	 * @return The initiators name
	 */
	public String getInitiatorName() {
		return this.initiatorName;
	}

	/**
	 * @return The responders name
	 */
	public String getResponderName() {
		return this.responderName;
	}

	@Override
	public void process(StoryVisitor visitor) {
		visitor.processCollaborativeTask(this);
	}

	@Override
	public void revalidateKnowItBindings() {
		for (StoryComponent child : this.getChildren()) {
			child.revalidateKnowItBindings();
		}
	}
}

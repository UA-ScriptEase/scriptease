package scriptease.model.complex.behaviours;

import java.util.ArrayList;
import java.util.List;

import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;
import scriptease.model.complex.ScriptIt;

/**
 * A collaborative task is a subclass of Task with a initiator subject and a
 * collaborator subject.
 * 
 * @author jyuen
 * 
 */
public class CollaborativeTask extends Task {

	private List<ScriptIt> initiatorEffects;
	private List<ScriptIt> responderEffects;

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

		initiatorEffects = new ArrayList<ScriptIt>();
		responderEffects = new ArrayList<ScriptIt>();

		this.initiatorName = initiatorName;
		this.responderName = responderName;
	}

	/**
	 * @return the initiatorEffects
	 */
	public List<ScriptIt> getInitiatorEffects() {
		return this.initiatorEffects;
	}

	/**
	 * @param initiatorEffects
	 *            the initiatorEffects to set
	 */
	public void setInitiatorEffects(List<ScriptIt> initiatorEffects) {
		this.initiatorEffects = initiatorEffects;
	}

	/**
	 * @return the responderEffectsContainer
	 */
	public List<ScriptIt> getResponderEffects() {
		return this.responderEffects;
	}

	/**
	 * @param responderEffectsContainer
	 *            the responderEffectsContainer to set
	 */
	public void setResponderEffects(List<ScriptIt> responderEffects) {
		this.responderEffects = responderEffects;
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

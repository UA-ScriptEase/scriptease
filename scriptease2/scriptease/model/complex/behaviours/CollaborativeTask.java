package scriptease.model.complex.behaviours;

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
	private StoryComponentContainer collaboratorEffectsContainer;

	private String initiatorName;
	private String collaboratorName;

	/**
	 * Constructor to create a CollaborativeTask.
	 * 
	 * @param initiatorName
	 * @param collaboratorName
	 */
	public CollaborativeTask(String initiatorName, String collaboratorName) {
		super(initiatorName + ":" + collaboratorName);

		this.initiatorEffectsContainer = new StoryComponentContainer(
				"Initiator Effects");
		this.initiatorEffectsContainer.registerChildType(ScriptIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);

		this.collaboratorEffectsContainer = new StoryComponentContainer(
				"Collaborator Effects");
		this.collaboratorEffectsContainer.registerChildType(ScriptIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);

		this.initiatorName = initiatorName;
		this.collaboratorName = collaboratorName;

		this.registerChildType(StoryComponentContainer.class, 2);
		this.addStoryChild(initiatorEffectsContainer);
		this.addStoryChild(collaboratorEffectsContainer);
	}

	/**
	 * Sets the initiators name
	 * 
	 * @param initiatorName
	 */
	public void setInitiatorName(String initiatorName) {
		this.initiatorName = initiatorName;

		this.setDisplayText(this.initiatorName + ":" + this.collaboratorName);
	}

	/**
	 * Sets the collaborators name
	 * 
	 * @param collaboratorName
	 */
	public void setCollaboratorName(String collaboratorName) {
		this.collaboratorName = collaboratorName;

		this.setDisplayText(this.initiatorName + ":" + this.collaboratorName);
	}

	/**
	 * @return The initiators name
	 */
	public String getInitiatorName() {
		return this.initiatorName;
	}

	/**
	 * @return The collaborators name
	 */
	public String getCollaboratorName() {
		return this.collaboratorName;
	}
}

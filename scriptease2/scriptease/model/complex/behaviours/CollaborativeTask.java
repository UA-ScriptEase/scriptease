package scriptease.model.complex.behaviours;

import java.util.ArrayList;
import java.util.List;

import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.PickIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * A collaborative task is a subclass of Task with a initiator subject and a
 * responder subject.
 * 
 * @author jyuen
 */
public class CollaborativeTask extends Task {

	private String initiatorName;
	private String responderName;

	private StoryComponentContainer initiatorContainer;
	private StoryComponentContainer responderContainer;

	public CollaborativeTask(LibraryModel library, int id) {
		this(library, id, "", "");
	}

	/**
	 * Constructor to create a CollaborativeTask.
	 * 
	 * @param initiatorName
	 * @param responderName
	 */
	public CollaborativeTask(LibraryModel library, int id,
			String initiatorName, String responderName) {
		super(library, id, initiatorName + ":" + responderName);

		final List<Class<? extends StoryComponent>> taskContainerTypes;

		this.initiatorName = initiatorName;
		this.responderName = responderName;

		// Register the initiator and responder task containers.
		this.registerChildType(StoryComponentContainer.class, 2);

		taskContainerTypes = new ArrayList<Class<? extends StoryComponent>>();

		// Define the valid types for the two sub-containers
		taskContainerTypes.add(ScriptIt.class);
		taskContainerTypes.add(KnowIt.class);
		taskContainerTypes.add(Note.class);
		taskContainerTypes.add(ControlIt.class);
		taskContainerTypes.add(PickIt.class);
		taskContainerTypes.add(AskIt.class);

		initiatorContainer = new StoryComponentContainer(taskContainerTypes);
		initiatorContainer.setDisplayText("Initiator:");
		responderContainer = new StoryComponentContainer(taskContainerTypes);
		responderContainer.setDisplayText("Responder:");

		this.addStoryChild(initiatorContainer);
		this.addStoryChild(responderContainer);
	}

	/**
	 * @return the initiatorContainer
	 */
	public StoryComponentContainer getInitiatorContainer() {
		return initiatorContainer;
	}

	/**
	 * @param initiatorContainer
	 *            the initiatorContainer to set
	 */
	public void setInitiatorContainer(StoryComponentContainer initiatorContainer) {
		this.initiatorContainer = initiatorContainer;
	}

	/**
	 * @return the responderContainer
	 */
	public StoryComponentContainer getResponderContainer() {
		return responderContainer;
	}

	/**
	 * @param responderContainer
	 *            the responderContainer to set
	 */
	public void setResponderContainer(StoryComponentContainer responderContainer) {
		this.responderContainer = responderContainer;
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
	public CollaborativeTask clone() {
		final CollaborativeTask component = (CollaborativeTask) super.clone();

		component.initiatorName = this.initiatorName;
		component.responderName = this.responderName;

		return component;
	}

	@Override
	public boolean addStoryChildBefore(StoryComponent newChild,
			StoryComponent sibling) {
		boolean success = super.addStoryChildBefore(newChild, sibling);
		if (success) {
			if (this.getChildren().iterator().next() == newChild)
				this.setInitiatorContainer((StoryComponentContainer) newChild);
			else
				this.setResponderContainer((StoryComponentContainer) newChild);
		}
		return success;
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

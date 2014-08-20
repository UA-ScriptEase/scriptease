package scriptease.model.complex.behaviours;

import java.util.ArrayList;
import java.util.List;

import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.complex.ActivityIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.PickIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.util.ListOp;

/**
 * A collaborative task is a subclass of Task with a initiator subject and a
 * responder subject.
 * 
 * @author jyuen
 */
public class CollaborativeTask extends Task {

	private String initiatorName;
	private String responderName;

	public CollaborativeTask(LibraryModel library) {
		this(library, "", "");
	}

	/**
	 * Constructor to create a CollaborativeTask.
	 * 
	 * @param initiatorName
	 * @param responderName
	 */
	public CollaborativeTask(LibraryModel library, String initiatorName,
			String responderName) {
		super(library, initiatorName + ":" + responderName);

		final List<Class<? extends StoryComponent>> taskContainerTypes;
		final StoryComponentContainer initiatorContainer;
		final StoryComponentContainer responderContainer;

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
		taskContainerTypes.add(ActivityIt.class);

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
		return (StoryComponentContainer) ListOp.head(this.getChildren());
	}

	/**
	 * @return the responderContainer
	 */
	public StoryComponentContainer getResponderContainer() {
		return (StoryComponentContainer) ListOp.last(this.getChildren());
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
	public void process(StoryVisitor visitor) {
		visitor.processCollaborativeTask(this);
	}
}

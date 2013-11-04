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

/**
 * A independent task is a subclass of Task with only one subject.
 * 
 * @author jyuen
 * 
 */
public class IndependentTask extends Task {

	private StoryComponentContainer initiatorContainer;

	/**
	 * Constructor. Creates a new independent task with the given name
	 * 
	 * @param name
	 */
	public IndependentTask(String name) {
		super(name);

		final List<Class<? extends StoryComponent>> taskContainerTypes;

		// Register the initiator and responder task containers.
		this.registerChildType(StoryComponentContainer.class, 1);

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

		this.addStoryChild(initiatorContainer);
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

	@Override
	public boolean addStoryChildBefore(StoryComponent newChild,
			StoryComponent sibling) {
		boolean success = super.addStoryChildBefore(newChild, sibling);

		if (success) {
			if (this.getChildren().iterator().next() == newChild)
				this.setInitiatorContainer((StoryComponentContainer) newChild);
		}
		
		return success;
	}

	@Override
	public void process(StoryVisitor visitor) {
		visitor.processIndependentTask(this);
	}

	@Override
	public void revalidateKnowItBindings() {
		for (StoryComponent child : this.getChildren()) {
			child.revalidateKnowItBindings();
		}
	}

	@Override
	public IndependentTask clone() {
		final IndependentTask component = (IndependentTask) super.clone();

		return component;
	}
}

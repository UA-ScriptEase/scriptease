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
 * A independent task is a subclass of Task with only one subject.
 * 
 * @author jyuen
 * 
 */
public class IndependentTask extends Task {

	public IndependentTask(LibraryModel library) {
		this(library, "");
	}

	/**
	 * Constructor. Creates a new independent task with the given name
	 * 
	 * @param name
	 */
	public IndependentTask(LibraryModel library, String name) {
		super(library, name);

		final List<Class<? extends StoryComponent>> taskContainerTypes;
		final StoryComponentContainer initiatorContainer;

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
		taskContainerTypes.add(ActivityIt.class);

		initiatorContainer = new StoryComponentContainer("Initiator:",
				taskContainerTypes);

		this.addStoryChild(initiatorContainer);
	}

	/**
	 * @return the initiatorContainer
	 */
	public StoryComponentContainer getInitiatorContainer() {
		return (StoryComponentContainer) ListOp.head(this.childComponents);
	}

	@Override
	public void process(StoryVisitor visitor) {
		visitor.processIndependentTask(this);
	}
}

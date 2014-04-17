package scriptease.model.complex;

import java.util.ArrayList;
import java.util.List;

import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * A CauseIt represents a StoryComponent. It acts as a trigger and a cause /
 * pre-req. to effects.
 * 
 * It contains codeBlocks, which are able to have parameters, implicits,
 * subjects and slots and return types. It also has 3 blocks - Active, Inactive,
 * and Always corresponding to the appropriate state of execution.
 * 
 * @author mfchurch
 * @author kschenk
 * @author jyuen
 */
public class CauseIt extends ScriptIt {
	public CauseIt(LibraryModel library, int id, String name) {
		super(library, id, name);
		final List<Class<? extends StoryComponent>> validTypes = new ArrayList<Class<? extends StoryComponent>>();

		validTypes.add(ScriptIt.class);
		validTypes.add(KnowIt.class);
		validTypes.add(AskIt.class);
		validTypes.add(StoryComponentContainer.class);
		validTypes.add(Note.class);
		validTypes.add(ControlIt.class);
		validTypes.add(Behaviour.class);
		validTypes.add(PickIt.class);
		validTypes.add(ActivityIt.class);

		this.registerChildTypes(validTypes, MAX_NUM_OF_ONE_TYPE);
	}

	@Override
	public CauseIt clone() {
		return (CauseIt) super.clone();
	}

	@Override
	public void process(StoryVisitor processController) {
		processController.processCauseIt(this);
	}

	@Override
	public String toString() {
		return "CauseIt [" + this.getDisplayText() + "]";
	}
}

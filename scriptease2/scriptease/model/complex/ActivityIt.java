package scriptease.model.complex;

import scriptease.controller.StoryVisitor;
import scriptease.model.atomic.KnowIt;

/**
 * Represents a container of effects, descriptions, questions, and controls.
 * 
 * @author jyuen
 */
public class ActivityIt extends ScriptIt {

	/**
	 * Constructor. Creates a new ActivityIt with the given name
	 * 
	 * @param name
	 */
	public ActivityIt(String name) {
		super(name);

		this.registerChildType(ScriptIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.registerChildType(KnowIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.registerChildType(ControlIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.registerChildType(AskIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.registerChildType(PickIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
	}

	@Override
	public void process(StoryVisitor processController) {
		processController.processActivityIt(this);
	}
}

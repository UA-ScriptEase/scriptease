package scriptease.model.complex;

import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;

/**
 * Represents a container of effects, descriptions, and controls.
 * 
 * @author jyuen
 */
public class FunctionIt extends ScriptIt {

	/**
	 * Constructor. Creates a new FunctionIt with the given name
	 * 
	 * @param name
	 */
	public FunctionIt(String name) {
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
	public void process(StoryVisitor visitor) {
		visitor.processFunctionIt(this);
	}

	@Override
	public void revalidateKnowItBindings() {
		for (StoryComponent child : this.getChildren()) {
			child.revalidateKnowItBindings();
		}
	}
}

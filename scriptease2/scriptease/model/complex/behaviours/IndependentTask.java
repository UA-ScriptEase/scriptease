package scriptease.model.complex.behaviours;

import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.PickIt;
import scriptease.model.complex.ScriptIt;

/**
 * A independent task is a subclass of Task with only one subject.
 * 
 * @author jyuen
 * 
 */
public class IndependentTask extends Task {

	/**
	 * Constructor. Creates a new independent task with the given name
	 * 
	 * @param name
	 */
	public IndependentTask(String name) {
		super(name);

		this.registerChildType(KnowIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.registerChildType(AskIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.registerChildType(ScriptIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.registerChildType(ControlIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		this.registerChildType(PickIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
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

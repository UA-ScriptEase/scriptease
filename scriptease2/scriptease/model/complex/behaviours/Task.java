package scriptease.model.complex.behaviours;

import scriptease.controller.StoryVisitor;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;

/**
 * A task is a series of effects. Each task has a probability of execution
 * depending on it's sibling tasks.
 * 
 * @author jyuen
 * 
 */
public class Task extends ComplexStoryComponent {

	public Task(String name) {
		super(name);

		this.registerChildType(ScriptIt.class,
				ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
	}

	@Override
	public void process(StoryVisitor visitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void revalidateKnowItBindings() {
		// TODO Auto-generated method stub

	}

}

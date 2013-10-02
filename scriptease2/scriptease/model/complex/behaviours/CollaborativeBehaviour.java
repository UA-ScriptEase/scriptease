package scriptease.model.complex.behaviours;

import scriptease.controller.StoryVisitor;
import scriptease.model.complex.ComplexStoryComponent;

/**
 * Collaborative behaviours are a extension of Behaviours {@link Behaviours}.
 * They represent behaviours that allow the subject to interact with one or more
 * subjects. Aside from Proactive Behaviours, they allow for Reactive Behaviours
 * (responses to the proactive behaviours).
 * 
 * @author jyuen
 */
public class CollaborativeBehaviour extends Behaviour {

	public CollaborativeBehaviour(String name) {
		super(name);

		this.registerChildType(ReactiveTaskTree.class,
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
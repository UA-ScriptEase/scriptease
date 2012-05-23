package scriptease.model.complex;

import java.util.Collection;

import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;

/**
 * This represents a sequence of any type of StoryComponent. In general, a
 * StoryItemSequence should have no real effect on Code Generation, and should
 * just be a logical grouping of components in the tree.
 * 
 * @author friesen
 */
public class StoryItemSequence extends ComplexStoryComponent {

	/************* CONSTRUCTORS ********************/

	public StoryItemSequence(
			Collection<Class<? extends StoryComponent>> validTypes) {
		for (Class<? extends StoryComponent> validType : validTypes) {
			registerChildType(validType,
					ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		}
	}

	/************* IMPORTANT CODE ******************/

	@Override
	public void process(StoryVisitor processController) {
		processController.processStoryItemSequence(this);
	}

	@Override
	public String toString() {
		return "StoryItemSequence [" + this.getDisplayText() + "]";
	}
}
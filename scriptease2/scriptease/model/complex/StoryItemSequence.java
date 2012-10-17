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

	public StoryItemSequence(
			Collection<Class<? extends StoryComponent>> validTypes) {
		for (Class<? extends StoryComponent> validType : validTypes) {
			registerChildType(validType,
					ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		}
	}

	@Override
	public void process(StoryVisitor processController) {
		processController.processStoryItemSequence(this);
	}

	/**
	 * Special check so that we do not add a cause to a StoryItemSequence. If we
	 * ever want to add Causes to StoryItemSequences in the future, we could
	 * just add a boolean flag for this check that gets added in the
	 * constructor.
	 */
	@Override
	public boolean canAcceptChild(StoryComponent potentialChild) {
		if ((potentialChild instanceof ScriptIt && ((ScriptIt) potentialChild)
				.isCause()))
			// Causes should not have other Causes inside of them, nor should
			// Effects allow children. We don't even know what would mean. To
			// think of it is terrifying. - remiller
			return false;
		else
			return super.canAcceptChild(potentialChild);
	}

	@Override
	public String toString() {
		return "StoryItemSequence [" + this.getDisplayText() + "]";
	}

	@Override
	public void revalidateKnowItBindings() {
		for (StoryComponent child : this.getChildren()) {
			child.revalidateKnowItBindings();
		}
	}
}
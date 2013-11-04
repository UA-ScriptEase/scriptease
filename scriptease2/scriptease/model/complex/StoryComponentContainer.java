package scriptease.model.complex;

import java.util.Collection;

import scriptease.controller.StoryVisitor;
import scriptease.model.StoryComponent;
import scriptease.model.complex.behaviours.Task;

/**
 * This class is a structural asset that simply allows for the model to group
 * whole Patterns together. <br>
 * <br>
 * In a tree view, it would be represented like a folder. It doesn't really have
 * many properties for itself, since its primary purpose is to group its
 * children together. If it helps, consider this type to be less of a direct
 * StoryComponent, and more of a meta-StoryComponent: it is the information that
 * its children belong together.<br>
 * <br>
 * Things like the top-level, overall story of the module would be a
 * StoryComponentContainer. <br>
 * <br>
 * If there are any major new ScriptEase pattern types, ie. on the level of
 * behaviours, encounters, stories, etc, they should be registered here.
 * 
 * @author remiller
 * @author mfchurch
 * @author jyuen
 * 
 */
public class StoryComponentContainer extends ComplexStoryComponent {
	public StoryComponentContainer() {
		this("");
	}

	public StoryComponentContainer(String displayName) {
		this.setDisplayText(displayName);
	}

	public StoryComponentContainer(
			Collection<Class<? extends StoryComponent>> validTypes) {
		this("", validTypes);
	}

	public StoryComponentContainer(String displayName,
			Collection<Class<? extends StoryComponent>> validTypes) {
		this(displayName);

		for (Class<? extends StoryComponent> validType : validTypes) {
			registerChildType(validType,
					ComplexStoryComponent.MAX_NUM_OF_ONE_TYPE);
		}
	}

	@Override
	public boolean canAcceptChild(StoryComponent potentialChild) {
		if (!(this.getOwner() instanceof Task))
			return super.canAcceptChild(potentialChild);

		// Special case for containers of Tasks. Task containers can only accept
		// TODO effects
		if (potentialChild instanceof ScriptIt
				&& !(potentialChild instanceof ControlIt))
			return super.canAcceptChild(potentialChild)
					&& potentialChild.getLabels().contains("TODO");

		return super.canAcceptChild(potentialChild);
	}

	@Override
	public StoryComponentContainer clone() {
		return (StoryComponentContainer) super.clone();
	}

	@Override
	public void process(StoryVisitor visitor) {
		visitor.processStoryComponentContainer(this);
	}

	@Override
	public String toString() {
		return "StoryComponentContainer (\"" + this.getDisplayText() + "\")";
	}

	@Override
	public void revalidateKnowItBindings() {
		for (StoryComponent child : this.getChildren()) {
			child.revalidateKnowItBindings();
		}
	}
}

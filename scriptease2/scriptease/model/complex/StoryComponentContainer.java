package scriptease.model.complex;

import scriptease.controller.StoryVisitor;

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
 * behaviours, encounters, quests, etc, they should be registered here.
 * 
 * @author remiller
 * @author mfchurch
 * 
 */
public class StoryComponentContainer extends ComplexStoryComponent {
	public StoryComponentContainer() {
		this("");
	}

	public StoryComponentContainer(String displayName) {
		this.setDisplayText(displayName);
	}

	@Override
	public void process(StoryVisitor visitor) {
		visitor.processStoryComponentContainer(this);
	}

	@Override
	public String toString() {
		return "StoryComponentContainer (\"" + this.getDisplayText() + "\")";
	}
}

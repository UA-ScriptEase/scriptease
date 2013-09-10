package scriptease.model.atomic.knowitbindings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import scriptease.controller.BindingVisitor;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.StoryGroup;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;

/**
 * This class represents a <b>Story Group</b> binding for a <code>KnowIt</code>.
 * 
 * @author jyuen
 * @see {@link scriptease.model.atomic.KnowIt}
 */
public class KnowItBindingStoryGroup extends KnowItBinding {

	private final StoryGroup storyGroup;

	/**
	 * Creates a new binding that wraps the given story group.
	 * 
	 * @param group
	 *            The story group to wrap.
	 */
	public KnowItBindingStoryGroup(StoryGroup group) {
		this.storyGroup = group;
	}

	/**
	 * Creates a new binding based on the given story group
	 * 
	 * @param other
	 *            The binding to mimic.
	 */
	public KnowItBindingStoryGroup(KnowItBindingStoryGroup other) {
		this.storyGroup = other.storyGroup;
	}

	@Override
	public String getScriptValue() {
		return this.storyGroup.getDisplayText();
	}

	@Override
	public StoryGroup getValue() {
		return this.storyGroup;
	}

	@Override
	public Collection<String> getTypes() {
		List<String> types = new ArrayList<String>();
		types.add(StoryGroup.STORY_GROUP_TYPE);
		return types;
	}

	@Override
	public String toString() {
		return this.getValue().getDisplayText();
	}

	@Override
	public boolean equals(Object other) {
		return (other instanceof KnowItBindingStoryGroup)
				&& ((KnowItBindingStoryGroup) other).storyGroup
						.equals(this.storyGroup);
	}

	@Override
	public KnowItBinding resolveBinding() {
		return this;
	}

	@Override
	public KnowItBinding clone() {
		final KnowItBindingStoryGroup clone = new KnowItBindingStoryGroup(this);

		return clone;
	}

	@Override
	public void process(BindingVisitor processController) {
		processController.processStoryGroup(this);
	}

	@Override
	public boolean compatibleWith(KnowIt knowIt) {
		if (typeMatches(knowIt.getAcceptableTypes())) {
			if (knowIt.getOwner() != null) {
				final SEModel model;

				model = SEModelManager.getInstance().getActiveModel();
				if (model instanceof StoryModel) {
					return ((StoryModel) model).getRoot().getDescendants()
							.contains(this.getValue());
				}
			} else
				return true;
		}
		return false;
	}
}

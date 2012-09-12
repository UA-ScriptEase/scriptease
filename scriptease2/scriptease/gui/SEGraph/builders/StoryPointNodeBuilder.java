package scriptease.gui.SEGraph.builders;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.model.complex.StoryPoint;

/**
 * Builds a new StoryPoint in the SEGraph and contains methods to get the
 * parents and children.
 * 
 * @author kschenk
 * 
 */
public class StoryPointNodeBuilder implements SEGraphNodeBuilder<StoryPoint> {
	private final StoryPoint start;

	public StoryPointNodeBuilder(StoryPoint start) {
		super();
		this.start = start;
	}

	@Override
	public StoryPoint buildNewNode() {
		return new StoryPoint("");
	}

	@Override
	public Collection<StoryPoint> getChildren(StoryPoint node) {
		return node.getSuccessors();
	}

	@Override
	public Collection<StoryPoint> getParents(StoryPoint node) {
		final Set<StoryPoint> parents;

		parents = new HashSet<StoryPoint>();

		for (StoryPoint descendant : this.start.getDescendants()) {
			for (StoryPoint successor : descendant.getSuccessors())
				if (successor == node) {
					parents.add(descendant);
				}
		}

		return parents;
	}
}

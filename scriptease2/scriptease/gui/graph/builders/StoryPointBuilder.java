package scriptease.gui.graph.builders;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import scriptease.gui.quests.StoryPoint;

/**
 * Builds a new StoryPoint in the SEGraph.
 * 
 * @author kschenk
 * 
 */
public class StoryPointBuilder extends SEGraphNodeBuilder<StoryPoint> {
	private final StoryPoint start;

	public StoryPointBuilder(StoryPoint start) {
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

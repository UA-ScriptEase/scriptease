package scriptease.gui.graph.builders;

import scriptease.gui.quests.StoryPoint;

/**
 * Builds a new StoryPoint in the SEGraph.
 * 
 * @author kschenk
 * 
 */
public class StoryPointBuilder extends SEGraphNodeBuilder<StoryPoint> {
	@Override
	public StoryPoint buildNewNode() {
		return new StoryPoint("");
	}
}

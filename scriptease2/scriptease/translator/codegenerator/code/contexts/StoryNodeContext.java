package scriptease.translator.codegenerator.code.contexts;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.model.complex.StoryNode;

/**
 * Context representing a StoryNode
 * 
 * @author jyuen
 *
 */
public class StoryNodeContext extends ComplexStoryComponentContext {

	/**
	 * Creates a new StoryNode context
	 * 
	 * @param other
	 * @param source
	 */
	public StoryNodeContext(Context other, StoryNode source) {
		super(other, source);
	}
	
	@Override
	public Collection<StoryNode> getStoryPointParents() {
		final Collection<StoryNode> parents;

		parents = new ArrayList<StoryNode>();

		for (StoryNode node : this.getStoryNodes()) {
			if (node.getSuccessors().contains(this.getComponent())) {
				parents.add(node);
			}
		}

		return parents;
	}
	
	@Override
	public String getUniqueID() {
		return this.getComponent().getUniqueID().toString();
	}
	
	@Override
	public Collection<StoryNode> getStoryPointChildren() {
		return this.getComponent().getSuccessors();
	}
	
	@Override
	public StoryNode getComponent() {
		return (StoryNode) super.getComponent();
	}
	
	@Override
	public String getName() {
		return this.getNameOf(this.getComponent());
	}
	
	@Override
	public String getValue() {
		return this.getName();
	}
}

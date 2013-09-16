package scriptease.translator.codegenerator.code.contexts;

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
	public String getUniqueID() {
		return this.getComponent().getUniqueID().toString();
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

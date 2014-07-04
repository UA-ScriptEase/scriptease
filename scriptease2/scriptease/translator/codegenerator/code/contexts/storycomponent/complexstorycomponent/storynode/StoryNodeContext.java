package scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.storynode;

import scriptease.model.complex.StoryNode;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.ComplexStoryComponentContext;

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

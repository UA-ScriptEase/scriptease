package scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.StoryComponentUtils;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.translator.codegenerator.code.contexts.Context;
import scriptease.translator.codegenerator.code.contexts.storycomponent.StoryComponentContext;

/**
 * Context representing a ComplexStoryComponent
 * 
 * @author mfchurch
 * 
 */
public class ComplexStoryComponentContext extends StoryComponentContext {

	/**
	 * Creates a new ComplexStoryComponentContext.
	 * 
	 * @param other
	 * @param source
	 */
	public ComplexStoryComponentContext(Context other,
			ComplexStoryComponent source) {
		super(other, source);
	}

	/**
	 * Get all of the ComplexStoryComponent's children
	 */
	@Override
	public final Collection<StoryComponent> getChildren() {
		return this.getComponent().getChildren();
	}

	/**
	 * Get all of the ComplexStoryComponent's knowIt children
	 */
	@Override
	public Collection<KnowIt> getVariables() {
		final Collection<KnowIt> variables = new ArrayList<KnowIt>();

		for (StoryComponent child : this.getChildren()) {
			variables.addAll(StoryComponentUtils.getVariables(child));
		}

		return variables;
	}

	@Override
	public ComplexStoryComponent getComponent() {
		return (ComplexStoryComponent) super.getComponent();
	}
	
	@Override
	public CodeBlock getMainCodeBlock() {
		// TODO Auto-generated method stub
		return super.getMainCodeBlock();
	}
}

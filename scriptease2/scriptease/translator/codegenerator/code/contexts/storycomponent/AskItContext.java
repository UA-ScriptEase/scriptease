package scriptease.translator.codegenerator.code.contexts.storycomponent;

import java.util.Collection;

import scriptease.controller.StoryComponentUtils;
import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.translator.codegenerator.code.contexts.Context;

/**
 * AskItContext is Context for a AskIt object.
 * 
 * @see Context
 * @author mfchurch
 * 
 */
public class AskItContext extends StoryComponentContext {

	/**
	 * Creates a new AskItContext with the source AskIt based on the context
	 * passed in.
	 * 
	 * @param other
	 * @param source
	 */
	public AskItContext(Context other, AskIt source) {
		super(other, source);
	}

	/**
	 * Get the AskIt's Condition
	 */
	@Override
	public String getCondition() {
		final KnowIt condition = this.getComponent().getCondition();
		if (condition != null)
			return this.getNameOf(condition);
		return "";
	}

	/**
	 * Get the AskIt's IfChild
	 */
	@Override
	public StoryComponentContainer getIfChild() {
		return this.getComponent().getIfBlock();
	}

	/**
	 * Get the AskIt's ElseChild
	 */
	@Override
	public StoryComponentContainer getElseChild() {
		return this.getComponent().getElseBlock();
	}

	/**
	 * Get the AskIt's condition and it's dependencies
	 */
	@Override
	public Collection<KnowIt> getVariables() {
		return StoryComponentUtils.getVariables(this.getComponent());
	}

	@Override
	public AskIt getComponent() {
		return (AskIt) super.getComponent();
	}
}

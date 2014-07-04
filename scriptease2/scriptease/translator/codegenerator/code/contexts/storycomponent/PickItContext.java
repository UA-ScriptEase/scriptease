package scriptease.translator.codegenerator.code.contexts.storycomponent;

import java.util.Collection;

import scriptease.model.StoryComponent;
import scriptease.model.complex.PickIt;
import scriptease.translator.codegenerator.code.contexts.Context;

/**
 * PickItContext is Context for a PickIt {@link PickIt} object.
 * 
 * @see Context
 * @author jyuen
 * 
 */
public class PickItContext extends StoryComponentContext {

	final PickIt pickIt;

	/**
	 * Creates a new PickItContext with the source PickIt based on the context
	 * passed in.
	 * 
	 * @param other
	 * @param source
	 */
	public PickItContext(Context other, PickIt source) {
		super(other, source);

		this.pickIt = source;
	}

	@Override
	public PickIt getComponent() {
		return (PickIt) super.getComponent();
	}

	@Override
	public String getTotalChoiceProbability() {
		int totalProbability = 0;
		
		for (Integer choiceProbability : pickIt.getChoices().values()) {
			totalProbability += choiceProbability;
		}
		
		return Integer.toString(totalProbability);
	}

	@Override
	public Collection<StoryComponent> getChoices() {
		return pickIt.getChildren();
	}
}

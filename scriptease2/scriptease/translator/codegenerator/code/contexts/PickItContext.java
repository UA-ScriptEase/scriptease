package scriptease.translator.codegenerator.code.contexts;

import scriptease.model.complex.PickIt;

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
	public String getNumChoices() {
		return Integer.toString(pickIt.getChildCount());
	}
}

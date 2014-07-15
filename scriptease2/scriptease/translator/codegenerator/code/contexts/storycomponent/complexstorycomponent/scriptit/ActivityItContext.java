package scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.scriptit;

import scriptease.model.complex.ActivityIt;
import scriptease.translator.codegenerator.code.contexts.Context;

/**
 * Context for {@link ActivityIt}
 * 
 * @author jyuen
 */
public class ActivityItContext extends ScriptItContext {

	/**
	 * Creates a new FunctionItContext with the source FunctionIt based on the context
	 * passed in.
	 * 
	 * @param other
	 * @param source
	 */
	public ActivityItContext(Context other, ActivityIt source) {
		super(other, source);
	}

	@Override
	public ActivityIt getComponent() {
		return (ActivityIt) super.getComponent();
	}
}

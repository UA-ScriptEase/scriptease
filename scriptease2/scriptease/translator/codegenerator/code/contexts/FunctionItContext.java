package scriptease.translator.codegenerator.code.contexts;

import scriptease.model.complex.FunctionIt;

/**
 * Context for {@link FunctionIt}
 * 
 * @author jyuen
 */
public class FunctionItContext extends ScriptItContext {

	/**
	 * Creates a new FunctionItContext with the source FunctionIt based on the context
	 * passed in.
	 * 
	 * @param other
	 * @param source
	 */
	public FunctionItContext(Context other, FunctionIt source) {
		super(other, source);
	}

	@Override
	public FunctionIt getComponent() {
		return (FunctionIt) super.getComponent();
	}
}

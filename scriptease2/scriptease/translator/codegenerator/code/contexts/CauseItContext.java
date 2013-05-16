package scriptease.translator.codegenerator.code.contexts;

import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
/**
 * Context for {@link CauseIt}s. This overrides some methods of
 * {@link ScriptItContext} that act differently for {@link CauseIt}s.
 * 
 * @author jyuen
 * 
 */
public class CauseItContext extends ScriptItContext {

	/**
	 * Creates a new {@link CauseItContext} from a previous Context and a
	 * source {@link CauseIt}.
	 * 
	 * @param other
	 * @param source
	 */
	public CauseItContext(Context other, ScriptIt source) {
		super(other, source);
	}

	/**
	 * Returns the active block of a Cause.
	 * 
	 * @see ScriptIt#getActiveBlock()
	 */
	@Override
	public StoryComponentContainer getActiveChild() {
		return this.getComponent().getActiveBlock();
	}

	/**
	 * Returns the inactive block of a Cause.
	 * 
	 * @see ScriptIt#getInactiveBlock()
	 */
	@Override
	public StoryComponentContainer getInactiveChild() {
		return this.getComponent().getInactiveBlock();
	}

	@Override
	public StoryComponentContainer getAlwaysChild() {
		return this.getComponent().getAlwaysBlock();
	}
	
	@Override
	public CauseIt getComponent() {
		return (CauseIt) super.getComponent();
	}

	@Override
	public String toString() {
		return "CauseItContext[" + this.getComponent() + "]";
	}
}

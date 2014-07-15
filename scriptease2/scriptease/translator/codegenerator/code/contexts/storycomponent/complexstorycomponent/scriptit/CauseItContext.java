package scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.scriptit;

import scriptease.model.atomic.KnowIt;
import scriptease.model.complex.CauseIt;
import scriptease.translator.codegenerator.code.contexts.Context;

/**
 * CauseItContexts are contexts for CauseIts.
 * 
 * @author jyuen
 */
public class CauseItContext extends ScriptItContext {

	public CauseItContext(Context other, CauseIt source) {
		super(other, source);
	}
	
	@Override
	public CauseIt getComponent() {
		return (CauseIt) super.getComponent();
	}
	
	@Override
	public KnowIt getSubject() {
		return this.getComponent().getMainCodeBlock().getSubject();
	}
}

package scriptease.controller.observer;

import scriptease.model.complex.ScriptIt;

/**
 * Observer for EffectHolderPanels that fires when an effect is set.
 * 
 * @author kschenk
 * 
 */
public interface SetEffectObserver {
	public void effectChanged(ScriptIt newEffect);
}

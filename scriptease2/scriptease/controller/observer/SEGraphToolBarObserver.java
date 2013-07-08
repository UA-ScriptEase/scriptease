package scriptease.controller.observer;

import scriptease.gui.SEGraph.SEGraphToolBar.Mode;

/**
 * Alerts changes to the mode.
 * 
 * @author kschenk
 * 
 */
public interface SEGraphToolBarObserver {
	/**
	 * Called when the mode changes.
	 * 
	 * @param mode
	 *            the new mode
	 */
	public void modeChanged(Mode mode);
}

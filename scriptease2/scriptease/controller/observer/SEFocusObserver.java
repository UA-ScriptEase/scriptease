package scriptease.controller.observer;

import java.awt.Component;

import scriptease.gui.SEFocusManager;

/**
 * This observer has its events fired when we set focus in
 * {@link SEFocusManager}. We use this primarily for deletion and cut, copy,
 * paste of StoryComponentPanels, the Library Panel, and the SEGraph since
 * regular focus tends to act bizarrely.
 * 
 * @author kschenk
 * 
 */
public interface SEFocusObserver {

	/**
	 * Fired when the current component gains focus.  We pass in the old focus in case we need to 
	 * use it, because the new focus can always be calculated via SEFocusManager.
	 * 
	 * @param oldFocus
	 *            The previously focused on component
	 */
	public void gainFocus(Component oldFocus);

	/**
	 * Fired when the current component loses focus. Focus gets set before this
	 * is called, so you can get the newly focused component with a call to
	 * {@link SEFocusManager#getFocus()} within the method body.
	 * 
	 * @param oldFocus
	 */
	public void loseFocus(Component oldFocus);
}

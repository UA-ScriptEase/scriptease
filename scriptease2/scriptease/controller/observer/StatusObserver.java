package scriptease.controller.observer;

/**
 * Allows implementers to be notified of changes to the ScriptEase status
 * 
 * @author kschenk
 */
public interface StatusObserver {
	/**
	 * Tells the <code>StatusObserver</code> that there has been a change to the
	 * status text.
	 * 
	 * @param newText
	 *            The text that the status has been changed to
	 */
	public void statusChanged(String newText);
}

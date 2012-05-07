package scriptease.controller.observer;


/**
 * Represents the observer side of the Observer pattern for StoryComponents.<br>
 * <br>
 * Classes that implement this interface update themselves when
 * {@link #componentChanged(StoryComponentEvent)} is called.
 * 
 * @author remiller
 */
public interface StoryComponentObserver {
	/**
	 * Tells the <code>StoryComponentObserver</code> that there has been a
	 * change made to a StoryComponent that it is observing. This can include
	 * changing a property or adding a parameter.
	 * 
	 * @param event
	 *            The event that encapsulates the information about the change.
	 */
	public void componentChanged(StoryComponentEvent event);
}

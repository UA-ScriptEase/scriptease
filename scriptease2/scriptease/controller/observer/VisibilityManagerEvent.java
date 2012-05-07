package scriptease.controller.observer;

import scriptease.controller.VisibilityManager;
import scriptease.model.StoryComponent;

/**
 * Encapsulates the possible changes in a {@link VisibilityManager}. An instance
 * of <code>VisibilityManagerEvent</code> will be passed to objects who are
 * interested in such changes, who can then query the event for more specific
 * information as to what has changed.
 * 
 * @author mfchurch
 * 
 */
public final class VisibilityManagerEvent {
	private final StoryComponent source;
	private final short eventType;

	public static final short VISIBILITY_ADDED = 0;
	public static final short VISIBILITY_REMOVED = 1;

	/**
	 * Builds a <code>VisibilityManagerEvent</code> which records the way in
	 * which the VisibilityManager has changed.
	 * 
	 * @param changed
	 *            The StoryComponent's visibility added, or removed from the
	 *            observed VisibilityManager.
	 * @param type
	 *            The type constant that represents how the VisibilityManager
	 *            changed.
	 */
	public VisibilityManagerEvent(StoryComponent changed, short type) {
		this.source = changed;
		this.eventType = type;
	}

	public StoryComponent getSource() {
		return this.source;
	}

	public short getEventType() {
		return this.eventType;
	}
}

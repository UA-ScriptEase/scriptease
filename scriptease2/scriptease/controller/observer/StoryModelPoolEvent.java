package scriptease.controller.observer;

import scriptease.model.StoryModel;
import scriptease.model.StoryModelPool;

/**
 * Encapsulates the possible changes in a {@link StoryModelPool}. An instance of
 * <code>StoryModelPoolEvent</code> will be passed to objects who are interested
 * in such changes, who can then query the event for more specific information
 * as to what has changed.
 * 
 * @author remiller
 * 
 */
public final class StoryModelPoolEvent {
	private final StoryModel source;
	private final short eventType;

	public static final short STORY_MODEL_ADDED = 0;
	public static final short STORY_MODEL_REMOVED = 1;
	public static final short STORY_MODEL_ACTIVATED = 2;

	/**
	 * Builds a <code>StoryModelPoolEvent</code> which records the way in which
	 * the StoryModelPool has changed.
	 * 
	 * @param changed
	 *            The StoryModel that affected (added, removed, activated, etc)
	 *            the state of the pool.
	 * @param type
	 *            The type constant that represents how the StoryModelPool
	 *            changed. This should be one of <code>STORY_MODEL_*</code>
	 */
	public StoryModelPoolEvent(StoryModel changed, short type) {
		this.source = changed;
		this.eventType = type;
	}

	public StoryModel getStoryModel() {
		return this.source;
	}

	public short getEventType() {
		return this.eventType;
	}
}

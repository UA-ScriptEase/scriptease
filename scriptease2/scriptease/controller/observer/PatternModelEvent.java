package scriptease.controller.observer;

import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;

/**
 * Encapsulates the possible changes in a {@link PatternModelManager}. An instance of
 * <code>PatternModelPoolEvent</code> will be passed to objects who are interested
 * in such changes, who can then query the event for more specific information
 * as to what has changed.
 * 
 * @author remiller
 * @author kschenk
 * 
 */
public final class PatternModelEvent {
	private final PatternModel source;
	private final short eventType;

	public static final short PATTERN_MODEL_ADDED = 0;
	public static final short PATTERN_MODEL_REMOVED = 1;
	public static final short PATTERN_MODEL_ACTIVATED = 2;

	/**
	 * Builds a <code>PatternModelPoolEvent</code> which records the way in which
	 * the PatternModelPool has changed.
	 * 
	 * @param changed
	 *            The PatternModel that affected (added, removed, activated, etc)
	 *            the state of the pool.
	 * @param type
	 *            The type constant that represents how the PatternModelPool
	 *            changed. This should be one of <code>PATTERN_MODEL_*</code>
	 */
	public PatternModelEvent(PatternModel changed, short type) {
		this.source = changed;
		this.eventType = type;
	}

	public PatternModel getPatternModel() {
		return this.source;
	}

	public short getEventType() {
		return this.eventType;
	}
}

package scriptease.controller.observer;

import scriptease.model.SEModel;
import scriptease.model.SEModelManager;

/**
 * Encapsulates the possible changes in a {@link SEModelManager}. An instance of
 * <code>PatternModelPoolEvent</code> will be passed to objects who are
 * interested in such changes, who can then query the event for more specific
 * information as to what has changed.
 * 
 * @author remiller
 * @author kschenk
 * 
 */
public final class SEModelEvent {
	private final SEModel source;
	private final Type eventType;

	public static enum Type {
		ACTIVATED, ADDED, REMOVED
	}

	/**
	 * Builds a <code>PatternModelPoolEvent</code> which records the way in
	 * which the PatternModelPool has changed.
	 * 
	 * @param changed
	 *            The PatternModel that affected (added, removed, activated,
	 *            etc) the state of the pool.
	 * @param type
	 *            The type constant that represents how the PatternModelPool
	 *            changed. This should be one of <code>PATTERN_MODEL_*</code>
	 */
	public SEModelEvent(SEModel changed, Type type) {
		this.source = changed;
		this.eventType = type;
	}

	public SEModel getPatternModel() {
		return this.source;
	}

	public Type getEventType() {
		return this.eventType;
	}
}

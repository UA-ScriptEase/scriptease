package scriptease.controller.observer;

import scriptease.model.LibraryModel;

/**
 * Encapsulates the possible changes in a {@link LibraryModel}. An instance of
 * <code>LibraryEvent</code> will be passed to objects who are interested in
 * such changes, who can then query the event for more specific information as
 * to what has changed.
 * 
 * @author mfchurch
 * 
 */
public final class LibraryEvent {
	private final StoryComponentEvent event;
	private final LibraryModel source;
	private final short eventType;

	public static final short STORYCOMPONENT_ADDED = 0;
	public static final short STORYCOMPONENT_REMOVED = 1;
	public static final short STORYCOMPONENT_CHANGED = 2;

	/**
	 * Builds a <code>LibraryEvent</code> which records the way in which the
	 * LibraryModel has changed.
	 * 
	 * @param changed
	 *            The StoryComponent added, or removed from the observed
	 *            LibaryModel. This may be null if the event is LIBRARY_REMOVED
	 * @param type
	 *            The type constant that represents how the LibraryModel
	 *            changed.
	 */
	public LibraryEvent(LibraryModel source, short type,
			StoryComponentEvent event) {
		this.source = source;
		this.eventType = type;
		this.event = event;
	}

	public LibraryModel getSource() {
		return this.source;
	}

	public short getEventType() {
		return this.eventType;
	}

	public StoryComponentEvent getEvent() {
		return this.event;
	}
}

package scriptease.controller.observer.library;

import scriptease.model.StoryComponent;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * Encapsulates the possible changes in a {@link LibraryModel}. An instance of
 * <code>LibraryEvent</code> will be passed to observers who can then query the
 * event for more specific information.
 * 
 * @author mfchurch
 * @author kschenk
 * 
 */
public final class LibraryEvent {
	private final StoryComponent source;

	public static enum Type {
		ADDITION, REMOVAL, CHANGE
	}

	private final Type eventType;

	/**
	 * Builds a <code>LibraryEvent</code> which records the way in which the
	 * LibraryModel has changed.
	 * 
	 * @param source
	 *            The StoryComponent added, or removed from the observed
	 *            LibaryModel. This may be null if the event is LIBRARY_REMOVED
	 * @param type
	 *            The type constant that represents how the LibraryModel
	 *            changed.
	 */
	public LibraryEvent(StoryComponent source, Type type) {
		this.source = source;
		this.eventType = type;
	}

	public Type getEventType() {
		return this.eventType;
	}

	public StoryComponent getSource() {
		return this.source;
	}
}

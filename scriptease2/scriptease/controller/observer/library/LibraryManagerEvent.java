package scriptease.controller.observer.library;

import scriptease.model.LibraryManager;
import scriptease.model.LibraryModel;

/**
 * Encapsulates the possible changes in a {@link LibraryManager}. An instance of
 * <code>LibraryManagerEvent</code> will be passed to objects who are interested
 * in such changes, who can then query the event for more specific information
 * as to what has changed.
 * 
 * @author mfchurch
 * 
 */
public final class LibraryManagerEvent {
	private final LibraryEvent event;
	private final LibraryModel source;
	private final short eventType;

	public static final short LIBRARYMODEL_ADDED = 0;
	public static final short LIBRARYMODEL_REMOVED = 1;
	public static final short LIBRARYMODEL_CHANGED = 2;

	/**
	 * Builds a <code>LibraryManagerEvent</code> which records the way in which
	 * the LibraryManager has changed.
	 * 
	 * @param changed
	 *            The LibraryModel added, or removed from the observed
	 *            LibraryManager.
	 * @param type
	 *            The type constant that represents how the LibraryManager
	 *            changed.
	 */
	public LibraryManagerEvent(LibraryModel changed, short type,
			LibraryEvent event) {
		this.source = changed;
		this.eventType = type;
		this.event = event;
	}

	public LibraryModel getSource() {
		return this.source;
	}

	public short getEventType() {
		return this.eventType;
	}

	public LibraryEvent getEvent() {
		return this.event;
	}
}

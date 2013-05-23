package scriptease.translator.apimanagers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import scriptease.model.atomic.KnowIt;
import scriptease.translator.io.model.Slot;

/**
 * Stores all of the slots used in a translator. Also keeps a record of the
 * default format keyword, which is the keyword used when none is specified for
 * a slot.
 * 
 * @author ?
 * @author kschenk
 */
public class EventSlotManager {

	public static final String DEFAULT_SLOT_TEXT = "";
	private final Map<String, Slot> slots;
	private String defaultFormatKeyword;

	/**
	 * Creates a new {@link #EventSlotManager()}. These should have a one to one
	 * relationship with Libraries.
	 */
	public EventSlotManager() {
		this.slots = new HashMap<String, Slot>();
	}

	/**
	 * Adds an event slot to the event slot manager, using the slot's keyword.
	 * 
	 * @param slot
	 */
	public void addEventSlot(Slot slot) {
		this.slots.put(slot.getKeyword(), slot);
	}

	/**
	 * Sets the default format keyword.
	 * 
	 * @param defaultFormatKeyword
	 */
	public void setDefaultFormatKeyword(String defaultFormatKeyword) {
		this.defaultFormatKeyword = defaultFormatKeyword;
	}

	/**
	 * Gets the default format for all of the slots.
	 * 
	 * @return
	 */
	public String getDefaultFormatKeyword() {
		return this.defaultFormatKeyword;
	}

	/**
	 * Returns a collection of implicit KnowIts from the original set for the
	 * slot.
	 * 
	 * @param keyword
	 *            the slot keyword to search for.
	 * @return
	 */
	public Collection<KnowIt> getImplicits(String keyword) {
		final Collection<KnowIt> implicits = new ArrayList<KnowIt>();
		final Slot slot = this.slots.get(keyword);

		if (slot != null) {
			implicits.addAll(slot.getImplicits());
		}

		return implicits;
	}

	/**
	 * Returns all parameters of the slot.
	 * 
	 * @param keyword
	 * @return
	 */
	public Collection<KnowIt> getParameters(String keyword) {
		final Slot slot = this.slots.get(keyword);
		if (slot != null) {
			return slot.getParameters();
		} else
			return new ArrayList<KnowIt>();
	}

	/**
	 * Returns the display name of the slot.
	 * 
	 * @param keyword
	 * @return
	 */
	public String getDisplayName(String keyword) {
		final Slot slot = this.slots.get(keyword);
		if (slot != null) {
			return slot.getDisplayName();
		} else
			return "";
	}

	/**
	 * Returns the condition of the slot.
	 * 
	 * @param keyword
	 * @return
	 * @see Slot#getCondition()
	 */
	public String getCondition(String keyword) {
		final Slot slot = this.slots.get(keyword);

		if (slot != null) {
			return slot.getCondition();
		} else
			return "";
	}

	/**
	 * Gets the slot whose keyword matches the passed in String.
	 * 
	 * @param slot
	 * @return
	 */
	public Slot getEventSlot(String slot) {
		return this.slots.get(slot);
	}

	/**
	 * Returns all slots known by the translator.
	 * 
	 * @return
	 */
	public Collection<Slot> getEventSlots() {
		return new ArrayList<Slot>(this.slots.values());
	}

	/**
	 * Adds a collection of event slots to the {@link #EventSlotManager()}
	 * 
	 * @param slots
	 */
	public void addEventSlots(Collection<Slot> slots) {
		for (Slot slot : slots)
			this.addEventSlot(slot);
	}

	@Override
	public String toString() {
		return "EventSlotManager [" + this.slots.keySet() + "]";
	}
}

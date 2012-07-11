package scriptease.controller.apimanagers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import scriptease.model.atomic.KnowIt;
import scriptease.translator.io.model.Slot;

public class EventSlotManager {

	public static final String DEFAULT_SLOT_TEXT = "";
	private final Map<String, Slot> slots;
	private String defaultFormatKeyword;

	public EventSlotManager() {
		slots = new HashMap<String, Slot>();
	}

	public void addEventSlot(Slot slot) {
		this.slots.put(slot.getKeyword(), slot);
	}

	public void setDefaultFormatKeyword(String defaultFormatKeyword) {
		this.defaultFormatKeyword = defaultFormatKeyword;
	}

	public String getDefaultFormatKeyword() {
		return this.defaultFormatKeyword;
	}

	/**
	 * Returns a collection of implicit KnowIts cloned from the original set for
	 * the slot.
	 * 
	 * @param keyword
	 * @return
	 */
	public Collection<KnowIt> getImplicits(String keyword) {
		final Slot slot = this.slots.get(keyword);
		if (slot != null) {
			return slot.getImplicits();
		} else
			return new ArrayList<KnowIt>();
	}

	public Collection<KnowIt> getParameters(String keyword) {
		final Slot slot = this.slots.get(keyword);
		if (slot != null) {
			return slot.getParameters();
		} else
			return new ArrayList<KnowIt>();
	}

	public void clear() {
		this.slots.clear();
	}

	public String getDisplayName(String keyword) {
		final Slot slot = this.slots.get(keyword);
		if (slot != null) {
			return slot.getDisplayName();
		} else
			return "";
	}

	public Slot getEventSlot(String slot) {
		return this.slots.get(slot);
	}

	public Collection<Slot> getEventSlots() {
		return new ArrayList<Slot>(this.slots.values());
	}

	public void addEventSlots(Collection<Slot> slots) {
		for (Slot slot : slots)
			this.addEventSlot(slot);
	}

	@Override
	public String toString() {
		return "EventSlotManager [" + this.slots.keySet() + "]";
	}
}

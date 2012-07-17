package scriptease.translator.io.model;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.controller.apimanagers.EventSlotManager;
import scriptease.model.atomic.KnowIt;
import scriptease.translator.TranslatorManager;

/**
 * Represents a game slot in Scriptease Each slot contains a unique keyword, a
 * displayName, a set of implicit KnowIts, and a file format indicating how to
 * generate code.
 * 
 * @author mfchurch
 * 
 */
public class Slot {
	private String displayName;
	private String keyword;
	private String formatKeyword;
	private Collection<KnowIt> implicits;
	private Collection<KnowIt> parameters;

	public Slot(String name, String keyword, Collection<KnowIt> parameters,
			Collection<KnowIt> implicits, String formatKeyword) {
		this.displayName = name;
		this.keyword = keyword;
		this.implicits = new ArrayList<KnowIt>();
		this.implicits.addAll(implicits);
		this.parameters = new ArrayList<KnowIt>();
		this.parameters.addAll(parameters);
		this.formatKeyword = formatKeyword;
	}

	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Gets the associated Language Dictionary format for this slot. The format
	 * is specified for a slot in the Language Dictionary by the attribute
	 * format. If a format is not specified, it will use the required
	 * defaultFormat attribute specified on the Slots entity.
	 * 
	 * @return
	 */
	public String getFormatKeyword() {
		if (formatKeyword == null || formatKeyword.isEmpty())
			return TranslatorManager.getInstance().getActiveTranslator()
					.getApiDictionary().getEventSlotManager()
					.getDefaultFormatKeyword();
		return formatKeyword;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Gets a new list containing this slot's implicit KnowIts. This should not be used except by
	 * EventSlotManager and SlotConverter. Use
	 * {@link EventSlotManager#getImplicits(String) instead.}
	 * 
	 * @return
	 */
	public Collection<KnowIt> getImplicits() {
		return new ArrayList<KnowIt>(this.implicits);
	}

	public void addImplicit(KnowIt implicit) {
		this.implicits.add(implicit);
	}

	public void addParameter(KnowIt parameter) {
		this.parameters.add(parameter);
	}

	public void addParameters(Collection<KnowIt> parameters) {
		this.parameters.addAll(parameters);
	}

	public void addImplicits(Collection<KnowIt> implicits) {
		this.implicits.addAll(implicits);
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public Collection<KnowIt> getParameters() {
		return this.parameters;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Slot) {
			if (obj == this) {
				return true;
			}
			Slot other = (Slot) obj;
			return other.keyword.equals(keyword);
		}
		return false;
	}

	@Override
	public String toString() {
		return this.displayName;
	}
}

package scriptease.translator.io.model;

import java.util.ArrayList;
import java.util.Collection;

import scriptease.model.atomic.KnowIt;
import scriptease.model.semodel.SEModelManager;

/**
 * Represents a game slot in Scriptease Each slot contains a unique keyword, a
 * displayName, a set of implicit KnowIts, and a file format indicating how to
 * generate code.
 * 
 * @author mfchurch
 * @author jyuen
 */
public class Slot {
	private String displayName;
	private String keyword;
	private String formatKeyword;
	private Collection<KnowIt> implicits;
	private Collection<KnowIt> parameters;
	private String condition;

	public Slot(String name, String keyword, Collection<KnowIt> parameters,
			Collection<KnowIt> implicits, String formatKeyword, String condition) {
		this.displayName = name;
		this.keyword = keyword;
		this.implicits = new ArrayList<KnowIt>();
		this.implicits.addAll(implicits);
		this.parameters = new ArrayList<KnowIt>();
		this.parameters.addAll(parameters);
		this.formatKeyword = formatKeyword;
		this.condition = condition;
	}

	public Slot clone() {
		final Collection<KnowIt> implicits = new ArrayList<KnowIt>();
		for (KnowIt implicit : this.implicits)
			implicits.add(implicit.clone());

		final Collection<KnowIt> parameters = new ArrayList<KnowIt>();
		for (KnowIt parameter : this.parameters)
			parameters.add(parameter.clone());

		return new Slot(this.displayName, this.keyword, parameters, implicits,
				this.formatKeyword, this.condition);
	}

	/**
	 * Returns the display name of the slot.
	 * 
	 * @return
	 */
	public String getDisplayName() {
		return this.displayName;
	}

	/**
	 * Gets the condition of the slot. The condition is a special piece of code
	 * that can be inserted when a slot is used. The original purpose of a
	 * condition was for Unity, where some Causes are in a "update()" function
	 * and fire when certain if conditions are met (such as if the mouse button
	 * is down).
	 * 
	 * @return
	 */
	public String getCondition() {
		return this.condition;
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
		if (this.formatKeyword == null || this.formatKeyword.isEmpty())
			return SEModelManager.getInstance().getActiveModel()
					.getSlotDefaultFormat();
		return this.formatKeyword;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Gets a new list containing this slot's implicit KnowIts. This should not
	 * be used except by the Library Model.
	 * 
	 * @return
	 */
	public Collection<KnowIt> getImplicits() {
		return new ArrayList<KnowIt>(this.implicits);
	}

	public void setImplicits(Collection<KnowIt> implicits) {
		this.implicits = implicits;
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
		return this.keyword;
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
			return other.keyword.equals(this.keyword);
		}
		return false;
	}

	@Override
	public String toString() {
		return this.displayName;
	}
}

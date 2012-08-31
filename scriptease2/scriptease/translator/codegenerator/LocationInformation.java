package scriptease.translator.codegenerator;

import scriptease.model.CodeBlock;
import scriptease.model.atomic.KnowIt;
import scriptease.translator.io.model.IdentifiableGameConstant;

/**
 * LocationInformation provides Context to CodeGeneration in the form of Subject
 * and Slot.
 * 
 * @author mfchurch
 * 
 */
public class LocationInformation {
	private final String slot;
	private final IdentifiableGameConstant subject;

	public LocationInformation(IdentifiableGameConstant subject, String slot) {
		this.subject = subject;
		this.slot = slot;
	}

	public LocationInformation(CodeBlock codeBlock) {
		KnowIt subject = codeBlock.getSubject();
		Object value = subject.getBinding().getValue();
		if (value instanceof IdentifiableGameConstant)
			this.subject = (IdentifiableGameConstant) value;
		else
			throw new IllegalArgumentException(
					"Subject must be bound to IdentifiableGameConstant.");
		this.slot = codeBlock.getSlot();
	}

	/**
	 * Returns true if the slot and subject match the location, otherwise false
	 * 
	 * @param codeBlock
	 * @return
	 */
	public boolean matchesLocation(CodeBlock codeBlock) {
		return this.slot.equals(codeBlock.getSlot())
				&& this.subject.equals(codeBlock.getSubject().getBinding()
						.getValue());
	}

	public String getSlot() {
		return this.slot;
	}

	public IdentifiableGameConstant getSubject() {
		return this.subject;
	}

	@Override
	public String toString() {
		return "LocationInfo [" + this.subject + ", " + this.slot + "]";
	}
}
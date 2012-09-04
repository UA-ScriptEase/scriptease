package scriptease.translator.codegenerator;

import scriptease.model.CodeBlock;
import scriptease.model.atomic.KnowIt;
import scriptease.translator.io.model.GameConstant;
import scriptease.translator.io.tools.SimpleGameConstant;

/**
 * LocationInformation provides Context to CodeGeneration in the form of Subject
 * and Slot.
 * 
 * @author mfchurch
 * 
 */
public class LocationInformation {
	private final String slot;
	private final GameConstant subject;

	public LocationInformation(GameConstant subject, String slot) {
		this.subject = subject;
		this.slot = slot;
	}

	public LocationInformation(CodeBlock codeBlock) {
		KnowIt subject = codeBlock.getSubject();
		Object value = subject.getBinding().getValue();
		if (!(value instanceof SimpleGameConstant)
				&& value instanceof GameConstant)
			this.subject = (GameConstant) value;
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

	public GameConstant getSubject() {
		return this.subject;
	}

	@Override
	public String toString() {
		return "LocationInfo [" + this.subject + ", " + this.slot + "]";
	}
}
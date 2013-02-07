package scriptease.translator.codegenerator;

import scriptease.model.CodeBlock;
import scriptease.model.atomic.KnowIt;
import scriptease.translator.io.model.Resource;
import scriptease.translator.io.model.SimpleResource;

/**
 * LocationInformation provides Context to CodeGeneration in the form of Subject
 * and Slot.
 * 
 * @author mfchurch
 * 
 */
public class LocationInformation {
	private final String slot;
	private final Resource subject;

	public LocationInformation(Resource subject, String slot) {
		this.subject = subject;
		this.slot = slot;
	}

	public LocationInformation(CodeBlock codeBlock) {
		KnowIt subject = codeBlock.getSubject();
		Object value = subject.getBinding().getValue();
		if (!(value instanceof SimpleResource) && value instanceof Resource)
			this.subject = (Resource) value;
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

	public Resource getSubject() {
		return this.subject;
	}

	@Override
	public String toString() {
		return "LocationInfo [" + this.subject + ", " + this.slot + "]";
	}
}
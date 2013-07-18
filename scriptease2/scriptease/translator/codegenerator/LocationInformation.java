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

	public LocationInformation(CodeBlock codeBlock) {
		// TODO - remove later?
		if (codeBlock.hasSubject()) {
			final KnowIt subject = codeBlock.getSubject();
			final Object value = subject.getBinding().getValue();

			if (!(value instanceof SimpleResource) && value instanceof Resource)
				this.subject = (Resource) value;
			else
				throw new IllegalArgumentException(
						"Subject must be bound to a Resource that is not simple.");

		} else
			this.subject = null;

		this.slot = codeBlock.getSlot();
	}

	/**
	 * Returns true if the slot and subject match the location, otherwise false
	 * 
	 * @param codeBlock
	 * @return
	 */
	public boolean matchesLocation(CodeBlock codeBlock) {
		final String slot;
		final Object subject;

		// TODO test - remove later?
		slot = codeBlock.getSlot();

		if (codeBlock.hasSubject())
			subject = codeBlock.getSubject().getBinding().getValue();
		else
			subject = null;

		if (this.subject != null && subject != null)
			return this.slot.equals(slot) && this.subject.equals(subject);
		else
			return this.slot.equals(slot);
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
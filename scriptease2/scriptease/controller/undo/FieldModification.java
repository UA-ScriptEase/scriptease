package scriptease.controller.undo;

import scriptease.model.StoryComponent;

/**
 * Represents and facilitates implementing the specific subgroup of
 * modifications that are simply field setting. The specific method used to set
 * or unset the field must be supplied in a subclass's implementation of
 * {@link #setOp(Object)}.<br>
 * <br>
 * For example, a modification that sets a <code>StoryComponent</code>'s name
 * could be implemented by subclassing <code>FieldModification</code> and
 * implementing {@link #setOp(Object)} to use
 * {@link StoryComponent#setDisplayText(String)} .
 * 
 * @author remiller
 * 
 * @param <T>
 *            The type of the field being set.
 */
public abstract class FieldModification<T> implements Modification {
	private final T newValue, oldValue;

	/**
	 * Builds a new Set-specific Modification object.
	 * 
	 * @param newValue
	 *            The new value used to set the property.
	 * @param oldValue
	 *            The original value used to un-set the property.
	 */
	public FieldModification(T newValue, T oldValue) {
		this.newValue = newValue;
		this.oldValue = oldValue;
	}

	@Override
	public void redo() {
		this.setOp(this.newValue);
	}

	@Override
	public void undo() {
		this.setOp(this.oldValue);
	}

	/**
	 * Performs the specific setX operation. This is what is called by
	 * {@link #undo()} and {@link #redo()}.
	 * 
	 * @param value
	 *            The value to set the property to.
	 */
	public abstract void setOp(T value);
}